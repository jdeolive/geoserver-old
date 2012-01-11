/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.security;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.security.config.SecurityNamedServiceConfig;
import org.geoserver.security.config.UsernamePasswordAuthenticationProviderConfig;
import org.geoserver.security.password.GeoServerPasswordEncoder;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/**
 * Authentication provider that delegates to a {@link GeoServerUserGroupService}.
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class UsernamePasswordAuthenticationProvider extends GeoServerAuthenticationProvider {

    /** auth provider to delegate to */
    DaoAuthenticationProvider authProvider;

    @Override
    public void initializeFromConfig(SecurityNamedServiceConfig config) throws IOException {
        UsernamePasswordAuthenticationProviderConfig upAuthConfig = 
                (UsernamePasswordAuthenticationProviderConfig) config;

        GeoServerUserGroupService ugService = 
            getSecurityManager().loadUserGroupService(upAuthConfig.getUserGroupServiceName());
        if (ugService == null) {
            throw new IllegalArgumentException("Unable to load user group service " 
                + upAuthConfig.getUserGroupServiceName());
        }

        //create delegate auth provider
        authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(ugService);
        
        //set up the password encoder
        GeoServerPasswordEncoder encoder = 
            getSecurityManager().loadPasswordEncoder(ugService.getPasswordEncoderName());

        encoder.initializeFor(ugService);
        authProvider.setPasswordEncoder(encoder);
        try {
            authProvider.afterPropertiesSet();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public boolean supports(Class<? extends Object> authentication, HttpServletRequest request) {
        return authProvider.supports(authentication);
    }

    @Override
    public Authentication authenticate(Authentication authentication, HttpServletRequest request)
            throws AuthenticationException {
        return authProvider.authenticate(authentication);
    }

//    static class SecurityProvider extends GeoServerSecurityProvider {
//        
//        @Override
//        public Class<? extends GeoServerAuthenticationProvider> getAuthenticationProviderClass() {
//            return UsernamePasswordAuthenticationProvider.class;
//        }
//
//        @Override
//        public GeoServerAuthenticationProvider createAuthenticationProvider(
//                SecurityNamedServiceConfig config) {
//            return new UsernamePasswordAuthenticationProvider();
//        }
//    }
}
