/* Copyright (c) 2001 - 2012 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.security.web.passwd;

import org.geoserver.security.config.PasswordPolicyConfig;
import org.geoserver.security.validation.PasswordValidatorImpl;
import org.geoserver.security.web.SecurityNamedServicePanelInfo;

/**
 * Configuration panel extension for {@link PasswordPolicy}.
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class PasswordPolicyPanelInfo 
    extends SecurityNamedServicePanelInfo<PasswordPolicyConfig,PasswordPolicyPanel> {

    public PasswordPolicyPanelInfo() {
        setComponentClass(PasswordPolicyPanel.class);
        setServiceClass(PasswordValidatorImpl.class);
        setServiceConfigClass(PasswordPolicyConfig.class);
        setPriority(0);
    }
}
