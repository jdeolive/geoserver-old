/* Copyright (c) 2001 - 2008 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.security.validation;


import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.security.GeoServerSecurityManager;

public abstract class AbstractSecurityValidator {

    protected GeoServerSecurityManager manager;

        
    public AbstractSecurityValidator() {
        manager=GeoServerExtensions.bean(GeoServerSecurityManager.class);
    }
    
       
    protected boolean isNotEmpty(String aString) {
        return aString !=null && aString.length()> 0;
    }

    
    /**
     * Subclasses must override  
     * 
     * @return
     */
    protected abstract AbstractSecurityValidationErrors getSecurityErrors();
    
    
}
