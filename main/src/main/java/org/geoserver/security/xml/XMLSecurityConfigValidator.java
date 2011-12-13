/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.geoserver.security.xml;

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
        validateFileName(XMLConstants.FILE_RR, xmlConfig.getFileName());
    }
    
    @Override
    public void validate(SecurityUserGroupServiceConfig config)
            throws SecurityConfigException {
        super.validate(config);
        XMLFileBasedUserGroupServiceConfigImpl xmlConfig = (XMLFileBasedUserGroupServiceConfigImpl) config;
        validateCheckIntervall(xmlConfig.getCheckInterval());
        validateFileName(XMLConstants.FILE_UR, xmlConfig.getFileName());
    }
    
    protected void validateCheckIntervall(long msecs) throws SecurityConfigException {
        if (msecs !=0 && msecs < 1000)
            throw createSecurityException(XMLSecurityConfigValidationErrors.SEC_ERR_100);
    }
    
    protected void validateFileName(String expected, String actual) throws SecurityConfigException {
        if (expected.equals(actual)==false)
            throw createSecurityException(XMLSecurityConfigValidationErrors.SEC_ERR_101,expected);
    }

    @Override
    protected SecurityConfigValidationErrors getSecurityErrors() {
        return new XMLSecurityConfigValidationErrors();
    }

}
