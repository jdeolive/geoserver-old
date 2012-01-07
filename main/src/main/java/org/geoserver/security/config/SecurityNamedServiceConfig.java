/* Copyright (c) 2001 - 2008 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.security.config;

/**
 * Base class for named security service configuration objects.
 * 
 * @author christian
 */
public interface SecurityNamedServiceConfig extends SecurityConfig {

    /**
     * The name of the service.
     */
    String getName();

    /**
     * Sets the name for a service.
     */
    void setName(String name);

    /**
     * Name of class for implementation of the service.
     */
    String getClassName();

    /**
     * Sets name of class for implementation of the service.
     */
    void setClassName(String className);
}
