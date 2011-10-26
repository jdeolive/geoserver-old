/* Copyright (c) 2001 - 2008 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.security.jdbc.config.impl;

import org.geoserver.security.config.JdbcBaseSecurityServiceConfig;
import org.geoserver.security.config.impl.SecurityNamedServiceConfigImpl;

/**
 * Implementation of {@link JdbcBaseSecurityServiceConfig} 
 * 
 * @author christian
 *
 */
public abstract class JdbcBaseSecurityServiceConfigImpl extends SecurityNamedServiceConfigImpl 
    implements JdbcBaseSecurityServiceConfig {

    private String propertyFileNameDDL;
    private String propertyFileNameDML;

    /* (non-Javadoc)
     * @see org.geoserver.security.config.JdbcBaseSecurityServiceConfig#getPropertyFileNameDDL()
     */
    public String getPropertyFileNameDDL() {
        return propertyFileNameDDL;
    }
    /* (non-Javadoc)
     * @see org.geoserver.security.config.JdbcBaseSecurityServiceConfig#setPropertyFileNameDDL(java.lang.String)
     */
    public void setPropertyFileNameDDL(String propertyFileNameDDL) {
        this.propertyFileNameDDL = propertyFileNameDDL;
    }
    /* (non-Javadoc)
     * @see org.geoserver.security.config.JdbcBaseSecurityServiceConfig#getPropertyFileNameDML()
     */
    public String getPropertyFileNameDML() {
        return propertyFileNameDML;
    }
    /* (non-Javadoc)
     * @see org.geoserver.security.config.JdbcBaseSecurityServiceConfig#setPropertyFileNameDML(java.lang.String)
     */
    /* (non-Javadoc)
     * @see org.geoserver.security.config.JdbcBaseSecurityServiceConfig#setPropertyFileNameDML(java.lang.String)
     */
    public void setPropertyFileNameDML(String propertyFileNameDML) {
        this.propertyFileNameDML = propertyFileNameDML;
    }


    
}
