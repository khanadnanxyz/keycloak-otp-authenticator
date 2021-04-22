import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.authentication.ConfigurableAuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.ArrayList;
import java.util.List;

public class OTPAuthenticatorFactory implements AuthenticatorFactory, ConfigurableAuthenticatorFactory {

    public static final String PROVIDER_ID = "otp-authenticator";
    private static final OTPAuthenticator SINGLETON = new OTPAuthenticator();

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        return SINGLETON;
    }

    private static AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED,
            AuthenticationExecutionModel.Requirement.DISABLED
    };

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public boolean isUserSetupAllowed() {
        return true;
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    private static final List<ProviderConfigProperty> configProperties = new ArrayList<ProviderConfigProperty>();

    static {
        ProviderConfigProperty property;

        property = new ProviderConfigProperty();
        property.setName(OTPAuthenticatorContstants.SMS_SCHEME);
        property.setLabel("SMS_SCHEME");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("SMS Scheme to follow, http or https.");
        configProperties.add(property);

        property = new ProviderConfigProperty();
        property.setName(OTPAuthenticatorContstants.SMS_HOST);
        property.setLabel("SMS_HOST");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("Host name of the SMS provider.");
        configProperties.add(property);

        property = new ProviderConfigProperty();
        property.setName(OTPAuthenticatorContstants.SMS_PATH);
        property.setLabel("SMS_PATH");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("SMS provider's url path.");
        configProperties.add(property);

        property = new ProviderConfigProperty();
        property.setName(OTPAuthenticatorContstants.SMS_USERNAME);
        property.setLabel("SMS_USERNAME");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("SMS provider's username.");
        configProperties.add(property);

        property = new ProviderConfigProperty();
        property.setName(OTPAuthenticatorContstants.SMS_PASSWORD);
        property.setLabel("SMS_PASSWORD");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("SMS provider's password.");
        configProperties.add(property);

        property = new ProviderConfigProperty();
        property.setName(OTPAuthenticatorContstants.SMS_FROM);
        property.setLabel("SMS_FROM");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("Platform name to send SMS from.");
        configProperties.add(property);

        property = new ProviderConfigProperty();
        property.setName(OTPAuthenticatorContstants.SMS_TO_PREFIX);
        property.setLabel("SMS_TO_PREFIX");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("Mobile number's prefix, i.e. - country code.");
        configProperties.add(property);

        property = new ProviderConfigProperty();
        property.setName(OTPAuthenticatorContstants.CONF_PRP_USR_ATTR_MOBILE);
        property.setLabel("CONF_PRP_USR_ATTR_MOBILE");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("Mobile number attribute in the user model.");
        configProperties.add(property);

        property = new ProviderConfigProperty();
        property.setName(OTPAuthenticatorContstants.CONF_PRP_SMS_CODE_TTL);
        property.setLabel("CONF_PRP_SMS_CODE_TTL");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("SMS validity.");
        configProperties.add(property);

        property = new ProviderConfigProperty();
        property.setName(OTPAuthenticatorContstants.CONF_PRP_SMS_CODE_LENGTH);
        property.setLabel("CONF_PRP_SMS_CODE_LENGTH");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("SMS otp length.");
        configProperties.add(property);

        property = new ProviderConfigProperty();
        property.setName(OTPAuthenticatorContstants.SMS_TEXT);
        property.setLabel("SMS_TEXT");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("SMS text to send with the code.");
        configProperties.add(property);


    }


    @Override
    public String getHelpText() {
        return "An one time password, that a user has to provide during login.";
    }

    @Override
    public String getDisplayType() {
        return "OTP via SMS";
    }

    @Override
    public String getReferenceCategory() {
        return "OTP via SMS";
    }

    @Override
    public void init(Config.Scope config) {

    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {

    }


}
