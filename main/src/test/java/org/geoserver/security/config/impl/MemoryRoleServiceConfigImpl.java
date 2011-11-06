/* Copyright (c) 2001 - 2008 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.security.config.impl;

import org.geoserver.security.config.SecurityRoleServiceConfig;

public class MemoryRoleServiceConfigImpl extends SecurityNamedServiceConfigImpl
        implements SecurityRoleServiceConfig {
    
    protected String adminRoleName;
    protected boolean lockingNeeded;
    protected String toBeEncrypted;
    
    public String getToBeEncrypted() {
        return toBeEncrypted;
    }

    public void setToBeEncrypted(String toBeEncrypted) {
        this.toBeEncrypted = toBeEncrypted;
    }

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
