/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.geoserver.security.jdbc;

import java.text.MessageFormat;

import org.geoserver.security.validation.SecurityConfigValidationErrors;

public class JdbcSecurityConfigValidationErrors extends SecurityConfigValidationErrors {

    public final static String SEC_ERR_200 = "SEC_ERR_200";
    public final static String SEC_ERR_201 = "SEC_ERR_201";
    public final static String SEC_ERR_202 = "SEC_ERR_202";
    public final static String SEC_ERR_203 = "SEC_ERR_203";
    public final static String SEC_ERR_210 = "SEC_ERR_210";
    
    @Override
    public String formatErrorMsg(String id, Object... args) {
        
        if (SEC_ERR_200.equals(id))
            return MessageFormat.format("Driver name is mandatory",args);
        if (SEC_ERR_201.equals(id))
            return MessageFormat.format("Username is mandatory",args);
        if (SEC_ERR_202.equals(id))
            return MessageFormat.format("Jdbc connect url is mandatory",args);        
        if (SEC_ERR_203.equals(id))
            return MessageFormat.format("Driver named {0} is not in class path",args);        
        if (SEC_ERR_210.equals(id))
            return MessageFormat.format("JNDI name is mandatory",args);


        return super.formatErrorMsg(id, args);
    }
    
    
}
