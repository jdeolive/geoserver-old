/* Copyright (c) 2001 - 2008 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.security.config.impl;

import org.geoserver.security.config.UserDetailsServiceConfig;

/**
 * Implementation of {@link UserDetailsServiceConfig}
 * 
 * @author christian
 *
 */
public class UserDetailsServiceConfigImpl extends SecurityConfigImpl implements UserDetailsServiceConfig {
    private String grantedAuthorityServiceName;
    private String userGroupServiceName;
    
    /* (non-Javadoc)
     * @see org.geoserver.security.config.SecurityServiceConfig#getGrantedAuthorityServiceName()
     */
    public String getGrantedAuthorityServiceName() {
        return grantedAuthorityServiceName;
    }
    /* (non-Javadoc)
     * @see org.geoserver.security.config.SecurityServiceConfig#setGrantedAuthorityServiceName(java.lang.String)
     */
    public void setGrantedAuthorityServiceName(String grantedAuthorityServiceName) {
        this.grantedAuthorityServiceName = grantedAuthorityServiceName;
    }
    /* (non-Javadoc)
     * @see org.geoserver.security.config.SecurityServiceConfig#getUserGroupServiceName()
     */
    public String getUserGroupServiceName() {
        return userGroupServiceName;
    }
    /* (non-Javadoc)
     * @see org.geoserver.security.config.SecurityServiceConfig#setUserGroupServiceName(java.lang.String)
     */
    public void setUserGroupServiceName(String userGroupServiceName) {
        this.userGroupServiceName = userGroupServiceName;
    }
    
}
