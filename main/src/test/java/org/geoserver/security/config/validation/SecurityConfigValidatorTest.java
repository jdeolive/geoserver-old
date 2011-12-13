package org.geoserver.security.config.validation;

import static org.geoserver.security.config.validation.SecurityConfigValidationErrors.*;

import java.util.logging.Logger;

import org.geoserver.security.GeoServerAuthenticationProvider;
import org.geoserver.security.GeoserverAuthenticationProcessingFilter;
import org.geoserver.security.GeoserverRoleService;
import org.geoserver.security.GeoserverUserGroupService;
import org.geoserver.security.UsernamePasswordAuthenticationProvider;
import org.geoserver.security.config.PasswordPolicyConfig;
import org.geoserver.security.config.SecurityAuthProviderConfig;
import org.geoserver.security.config.SecurityManagerConfig;
import org.geoserver.security.config.SecurityNamedServiceConfig;
import org.geoserver.security.config.SecurityRoleServiceConfig;
import org.geoserver.security.config.SecurityUserGroupServiceConfig;
import org.geoserver.security.config.impl.MemoryRoleServiceConfigImpl;
import org.geoserver.security.config.impl.MemoryUserGroupServiceConfigImpl;
import org.geoserver.security.config.impl.PasswordPolicyConfigImpl;
import org.geoserver.security.config.impl.SecurityManagerConfigImpl;
import org.geoserver.security.config.impl.SecurityNamedServiceConfigImpl;
import org.geoserver.security.config.impl.UsernamePasswordAuthenticationProviderConfig;
import org.geoserver.security.impl.GeoserverRole;
import org.geoserver.security.impl.MemoryUserGroupService;
import org.geoserver.security.password.GeoserverConfigPBEPasswordEncoder;
import org.geoserver.security.password.GeoserverPlainTextPasswordEncoder;
import org.geoserver.security.password.PasswordValidator;
import org.geoserver.security.password.PasswordValidatorImpl;
import org.geoserver.security.xml.XMLRoleService;
import org.geoserver.security.xml.XMLUserGroupService;
import org.geoserver.test.GeoServerTestSupport;
import org.geotools.util.logging.Logging;

public class SecurityConfigValidatorTest extends GeoServerTestSupport {

    
    static protected Logger LOGGER = Logging.getLogger("org.geoserver.security");
    protected SecurityConfigValidator validator;
    
    
    @Override
    protected void setUpInternal() throws Exception {
        super.setUpInternal();
        validator = new SecurityConfigValidator(); 
         
    }
        
    public void testMasterConfigValidation() throws Exception{
        SecurityManagerConfig config = new SecurityManagerConfigImpl();
        config.setRoleServiceName(XMLRoleService.DEFAULT_NAME);
        config.setConfigPasswordEncrypterName(GeoserverConfigPBEPasswordEncoder.BeanName);
        config.getAuthProviderNames().add(GeoServerAuthenticationProvider.DEFAULT_NAME);
        
        validator.validateManagerConfig(config);
        config.setConfigPasswordEncrypterName(null);
        validator.validateManagerConfig(config);
        
        config.setConfigPasswordEncrypterName("abc");
        
        boolean failed = false;
        try {
            validator.validateManagerConfig(config);
        } catch (SecurityConfigException ex){
            assertEquals(SEC_ERR_01,ex.getErrorId());
            LOGGER.info(ex.getMessage());
            failed=true;
        }
        assertTrue(failed);
        
        config.setConfigPasswordEncrypterName(GeoserverConfigPBEPasswordEncoder.BeanName);
        config.setRoleServiceName("XX");
        
        failed = false;
        try {
            validator.validateManagerConfig(config);
        } catch (SecurityConfigException ex){
            assertEquals(SEC_ERR_02,ex.getErrorId());
            LOGGER.info(ex.getMessage());
            failed=true;
        }
        assertTrue(failed);
        
        config.setRoleServiceName(null);        
        failed = false;
        try {
            validator.validateManagerConfig(config);
        } catch (SecurityConfigException ex){
            assertEquals(SEC_ERR_02,ex.getErrorId());
            LOGGER.info(ex.getMessage());
            failed=true;
        }
        assertTrue(failed);


        config.setRoleServiceName(XMLRoleService.DEFAULT_NAME);
        config.getAuthProviderNames().add("XX");
        
        failed = false;
        try {
            validator.validateManagerConfig(config);
        } catch (SecurityConfigException ex){
            assertEquals(SEC_ERR_03,ex.getErrorId());
            LOGGER.info(ex.getMessage());
            failed=true;
        }
        assertTrue(failed);
        config.getAuthProviderNames().remove("XX");

    }
    
