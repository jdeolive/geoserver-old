/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.geoserver.security.password;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.security.config.PasswordPolicyConfig;


/**
 * Implementation of the password {@link PasswordValidator} interface
 * 
 * @author christian
 *
 */
public class PasswordValidatorImpl implements PasswordValidator {

    protected PasswordPolicyConfig config;
    protected Set<String> notAllowedPrefixes;
    
    /**
     * Calculates not allowed prefixes
     */
    public PasswordValidatorImpl() {
        notAllowedPrefixes = new HashSet<String>();
        for (GeoserverPasswordEncoder enc : GeoServerExtensions.extensions(
                GeoserverPasswordEncoder.class)) {
            notAllowedPrefixes.add(enc.getPrefix()+GeoserverPasswordEncoder.PREFIX_DELIMTER);
        }
    }
    
    @Override
    public void setConfig(PasswordPolicyConfig config) {
        this.config=config;
    }

    @Override
    public PasswordPolicyConfig getConfig() {
        return config;
    }

    @Override
    public void validatePassword(String password) throws PasswordValidationException {
        if (password==null)
            throw new PasswordValidationException(PasswordInvalidReason.PW_IS_NULL);
        
        if (password.length() < config.getMinLength())
            throw new PasswordValidationException(PasswordInvalidReason.PW_MIN_LENGTH);
        
        if (config.getMaxLength() >=0 &&  password.length() >config.getMaxLength())
            throw new PasswordValidationException(PasswordInvalidReason.PW_MAX_LENGTH);

        char[] charArray = password.toCharArray();
        
        if (config.isDigitRequired()) {
            if (checkUsingMethod("isDigit", charArray)==false)
                throw new PasswordValidationException(PasswordInvalidReason.PW_NO_DIGIT);
        }
        if (config.isUppercaseRequired()) {
            if (checkUsingMethod("isUpperCase", charArray)==false)
                throw new PasswordValidationException(PasswordInvalidReason.PW_NO_UPPERCASE);
        }
        if (config.isLowercaseRequired()) {
            if (checkUsingMethod("isLowerCase", charArray)==false)
                throw new PasswordValidationException(PasswordInvalidReason.PW_NO_LOWERCASE);
        }    
        
        for (String prefix: notAllowedPrefixes) {
            if (password.startsWith(prefix))
                throw new PasswordValidationException(PasswordInvalidReason.PW_RESERVED_PREFIX);
        }
    }
    
    /**
     * Executes statis check methods from the character class
     * 
     * @param methodname
     * @param charArray
     * @return
     */
    protected boolean checkUsingMethod(String methodname, char[] charArray) {
        try {
            Method m = getClass().getMethod(methodname, Character.class);
            for (char c : charArray) {
                Boolean result = (Boolean) m.invoke(this, c);
                if (result)
                    return true;
            }        
            return false;
        } catch (Exception ex) {
            throw new RuntimeException("never should reach this point",ex);
        }
    }

    public boolean isDigit(Character c) {
        return Character.isDigit(c);
    }
    public boolean isUpperCase(Character c) {
        return Character.isUpperCase(c);
    }

    public boolean isLowerCase(Character c) {
        return Character.isLowerCase(c);
    }

}
