/* Copyright (c) 2001 - 2008 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.security.config;

import org.geoserver.security.GeoserverUserGroupService;
import org.geoserver.security.concurrent.LockingUserGroupService;

/**
 * 
 * Methods for a {@link GeoserverUserGroupService} object
 *  
 * @author christian
 *
 */
public interface SecurityUserGroupServiceConfig extends SecurityNamedServiceConfig {

    public String getPasswordEncoderName();
    public void   setPasswordEncoderName(String name);
    public String getPasswordPolicyName();
    public void   setPasswordPolicyName(String name);
}
