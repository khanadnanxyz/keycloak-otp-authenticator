import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.*;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class OTPAuthenticator implements Authenticator {
    private static Logger logger = Logger.getLogger(OTPAuthenticator.class);

    private static enum CODE_STATUS {
        VALID,
        INVALID,
        EXPIRED
    }

    public void authenticate(AuthenticationFlowContext context) {
        AuthenticatorConfigModel config = context.getAuthenticatorConfig();
        String mobileNumberAttribute = OTPAuthenticatorUtil.getConfigString(config, OTPAuthenticatorContstants.CONF_PRP_USR_ATTR_MOBILE);
        if (mobileNumberAttribute == null) {
            Response challenge = context.form()
                    .setError("Phone number could not be determined.")
                    .createForm("sms-validation-error.ftl");
            context.failureChallenge(AuthenticationFlowError.INTERNAL_ERROR, challenge);
            return;
        }
        String mobileNumber = OTPAuthenticatorUtil.getAttributeValue(context.getUser(), mobileNumberAttribute);
        if (mobileNumber != null && mobileNumber != "") {
            long nrOfDigits = OTPAuthenticatorUtil.getConfigLong(config, OTPAuthenticatorContstants.CONF_PRP_SMS_CODE_LENGTH, 8L);
            long ttl = OTPAuthenticatorUtil.getConfigLong(config, OTPAuthenticatorContstants.CONF_PRP_SMS_CODE_TTL, 10 * 60L); // 10 minutes in s


            String code = getSmsCode(nrOfDigits);

            storeSMSCode(context, code, new Date().getTime() + (ttl * 1000)); // s --> ms
            if (sendSmsCode(mobileNumber, code, context.getAuthenticatorConfig())) {
                Response challenge = context.form().createForm("sms-validation.ftl");
                context.challenge(challenge);
            } else {
                Response challenge = context.form()
                        .setError("SMS could not be sent.")
                        .createForm("sms-validation-error.ftl");
                context.failureChallenge(AuthenticationFlowError.INTERNAL_ERROR, challenge);
                return;
            }
        } else {
            Response challenge = context.form()
                    .setError("SMS could not be sent.")
                    .createForm("sms-validation-error.ftl");
            context.failureChallenge(AuthenticationFlowError.INTERNAL_ERROR, challenge);
            return;
        }
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        CODE_STATUS status = validateCode(context);
        Response challenge = null;
        switch (status) {
            case EXPIRED:
                challenge = context.form()
                        .setError("The code has been expired.")
                        .createForm("sms-validation.ftl");
                context.failureChallenge(AuthenticationFlowError.EXPIRED_CODE, challenge);
                break;

            case INVALID:
                if (context.getExecution().getRequirement() == AuthenticationExecutionModel.Requirement.CONDITIONAL ||
                        context.getExecution().getRequirement() == AuthenticationExecutionModel.Requirement.ALTERNATIVE) {
                    System.out.println("Calling context.attempted()");
                    context.attempted();
                } else if (context.getExecution().getRequirement() == AuthenticationExecutionModel.Requirement.REQUIRED) {
                    challenge = context.form()
                            .setError("The code is invalid")
                            .createForm("sms-validation.ftl");
                    context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, challenge);
                } else {
                    // Something strange happened
                    logger.error("Undefined execution ...");
                }
                break;

            case VALID:
                context.success();
                UserModel user = context.getUser();
                user.removeAttribute("otp");
                user.removeAttribute("otp_expiry");
                break;

        }
    }

    private void storeSMSCode(AuthenticationFlowContext context, String code, Long expiringAt) {
        UserModel user = context.getUser();
        user.setSingleAttribute("otp", code);
        user.setSingleAttribute("otp_expiry", expiringAt.toString());

        context.success();
    }

    private boolean sendSmsCode(String mobileNumber, String code, AuthenticatorConfigModel config) {

        String smsScheme = OTPAuthenticatorUtil.getConfigString(config, OTPAuthenticatorContstants.SMS_SCHEME);
        String smsHost = OTPAuthenticatorUtil.getConfigString(config, OTPAuthenticatorContstants.SMS_HOST);
        String smsPath = OTPAuthenticatorUtil.getConfigString(config, OTPAuthenticatorContstants.SMS_PATH);
        String smsUsername = OTPAuthenticatorUtil.getConfigString(config, OTPAuthenticatorContstants.SMS_USERNAME);
        String smsPassword = OTPAuthenticatorUtil.getConfigString(config, OTPAuthenticatorContstants.SMS_PASSWORD);
        String smsFrom = OTPAuthenticatorUtil.getConfigString(config, OTPAuthenticatorContstants.SMS_FROM);
        String smsToPrefix = OTPAuthenticatorUtil.getConfigString(config, OTPAuthenticatorContstants.SMS_TO_PREFIX);
        String smsText = OTPAuthenticatorUtil.getConfigString(config, OTPAuthenticatorContstants.SMS_TEXT);

        logger.debug(smsText + " " + code);

        try {

            URIBuilder builder = new URIBuilder();
            builder.setScheme(smsScheme).setHost(smsHost).setPath(smsPath);
            URI uri = builder.build();

            HttpPost httpPost = new HttpPost(uri);

            List<NameValuePair> pairs = new ArrayList<>();
            pairs.add(new BasicNameValuePair("Username", smsUsername));
            pairs.add(new BasicNameValuePair("Password", smsPassword));
            pairs.add(new BasicNameValuePair("From", smsFrom));
            pairs.add(new BasicNameValuePair("To", smsToPrefix + mobileNumber));
            pairs.add(new BasicNameValuePair("Message", smsText + " " + code));

            httpPost.setEntity(new UrlEncodedFormEntity(pairs));
            HttpClient httpClient = HttpClients.createDefault();
            CloseableHttpResponse response = (CloseableHttpResponse) httpClient.execute(httpPost);
            StatusLine sl = response.getStatusLine();
            response.close();
            if (sl.getStatusCode() != 200) {
                logger.error("SMS code for " + mobileNumber + " could not be sent: " + sl.getStatusCode() + " - " + sl.getReasonPhrase());
            }
            return sl.getStatusCode() == 200;
        } catch (IOException | URISyntaxException e) {
            logger.error("sendSms called .. ." + e);
            return false;
        }
    }

    private String getSmsCode(long nrOfDigits) {
        if (nrOfDigits < 1) {
            throw new RuntimeException("Nr of digits must be bigger than 0");
        }

        double maxValue = Math.pow(10.0, nrOfDigits); // 10 ^ nrOfDigits;
        Random r = new Random();
        long code = (long) (r.nextFloat() * maxValue);
        return Long.toString(code);
    }

    protected CODE_STATUS validateCode(AuthenticationFlowContext context) {
        CODE_STATUS result = CODE_STATUS.VALID;
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        String enteredCode = formData.getFirst(OTPAuthenticatorContstants.ANSW_SMS_CODE);
        String expectedCode = OTPAuthenticatorUtil.getAttributeValue(context.getUser(), "otp");
        String expTimeString = OTPAuthenticatorUtil.getAttributeValue(context.getUser(), "otp_expiry");
        if (expectedCode != null) {
            result = enteredCode.equals(expectedCode) ? CODE_STATUS.VALID : CODE_STATUS.INVALID;
            long now = new Date().getTime();

            if (result == CODE_STATUS.VALID) {
                if (Long.parseLong(expTimeString) < now) {
                    result = CODE_STATUS.EXPIRED;
                }
            }
        }
        return result;
    }


    @Override
    public boolean requiresUser() {
        return true;
    }

    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {

    }

    @Override
    public void close() {

    }
}