    public void testNamedServices() {
        boolean fail;
        Class<?>[] extensionPoints = new Class<?>[] {
                GeoserverUserGroupService.class,
                GeoserverRoleService.class,
                PasswordValidator.class,
                GeoServerAuthenticationProvider.class,
                GeoserverAuthenticationProcessingFilter.class                
        };
        
        for (Class<?> ep : extensionPoints) {
           fail=false;
           try {
               validator.checkExtensionPont(ep, "a.b.c");
           } catch (SecurityConfigException ex) {
               assertEquals(ex.getErrorId(), SEC_ERR_20);
               assertEquals(ex.getArgs()[0],ep);
               assertEquals(ex.getArgs()[1],"a.b.c");
               LOGGER.info(ex.getMessage());
               fail=true;               
           }
           assertTrue(fail);
           fail=false;
           try {
               validator.checkExtensionPont(ep, "java.lang.String");
           } catch (SecurityConfigException ex) {
               assertEquals(ex.getErrorId(), SEC_ERR_21);
               assertEquals(ex.getArgs()[0],ep);
               assertEquals(ex.getArgs()[1],"java.lang.String");
               LOGGER.info(ex.getMessage());
               fail=true;               
           }
           assertTrue(fail);

           fail=false;
           String name = ep == GeoserverUserGroupService.class ? null : "";
           try {               
               validator.checkServiceName(ep, name);
           } catch (SecurityConfigException ex) {
               assertEquals(ex.getErrorId(), SEC_ERR_22);
               assertEquals(ex.getArgs()[0],ep);
               assertEquals(ex.getArgs()[1],name);
               LOGGER.info(ex.getMessage());
               fail=true;               
           }
           assertTrue(fail);
        }

        // test names
        fail=false;
        try {
            validator.validateAddPasswordPolicyService(
                getPolicyConfig(PasswordValidator.DEFAULT_NAME, PasswordValidatorImpl.class, 1,10));
        } catch (SecurityConfigException ex) {
            assertEquals(ex.getErrorId(), SEC_ERR_23);
            assertEquals(ex.getArgs()[0],PasswordValidator.class);
            assertEquals(ex.getArgs()[1],PasswordValidator.DEFAULT_NAME);
            LOGGER.info(ex.getMessage());
            fail=true;
        }
        assertTrue(fail);
        
        fail=false;
        try {
            validator.validateModifiedPasswordPolicyService(
                getPolicyConfig("default2", PasswordValidatorImpl.class, 1,10));
        } catch (SecurityConfigException ex) {
            assertEquals(ex.getErrorId(), SEC_ERR_24);
            assertEquals(ex.getArgs()[0],PasswordValidator.class);
            assertEquals(ex.getArgs()[1],"default2");
            LOGGER.info(ex.getMessage());
            fail=true;
        }
        assertTrue(fail);
        
        fail=false;
        try {
            validator.validateAddUserGroupService(
                getUGConfig(XMLUserGroupService.DEFAULT_NAME, GeoserverUserGroupService.class, 
                        GeoserverPlainTextPasswordEncoder.BeanName,PasswordValidator.DEFAULT_NAME));
        } catch (SecurityConfigException ex) {
            assertEquals(ex.getErrorId(), SEC_ERR_23);
            assertEquals(ex.getArgs()[0],GeoserverUserGroupService.class);
            assertEquals(ex.getArgs()[1],XMLUserGroupService.DEFAULT_NAME);
            LOGGER.info(ex.getMessage());
            fail=true;
        }
        assertTrue(fail);
        
        fail=false;
        try {
            validator.validateModifiedUserGroupService(
                    getUGConfig("default2", GeoserverUserGroupService.class, 
                            GeoserverPlainTextPasswordEncoder.BeanName,
                            PasswordValidator.DEFAULT_NAME));
        } catch (SecurityConfigException ex) {
            assertEquals(ex.getErrorId(), SEC_ERR_24);
            assertEquals(ex.getArgs()[0],GeoserverUserGroupService.class);
            assertEquals(ex.getArgs()[1],"default2");
            LOGGER.info(ex.getMessage());
            fail=true;
        }
        assertTrue(fail);
        
        fail=false;
        try {
            validator.validateAddRoleService(
                getRoleConfig(XMLRoleService.DEFAULT_NAME, GeoserverRoleService.class, 
                        GeoserverRole.ADMIN_ROLE.getAuthority()));
        } catch (SecurityConfigException ex) {
            assertEquals(ex.getErrorId(), SEC_ERR_23);
            assertEquals(ex.getArgs()[0],GeoserverRoleService.class);
            assertEquals(ex.getArgs()[1],XMLRoleService.DEFAULT_NAME);
            LOGGER.info(ex.getMessage());
            fail=true;
        }
        assertTrue(fail);
        
        fail=false;
        try {
            validator.validateModifiedRoleService(
                    getRoleConfig("default2", GeoserverRoleService.class, 
                            GeoserverRole.ADMIN_ROLE.getAuthority()));
        } catch (SecurityConfigException ex) {
            assertEquals(ex.getErrorId(), SEC_ERR_24);
            assertEquals(ex.getArgs()[0],GeoserverRoleService.class);
            assertEquals(ex.getArgs()[1],"default2");
            LOGGER.info(ex.getMessage());
            fail=true;
        }
        
        assertTrue(fail);
        fail=false;
        try {
            validator.validateAddAuthProvider(
                getAuthConfig(GeoServerAuthenticationProvider.DEFAULT_NAME, UsernamePasswordAuthenticationProvider.class,
                        XMLUserGroupService.DEFAULT_NAME));                         
        } catch (SecurityConfigException ex) {
            assertEquals(ex.getErrorId(), SEC_ERR_23);
            assertEquals(ex.getArgs()[0],GeoServerAuthenticationProvider.class);
            assertEquals(ex.getArgs()[1],GeoServerAuthenticationProvider.DEFAULT_NAME);
            LOGGER.info(ex.getMessage());
            fail=true;
        }
        assertTrue(fail);
        
        fail=false;
        try {
            validator.validateModifiedAuthProvider(
                    getAuthConfig("default2", UsernamePasswordAuthenticationProvider.class, XMLUserGroupService.DEFAULT_NAME));                         
        } catch (SecurityConfigException ex) {
            assertEquals(ex.getErrorId(), SEC_ERR_24);
            assertEquals(ex.getArgs()[0],GeoServerAuthenticationProvider.class);
            assertEquals(ex.getArgs()[1],"default2");
            LOGGER.info(ex.getMessage());
            fail=true;
        }
        assertTrue(fail);
    }
        
