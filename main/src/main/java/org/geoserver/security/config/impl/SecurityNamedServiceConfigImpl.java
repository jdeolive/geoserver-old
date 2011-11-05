/* Copyright (c) 2001 - 2008 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.security.config.impl;

import org.geoserver.security.config.SecurityNamedServiceConfig;

/**
 * Implementation of {@link SecurityNamedServiceConfig}
 * 
 * @author christian
 *
 */
public class SecurityNamedServiceConfigImpl extends SecurityConfigImpl implements SecurityNamedServiceConfig {
    private String name;
    private String className;

    
    /* (non-Javadoc)
     * @see org.geoserver.security.config.SecurityNamedServiceConfig#getClassName()
     */
    public String getClassName() {
        return className;
    }
    /* (non-Javadoc)
     * @see org.geoserver.security.config.SecurityNamedServiceConfig#setClassName(java.lang.String)
     */
    public void setClassName(String className) {
        this.className = className;
    }
    /* (non-Javadoc)
     * @see org.geoserver.security.config.SecurityNamedServiceConfig#getName()
     */
    public String getName() {
        return name;
    }
    /* (non-Javadoc)
     * @see org.geoserver.security.config.SecurityNamedServiceConfig#setName(java.lang.String)
     */
    public void setName(String name) {
        this.name = name;
    }
    
    
}
