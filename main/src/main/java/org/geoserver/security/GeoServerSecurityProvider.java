/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.security;

import java.io.IOException;

import org.geoserver.config.util.XStreamPersister;
import org.geoserver.security.config.SecurityNamedServiceConfig;
import org.springframework.security.authentication.AuthenticationProvider;

/**
 * Extension point for backend authentication and authorization services.
 * <p>
 * Subclasses of this class should be registered in the spring context. 
 * </p>
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public abstract class GeoServerSecurityProvider {

    /**
     * Flag determining if this provider is available.
     * <p>
     * This default implementation returns <code>true</code>, subclasses should override in cases
     * where a meaningful check can be made... for instance checking for a jdbc driver, etc...
     * </p>
     */
    public boolean isAvaialble() {
        return true;
    }

    /**
     * Configures the xstream instance used to serialize/deserialize provider configuration. 
     */
    public void configure(XStreamPersister xp) {
    }

    /**
     * Creates an authentication provider.
     * <p>
     * If the extension does not provide an authentication provider this method should simply return
     * <code>null</code>. 
     */
    public abstract AuthenticationProvider createAuthProvider(SecurityNamedServiceConfig config);

    /**
     * Returns the specific class of the user group service created by 
     * {@link #createUserGroupService(SecurityNamedServiceConfig)}.
     * <p>
     * If the extension does not provide a user group service this method should simply return
     * <code>null</code>. 
     * </p> 
     */
    public abstract Class<? extends GeoserverUserGroupService> getUserGroupServiceClass();
    
    /**
     * Creates a new user group service.
     * <p>
     * If the extension does not provide a user group service this method should simply return
     * <code>null</code>. 
     * </p>
     */
    public abstract GeoserverUserGroupService createUserGroupService(SecurityNamedServiceConfig config)
        throws IOException;

    /**
     * Returns the specific class of the role service created by 
     * {@link #createRoleService(SecurityNamedServiceConfig)}
     * <p>
     * If the extension does not provide a role service this method should simply return
     * <code>null</code>. 
     * </p> 
     */
    public abstract Class<? extends GeoserverGrantedAuthorityService> getRoleServiceClass();
    
    /**
     * Creates a new role group service.
     * <p>
     * If the extension does not provide a role service this method should simply return
     * <code>null</code>. 
     * </p>
     */
    public abstract GeoserverGrantedAuthorityService createRoleService(SecurityNamedServiceConfig config) 
        throws IOException;

    
}
