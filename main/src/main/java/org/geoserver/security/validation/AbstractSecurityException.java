/* Copyright (c) 2001 - 2008 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */


package org.geoserver.security.validation;

/**
 * Base class for exceptions used for validation errors 
 * 
 * 
 * @author christian
 *
 */
public class AbstractSecurityException extends Exception {
    private static final long serialVersionUID = 1L;
    private String errorId;
    private Object args[];
    


    /**
     * errorid is a unique identifier, message is a 
     * default error description, args are message arguments
     * to be used for an alternative message (i18n) 
     * 
     * @param errorId 
     * @param message
     * @param args 
     */
    public AbstractSecurityException(String errorId ,String message, Object... args) {        
        super(message);
        this.errorId=errorId;
        this.args=args;        
    }
        
    public String getErrorId() {
        return errorId;
    }

    public Object[] getArgs() {
        return args;
    }




    
    
    
}