    protected SecurityAuthProviderConfig getAuthConfig(String name, Class<?> aClass,String userGroupServiceName) {
        SecurityAuthProviderConfig config = new UsernamePasswordAuthenticationProviderConfig();
        config.setName(name);
        config.setClassName(aClass.getName());
        config.setUserGroupServiceName(userGroupServiceName);
        return config;
    }
    
    protected SecurityUserGroupServiceConfig getUGConfig(String name, Class<?> aClass,
            String encoder, String policyName) {
        MemoryUserGroupServiceConfigImpl config = new MemoryUserGroupServiceConfigImpl();
        config.setName(name);
        config.setClassName(aClass.getName());
        config.setPasswordEncoderName(encoder);
        config.setPasswordPolicyName(policyName);
        return config;
    }
    
    protected SecurityRoleServiceConfig getRoleConfig(String name, Class<?> aClass,String adminRole) {
        SecurityRoleServiceConfig config = new MemoryRoleServiceConfigImpl();
        config.setName(name);
        config.setClassName(aClass.getName());
        config.setAdminRoleName(adminRole);
        return config;
    }

    protected PasswordPolicyConfig getPolicyConfig(String name, Class<?> aClass,int min, int max) {
        PasswordPolicyConfig config = new PasswordPolicyConfigImpl();
        config.setName(name);
        config.setClassName(aClass.getName());
        config.setMinLength(min);
        config.setMaxLength(max);
        return config;
    }
    
