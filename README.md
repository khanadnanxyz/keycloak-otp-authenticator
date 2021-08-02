[![Maven Package](https://github.com/khanadnanxyz/keycloak-otp-authenticator/actions/workflows/maven-publish.yml/badge.svg)](https://github.com/khanadnanxyz/keycloak-otp-authenticator/actions/workflows/maven-publish.yml)
# keycloak-otp-authenticator

To install the OTP Authenticator one has to:
* Download keycloak-otp-authenticator.jar file from the latest [releases](https://github.com/khanadnanxyz/keycloak-otp-authenticator/releases)

* Add the jar to the Keycloak server:
  * `$ cp ../keycloak-otp-authenticator.jar _KEYCLOAK_HOME_/standalone/deployments`

* Add two templates to the Keycloak server:
  * `$ cp templates/otp.ftl _KEYCLOAK_HOME_/themes/base/login/`


Configure your REALM to use the OTP Authentication.
First create a new REALM (or select a previously created REALM).

Under Authentication > Flows:
* Copy 'Browse' flow to 'Browser with OTP' flow
* Click on 'Actions > Add execution on the 'Browser with OTP Forms' line and add the 'OTP Authentication'
* Set 'OTP Authentication' to 'REQUIRED' or 'ALTERNATIVE'
* To configure the OTP Authenticator, click on Actions  Config and fill in the attributes.


Under Authentication > Bindings:
* Select 'Browser with OTP' as the 'Browser Flow' for the REALM.

Under Authentication > Required Actions:
* Click on Register and select 'OTP Authentication' to add the Required Action to the REALM.
* Make sure that for the 'OTP Authentication' both the 'Enabled' and 'Default Action' options are checked.

You have configure this OTP Authenticator, from config

SMS_SCHEME > SMS Scheme to follow, http or https.
SMS_HOST > Host name of the SMS provider.
SMS_PATH > SMS provider's url path.
SMS_USERNAME > SMS provider's username.
SMS_PASSWORD > SMS provider's password.
SMS_FROM > Platform name to send SMS from.
SMS_TO_PREFIX > Mobile number's prefix, i.e. - country code.

CONF_PRP_USR_ATTR_MOBILE > Mobile number attribute in the user model.
CONF_PRP_SMS_CODE_TTL > SMS validity.
CONF_PRP_SMS_CODE_LENGTH > SMS otp length.
SMS_TEXT > SMS text to send with the code.

These are currently available config from https://ada-asia.com/ as the SMS service provider.
Hoping to make it dynamic with different SMS service providers like, Twilio, MessageBird and more.

Till then, you can reach out to me if you want to integrate this package with a different SMS provider; I will be glad to help.
