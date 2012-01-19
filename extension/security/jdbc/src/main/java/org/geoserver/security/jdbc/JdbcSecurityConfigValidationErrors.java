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
    public final static String SEC_ERR_204 = "SEC_ERR_204";
    public final static String SEC_ERR_210 = "SEC_ERR_210";
    public final static String SEC_ERR_211 = "SEC_ERR_211";
    public final static String SEC_ERR_212 = "SEC_ERR_212";
    public final static String SEC_ERR_213 = "SEC_ERR_213";
    
    
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
        if (SEC_ERR_204.equals(id))
            return MessageFormat.format("Cannot create tables without a DDL property file",args);                
        if (SEC_ERR_210.equals(id))
            return MessageFormat.format("JNDI name is mandatory",args);
        if (SEC_ERR_211.equals(id))
            return MessageFormat.format("Cannot open DDL file {0}",args);
        if (SEC_ERR_212.equals(id))
            return MessageFormat.format("DML file is required",args);
        if (SEC_ERR_213.equals(id))
            return MessageFormat.format("Cannot open DML file {0}",args);


        return super.formatErrorMsg(id, args);
    }
    
    
}
