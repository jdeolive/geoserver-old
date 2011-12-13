/* Copyright (c) 2001 - 2008 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.security.config;

import org.geoserver.security.GeoserverRoleService;


/**
 * 
 * Methods for a {@link GeoserverRoleService} object
 *  
 * @author christian
 *
 */
public interface SecurityRoleServiceConfig extends SecurityNamedServiceConfig {

    public String getAdminRoleName();
    public void   setAdminRoleName(String name);

}
