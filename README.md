[![Maven Package](https://github.com/khanadnanxyz/keycloak-otp-authenticator/actions/workflows/maven-publish.yml/badge.svg)](https://github.com/khanadnanxyz/keycloak-otp-authenticator/actions/workflows/maven-publish.yml)
# keycloak-otp-authenticator

To install the OTP Authenticator one has to:

* Add the jar to the Keycloak server:
  * `$ cp target/keycloak-otp-authenticator.jar _KEYCLOAK_HOME_/standalone/deployments`

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
