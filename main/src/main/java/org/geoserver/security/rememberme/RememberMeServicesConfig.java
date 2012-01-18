/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.security.rememberme;

import org.geoserver.security.config.BaseSecurityNamedServiceConfig;

/**
 * Configuration object for remember me services.
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class RememberMeServicesConfig extends BaseSecurityNamedServiceConfig {

    public static final String DEFAULT_KEY = "geoserver";

    String userGroupService;
    String key = DEFAULT_KEY;

    public RememberMeServicesConfig() {
    }

    public RememberMeServicesConfig(RememberMeServicesConfig other) {
        super(other);
        setUserGroupService(other.getUserGroupService());
        setKey(other.getKey());
    }

    public void setUserGroupService(String userGroupService) {
        this.userGroupService = userGroupService;
    }

    public String getUserGroupService() {
        return userGroupService;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

}
