/* Copyright (c) 2001 - 2008 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */


package org.geoserver.security.validation;

/**
 * Exception used for validation errors 
 * during security configuration
 * 
 * @author christian
 *
 */
public class SecurityConfigException extends AbstractSecurityException {
    private static final long serialVersionUID = 1L;
    
    public SecurityConfigException(String errorId, String message, Object[] args) {
        super(errorId, message, args);        
    }               
}
