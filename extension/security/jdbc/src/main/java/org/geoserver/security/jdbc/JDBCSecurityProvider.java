/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.security.jdbc;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.geoserver.config.util.XStreamPersister;
import org.geoserver.security.GeoServerSecurityProvider;
import org.geoserver.security.GeoserverRoleService;
import org.geoserver.security.GeoserverUserGroupService;
import org.geoserver.security.config.SecurityNamedServiceConfig;
import org.geoserver.security.jdbc.config.impl.JdbcJndiRoleServiceConfigImpl;
import org.geoserver.security.jdbc.config.impl.JdbcJndiUserGroupServiceConfigImpl;
import org.geoserver.security.jdbc.config.impl.JdbcRoleServiceConfigImpl;
import org.geoserver.security.jdbc.config.impl.JdbcUserGroupServiceConfigImpl;
import org.springframework.security.authentication.AuthenticationProvider;

/**
 * Provider for JDBC based security services.
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class JDBCSecurityProvider extends GeoServerSecurityProvider {

    @Override
    public void configure(XStreamPersister xp) {
        super.configure(xp);
        xp.getXStream().alias("jdbcusergroupservice", JdbcUserGroupServiceConfigImpl.class);
        xp.getXStream().alias("jdbcroleservice", JdbcRoleServiceConfigImpl.class);
        xp.getXStream().alias("jndiusergroupservice", JdbcJndiUserGroupServiceConfigImpl.class);
        xp.getXStream().alias("jndiroleservice", JdbcJndiRoleServiceConfigImpl.class);        
    }

    @Override
    public Map<Class<?>, Set<String>> getFieldsForEncryption() {
        Map<Class<?>, Set<String>> map = new HashMap <Class<?>, Set<String>>();
        
        Set<String> fields= new HashSet<String>();
        fields.add("password");        
        map.put(JdbcRoleServiceConfigImpl.class,fields);
        map.put(JdbcUserGroupServiceConfigImpl.class,fields);
        return map;
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
