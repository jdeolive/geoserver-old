/* Copyright (c) 2001 - 2008 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.security.jdbc.config.impl;

import org.geoserver.security.jdbc.config.JdbcSecurityServiceConfig;

/**
 * Implementation of {@link JdbcSecurityServiceConfig} 
 * 
 * @author christian
 *
 */
public class JdbcSecurityServiceConfigImpl extends JdbcBaseSecurityServiceConfigImpl 
    implements JdbcSecurityServiceConfig {
    private String connectURL;
    private String userName;
    private String password;
    private String driverClassName;
    
    /* (non-Javadoc)
     * @see org.geoserver.security.config.JdbcSecurityServiceConfig#getConnectURL()
     */
    public String getConnectURL() {
        return connectURL;
    }
    /* (non-Javadoc)
     * @see org.geoserver.security.config.JdbcSecurityServiceConfig#setConnectURL(java.lang.String)
     */
    public void setConnectURL(String connectURL) {
        this.connectURL = connectURL;
    }
    /* (non-Javadoc)
     * @see org.geoserver.security.config.JdbcSecurityServiceConfig#getUserName()
     */
    public String getUserName() {
        return userName;
    }
    /* (non-Javadoc)
     * @see org.geoserver.security.config.JdbcSecurityServiceConfig#setUserName(java.lang.String)
     */
    /* (non-Javadoc)
     * @see org.geoserver.security.config.JdbcSecurityServiceConfig#setUserName(java.lang.String)
     */
    public void setUserName(String username) {
        this.userName = username;
    }
    /* (non-Javadoc)
     * @see org.geoserver.security.config.JdbcSecurityServiceConfig#getPassword()
     */
    public String getPassword() {
        return password;
    }
    /* (non-Javadoc)
     * @see org.geoserver.security.config.JdbcSecurityServiceConfig#setPassword(java.lang.String)
     */
    public void setPassword(String password) {
        this.password = password;
    }
    /* (non-Javadoc)
     * @see org.geoserver.security.config.JdbcSecurityServiceConfig#getDriverClassName()
     */
    public String getDriverClassName() {
        return driverClassName;
    }
    /* (non-Javadoc)
     * @see org.geoserver.security.config.JdbcSecurityServiceConfig#setDriverClassName(java.lang.String)
     */
    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }


    
}
