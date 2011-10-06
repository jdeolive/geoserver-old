/* Copyright (c) 2001 - 2008 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.security.config.impl;

import org.geoserver.security.config.JdbcJndiSecurityServiceConfig;

/**
 * Implementation of {@link JdbcJndiSecurityServiceConfig} 
 * 
 * @author christian
 *
 */
public class JdbcJndiSecurityServiceConfigImpl extends JdbcBaseSecurityServiceConfigImpl 
    implements JdbcJndiSecurityServiceConfig {
    private String jndiName;

    /* (non-Javadoc)
     * @see org.geoserver.security.config.JdbcJndiSecurityServiceConfig#getJndiName()
     */
    public String getJndiName() {
        return jndiName;
    }

    /* (non-Javadoc)
     * @see org.geoserver.security.config.JdbcJndiSecurityServiceConfig#setJndiName(java.lang.String)
     */
    public void setJndiName(String jndiName) {
        this.jndiName = jndiName;
    }

    
}
