/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.geoserver.security.validation;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.security.GeoServerSecurityManager;
import org.geoserver.security.config.PasswordPolicyConfig;
import org.geoserver.security.password.GeoServerPasswordEncoder;
import org.geoserver.security.password.PasswordValidator;


/**
 * Implementation of the password {@link PasswordValidator} interface
 * 
 * @author christian
 *
 */
public class PasswordValidatorImpl extends AbstractSecurityValidator implements PasswordValidator {

    protected PasswordPolicyConfig config;
    protected Set<String> notAllowedPrefixes;
    
    /**
     * Calculates not allowed prefixes
     */
    public PasswordValidatorImpl(GeoServerSecurityManager securityManager) {
        super(securityManager);
        notAllowedPrefixes = new HashSet<String>();
        for (GeoServerPasswordEncoder enc : GeoServerExtensions.extensions(
                GeoServerPasswordEncoder.class)) {
            notAllowedPrefixes.add(enc.getPrefix()+GeoServerPasswordEncoder.PREFIX_DELIMTER);
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
    public void validatePassword(String password) throws IOException {
        if (password==null)
            throw createSecurityException(PasswordValidationErrors.PW_IS_NULL); 
            
        
        if (password.length() < config.getMinLength())
            throw createSecurityException(PasswordValidationErrors.PW_MIN_LENGTH, config.getMinLength());
        
        if (config.getMaxLength() >=0 &&  password.length() >config.getMaxLength())
            throw createSecurityException(PasswordValidationErrors.PW_MAX_LENGTH,config.getMaxLength());

        char[] charArray = password.toCharArray();
        
        if (config.isDigitRequired()) {
            if (checkUsingMethod("isDigit", charArray)==false)
                throw createSecurityException(PasswordValidationErrors.PW_NO_DIGIT);
        }
        if (config.isUppercaseRequired()) {
            if (checkUsingMethod("isUpperCase", charArray)==false)
                throw createSecurityException(PasswordValidationErrors.PW_NO_UPPERCASE);
        }
        if (config.isLowercaseRequired()) {
            if (checkUsingMethod("isLowerCase", charArray)==false)
                throw createSecurityException(PasswordValidationErrors.PW_NO_LOWERCASE);
        }    
        
        for (String prefix: notAllowedPrefixes) {
            if (password.startsWith(prefix))
                throw createSecurityException(PasswordValidationErrors.PW_RESERVED_PREFIX,prefix);
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

    
    @Override
    protected AbstractSecurityValidationErrors getSecurityErrors() {
        return new PasswordValidationErrors();
    }

    
    /**
     * Helper method for creating a proper
     * {@link SecurityConfigException} object
     * 
     * @param errorid
     * @param args
     * @return
     */
    protected IOException createSecurityException (String errorid, Object ...args) {
        String message = getSecurityErrors().formatErrorMsg(errorid, args);
        PasswordValidationException ex =  new PasswordValidationException(errorid,message,args);
        return new IOException("Details are in the nested excetpion",ex);
    }

}
