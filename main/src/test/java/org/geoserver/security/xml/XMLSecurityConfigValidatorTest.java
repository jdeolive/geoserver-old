package org.geoserver.security.xml;

import static org.geoserver.security.xml.XMLSecurityConfigValidationErrors.*;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import junit.framework.Assert;

import org.geoserver.security.GeoserverRoleStore;
import org.geoserver.security.GeoserverUserGroupStore;
import org.geoserver.security.config.SecurityRoleServiceConfig;
import org.geoserver.security.config.SecurityUserGroupServiceConfig;
import org.geoserver.security.config.impl.XMLFileBasedRoleServiceConfigImpl;
import org.geoserver.security.config.impl.XMLFileBasedUserGroupServiceConfigImpl;
import org.geoserver.security.impl.GeoserverRole;
import org.geoserver.security.impl.GeoserverUserGroup;
import org.geoserver.security.password.GeoserverPlainTextPasswordEncoder;
import org.geoserver.security.password.PasswordValidator;
import org.geoserver.security.validation.SecurityConfigException;
import org.geoserver.security.validation.SecurityConfigValidatorTest;
import org.geotools.util.logging.Logging;

public class XMLSecurityConfigValidatorTest extends SecurityConfigValidatorTest {

    
    static protected Logger LOGGER = Logging.getLogger("org.geoserver.security");
    
    
    @Override
    protected void setUpInternal() throws Exception {
        super.setUpInternal();
    }
        
    protected SecurityUserGroupServiceConfig getUGConfig(String name, Class<?> aClass,
            String encoder, String policyName, String fileName) {
        XMLFileBasedUserGroupServiceConfigImpl config = new XMLFileBasedUserGroupServiceConfigImpl();
        config.setName(name);
        config.setClassName(aClass.getName());
        config.setPasswordEncoderName(encoder);
        config.setPasswordPolicyName(policyName);
        config.setCheckInterval(0);
        config.setFileName(fileName);
        return config;
    }
    
    protected SecurityRoleServiceConfig getRoleConfig(String name, Class<?> aClass,String adminRole,String fileName) {
        XMLFileBasedRoleServiceConfigImpl config = new XMLFileBasedRoleServiceConfigImpl();
        config.setName(name);
        config.setClassName(aClass.getName());
        config.setAdminRoleName(adminRole);
        config.setCheckInterval(0);
        config.setFileName(fileName);
        return config;
    }

