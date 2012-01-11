package org.geoserver.security.jdbc;

import static org.geoserver.security.jdbc.JdbcSecurityConfigValidationErrors.SEC_ERR_200;
import static org.geoserver.security.jdbc.JdbcSecurityConfigValidationErrors.SEC_ERR_201;
import static org.geoserver.security.jdbc.JdbcSecurityConfigValidationErrors.SEC_ERR_202;
import static org.geoserver.security.jdbc.JdbcSecurityConfigValidationErrors.SEC_ERR_203;
import static org.geoserver.security.jdbc.JdbcSecurityConfigValidationErrors.SEC_ERR_210;

import java.io.IOException;
import java.util.logging.Logger;

import org.geoserver.security.config.SecurityRoleServiceConfig;
import org.geoserver.security.config.SecurityUserGroupServiceConfig;
import org.geoserver.security.impl.GeoServerRole;
import org.geoserver.security.jdbc.config.JDBCRoleServiceConfig;
import org.geoserver.security.jdbc.config.JDBCUserGroupServiceConfig;
import org.geoserver.security.password.PasswordValidator;
import org.geoserver.security.validation.SecurityConfigException;
import org.geoserver.security.validation.SecurityConfigValidatorTest;
import org.geotools.util.logging.Logging;

public class JdbcSecurityConfigValidatorTest extends SecurityConfigValidatorTest {

    static protected Logger LOGGER = Logging.getLogger("org.geoserver.security");
    
    
    @Override
    protected void setUpInternal() throws Exception {
        super.setUpInternal();
    }
        
    protected SecurityUserGroupServiceConfig getUGConfig(String name, Class<?> aClass,
            String encoder, String policyName) {
        JDBCUserGroupServiceConfig config = new JDBCUserGroupServiceConfig();
        config.setName(name);
        config.setClassName(aClass.getName());
        config.setPasswordEncoderName(encoder);
        config.setPasswordPolicyName(policyName);
        return config;
    }
    
    
    protected SecurityRoleServiceConfig getRoleConfig(String name, Class<?> aClass,String adminRole) {
        JDBCRoleServiceConfig config = new JDBCRoleServiceConfig();
        config.setName(name);
        config.setClassName(aClass.getName());
        config.setAdminRoleName(adminRole);
        return config;
    }
    


