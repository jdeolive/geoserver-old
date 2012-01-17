package org.geoserver.security.web.passwd;

import org.geoserver.security.config.PasswordPolicyConfig;
import org.geoserver.security.validation.PasswordValidatorImpl;
import org.geoserver.security.web.SecurityNamedServicePanelInfo;

public class PasswordPolicyPanelInfo 
    extends SecurityNamedServicePanelInfo<PasswordPolicyConfig,PasswordPolicyPanel> {

    public PasswordPolicyPanelInfo() {
        setComponentClass(PasswordPolicyPanel.class);
        setServiceClass(PasswordValidatorImpl.class);
        setServiceConfigClass(PasswordPolicyConfig.class);
    }
}
