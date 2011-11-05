/* Copyright (c) 2001 - 2008 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.security.config;

import org.geoserver.security.GeoserverRoleService;
import org.geoserver.security.concurrent.LockingRoleService;


/**
 * 
 * Methods for a {@link GeoserverRoleService} object
 *  
 * @author christian
 *
 */
public interface SecurityRoleServiceConfig {

    public String getAdminRoleName();
    public void   setAdminRoleName(String name);
    /**
     * Indicates if a {@link LockingRoleService} wrapper
     * is created automatically to protect concurrent access
     * to user/group objects.
     * 
     * @return
     */
    public boolean isLockingNeeded();
    public void setLockingNeeded(boolean needed);

}