    protected SecurityNamedServiceConfig getFilterConfig(String name, Class<?> aClass) {
        SecurityNamedServiceConfig config = new SecurityNamedServiceConfigImpl();
        config.setName(name);
        config.setClassName(aClass.getName());
        return config;
    }

    
    public void testPasswordPolicy() {
        
        PasswordPolicyConfig config = getPolicyConfig(PasswordValidator.DEFAULT_NAME, PasswordValidatorImpl.class, 0,10);
        boolean fail;
        
        fail=false;
        try {
            config.setName("default2");
            validator.validateAddPasswordPolicyService(config);                         
        } catch (SecurityConfigException ex) {
            assertEquals( SEC_ERR_40,ex.getErrorId());
            assertEquals(0,ex.getArgs().length);
            LOGGER.info(ex.getMessage());
            fail=true;
        }
        assertTrue(fail);
        
        fail=false;
        try {
            config.setName(PasswordValidator.DEFAULT_NAME);
            validator.validateModifiedPasswordPolicyService(config);                         
        } catch (SecurityConfigException ex) {
            assertEquals( SEC_ERR_40,ex.getErrorId());
            assertEquals(0,ex.getArgs().length);
            LOGGER.info(ex.getMessage());
            fail=true;
        }
        assertTrue(fail);

        config.setMinLength(1);
        config.setMaxLength(0);
        
        fail=false;
        try {
            config.setName("default2");
            validator.validateAddPasswordPolicyService(config);                         
        } catch (SecurityConfigException ex) {
            assertEquals( SEC_ERR_41,ex.getErrorId());
            assertEquals(0,ex.getArgs().length);
            LOGGER.info(ex.getMessage());
            fail=true;
        }
        assertTrue(fail);
        
        fail=false;
        try {
            config.setName(PasswordValidator.DEFAULT_NAME);
            validator.validateModifiedPasswordPolicyService(config);                         
        } catch (SecurityConfigException ex) {
            assertEquals( SEC_ERR_41,ex.getErrorId());
            assertEquals(0,ex.getArgs().length);
            LOGGER.info(ex.getMessage());
            fail=true;
        }
        assertTrue(fail);

        config.setMaxLength(-1);
        
        
        fail=false;
        try {
            config.setName("");
            validator.validateRemovePasswordPolicyService(config);                         
        } catch (SecurityConfigException ex) {
            assertEquals( SEC_ERR_22,ex.getErrorId());
            assertEquals(PasswordValidator.class,ex.getArgs()[0]);
            assertEquals("",ex.getArgs()[1]);
            LOGGER.info(ex.getMessage());
            fail=true;
        }
        assertTrue(fail);
        
        
        fail=false;
        try {
            config.setName(PasswordValidator.DEFAULT_NAME);
            validator.validateRemovePasswordPolicyService(config);                         
        } catch (SecurityConfigException ex) {
            assertEquals( SEC_ERR_34,ex.getErrorId());
            assertEquals(PasswordValidator.DEFAULT_NAME,ex.getArgs()[0]);
            assertEquals(XMLUserGroupService.DEFAULT_NAME,ex.getArgs()[1]);
            LOGGER.info(ex.getMessage());
            fail=true;
        }
        assertTrue(fail);
        
        
        fail=false;
        try {
            config.setName(PasswordValidator.MASTERPASSWORD_NAME);
            validator.validateRemovePasswordPolicyService(config);                         
        } catch (SecurityConfigException ex) {
            assertEquals( SEC_ERR_42,ex.getErrorId());
            assertEquals(0,ex.getArgs().length);
            LOGGER.info(ex.getMessage());
            fail=true;
        }
        assertTrue(fail);
        
        
    }

