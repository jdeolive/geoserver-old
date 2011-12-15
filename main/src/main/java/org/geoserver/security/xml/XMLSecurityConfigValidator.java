/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.geoserver.security.xml;

import java.io.File;
import java.io.IOException;

import org.geoserver.security.GeoserverUserGroupService;
import org.geoserver.security.config.SecurityRoleServiceConfig;
import org.geoserver.security.config.SecurityUserGroupServiceConfig;
import org.geoserver.security.config.impl.XMLFileBasedRoleServiceConfigImpl;
import org.geoserver.security.config.impl.XMLFileBasedUserGroupServiceConfigImpl;
import org.geoserver.security.config.validation.SecurityConfigException;
import org.geoserver.security.config.validation.SecurityConfigValidationErrors;
import org.geoserver.security.config.validation.SecurityConfigValidator;


/**
 * Validator for the XML implementation
 * 
 * @author christian
 *
 */
public class XMLSecurityConfigValidator extends SecurityConfigValidator {

    @Override
    public void validate(SecurityRoleServiceConfig config) throws SecurityConfigException {
        super.validate(config);
        XMLFileBasedRoleServiceConfigImpl xmlConfig = (XMLFileBasedRoleServiceConfigImpl) config;
        validateCheckIntervall(xmlConfig.getCheckInterval());
        validateFileName(xmlConfig.getFileName());
        
    }
    
    @Override
    public void validate(SecurityUserGroupServiceConfig config)
            throws SecurityConfigException {
        super.validate(config);
        XMLFileBasedUserGroupServiceConfigImpl xmlConfig = (XMLFileBasedUserGroupServiceConfigImpl) config;
        validateCheckIntervall(xmlConfig.getCheckInterval());
        validateFileName(xmlConfig.getFileName());
        
    }
    
    protected void validateFileName(String fileName) throws SecurityConfigException {
        if (fileName==null || fileName.length()==0)
            throw createSecurityException(XMLSecurityConfigValidationErrors.SEC_ERR_104);
    }
    
    protected void validateCheckIntervall(long msecs) throws SecurityConfigException {
        if (msecs !=0 && msecs < 1000)
            throw createSecurityException(XMLSecurityConfigValidationErrors.SEC_ERR_100);
    }
    

    @Override
    protected SecurityConfigValidationErrors getSecurityErrors() {
        return new XMLSecurityConfigValidationErrors();
    }
    
    /**
     * Additional Validation. Removing this configuration may also remove the file
     * where the roles are contained. (the file may be stored within the configuration
     * sub directory). The design insists on an empty role file.  
     * 
     */
    @Override
    public void validateRemoveRoleService(SecurityRoleServiceConfig config)
            throws SecurityConfigException {
        super.validateRemoveRoleService(config);
        
        XMLFileBasedRoleServiceConfigImpl xmlConfig = (XMLFileBasedRoleServiceConfigImpl) config;
        File file = new File(xmlConfig.getFileName());                
        // check if if file name is absolute and not in standard role directory
        try {
            
            if (file.isAbsolute() && 
                file.getCanonicalPath().startsWith(
                        new File(manager.getRoleRoot(),config.getName()).getCanonicalPath()+File.separator)==false)
                return;
            // file in security sub dir, check if roles exists
            if (manager.loadRoleService(config.getName()).getRoles().size()>0) {
                throw createSecurityException(XMLSecurityConfigValidationErrors.SEC_ERR_102, config.getName());
            }
            
        } catch (IOException e) {
            throw new RuntimeException();
        }
        
    }

    /**
     * Additional Validation. Removing this configuration may also remove the file
     * where the users and groups are contained. (the file may be stored within the configuration
     * sub directory). The design insists on an empty user/group file.  
     * 
     */

    @Override
    public void validateRemoveUserGroupService(SecurityUserGroupServiceConfig config)
            throws SecurityConfigException {
        XMLFileBasedUserGroupServiceConfigImpl xmlConfig = (XMLFileBasedUserGroupServiceConfigImpl) config;
        File file = new File(xmlConfig.getFileName());                
        // check if if file name is absolute and not in standard role directory
        try {
            
            if (file.isAbsolute() && 
                file.getCanonicalPath().startsWith(
                        new File(manager.getUserGroupRoot(),config.getName()).getCanonicalPath()+File.separator)==false)
                return;
            // file in security sub dir, check if roles exists
            GeoserverUserGroupService service = manager.loadUserGroupService(config.getName()); 
            if (service.getUserGroups().size()>0 || service.getUsers().size()>0) {
                throw createSecurityException(XMLSecurityConfigValidationErrors.SEC_ERR_103, config.getName());
            }
            
        } catch (IOException e) {
            throw new RuntimeException();
        }        
        super.validateRemoveUserGroupService(config);
    }

    /** 
     * Additional validation, check if the file exists or can be created 
     */
    @Override
    public void validateAddRoleService(SecurityRoleServiceConfig config)
            throws SecurityConfigException {
        super.validateAddRoleService(config);
        XMLFileBasedRoleServiceConfigImpl xmlConfig = (XMLFileBasedRoleServiceConfigImpl) config;
        File file  = new File(xmlConfig.getFileName());
        checkFile(file);        
    }
 
    /** 
     * Additional validation, check if the file exists or can be created 
     */

    @Override
    public void validateAddUserGroupService(SecurityUserGroupServiceConfig config)
            throws SecurityConfigException {
        super.validateAddUserGroupService(config);
        XMLFileBasedUserGroupServiceConfigImpl xmlConfig = 
                (XMLFileBasedUserGroupServiceConfigImpl) config;
        File file  = new File(xmlConfig.getFileName());
        checkFile(file);        
    }

    protected void checkFile(File file) throws SecurityConfigException {
        if (file.isAbsolute()==false) return;
        try {
            if (file.exists()==false) {
                file.createNewFile();
                file.delete();
            }
        } catch (IOException ex) {
            try {
                throw createSecurityException(XMLSecurityConfigValidationErrors.SEC_ERR_101,
                        file.getCanonicalPath());
            } catch (IOException ex2) {
                throw new RuntimeException(ex2);
            }
        }                
    }
}
