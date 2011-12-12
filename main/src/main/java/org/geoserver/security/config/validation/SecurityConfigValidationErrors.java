/* Copyright (c) 2001 - 2008 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */


package org.geoserver.security.config.validation;

import java.text.MessageFormat;

/**
 *  
 * Validation error ids Constants
 * 
 * @author christian
 *
 */
public class SecurityConfigValidationErrors  {

    public final static String SEC_ERR_01 = "SEC_ERR_01";
    public final static String SEC_ERR_02 = "SEC_ERR_02";
    public final static String SEC_ERR_03 = "SEC_ERR_03";
    public final static String SEC_ERR_20 = "SEC_ERR_20";
    public final static String SEC_ERR_21 = "SEC_ERR_21";
    public final static String SEC_ERR_22 = "SEC_ERR_22";
    public final static String SEC_ERR_23 = "SEC_ERR_23";
    public final static String SEC_ERR_24 = "SEC_ERR_24";
    
    public final static String SEC_ERR_30 = "SEC_ERR_30";
    
        
    public static String formatErrorMsg(String id,Object ... args) {
    
    if (SEC_ERR_01==id)
        return MessageFormat.format("Bean {0} is not a valid configuration password encrypter",args);
    if (SEC_ERR_02==id)
        return MessageFormat.format("No role servcie named {0} ",args);
    if (SEC_ERR_03==id)
        return MessageFormat.format("No authentication provider named {0} ",args);
    if (SEC_ERR_20==id)
        return MessageFormat.format("Class not found {0} ",args);
    if (SEC_ERR_21==id)
        return MessageFormat.format("Class {1} ist not of type {0} ",args);
    if (SEC_ERR_22==id)
        return MessageFormat.format("Service name is required ",args);
    if (SEC_ERR_23==id)
        return MessageFormat.format("Service name {1} alreday exists",args);
    if (SEC_ERR_24==id)
        return MessageFormat.format("Service name {1} does not exist",args);
    
    if (SEC_ERR_24==id)
        return MessageFormat.format("Role service {0} is active and cannot be deleted",args);


            
    return "Unknown error id "+id;
    }
}
