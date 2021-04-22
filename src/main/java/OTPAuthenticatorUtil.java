import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.UserModel;

import java.util.List;

public class OTPAuthenticatorUtil {
    public static String getAttributeValue(UserModel user, String attributeName) {
        String result = null;
        List<String> values = user.getAttribute(attributeName);
        if (values != null && values.size() > 0) {
            result = values.get(0);
        }

        return result;
    }

    public static String getConfigString(AuthenticatorConfigModel config, String configName) {
        return getConfigString(config, configName, null);
    }

    public static String getConfigString(AuthenticatorConfigModel config, String configName, String defaultValue) {

        String value = defaultValue;

        if (config.getConfig() != null) {
            value = config.getConfig().get(configName);
        }

        return value;
    }

    public static Long getConfigLong(AuthenticatorConfigModel config, String configName, Long defaultValue) {

        Long value = defaultValue;

        if (config.getConfig() != null) {
            Object obj = config.getConfig().get(configName);
            try {
                value = Long.valueOf((String) obj);
            } catch (NumberFormatException nfe) {
                System.out.println("Can not convert " + obj + " to a number.");
            }
        }

        return value;
    }
}
