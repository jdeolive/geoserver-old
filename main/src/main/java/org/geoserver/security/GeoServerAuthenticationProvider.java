/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.security;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.geoserver.security.config.SecurityNamedServiceConfig;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/**
 * Extension of {@link AuthenticationProvider} that also provides access to the filter security
 * chain. 
 * 
 * @author Justin Deoliveira, OpenGeo
 * 
 * TODO: this class shared much with {@link GeoServerSecurityService}, perhaps it should just 
 * implement/extend it.
 */
public abstract class GeoServerAuthenticationProvider implements AuthenticationProvider {

    public static String DEFAULT_NAME = "default";

    /**
     * name of the auth provider
     */
    protected String name;

    /**
     * reference back to security manager
     */
    protected GeoServerSecurityManager securityManager;

    /**
     * Name of the authentication provider.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets name of the authentication provider.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * The security manager facade.
     */
    public GeoServerSecurityManager getSecurityManager() {
        return securityManager;
    }

    /**
     * Sets the security manager facade.
     */
    public void setSecurityManager(GeoServerSecurityManager securityManager) {
        this.securityManager = securityManager;
    }

    /**
     * Initializes the authentication provider from its configuration object. 
     */
    public void initializeFromConfig(SecurityNamedServiceConfig config) throws IOException {
    }

    /**
     * Configures the filter security chain, inserting any filters that are required by the 
     * authentication provider. 
     */
    public void configureFilterChain(GeoServerSecurityFilterChain filterChain) {
        //subclasses override, default does nothing
    }

    /**
     * Deconfigures the filter security chain, cleaning up any filters inserted by 
     *  {@link #configure(GeoServerSecurityFilterChain)}.
     */
    public void deconfigureFilterChain(GeoServerSecurityFilterChain filterChain) {
       //subclasses override, default does nothing
    }

    @Override
    public final boolean supports(Class<? extends Object> authentication) {
        return supports(authentication, request());
    }

    /**
     * Same function as {@link #supports(Class)} but is provided with the current request object.
     */
    public abstract boolean supports(Class<? extends Object> authentication, HttpServletRequest request);

    @Override
    public final Authentication authenticate(Authentication authentication)
            throws AuthenticationException {
        return authenticate(authentication, request());
    }

    /**
     * Same function as {@link #authenticate(Authentication)} but is provided with the current 
     * request object.
     */
    public abstract Authentication authenticate(Authentication authentication, HttpServletRequest request)
            throws AuthenticationException;

    /**
     * The current request.
     */
    HttpServletRequest request() {
        return GeoServerSecurityFilterChainProxy.REQUEST.get();
    }
}
