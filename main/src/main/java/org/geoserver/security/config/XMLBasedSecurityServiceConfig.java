/* Copyright (c) 2001 - 2008 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.security.config;



/**
 * Interface for service based on xml dom
 * 
 * @author christian
 *
 */
public interface XMLBasedSecurityServiceConfig  extends SecurityNamedServiceConfig {
    
    /**
     * @return if  xml schema
     * validation is active
     */
    public boolean isValidating();
    
    /**
     * activate/deactivate schema validation
     * 
     * @param value
     */
    public void setValidating(boolean value);

}
