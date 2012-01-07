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
import org.geoserver.security.jdbc.config.JDBCRoleServiceConfig;
import org.geoserver.security.jdbc.config.JDBCUserGroupServiceConfig;
import org.geoserver.security.validation.SecurityConfigValidator;

/**
 * Provider for JDBC based security services.
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class JDBCSecurityProvider extends GeoServerSecurityProvider {

    @Override
    public void configure(XStreamPersister xp) {
        super.configure(xp);
        xp.getXStream().alias("jdbcusergroupservice", JDBCUserGroupServiceConfig.class);
        xp.getXStream().alias("jdbcroleservice", JDBCRoleServiceConfig.class);
    }

    @Override
    public Map<Class<?>, Set<String>> getFieldsForEncryption() {
        Map<Class<?>, Set<String>> map = new HashMap <Class<?>, Set<String>>();
        
        Set<String> fields= new HashSet<String>();
        fields.add("password");        
        map.put(JDBCRoleServiceConfig.class,fields);
        map.put(JDBCUserGroupServiceConfig.class,fields);
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

    @Override
    public SecurityConfigValidator getConfigurationValidator() {
        return new JdbcSecurityConfigValidator(); 
     }

}
