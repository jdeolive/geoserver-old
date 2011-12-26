/* Copyright (c) 2001 - 2008 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.security.jdbc.config.impl;

import org.geoserver.security.config.impl.SecurityNamedServiceConfigImpl;
import org.geoserver.security.jdbc.config.JdbcBaseSecurityServiceConfig;

/**
 * Implementation of {@link JdbcBaseSecurityServiceConfig} 
 * 
 * @author christian
 *
 */
public abstract class JdbcBaseSecurityServiceConfigImpl extends SecurityNamedServiceConfigImpl
    implements JdbcBaseSecurityServiceConfig {

    private static final long serialVersionUID = 1L;
    private String propertyFileNameDDL;
    private String propertyFileNameDML;
    private String jndiName;
    private boolean jndi;
    private String connectURL;
    private String userName;
    private String password;
    
    public boolean isJndi() {
        return jndi;
    }

    public void setJndi(boolean jndi) {
        this.jndi = jndi;
    }

    public String getConnectURL() {
        return connectURL;
    }

    public void setConnectURL(String connectURL) {
        this.connectURL = connectURL;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }
    private String driverClassName;

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
