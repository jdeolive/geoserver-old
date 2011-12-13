/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.geoserver.security.xml;

import java.text.MessageFormat;

import org.geoserver.security.config.validation.SecurityConfigValidationErrors;


public class XMLSecurityConfigValidationErrors extends SecurityConfigValidationErrors {

    public final static String SEC_ERR_100 = "SEC_ERR_100";
    public final static String SEC_ERR_101 = "SEC_ERR_101";
    
    @Override
    public String formatErrorMsg(String id, Object... args) {
        
        if (SEC_ERR_100==id)
            return MessageFormat.format("Check interval in millisecs can be 0 (disabled) or >= 1000",args);
        if (SEC_ERR_101==id)
            return MessageFormat.format("File name must be {0}",args);        
        return super.formatErrorMsg(id, args);
    }
}
