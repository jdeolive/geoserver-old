package org.geoserver.security.impl;

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
import org.geoserver.security.config.impl.MemoryRoleServiceConfigImpl;
import org.geoserver.security.config.impl.MemoryUserGroupServiceConfigImpl;
import org.geoserver.security.config.validation.SecurityConfigValidator;

/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

public class MemorySecurityProvider extends GeoServerSecurityProvider {
    
    
    public void configure(XStreamPersister xp) {
        super.configure(xp);
        xp.getXStream().alias("memorygroupservice", MemoryUserGroupServiceConfigImpl.class);
        xp.getXStream().alias("memoryroleservice", MemoryRoleServiceConfigImpl.class);
    }

    @Override
    public Map<Class<?>, Set<String>> getFieldsForEncryption() {
        Map<Class<?>, Set<String>> map = new HashMap <Class<?>, Set<String>>();
        
        Set<String> fields= new HashSet<String>();
        fields.add("toBeEncrypted");        
        map.put(MemoryRoleServiceConfigImpl.class,fields);
        map.put(MemoryUserGroupServiceConfigImpl.class,fields);
        return map;
    }

    
    @Override
    public AuthenticationProvider createAuthProvider(SecurityNamedServiceConfig config) {
        return null;
    }

    @Override
    public Class<? extends GeoserverUserGroupService> getUserGroupServiceClass() {
        return MemoryUserGroupService.class;
    }

    @Override
    public GeoserverUserGroupService createUserGroupService(SecurityNamedServiceConfig config)
            throws IOException {
        return new MemoryUserGroupService();
    }

    @Override
    public Class<? extends GeoserverRoleService> getRoleServiceClass() {
        return MemoryRoleService.class;
    }

    @Override
    public GeoserverRoleService createRoleService(SecurityNamedServiceConfig config)
            throws IOException {
        return new MemoryRoleService();
    }

    @Override
    public SecurityConfigValidator getConfigurationValidator() {
        return new MemorySecurityConfigValidator(); 
     }

}
