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
    public final static String SEC_ERR_04 = "SEC_ERR_04";
    public final static String SEC_ERR_05 = "SEC_ERR_05";
    public final static String SEC_ERR_06 = "SEC_ERR_06";
    public final static String SEC_ERR_20 = "SEC_ERR_20";
    public final static String SEC_ERR_21 = "SEC_ERR_21";
    public final static String SEC_ERR_22 = "SEC_ERR_22";
    public final static String SEC_ERR_23 = "SEC_ERR_23";    
    public final static String SEC_ERR_24 = "SEC_ERR_24";
    public final static String SEC_ERR_30 = "SEC_ERR_30";    
    public final static String SEC_ERR_31 = "SEC_ERR_31";
    public final static String SEC_ERR_32 = "SEC_ERR_32";
    public final static String SEC_ERR_33 = "SEC_ERR_33";
    public final static String SEC_ERR_34 = "SEC_ERR_34";
    public final static String SEC_ERR_35 = "SEC_ERR_35";
    public final static String SEC_ERR_40 = "SEC_ERR_40";
    public final static String SEC_ERR_41 = "SEC_ERR_41";
    public final static String SEC_ERR_42 = "SEC_ERR_42";
    public final static String SEC_ERR_50 = "SEC_ERR_50";
    
    
        
    public  String formatErrorMsg(String id,Object ... args) {
    
    if (SEC_ERR_01==id)
        return MessageFormat.format("Bean {0} is not a valid configuration password encrypter",args);
    if (SEC_ERR_02==id)
        return MessageFormat.format("No role service named {0} ",args);
    if (SEC_ERR_03==id)
        return MessageFormat.format("No authentication provider named {0} ",args);
    if (SEC_ERR_04==id)
        return MessageFormat.format("Bean {0} is not a valid password password encoder",args);
    if (SEC_ERR_05==id)
        return MessageFormat.format("Install unrestricted security policy files before using a strong configuration " +
        		"password encrypter",args);
    if (SEC_ERR_06==id)
        return MessageFormat.format("Install unrestricted security policy files before using a strong user " +
                        "password encrypter",args);    
    if (SEC_ERR_20==id)
        return MessageFormat.format("Class not found {0} ",args);
    if (SEC_ERR_21==id)
        return MessageFormat.format("Class {1} ist not of type {0} ",args);
    if (SEC_ERR_22==id)
        return MessageFormat.format("Service name is required ",args);
    if (SEC_ERR_23==id)
        return MessageFormat.format("Service named {1} alreday exists",args);
    if (SEC_ERR_24==id)
        return MessageFormat.format("Service named {1} does not exist",args);    
    if (SEC_ERR_30==id)
        return MessageFormat.format("Role service {0} is active and cannot be deleted",args);
    if (SEC_ERR_31==id)
        return MessageFormat.format("Authentication provider {0} is active and cannot be deleted",args);
    if (SEC_ERR_32==id)
        return MessageFormat.format("No password encoder specified for user/group service {0}",args);
    if (SEC_ERR_33==id)
        return MessageFormat.format("No password policy specified for user/group service {0}",args);
    if (SEC_ERR_34==id)
        return MessageFormat.format("Password policy {0} is used by user/group service {1}",args);
    if (SEC_ERR_35==id)
        return MessageFormat.format("User/group service {0} is used by authentication provider {1}",args);    
    if (SEC_ERR_40==id)
        return MessageFormat.format("Minimum length of password must be >= 0",args);
    if (SEC_ERR_41==id)
        return MessageFormat.format("Maximum length of password must be greater or equal to the minimum length",args);
    if (SEC_ERR_42==id)
        return MessageFormat.format("Policy for the master password cannot be deleted",args);
    if (SEC_ERR_50==id)
        return MessageFormat.format("The aministrator role has to be: {0}",args);
            
    return "Unknown error id "+id;
    }
}
