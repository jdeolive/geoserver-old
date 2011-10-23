/* Copyright (c) 2001 - 2008 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.security.config;

import org.geoserver.security.GeoserverRoleService;
import org.geoserver.security.GeoserverUserGroupService;


/**
 * Interface for a {@link GeoserverUserDetailsService} configuration
 * 
 * @author christian
 *
 */
public interface SecurityManagerConfig  extends SecurityConfig {

    /**
     * @return the name for a 
     * {@link GeoserverRoleService} object
     * 
     */
    public String getRoleServiceName();
    /**
     * @param roleServiceName, the name of a
     * {@link GeoserverRoleService} object 
     */
    public void setRoleServiceName(String roleServiceName);
    
    /**
     * @return the name for a 
     * {@link GeoserverUserGroupService} object
     */
    public String getUserGroupServiceName();
    
    /**
     * @param userGroupServiceName, the name of a
     * {@link GeoserverUserGroupService} object
     */
    public void setUserGroupServiceName(String userGroupServiceName);

}
