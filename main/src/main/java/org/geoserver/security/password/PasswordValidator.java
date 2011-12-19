/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.geoserver.security.password;

import java.io.IOException;

import org.geoserver.security.config.PasswordPolicyConfig;
import org.geoserver.security.validation.PasswordValidationException;
import org.geoserver.security.validation.PasswordValidatorImpl;

/**
 * Validates a password based on 
 * {@link PasswordPolicyConfig} object
 * 
 * At a bare minimum, <code>null</code> passwords
 * should not be allowed.
 * 
 * Additionally, password must not start with 
 * prefixes used by the {@link GeoserverPasswordEncoder} objects
 * To get the prefixes use
 * 
 * <code>
 * for (GeoserverPasswordEncoder enc : GeoServerExtensions.extensions(
 *           GeoserverPasswordEncoder.class)) {
 *     System.out.println(enc.getPrefix()+GeoserverPasswordEncoder.PREFIX_DELIMTER);
 *         }
 * </code>
 * 
 * A concrete example can be found in
 * {@link PasswordValidatorImpl#PasswordValidatorImpl()}
 * 
 * @author christian
 *
 */
public interface PasswordValidator {
    
    public final static String DEFAULT_NAME="default";
    public final static String MASTERPASSWORD_NAME="master";
    
    /**
     * setter for the config
     * 
     * @param config
     */
    public void setConfig(PasswordPolicyConfig config);
    
    /**
     * Getter for the config
     * @return
     */
    public PasswordPolicyConfig getConfig();
    
    /**
     * Validates the password, throws an exception
     * if the password is not valid
     * 
     * @param password
     * @throws PasswordValidationException
     */
    public void validatePassword(String password) throws IOException;

}
