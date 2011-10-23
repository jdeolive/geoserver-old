/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.security.jdbc;

import java.io.IOException;

import org.geoserver.security.GeoServerSecurityProvider;
import org.geoserver.security.GeoserverRoleService;
import org.geoserver.security.GeoserverUserGroupService;
import org.geoserver.security.config.SecurityNamedServiceConfig;
import org.springframework.security.authentication.AuthenticationProvider;

/**
 * Provider for JDBC based security services.
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class JDBCSecurityProvider extends GeoServerSecurityProvider {

    @Override
    public AuthenticationProvider createAuthProvider(SecurityNamedServiceConfig config) {
        return null;
    }

    @Override
    public Class<? extends GeoserverUserGroupService> getUserGroupServiceClass() {
        return JDBCUserGroupService.class;
    }

    @Override
    public GeoserverUserGroupService createUserGroupService(SecurityNamedServiceConfig config)
        throws IOException {
        return new JDBCUserGroupService();
    }

    @Override
    public Class<? extends GeoserverRoleService> getRoleServiceClass() {
        return JDBCRoleService.class; 
    }

    @Override
    public GeoserverRoleService createRoleService(SecurityNamedServiceConfig config)
            throws IOException {
        return new JDBCRoleService();
    }

}
