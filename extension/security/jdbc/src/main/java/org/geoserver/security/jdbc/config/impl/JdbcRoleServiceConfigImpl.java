/* Copyright (c) 2001 - 2008 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.security.jdbc.config.impl;

import org.geoserver.security.config.SecurityRoleServiceConfig;

public class JdbcRoleServiceConfigImpl extends JdbcSecurityServiceConfigImpl
        implements SecurityRoleServiceConfig {
    
    protected String adminRoleName;
    
    @Override
    public String getAdminRoleName() {
        return adminRoleName;
    }

    @Override
    public void setAdminRoleName(String name) {
        adminRoleName=name;
    }

}
