/* Copyright (c) 2001 - 2008 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */


package org.geoserver.security.validation;

import java.text.MessageFormat;

import org.geoserver.security.password.PasswordValidator;

/**
 *  
 * Validation errors for {@link PasswordValidator} 
 * 
 * 
 * @author christian
 *
 */
public class PasswordValidationErrors  extends AbstractSecurityValidationErrors{

    
    public final static String PW_IS_NULL="PW_IS_NULL";
    public final static String PW_NO_DIGIT="PW_NO_DIGIT";
    public final static String PW_NO_UPPERCASE="PW_NO_UPPERCASE";
    public final static String PW_NO_LOWERCASE="PW_NO_LOWERCASE";
    public final static String PW_MIN_LENGTH="PW_MIN_LENGTH";
    public final static String PW_MAX_LENGTH="PW_MAX_LENGTH";
    public final static String PW_RESERVED_PREFIX="PW_RESERVED_PREFIX";
                  
    @Override
    public  String formatErrorMsg(String id,Object ... args) {
    
    if (PW_IS_NULL.equals(id))
        return MessageFormat.format("Password is mandatory",args);
    if (PW_NO_DIGIT.equals(id))
        return MessageFormat.format("Password does not contain a digit",args);
    if (PW_NO_UPPERCASE.equals(id))
        return MessageFormat.format("password does not contain an upper case letter",args);
    if (PW_NO_LOWERCASE.equals(id))
        return MessageFormat.format("password does not contain a lower case letter",args);
    if (PW_MIN_LENGTH.equals(id))
        return MessageFormat.format("password must have {0} characters",args);
    if (PW_MAX_LENGTH.equals(id))
        return MessageFormat.format("password has more than {0} characters",args);
    if (PW_RESERVED_PREFIX.equals(id))
        return MessageFormat.format("password  starts with reserved prefix {0}",args);

    return super.formatErrorMsg(id, args);
    }
}
