/* Copyright (c) 2001 - 2008 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */


package org.geoserver.security.validation;


/**
 *  
 * Base class for validation error ids  
 * 
 * @author christian
 *
 */
public abstract class AbstractSecurityValidationErrors  {

    
    
        
    public  String formatErrorMsg(String id,Object ... args) {                
        throw new RuntimeException( "Unknown error id "+id);
    }
}
