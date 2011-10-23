/* Copyright (c) 2001 - 2008 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.security.config.impl;

import org.geoserver.security.config.SecurityManagerConfig;

/**
 * Implementation of {@link SecurityManagerConfig}
 * 
 * @author christian
 *
 */
public class SecurityManagerConfigImpl extends SecurityConfigImpl implements SecurityManagerConfig {
    
    private String roleServiceName;
    private String userGroupServiceName;
    
    public String getRoleServiceName() {
        return roleServiceName;
    }
    public void setRoleServiceName(String roleServiceName) {
        this.roleServiceName = roleServiceName;
    }
    public String getUserGroupServiceName() {
        return userGroupServiceName;
    }
    public void setUserGroupServiceName(String userGroupServiceName) {
        this.userGroupServiceName = userGroupServiceName;
    }
    
}
