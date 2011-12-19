/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.geoserver.security.validation;


public class PasswordValidationException extends AbstractSecurityException {
    private static final long serialVersionUID = 1L;

    public PasswordValidationException(String errorId, String message, Object[] args) {
        super(errorId, message, args);
        
    }       
}
