/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.security.rememberme;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;

/**
 * Token based remember me services that appends a user group service name to generated tokens.
 * <p>
 * The user group service name is used by {@link RememberMeUserDetailsService} in order to delegate
 * to the proper user group service.
 * </p> 
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class GeoServerTokenBasedRememberMeServices extends TokenBasedRememberMeServices {

    String userGroupServiceName;

    public void setUserGroupServiceName(String userGroupServiceName) {
        this.userGroupServiceName = userGroupServiceName;
    }

    @Override
    protected String makeTokenSignature(long tokenExpiryTime, String username, String password) {
        return super.makeTokenSignature(tokenExpiryTime, encode(username), password);
    }
    
    protected String retrieveUserName(Authentication authentication) {
        return encode(super.retrieveUserName(authentication));
    };
    
    String encode(String username) {
        if (userGroupServiceName == null) {
            return username;
        }
        if (username.endsWith("@" + userGroupServiceName)) {
            return username;
        }

        //escape any @ symboles present in the username, and append '@userGroupServiceName')
        return username.replace("@","\\@") + "@" + userGroupServiceName;
    }
}
