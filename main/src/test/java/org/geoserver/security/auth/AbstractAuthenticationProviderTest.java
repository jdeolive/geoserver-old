/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.security.auth;

import org.geoserver.security.GeoServerRoleService;
import org.geoserver.security.GeoServerUserGroupService;
import org.geoserver.security.config.SecurityRoleServiceConfig;
import org.geoserver.security.config.SecurityUserGroupServiceConfig;
import org.geoserver.security.config.impl.MemoryRoleServiceConfigImpl;
import org.geoserver.security.config.impl.MemoryUserGroupServiceConfigImpl;
import org.geoserver.security.impl.AbstractSecurityServiceTest;
import org.geoserver.security.impl.GeoServerRole;
import org.geoserver.security.impl.MemoryRoleService;
import org.geoserver.security.impl.MemoryUserGroupService;
import org.geoserver.security.password.PasswordValidator;

public abstract class AbstractAuthenticationProviderTest extends AbstractSecurityServiceTest {

    
    @Override
    public GeoServerRoleService createRoleService(String name) throws Exception {
        SecurityRoleServiceConfig config = getRoleConfig(name);
        getSecurityManager().saveRoleService(config/*,isNewRoleService(name)*/);
        return getSecurityManager().loadRoleService(name);        
    }
    
    
    public MemoryRoleServiceConfigImpl getRoleConfig(String name) {
        MemoryRoleServiceConfigImpl config = new MemoryRoleServiceConfigImpl();
        config.setName(name);
        config.setClassName(MemoryRoleService.class.getName());
        config.setAdminRoleName(GeoServerRole.ADMIN_ROLE.getAuthority());
        config.setToBeEncrypted("encryptme");
        return config;
        
    }

    @Override
    public GeoServerUserGroupService createUserGroupService(String name) throws Exception {
        return createUserGroupService(name, getPBEPasswordEncoder().getName());

    }
    
    public GeoServerUserGroupService createUserGroupService(String name,String passwordEncoderName) throws Exception {
        SecurityUserGroupServiceConfig config =  getUserGroupConfg(name, passwordEncoderName);                 
        getSecurityManager().saveUserGroupService(config/*,isNewUGService(name)*/);
        return getSecurityManager().loadUserGroupService(name);

    }
    

    public MemoryUserGroupServiceConfigImpl getUserGroupConfg(String name, String passwordEncoderName) {
        MemoryUserGroupServiceConfigImpl config = new MemoryUserGroupServiceConfigImpl();         
        config.setName(name);
        config.setClassName(MemoryUserGroupService.class.getName());
        config.setPasswordEncoderName(passwordEncoderName);
        config.setPasswordPolicyName(PasswordValidator.DEFAULT_NAME);
        config.setToBeEncrypted("encryptme");
        return config;
    }

            
    


}