    public void testRoleConfig() {
        
        SecurityRoleServiceConfig config = getRoleConfig(XMLRoleService.DEFAULT_NAME, GeoserverRoleService.class, 
                GeoserverRole.ADMIN_ROLE.getAuthority());
        boolean fail;

        
        fail=false;
        try {
            config.setName("default2");
            config.setAdminRoleName("adminrole");
            validator.validateAddRoleService(config);                         
        } catch (SecurityConfigException ex) {
            assertEquals( SEC_ERR_50,ex.getErrorId());
            assertEquals(GeoserverRole.ADMIN_ROLE.getAuthority(),ex.getArgs()[0]);
            LOGGER.info(ex.getMessage());
            fail=true;
        }
        assertTrue(fail);

        fail=false;
        try {
            config.setName(XMLRoleService.DEFAULT_NAME);
            config.setAdminRoleName("adminrole");
            validator.validateModifiedRoleService(config);                         
        } catch (SecurityConfigException ex) {
            assertEquals( SEC_ERR_50,ex.getErrorId());
            assertEquals(GeoserverRole.ADMIN_ROLE.getAuthority(),ex.getArgs()[0]);
            LOGGER.info(ex.getMessage());
            fail=true;
        }
        assertTrue(fail);

        config.setAdminRoleName(GeoserverRole.ADMIN_ROLE.getAuthority());
        
        fail=false;
        try {
            config.setName(null);
            validator.validateRemoveRoleService(config);                         
        } catch (SecurityConfigException ex) {
            assertEquals( SEC_ERR_22,ex.getErrorId());
            assertEquals(GeoserverRoleService.class,ex.getArgs()[0]);
            assertNull(ex.getArgs()[1]);
            LOGGER.info(ex.getMessage());
            fail=true;
        }
        assertTrue(fail);
        
        
        fail=false;
        try {
            config.setName(XMLRoleService.DEFAULT_NAME);
            validator.validateRemoveRoleService(config);                         
        } catch (SecurityConfigException ex) {
            assertEquals( SEC_ERR_30,ex.getErrorId());
            assertEquals(XMLRoleService.DEFAULT_NAME,ex.getArgs()[0]);
            LOGGER.info(ex.getMessage());
            fail=true;
        }
        assertTrue(fail);
        

        
    }

    
    public void testAuthenticationProvider() {
        
        SecurityAuthProviderConfig config = getAuthConfig(GeoServerAuthenticationProvider.DEFAULT_NAME, 
                UsernamePasswordAuthenticationProvider.class, "default2");
        boolean fail;
        
        fail=false;
        try {
            config.setName("default2");            
            validator.validateAddAuthProvider(config);                         
        } catch (SecurityConfigException ex) {
            assertEquals( SEC_ERR_24,ex.getErrorId());
            assertEquals(GeoserverUserGroupService.class,ex.getArgs()[0]);
            assertEquals("default2",ex.getArgs()[1]);
            LOGGER.info(ex.getMessage());
            fail=true;
        }
        assertTrue(fail);
        
        fail=false;
        try {
            config.setName(GeoServerAuthenticationProvider.DEFAULT_NAME);            
            validator.validateModifiedAuthProvider(config);                         
        } catch (SecurityConfigException ex) {
            assertEquals( SEC_ERR_24,ex.getErrorId());
            assertEquals(GeoserverUserGroupService.class,ex.getArgs()[0]);
            assertEquals("default2",ex.getArgs()[1]);
            LOGGER.info(ex.getMessage());
            fail=true;
        }
        assertTrue(fail);
                        
        fail=false;
        try {
            config.setName("");
            validator.validateRemoveAuthProvider(config);                         
        } catch (SecurityConfigException ex) {
            assertEquals( SEC_ERR_22,ex.getErrorId());
            assertEquals(GeoServerAuthenticationProvider.class,ex.getArgs()[0]);
            assertEquals("",ex.getArgs()[1]);
            LOGGER.info(ex.getMessage());
            fail=true;
        }
        assertTrue(fail);
        
        
        
        fail=false;
        try {
            config.setName(GeoServerAuthenticationProvider.DEFAULT_NAME);
            validator.validateRemoveAuthProvider(config);                         
        } catch (SecurityConfigException ex) {
            assertEquals( SEC_ERR_31,ex.getErrorId());
            assertEquals(GeoServerAuthenticationProvider.DEFAULT_NAME,ex.getArgs()[0]);
            LOGGER.info(ex.getMessage());
            fail=true;
        }
        assertTrue(fail);
        
    }


