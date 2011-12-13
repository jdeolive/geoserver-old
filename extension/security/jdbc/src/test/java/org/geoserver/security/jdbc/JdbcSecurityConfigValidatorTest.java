package org.geoserver.security.jdbc;

import static org.geoserver.security.jdbc.JdbcSecurityConfigValidationErrors.SEC_ERR_200;
import static org.geoserver.security.jdbc.JdbcSecurityConfigValidationErrors.SEC_ERR_201;
import static org.geoserver.security.jdbc.JdbcSecurityConfigValidationErrors.SEC_ERR_202;
import static org.geoserver.security.jdbc.JdbcSecurityConfigValidationErrors.SEC_ERR_203;
import static org.geoserver.security.jdbc.JdbcSecurityConfigValidationErrors.SEC_ERR_210;

import java.util.logging.Logger;

import org.geoserver.security.config.SecurityRoleServiceConfig;
import org.geoserver.security.config.SecurityUserGroupServiceConfig;
import org.geoserver.security.config.validation.SecurityConfigException;
import org.geoserver.security.config.validation.SecurityConfigValidatorTest;
import org.geoserver.security.impl.GeoserverRole;
import org.geoserver.security.jdbc.config.impl.JdbcJndiRoleServiceConfigImpl;
import org.geoserver.security.jdbc.config.impl.JdbcJndiUserGroupServiceConfigImpl;
import org.geoserver.security.jdbc.config.impl.JdbcRoleServiceConfigImpl;
import org.geoserver.security.jdbc.config.impl.JdbcUserGroupServiceConfigImpl;
import org.geoserver.security.password.GeoserverPlainTextPasswordEncoder;
import org.geoserver.security.password.PasswordValidator;
import org.geotools.util.logging.Logging;

public class JdbcSecurityConfigValidatorTest extends SecurityConfigValidatorTest {

    static protected Logger LOGGER = Logging.getLogger("org.geoserver.security");
    
    
    @Override
    protected void setUpInternal() throws Exception {
        super.setUpInternal();
        validator = new JdbcSecurityConfigValidator();          
    }
        
    protected SecurityUserGroupServiceConfig getUGConfig(String name, Class<?> aClass,
            String encoder, String policyName) {
        JdbcUserGroupServiceConfigImpl config = new JdbcUserGroupServiceConfigImpl();
        config.setName(name);
        config.setClassName(aClass.getName());
        config.setPasswordEncoderName(encoder);
        config.setPasswordPolicyName(policyName);
        return config;
    }
    
    protected SecurityUserGroupServiceConfig getUGConfigJNDI(String name, Class<?> aClass,
            String encoder, String policyName) {
        JdbcJndiUserGroupServiceConfigImpl config = new JdbcJndiUserGroupServiceConfigImpl();
        config.setName(name);
        config.setClassName(aClass.getName());
        config.setPasswordEncoderName(encoder);
        config.setPasswordPolicyName(policyName);
        return config;
    }

    
    protected SecurityRoleServiceConfig getRoleConfig(String name, Class<?> aClass,String adminRole) {
        JdbcRoleServiceConfigImpl config = new JdbcRoleServiceConfigImpl();
        config.setName(name);
        config.setClassName(aClass.getName());
        config.setAdminRoleName(adminRole);
        return config;
    }
    
    protected SecurityRoleServiceConfig getRoleConfigJNDI(String name, Class<?> aClass,String adminRole) {
        JdbcJndiRoleServiceConfigImpl config = new JdbcJndiRoleServiceConfigImpl();
        config.setName(name);
        config.setClassName(aClass.getName());
        config.setAdminRoleName(adminRole);
        return config;
    }


    public void testRoleConfig() {
        
        super.testRoleConfig();
        
        JdbcRoleServiceConfigImpl  config = 
                (JdbcRoleServiceConfigImpl)getRoleConfig("jdbc", JDBCRoleService.class, 
                GeoserverRole.ADMIN_ROLE.getAuthority());
        
        config.setDriverClassName("a.b.c");
        config.setUserName("user");
        config.setConnectURL("jdbc:connect");
        
        JdbcJndiRoleServiceConfigImpl  configJNDI = 
                (JdbcJndiRoleServiceConfigImpl)getRoleConfigJNDI("jndi", JDBCRoleService.class, 
                GeoserverRole.ADMIN_ROLE.getAuthority());
        configJNDI.setJndiName("jndi:connect");
        
        boolean fail;

        
        
        fail=false;
        try {            
            configJNDI.setJndiName("");
            validator.validateAddRoleService(configJNDI);                         
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
            validator.validateAddRoleService(config);                         
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
            validator.validateAddRoleService(config);                         
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
            validator.validateAddRoleService(config);                         
        } catch (SecurityConfigException ex) {
            assertEquals( SEC_ERR_202,ex.getErrorId());
            assertEquals(0,ex.getArgs().length);
            LOGGER.info(ex.getMessage());
            fail=true;
        }
        assertTrue(fail);
        
        config.setConnectURL("jdbc:connect");
        try {            
            config.setConnectURL(null);
            validator.validateAddRoleService(config);                         
        } catch (SecurityConfigException ex) {
            assertEquals( SEC_ERR_203,ex.getErrorId());
            assertEquals("a.b.c",ex.getArgs()[0]);
            LOGGER.info(ex.getMessage());
            fail=true;
        }

        
    }

    
    public void testUserGroupConfig() {

        super.testUserGroupConfig();
        
        JdbcUserGroupServiceConfigImpl  config = 
                (JdbcUserGroupServiceConfigImpl)getUGConfig("jdbc", JDBCUserGroupService.class, 
                GeoserverPlainTextPasswordEncoder.BeanName,PasswordValidator.DEFAULT_NAME);

        config.setDriverClassName("a.b.c");
        config.setUserName("user");
        config.setConnectURL("jdbc:connect");

        JdbcJndiUserGroupServiceConfigImpl  configJNDI = 
                (JdbcJndiUserGroupServiceConfigImpl)getUGConfigJNDI("jdbc", JDBCUserGroupService.class, 
                GeoserverPlainTextPasswordEncoder.BeanName,PasswordValidator.DEFAULT_NAME);
                        
        configJNDI.setJndiName("jndi:connect");
        
        boolean fail;

        
        
        fail=false;
        try {            
            configJNDI.setJndiName("");
            validator.validateAddUserGroupService(configJNDI);                         
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
            validator.validateAddUserGroupService(config);                         
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
            validator.validateAddUserGroupService(config);                         
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
            validator.validateAddUserGroupService(config);                         
        } catch (SecurityConfigException ex) {
            assertEquals( SEC_ERR_202,ex.getErrorId());
            assertEquals(0,ex.getArgs().length);
            LOGGER.info(ex.getMessage());
            fail=true;
        }
        assertTrue(fail);
        
        config.setConnectURL("jdbc:connect");
        try {            
            config.setConnectURL(null);
            validator.validateAddUserGroupService(config);                         
        } catch (SecurityConfigException ex) {
            assertEquals( SEC_ERR_203,ex.getErrorId());
            assertEquals("a.b.c",ex.getArgs()[0]);
            LOGGER.info(ex.getMessage());
            fail=true;
        }

        
        
    }

}
