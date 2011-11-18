/* Copyright (c) 2001 - 2008 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.security.config.impl;

import org.geoserver.security.config.PasswordPolicyConfig;

/**
 * Implementation of {@link PasswordPolicyConfig}
 * 
 * @author christian
 *
 */
public class PasswordPolicyConfigImpl extends SecurityNamedServiceConfigImpl implements PasswordPolicyConfig {
    private static final long serialVersionUID = 1L;
    private boolean uppercaseRequired,lowercaseRequired,digitRequired;
    private int minLength,maxLength;
    
    public PasswordPolicyConfigImpl() {
        maxLength=-1;
    }
    
    public boolean isUppercaseRequired() {
        return uppercaseRequired;
    }
    public void setUppercaseRequired(boolean uppercaseRequired) {
        this.uppercaseRequired = uppercaseRequired;
    }
    public boolean isLowercaseRequired() {
        return lowercaseRequired;
    }
    public void setLowercaseRequired(boolean lowercaseRequired) {
        this.lowercaseRequired = lowercaseRequired;
    }
    public boolean isDigitRequired() {
        return digitRequired;
    }
    public void setDigitRequired(boolean digitRequired) {
        this.digitRequired = digitRequired;
    }
    public int getMinLength() {
        return minLength;
    }
    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }
    public int getMaxLength() {
        return maxLength;
    }
    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }
                
}