    public void testRoleConfig() throws IOException{
        
        super.testRoleConfig();
        
        XMLFileBasedRoleServiceConfigImpl  config = 
                (XMLFileBasedRoleServiceConfigImpl )getRoleConfig(XMLRoleService.DEFAULT_NAME, XMLRoleService.class, 
                GeoserverRole.ADMIN_ROLE.getAuthority(),XMLConstants.FILE_RR);
        boolean fail;

        
        fail=false;
        try {
            config.setName("default2");
            config.setCheckInterval(-1l);
            getSecurityManager().saveRoleService(config, true);                                     
        } catch (SecurityConfigException ex) {
            assertEquals( SEC_ERR_100,ex.getErrorId());
            assertEquals(0,ex.getArgs().length);
            LOGGER.info(ex.getMessage());
            fail=true;
        }
        assertTrue(fail);
        

                
        fail=false;
        try {
            config.setName(XMLRoleService.DEFAULT_NAME);
            config.setCheckInterval(999l);
            getSecurityManager().saveRoleService(config, false);                         
        } catch (SecurityConfigException ex) {
            assertEquals( SEC_ERR_100,ex.getErrorId());
            assertEquals(0,ex.getArgs().length);
            LOGGER.info(ex.getMessage());
            fail=true;
        }
        assertTrue(fail);

        config.setCheckInterval(0);
        
        XMLFileBasedRoleServiceConfigImpl xmlConfig = (XMLFileBasedRoleServiceConfigImpl) 
                getRoleConfig("test1",XMLRoleService.class,GeoserverRole.ADMIN_ROLE.getAuthority(),"test1.xml");
        
        try {
            getSecurityManager().saveRoleService(xmlConfig, true);
            getSecurityManager().removeRoleService(xmlConfig);
        } catch (SecurityConfigException ex) {
            Assert.fail("Should work");
        }
        
        fail=false;
        xmlConfig = (XMLFileBasedRoleServiceConfigImpl) 
                getRoleConfig("test2",XMLRoleService.class,GeoserverRole.ADMIN_ROLE.getAuthority(),"test2.xml");
        try {
            getSecurityManager().saveRoleService(xmlConfig, true);
            GeoserverRoleStore store = getSecurityManager().loadRoleService("test2").createStore();
            store.addRole(GeoserverRole.ADMIN_ROLE);
            store.store();
            getSecurityManager().removeRoleService(xmlConfig);
        } catch (SecurityConfigException ex) {
            assertEquals(SEC_ERR_102, ex.getErrorId());
            assertEquals("test2", ex.getArgs()[0]);
            fail=true;
        }
        assertTrue(fail);

        xmlConfig = (XMLFileBasedRoleServiceConfigImpl) 
                getRoleConfig("test3",XMLRoleService.class,GeoserverRole.ADMIN_ROLE.getAuthority(),                        
                        new File(getSecurityManager().getRoleRoot(),"test3.xml").getAbsolutePath());
        try {
            getSecurityManager().saveRoleService(xmlConfig, true);
            GeoserverRoleStore store = getSecurityManager().loadRoleService("test3").createStore();
            store.addRole(GeoserverRole.ADMIN_ROLE);
            store.store();
            getSecurityManager().removeRoleService(xmlConfig);
        } catch (SecurityConfigException ex) {
            Assert.fail("Should work");
        }

        // run only if a temp dir is availbale
        if (new XMLSecurityConfigValidator().getTempDir()!=null) {
            String invalidPath="abc"+File.separator+"def.xml";
            xmlConfig = (XMLFileBasedRoleServiceConfigImpl) 
                    getRoleConfig("test4",XMLRoleService.class,GeoserverRole.ADMIN_ROLE.getAuthority(),                        
                            invalidPath);
            
            fail=false;
            try {
                getSecurityManager().saveRoleService(xmlConfig, true);
            } catch (SecurityConfigException ex) {
                assertEquals(SEC_ERR_101, ex.getErrorId());
                assertEquals(invalidPath, ex.getArgs()[0]);
                fail=true;
            }
            assertTrue(fail);
        }
        /////////////// test modify
        xmlConfig = (XMLFileBasedRoleServiceConfigImpl)
                getRoleConfig("test4",XMLRoleService.class,GeoserverRole.ADMIN_ROLE.getAuthority(),                        
                        "testModify.xml");

        try {
            getSecurityManager().saveRoleService(xmlConfig, true);
            xmlConfig.setValidating(true);
            getSecurityManager().saveRoleService(xmlConfig, false);
        } catch (SecurityConfigException ex) {
            Assert.fail("Should work");
        }

        fail=false;
        try {
            xmlConfig.setFileName("xyz.xml");
            getSecurityManager().saveRoleService(xmlConfig, false);
        } catch (SecurityConfigException ex) {
            assertEquals(SEC_ERR_105, ex.getErrorId());
            assertEquals("testModify.xml", ex.getArgs()[0]);
            assertEquals("xyz.xml", ex.getArgs()[1]);
            fail=true;
        }
        assertTrue(fail);

                
    }

    
    public void testUserGroupConfig() throws IOException{

        super.testUserGroupConfig();
        XMLFileBasedUserGroupServiceConfigImpl config = (XMLFileBasedUserGroupServiceConfigImpl) 
                getUGConfig(XMLUserGroupService.DEFAULT_NAME, XMLUserGroupService.class, 
                GeoserverPlainTextPasswordEncoder.BeanName,PasswordValidator.DEFAULT_NAME,XMLConstants.FILE_UR);
        boolean fail;
        
        fail=false;
        try {
            config.setName("default2");
            config.setCheckInterval(-1l);
            getSecurityManager().saveUserGroupService(config, true);                         
        } catch (SecurityConfigException ex) {
            assertEquals( SEC_ERR_100,ex.getErrorId());
            assertEquals(0,ex.getArgs().length);
            LOGGER.info(ex.getMessage());
            fail=true;
        }
        assertTrue(fail);

        fail=false;
        try {
            config.setName(XMLUserGroupService.DEFAULT_NAME);
            config.setCheckInterval(999l);
            getSecurityManager().saveUserGroupService(config, false);                         
        } catch (SecurityConfigException ex) {
            assertEquals( SEC_ERR_100,ex.getErrorId());
            assertEquals(0,ex.getArgs().length);
            LOGGER.info(ex.getMessage());
            fail=true;
        }
        assertTrue(fail);

        config.setCheckInterval(0);

        XMLFileBasedUserGroupServiceConfigImpl xmlConfig = (XMLFileBasedUserGroupServiceConfigImpl) 
                getUGConfig("test1", XMLUserGroupService.class, 
                GeoserverPlainTextPasswordEncoder.BeanName,PasswordValidator.DEFAULT_NAME,"test1.xml");

        GeoserverUserGroup group=new GeoserverUserGroup("testgroup");
        
        try {
            getSecurityManager().saveUserGroupService(xmlConfig, true);
            getSecurityManager().removeUserGroupService(xmlConfig);
        } catch (SecurityConfigException ex) {
            Assert.fail("Should work");
        }
        
        
        
        fail=false;
        xmlConfig = (XMLFileBasedUserGroupServiceConfigImpl) 
                getUGConfig("test2", XMLUserGroupService.class, 
                GeoserverPlainTextPasswordEncoder.BeanName,PasswordValidator.DEFAULT_NAME,"test2.xml");
        try {
            getSecurityManager().saveUserGroupService(xmlConfig, true);
            GeoserverUserGroupStore store = getSecurityManager().loadUserGroupService("test2").createStore();
            store.addGroup(group);
            store.store();
            getSecurityManager().removeUserGroupService(xmlConfig);
        } catch (SecurityConfigException ex) {
            assertEquals(SEC_ERR_103, ex.getErrorId());
            assertEquals("test2", ex.getArgs()[0]);
            fail=true;
        }
        assertTrue(fail);

        xmlConfig = (XMLFileBasedUserGroupServiceConfigImpl) 
                getUGConfig("test3", XMLUserGroupService.class, 
                GeoserverPlainTextPasswordEncoder.BeanName,PasswordValidator.DEFAULT_NAME,
                new File(getSecurityManager().getUserGroupRoot(),"test3.xml").getAbsolutePath());

        try {
            getSecurityManager().saveUserGroupService(xmlConfig, true);
            GeoserverUserGroupStore store = getSecurityManager().loadUserGroupService("test3").createStore();
            store.addGroup(group);
            store.store();
            getSecurityManager().removeUserGroupService(xmlConfig);
        } catch (SecurityConfigException ex) {
            Assert.fail("Should work");
        }

        // run only if a temp dir is availbale
        if (new XMLSecurityConfigValidator().getTempDir()!=null) {
            String invalidPath="abc"+File.separator+"def.xml";
            xmlConfig = (XMLFileBasedUserGroupServiceConfigImpl) 
                    getUGConfig("test4", XMLUserGroupService.class, 
                    GeoserverPlainTextPasswordEncoder.BeanName,PasswordValidator.DEFAULT_NAME,
                    invalidPath);
            
            fail=false;
            try {
                getSecurityManager().saveUserGroupService(xmlConfig, true);
            } catch (SecurityConfigException ex) {
                assertEquals(SEC_ERR_101, ex.getErrorId());
                assertEquals(invalidPath, ex.getArgs()[0]);
                fail=true;
            }
            assertTrue(fail);
        }
        
        
        xmlConfig = (XMLFileBasedUserGroupServiceConfigImpl) 
                getUGConfig("test5", XMLUserGroupService.class, 
                GeoserverPlainTextPasswordEncoder.BeanName,PasswordValidator.DEFAULT_NAME,
                "abc.xml");
        try {
            getSecurityManager().saveUserGroupService(xmlConfig, true);
        } catch (SecurityConfigException ex) {
            Assert.fail("Should work");
        }
        

        fail=false;
        try {
            xmlConfig.setFileName("");
            getSecurityManager().saveUserGroupService(xmlConfig, false);
        } catch (SecurityConfigException ex) {
            assertEquals(SEC_ERR_104, ex.getErrorId());
            assertEquals(0, ex.getArgs().length);
            fail=true;
        }
        assertTrue(fail);

        /////////////// test modify
        xmlConfig = (XMLFileBasedUserGroupServiceConfigImpl) 
                getUGConfig("testModify", XMLUserGroupService.class, 
                GeoserverPlainTextPasswordEncoder.BeanName,PasswordValidator.DEFAULT_NAME,"testModify.xml");
        try {
            getSecurityManager().saveUserGroupService(xmlConfig, true);
            xmlConfig.setValidating(true);
            getSecurityManager().saveUserGroupService(xmlConfig, false);
        } catch (SecurityConfigException ex) {
            Assert.fail("Should work");
        }

        fail=false;
        try {
            xmlConfig.setFileName("xyz.xml");
            getSecurityManager().saveUserGroupService(xmlConfig, false);
        } catch (SecurityConfigException ex) {
            assertEquals(SEC_ERR_105, ex.getErrorId());
            assertEquals("testModify.xml", ex.getArgs()[0]);
            assertEquals("xyz.xml", ex.getArgs()[1]);
            fail=true;
        }
        assertTrue(fail);
    }


}
