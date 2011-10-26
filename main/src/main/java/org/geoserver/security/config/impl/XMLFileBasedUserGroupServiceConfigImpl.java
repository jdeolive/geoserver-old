/* Copyright (c) 2001 - 2008 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.security.config.impl;

import org.geoserver.security.config.SecurityUserGoupServiceConfig;

public class XMLFileBasedUserGroupServiceConfigImpl extends XMLFileBasedSecurityServiceConfigImpl implements SecurityUserGoupServiceConfig {

    protected String passwordEncoderName;
    @Override
    public String getPasswordEncoderName() {
        return passwordEncoderName;
    }

    @Override
    public void setPasswordEncoderName(String name) {
        passwordEncoderName=name;
    }

}
