/* Copyright (c) 2001 - 2008 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.security.config;

/**
 * Password Policy configuration file
 * 
 * @author christian
 *
 */
public interface PasswordPolicyConfig extends SecurityNamedServiceConfig {
        
    /**
     * Is an upper case letter required
     * {@link Character#isUpperCase(char)}
     * 
     * @return 
     */
    public boolean isUppercaseRequired();
    public void setUppercaseRequired(boolean uppercaseRequired);
    
    /**
     * Is lower case letter required
     * {@link Character#isLowerCase(char)}
     * @return
     */
    public boolean isLowercaseRequired();
    public void setLowercaseRequired(boolean lowercaseRequired);
    
    /**
     * is digit required
     * {@link Character#isDigit(char)}
     * @return
     */
    public boolean isDigitRequired();
    public void setDigitRequired(boolean digitRequired);

    
    /**
     * The minimal length of a password
     * 
     * @return
     */
    public int getMinLength();
    public void setMinLength(int minLength);
    
    /**
     * The maximal length of a password
     * -1 means no restriction
     * @return
     */
    public int getMaxLength();
    public void setMaxLength(int maxLength);
    
}