    public void testRoleConfig() throws IOException {
        
        super.testRoleConfig();
        
        JDBCRoleServiceConfig  config = 
                (JDBCRoleServiceConfig)getRoleConfig("jdbc", JDBCRoleService.class, 
                GeoServerRole.ADMIN_ROLE.getAuthority());
        
        config.setDriverClassName("a.b.c");
        config.setUserName("user");
        config.setConnectURL("jdbc:connect");
        
        JDBCRoleServiceConfig  configJNDI = (JDBCRoleServiceConfig) 
                getRoleConfig("jndi", JDBCRoleService.class, 
                GeoServerRole.ADMIN_ROLE.getAuthority());
        configJNDI.setJndi(true);
        configJNDI.setJndiName("jndi:connect");
        
        boolean fail;

        
        
        fail=false;
        try {            
            configJNDI.setJndiName("");
            getSecurityManager().saveRoleService(configJNDI, true);
        } catch (SecurityConfigException ex) {
            assertEquals( SEC_ERR_210,ex.getErrorId());
            assertEquals(0,ex.getArgs().length);
            LOGGER.info(ex.getMessage());
            fail=true;
        }
        assertTrue(fail);

        fail=false;
        try {            
            config.setDriverClassName("");
            getSecurityManager().saveRoleService(config, true);
        } catch (SecurityConfigException ex) {
            assertEquals( SEC_ERR_200,ex.getErrorId());
            assertEquals(0,ex.getArgs().length);
            LOGGER.info(ex.getMessage());
            fail=true;
        }
        assertTrue(fail);

        config.setDriverClassName("a.b.c");
        fail=false;
        try {            
            config.setUserName("");
            getSecurityManager().saveRoleService(config, true);                         
        } catch (SecurityConfigException ex) {
            assertEquals( SEC_ERR_201,ex.getErrorId());
            assertEquals(0,ex.getArgs().length);
            LOGGER.info(ex.getMessage());
            fail=true;
        }
        assertTrue(fail);

        config.setUserName("user");
        fail=false;
        try {            
            config.setConnectURL(null);
            getSecurityManager().saveRoleService(config, true);                         
        } catch (SecurityConfigException ex) {
            assertEquals( SEC_ERR_202,ex.getErrorId());
            assertEquals(0,ex.getArgs().length);
            LOGGER.info(ex.getMessage());
            fail=true;
        }
        assertTrue(fail);
        
        config.setConnectURL("jdbc:connect");
        try {            
            getSecurityManager().saveRoleService(config, true);                         
        } catch (SecurityConfigException ex) {
            assertEquals( SEC_ERR_203,ex.getErrorId());
            assertEquals("a.b.c",ex.getArgs()[0]);
            LOGGER.info(ex.getMessage());
            fail=true;
        }

        
    }

    
    public void testUserGroupConfig() throws IOException {

        super.testUserGroupConfig();
        
        JDBCUserGroupServiceConfig  config = 
                (JDBCUserGroupServiceConfig)getUGConfig("jdbc", JDBCUserGroupService.class,
                getPlainTextPasswordEncoder().getName() ,PasswordValidator.DEFAULT_NAME);

        config.setDriverClassName("a.b.c");
        config.setUserName("user");
        config.setConnectURL("jdbc:connect");

        JDBCUserGroupServiceConfig  configJNDI = (JDBCUserGroupServiceConfig) 
                getUGConfig("jdbc", JDBCUserGroupService.class, 
                getPlainTextPasswordEncoder().getName(),PasswordValidator.DEFAULT_NAME);
        configJNDI.setJndi(true);                        
        configJNDI.setJndiName("jndi:connect");
        
        boolean fail;

        
        
        fail=false;
        try {            
            configJNDI.setJndiName("");
            getSecurityManager().saveUserGroupService(configJNDI, true);                                     
        } catch (SecurityConfigException ex) {
            assertEquals( SEC_ERR_210,ex.getErrorId());
            assertEquals(0,ex.getArgs().length);
            LOGGER.info(ex.getMessage());
            fail=true;
        }
        assertTrue(fail);

        fail=false;
        try {            
            config.setDriverClassName("");
            getSecurityManager().saveUserGroupService(config, true);                         
        } catch (SecurityConfigException ex) {
            assertEquals( SEC_ERR_200,ex.getErrorId());
            assertEquals(0,ex.getArgs().length);
            LOGGER.info(ex.getMessage());
            fail=true;
        }
        assertTrue(fail);

        config.setDriverClassName("a.b.c");
        fail=false;
        try {            
            config.setUserName("");
            getSecurityManager().saveUserGroupService(config, true);                         
        } catch (SecurityConfigException ex) {
            assertEquals( SEC_ERR_201,ex.getErrorId());
            assertEquals(0,ex.getArgs().length);
            LOGGER.info(ex.getMessage());
            fail=true;
        }
        assertTrue(fail);

        config.setUserName("user");
        fail=false;
        try {            
            config.setConnectURL(null);
            getSecurityManager().saveUserGroupService(config, true);                         
        } catch (SecurityConfigException ex) {
            assertEquals( SEC_ERR_202,ex.getErrorId());
            assertEquals(0,ex.getArgs().length);
            LOGGER.info(ex.getMessage());
            fail=true;
        }
        assertTrue(fail);
        
        config.setConnectURL("jdbc:connect");
        try {            
            getSecurityManager().saveUserGroupService(config, true);                         
        } catch (SecurityConfigException ex) {
            assertEquals( SEC_ERR_203,ex.getErrorId());
            assertEquals("a.b.c",ex.getArgs()[0]);
            LOGGER.info(ex.getMessage());
            fail=true;
        }

        
        
    }

}
