package org.geoserver.security.config.validation;

import static org.geoserver.security.config.validation.SecurityConfigValidationErrors.SEC_ERR_01;
import static org.geoserver.security.config.validation.SecurityConfigValidationErrors.SEC_ERR_02;
import static org.geoserver.security.config.validation.SecurityConfigValidationErrors.SEC_ERR_03;

import java.util.logging.Logger;

import org.geoserver.security.config.SecurityManagerConfig;
import org.geoserver.security.config.impl.SecurityManagerConfigImpl;
import org.geoserver.security.password.GeoserverConfigPBEPasswordEncoder;
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
        config.setRoleServiceName("default");
        config.setConfigPasswordEncrypterName(GeoserverConfigPBEPasswordEncoder.BeanName);
        config.getAuthProviderNames().add("default");
        
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


        config.setRoleServiceName("default");
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
    
        
}
