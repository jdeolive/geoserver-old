/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.security;

import org.geoserver.security.config.impl.SecurityNamedServiceConfigImpl;

/**
 * Config object for {@link UsernamePasswordAuthenticationProvider}.
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class UsernamePasswordAuthenticationProviderConfig extends SecurityNamedServiceConfigImpl {

    String userGroupServiceName;

    public String getUserGroupServiceName() {
        return userGroupServiceName;
    }

    public void setUserGroupServiceName(String userGroupServiceName) {
        this.userGroupServiceName = userGroupServiceName;
    }
}
