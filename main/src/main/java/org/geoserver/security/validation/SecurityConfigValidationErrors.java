/* Copyright (c) 2001 - 2008 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */


package org.geoserver.security.validation;

import java.text.MessageFormat;

import org.geoserver.security.config.SecurityConfig;

/**
 *  
 * Validation errors for {@link SecurityConfig} objects
 * 
 * @author christian
 *
 */
public class SecurityConfigValidationErrors  extends AbstractSecurityValidationErrors{

    public final static String SEC_ERR_01 = "SEC_ERR_01";
    public final static String SEC_ERR_02 = "SEC_ERR_02";
    public final static String SEC_ERR_03 = "SEC_ERR_03";
    public final static String SEC_ERR_04 = "SEC_ERR_04";
    public final static String SEC_ERR_05 = "SEC_ERR_05";
    public final static String SEC_ERR_06 = "SEC_ERR_06";
    public final static String SEC_ERR_07 = "SEC_ERR_07";
    public final static String SEC_ERR_20 = "SEC_ERR_20";
    public final static String SEC_ERR_21 = "SEC_ERR_21";
    public final static String SEC_ERR_22 = "SEC_ERR_22";
    
    public final static String SEC_ERR_23a = "SEC_ERR_23a";
    public final static String SEC_ERR_23b = "SEC_ERR_23b";
    public final static String SEC_ERR_23c = "SEC_ERR_23c";
    public final static String SEC_ERR_23d = "SEC_ERR_23d";
    public final static String SEC_ERR_23e = "SEC_ERR_23e";
    
    public final static String SEC_ERR_24a = "SEC_ERR_24a";
    public final static String SEC_ERR_24b = "SEC_ERR_24b";
    public final static String SEC_ERR_24c = "SEC_ERR_24c";
    public final static String SEC_ERR_24d = "SEC_ERR_24d";
    public final static String SEC_ERR_24e = "SEC_ERR_24e";
    
    public final static String SEC_ERR_25 = "SEC_ERR_25";
    
    public final static String SEC_ERR_30 = "SEC_ERR_30";    
    public final static String SEC_ERR_31 = "SEC_ERR_31";
    public final static String SEC_ERR_32 = "SEC_ERR_32";
    public final static String SEC_ERR_33 = "SEC_ERR_33";
    public final static String SEC_ERR_34 = "SEC_ERR_34";
    public final static String SEC_ERR_35 = "SEC_ERR_35";
    public final static String SEC_ERR_40 = "SEC_ERR_40";
    public final static String SEC_ERR_41 = "SEC_ERR_41";
    public final static String SEC_ERR_42 = "SEC_ERR_42";
    //public final static String SEC_ERR_50 = "SEC_ERR_50";
    
    
    
    @Override
    public  String formatErrorMsg(String id,Object ... args) {
    
    if (SEC_ERR_01.equals(id))
        return MessageFormat.format("Bean {0} is not a valid configuration password encoder",args);
    if (SEC_ERR_02.equals(id))
        return MessageFormat.format("No role service named {0} ",args);
    if (SEC_ERR_03.equals(id))
        return MessageFormat.format("No authentication provider named {0} ",args);
    if (SEC_ERR_04.equals(id))
        return MessageFormat.format("Bean {0} is not a valid password password encoder",args);
    if (SEC_ERR_05.equals(id))
        return MessageFormat.format("Install unrestricted security policy files before using a strong configuration " +
        		"password encoder",args);
    if (SEC_ERR_06.equals(id))
        return MessageFormat.format("Install unrestricted security policy files before using a strong user " +
                        "password encoder",args);
    if (SEC_ERR_07.equals(id))
        return MessageFormat.format("Configuration password encoder is required",args);
    
    if (SEC_ERR_20.equals(id))
        return MessageFormat.format("Class not found {0} ",args);
    if (SEC_ERR_21.equals(id))
        return MessageFormat.format("Class {1} ist not of type {0} ",args);
    if (SEC_ERR_22.equals(id))
        return MessageFormat.format("Name is required ",args);
    
    if (SEC_ERR_23a.equals(id))
        return MessageFormat.format("Authentication provider {0} alreday exists",args);
    if (SEC_ERR_23b.equals(id))
        return MessageFormat.format("Password policy {0} alreday exists",args);
    if (SEC_ERR_23c.equals(id))
        return MessageFormat.format("Role service {0} alreday exists",args);
    if (SEC_ERR_23d.equals(id))
        return MessageFormat.format("User/group service {0} alreday exists",args);
    if (SEC_ERR_23e.equals(id))
        return MessageFormat.format("Authentication filter {0} alreday exists",args);
    
    
    if (SEC_ERR_24a.equals(id))
        return MessageFormat.format("Authentication provider {0} does not exist",args);
    if (SEC_ERR_24b.equals(id))
        return MessageFormat.format("Password policy {0} does not exist",args);
    if (SEC_ERR_24c.equals(id))
        return MessageFormat.format("Role service {0} {0} does not exist",args);
    if (SEC_ERR_24d.equals(id))
        return MessageFormat.format("User/group service {0} does not exist",args);
    if (SEC_ERR_24e.equals(id))
        return MessageFormat.format("Authentication filter {0} does not exist",args);
    
    if (SEC_ERR_25.equals(id))
        return MessageFormat.format("Implementation name is required",args);

    
    if (SEC_ERR_30.equals(id))
        return MessageFormat.format("Role service {0} is active and cannot be deleted",args);
    if (SEC_ERR_31.equals(id))
        return MessageFormat.format("Authentication provider {0} is active and cannot be deleted",args);
    if (SEC_ERR_32.equals(id))
        return MessageFormat.format("No password encoder specified for user/group service {0}",args);
    if (SEC_ERR_33.equals(id))
        return MessageFormat.format("No password policy specified for user/group service {0}",args);
    if (SEC_ERR_34.equals(id))
        return MessageFormat.format("Password policy {0} is used by user/group service {1}",args);
    if (SEC_ERR_35.equals(id))
        return MessageFormat.format("User/group service {0} is used by authentication provider {1}",args);    
    if (SEC_ERR_40.equals(id))
        return MessageFormat.format("Minimum length of password must be >= 0",args);
    if (SEC_ERR_41.equals(id))
        return MessageFormat.format("Maximum length of password must be greater or equal to the minimum length",args);
    if (SEC_ERR_42.equals(id))
        return MessageFormat.format("Policy for the master password cannot be deleted",args);
//    if (SEC_ERR_50.equals(id))
//        return MessageFormat.format("The administrator role has to be: {0}",args);

    return super.formatErrorMsg(id, args);
    }
}
