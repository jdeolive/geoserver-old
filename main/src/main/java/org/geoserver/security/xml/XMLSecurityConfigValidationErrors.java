/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.geoserver.security.xml;

import java.text.MessageFormat;

import org.geoserver.security.validation.SecurityConfigValidationErrors;


public class XMLSecurityConfigValidationErrors extends SecurityConfigValidationErrors {

    public final static String SEC_ERR_100 = "SEC_ERR_100";
    public final static String SEC_ERR_101 = "SEC_ERR_101";
    public final static String SEC_ERR_102 = "SEC_ERR_102";
    public final static String SEC_ERR_103 = "SEC_ERR_103";
    public final static String SEC_ERR_104 = "SEC_ERR_104";
    public final static String SEC_ERR_105 = "SEC_ERR_105";
    public final static String SEC_ERR_106 = "SEC_ERR_106";
    
    @Override
    public String formatErrorMsg(String id, Object... args) {
        
        if (SEC_ERR_100.equals(id))
            return MessageFormat.format("Check interval in milliseconds can be 0 (disabled) or >= 1000",args);
        if (SEC_ERR_101.equals(id))
            return MessageFormat.format("Cannot create file {0}",args);
        if (SEC_ERR_102.equals(id))
            return MessageFormat.format("Role service {0} must be empty",args);        
        if (SEC_ERR_103.equals(id))
            return MessageFormat.format("User/group service {0} must be empty",args);
        if (SEC_ERR_104.equals(id))
            return MessageFormat.format("File name required",args);        
        if (SEC_ERR_105.equals(id))
            return MessageFormat.format("Cannot change file name from {0} to {1}",args);
        if (SEC_ERR_106.equals(id))
            return MessageFormat.format("User/group service is required",args);        

        


        return super.formatErrorMsg(id, args);
    }
}
