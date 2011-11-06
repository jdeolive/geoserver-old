/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.geoserver.security.password;

public class PasswordValidationException extends Exception {

    private static final long serialVersionUID = 1L;
    protected PasswordInvalidReason reason;
    
    
    public PasswordValidationException(PasswordInvalidReason reason){
        super();
        this.reason=reason;
    }

    public PasswordInvalidReason getReason() {
        return reason;
    }
    
}