    public void testUserGroupConfig() {
        
        SecurityUserGroupServiceConfig config = getUGConfig(XMLUserGroupService.DEFAULT_NAME, MemoryUserGroupService.class, 
                GeoserverPlainTextPasswordEncoder.BeanName,PasswordValidator.DEFAULT_NAME);
        boolean fail;
        
        fail=false;
        try {
            config.setName("default2");
            config.setPasswordEncoderName("xxx");
            validator.validateAddUserGroupService(config);                         
        } catch (SecurityConfigException ex) {
            assertEquals( SEC_ERR_04,ex.getErrorId());
            assertEquals("xxx",ex.getArgs()[0]);
            LOGGER.info(ex.getMessage());
            fail=true;
        }
        assertTrue(fail);
        
        fail=false;
        try {
            config.setName(XMLUserGroupService.DEFAULT_NAME);
            config.setPasswordEncoderName("xxx");
            validator.validateModifiedUserGroupService(config);                         
        } catch (SecurityConfigException ex) {
            assertEquals( SEC_ERR_04,ex.getErrorId());
            assertEquals("xxx",ex.getArgs()[0]);
            LOGGER.info(ex.getMessage());
            fail=true;
        }
        assertTrue(fail);
        
        fail=false;
        try {
            config.setName("default2");
            config.setPasswordEncoderName("");
            validator.validateAddUserGroupService(config);                         
        } catch (SecurityConfigException ex) {
            assertEquals( SEC_ERR_32,ex.getErrorId());
            assertEquals("default2",ex.getArgs()[0]);
            LOGGER.info(ex.getMessage());
            fail=true;
        }
        assertTrue(fail);
        
        fail=false;
        try {
            config.setName(XMLUserGroupService.DEFAULT_NAME);
            config.setPasswordEncoderName(null);
            validator.validateModifiedUserGroupService(config);                         
        } catch (SecurityConfigException ex) {
            assertEquals( SEC_ERR_32,ex.getErrorId());
            assertEquals(XMLUserGroupService.DEFAULT_NAME,ex.getArgs()[0]);
            LOGGER.info(ex.getMessage());
            fail=true;
        }
        assertTrue(fail);

        

        config.setPasswordEncoderName(GeoserverPlainTextPasswordEncoder.BeanName);
        
        fail=false;
        try {
            config.setName("default2");
            config.setPasswordPolicyName("default2");
            validator.validateAddUserGroupService(config);                         
        } catch (SecurityConfigException ex) {
            assertEquals( SEC_ERR_24,ex.getErrorId());
            assertEquals(PasswordValidator.class,ex.getArgs()[0]);
            assertEquals("default2",ex.getArgs()[1]);
            LOGGER.info(ex.getMessage());
            fail=true;
        }
        assertTrue(fail);
        
        fail=false;
        try {
            config.setName(XMLUserGroupService.DEFAULT_NAME);
            config.setPasswordPolicyName("default2");
            validator.validateModifiedUserGroupService(config);                         
        } catch (SecurityConfigException ex) {
            assertEquals( SEC_ERR_24,ex.getErrorId());
            assertEquals(PasswordValidator.class,ex.getArgs()[0]);
            assertEquals("default2",ex.getArgs()[1]);
            LOGGER.info(ex.getMessage());
            fail=true;
        }
        assertTrue(fail);
        
        fail=false;
        try {
            config.setName("default2");
            config.setPasswordPolicyName("");
            validator.validateAddUserGroupService(config);                         
        } catch (SecurityConfigException ex) {
            assertEquals( SEC_ERR_33,ex.getErrorId());
            assertEquals("default2",ex.getArgs()[0]);
            LOGGER.info(ex.getMessage());
            fail=true;
        }
        assertTrue(fail);
        
        fail=false;
        try {
            config.setName(XMLUserGroupService.DEFAULT_NAME);
            config.setPasswordPolicyName(null);
            validator.validateModifiedUserGroupService(config);                         
        } catch (SecurityConfigException ex) {
            assertEquals( SEC_ERR_33,ex.getErrorId());
            assertEquals(XMLUserGroupService.DEFAULT_NAME,ex.getArgs()[0]);
            LOGGER.info(ex.getMessage());
            fail=true;
        }
        assertTrue(fail);


                
        fail=false;
        try {
            config.setName(null);
            validator.validateRemoveUserGroupService(config);                         
        } catch (SecurityConfigException ex) {
            assertEquals( SEC_ERR_22,ex.getErrorId());
            assertEquals(GeoserverUserGroupService.class,ex.getArgs()[0]);
            assertNull(ex.getArgs()[1]);
            LOGGER.info(ex.getMessage());
            fail=true;
        }
        assertTrue(fail);
        
        
        fail=false;
        try {
            config.setName(XMLUserGroupService.DEFAULT_NAME);
            validator.validateRemoveUserGroupService(config);                         
        } catch (SecurityConfigException ex) {
            assertEquals( SEC_ERR_35,ex.getErrorId());
            assertEquals(XMLUserGroupService.DEFAULT_NAME,ex.getArgs()[0]);
            assertEquals(GeoServerAuthenticationProvider.DEFAULT_NAME,ex.getArgs()[1]);
            LOGGER.info(ex.getMessage());
            fail=true;
        }
        assertTrue(fail);
        
    }

}
