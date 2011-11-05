/* Copyright (c) 2001 - 2008 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.security.jdbc.config.impl;

import org.geoserver.security.config.SecurityRoleServiceConfig;

public class JdbcJndiRoleServiceConfigImpl extends JdbcJndiSecurityServiceConfigImpl
        implements SecurityRoleServiceConfig {
    
    protected String adminRoleName;
    protected boolean lockingNeeded;
    
    public boolean isLockingNeeded() {
        return lockingNeeded;
    }

    public void setLockingNeeded(boolean lockingNeeded) {
        this.lockingNeeded = lockingNeeded;
    }

    @Override
    public String getAdminRoleName() {
        return adminRoleName;
    }

    @Override
    public void setAdminRoleName(String name) {
        adminRoleName=name;
    }

}
