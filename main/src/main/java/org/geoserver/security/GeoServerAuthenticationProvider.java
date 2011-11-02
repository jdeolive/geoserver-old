/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.security;

import org.springframework.security.authentication.AuthenticationProvider;

/**
 * Extension of {@link AuthenticationProvider} that also provides access to the filter security
 * chain. 
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public abstract class GeoServerAuthenticationProvider implements AuthenticationProvider {

    /**
     * Configures the filter security chain, inserting any filters that are required by the 
     * authentication provider. 
     */
    public void configure(GeoServerSecurityFilterChain filterChain) {
        //subclasses override, default does nothing
    }

    /**
     * Deconfigures the filter security chain, cleaning up any filters inserted by 
     *  {@link #configure(GeoServerSecurityFilterChain)}.
     */
    public void deconfigure(GeoServerSecurityFilterChain filterChain) {
       //subclasses override, default does nothing
    }
}
