package org.geoserver.security.xml;

import static org.geoserver.security.xml.XMLSecurityConfigValidationErrors.SEC_ERR_100;

import java.io.IOException;
import java.util.logging.Logger;

import org.geoserver.security.config.SecurityRoleServiceConfig;
import org.geoserver.security.config.SecurityUserGroupServiceConfig;
import org.geoserver.security.config.impl.XMLFileBasedRoleServiceConfigImpl;
import org.geoserver.security.config.impl.XMLFileBasedUserGroupServiceConfigImpl;
import org.geoserver.security.config.validation.SecurityConfigException;
import org.geoserver.security.config.validation.SecurityConfigValidatorTest;
import org.geoserver.security.impl.GeoserverRole;
import org.geoserver.security.password.GeoserverPlainTextPasswordEncoder;
import org.geoserver.security.password.PasswordValidator;
import org.geotools.util.logging.Logging;

public class XMLSecurityConfigValidatorTest extends SecurityConfigValidatorTest {

    
    static protected Logger LOGGER = Logging.getLogger("org.geoserver.security");
    
    
    @Override
    protected void setUpInternal() throws Exception {
        super.setUpInternal();
    }
        
    protected SecurityUserGroupServiceConfig getUGConfig(String name, Class<?> aClass,
            String encoder, String policyName) {
        XMLFileBasedUserGroupServiceConfigImpl config = new XMLFileBasedUserGroupServiceConfigImpl();
        config.setName(name);
        config.setClassName(aClass.getName());
        config.setPasswordEncoderName(encoder);
        config.setPasswordPolicyName(policyName);
        config.setCheckInterval(0);
        config.setFileName(XMLConstants.FILE_UR);
        return config;
    }
    
    protected SecurityRoleServiceConfig getRoleConfig(String name, Class<?> aClass,String adminRole) {
        XMLFileBasedRoleServiceConfigImpl config = new XMLFileBasedRoleServiceConfigImpl();
        config.setName(name);
        config.setClassName(aClass.getName());
        config.setAdminRoleName(adminRole);
        config.setCheckInterval(0);
        config.setFileName(XMLConstants.FILE_RR);
        return config;
    }

    public void testRoleConfig() throws IOException{
        
        super.testRoleConfig();
        
        XMLFileBasedRoleServiceConfigImpl  config = 
                (XMLFileBasedRoleServiceConfigImpl )getRoleConfig(XMLRoleService.DEFAULT_NAME, XMLRoleService.class, 
                GeoserverRole.ADMIN_ROLE.getAuthority());
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
        
/*        
        fail=false;
        try {
            config.setName("default2");
            config.setFileName("a.xml");
            validator.validateAddRoleService(config);                         
        } catch (SecurityConfigException ex) {
            assertEquals( SEC_ERR_101,ex.getErrorId());
            assertEquals(XMLConstants.FILE_RR,ex.getArgs()[0]);
            LOGGER.info(ex.getMessage());
            fail=true;
        }
        assertTrue(fail);

        fail=false;
        try {
            config.setName(XMLRoleService.DEFAULT_NAME);
            config.setFileName("a.xml");
            validator.validateModifiedRoleService(config);                         
        } catch (SecurityConfigException ex) {
            assertEquals( SEC_ERR_101,ex.getErrorId());
            assertEquals(XMLConstants.FILE_RR,ex.getArgs()[0]);
            LOGGER.info(ex.getMessage());
            fail=true;
        }
        assertTrue(fail);
*/

        
    }

    
    public void testUserGroupConfig() throws IOException{

        super.testUserGroupConfig();
        XMLFileBasedUserGroupServiceConfigImpl config = (XMLFileBasedUserGroupServiceConfigImpl) 
                getUGConfig(XMLUserGroupService.DEFAULT_NAME, XMLUserGroupService.class, 
                GeoserverPlainTextPasswordEncoder.BeanName,PasswordValidator.DEFAULT_NAME);
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
        
  /*      
        fail=false;
        try {
            config.setName("default2");
            config.setFileName("a.xml");
            validator.validateAddUserGroupService(config);                         
        } catch (SecurityConfigException ex) {
            assertEquals( SEC_ERR_101,ex.getErrorId());
            assertEquals(XMLConstants.FILE_UR,ex.getArgs()[0]);
            LOGGER.info(ex.getMessage());
            fail=true;
        }
        assertTrue(fail);

        fail=false;
        try {
            config.setName(XMLUserGroupService.DEFAULT_NAME);
            config.setFileName("a.xml");
            validator.validateModifiedUserGroupService(config);                         
        } catch (SecurityConfigException ex) {
            assertEquals( SEC_ERR_101,ex.getErrorId());
            assertEquals(XMLConstants.FILE_UR,ex.getArgs()[0]);
            LOGGER.info(ex.getMessage());
            fail=true;
        }
        assertTrue(fail);
    */    
    }


}
