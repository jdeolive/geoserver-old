/* Copyright (c) 2001 - 2008 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.security.config.impl;

import org.geoserver.security.config.SecurityUserGoupServiceConfig;

public class XMLFileBasedUserGroupServiceConfigImpl extends XMLFileBasedSecurityServiceConfigImpl implements SecurityUserGoupServiceConfig {

    protected String passwordEncoderName;
    protected String passwordPolicyName;
    protected boolean lockingNeeded;
    private static final long serialVersionUID = 1L;
    
    public boolean isLockingNeeded() {
        return lockingNeeded;
    }

    public void setLockingNeeded(boolean lockingNeeded) {
        this.lockingNeeded = lockingNeeded;
    }

    public String getPasswordPolicyName() {
        return passwordPolicyName;
    }

    public void setPasswordPolicyName(String passwordPolicyName) {
        this.passwordPolicyName = passwordPolicyName;
    }

    @Override
    public String getPasswordEncoderName() {
        return passwordEncoderName;
    }

    @Override
    public void setPasswordEncoderName(String name) {
        passwordEncoderName=name;
    }

}
