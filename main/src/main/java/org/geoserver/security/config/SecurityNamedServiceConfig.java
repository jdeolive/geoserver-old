/* Copyright (c) 2001 - 2008 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.security.config;


/**
 * Common base interface for all named security
 * services configuration objects
 * 
 * @author christian
 *
 */
public interface SecurityNamedServiceConfig extends SecurityConfig {
    /**
     * @return the name of the service
     */
    public String getName();
    /**
     * sets the name for a service
     * 
     * @param name
     */
    public void setName(String name);
    
        
    /**
     * The class name of the service to be constructed
     * The class must have a constructor with a string
     * argument, specifying the name of the service
     * 
     * @param className
     */
    public void setClassName(String className);
    
    
    /**
     * @return the service class name
     */
    public String getClassName();
}
