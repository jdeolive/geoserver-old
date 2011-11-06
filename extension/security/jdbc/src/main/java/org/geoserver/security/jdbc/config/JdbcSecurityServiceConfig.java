/* Copyright (c) 2001 - 2008 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.security.jdbc.config;




/**
 * Interface for service based on a datasource
 * obtained from apache commons connection pooling
 * 
 * @author christian
 *
 */
public interface JdbcSecurityServiceConfig  extends JdbcBaseSecurityServiceConfig {
    
    /**
     * @return the JDBC driver class name
     */
    public String getDriverClassName();
    
    /**
     * set the driver class name
     * 
     * @param driverName
     */
    public void setDriverClassName(String driverName);
    
    /**
     * @return the JDBC connect url
     */
    public String getConnectURL();
    
    /**
     * set the JDBC connect url
     * 
     * @param urlString
     */
    public void setConnectURL(String urlString);

    /**
     * @return the user name for login
     */
    public String getUserName();
    /**
     * set the username
     * @param userName
     */
    public void setUserName(String userName);
    
    /**
     * @return the password for login
     */
    public String getPassword();
    /**
     * set the password for login
     * @param password
     */
    public void setPassword(String password);
    
}
