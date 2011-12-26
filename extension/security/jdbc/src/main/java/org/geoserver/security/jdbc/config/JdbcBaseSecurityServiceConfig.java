/* Copyright (c) 2001 - 2008 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.security.jdbc.config;

import org.geoserver.security.config.SecurityNamedServiceConfig;



/**
 * Base interface for service based on jdbc
 * get the XML property file names for DML and DDL statements
 * 
 * 
 * @author christian
 *
 */
public interface JdbcBaseSecurityServiceConfig  extends SecurityNamedServiceConfig {
    

    /**
     * Connect parameters via JNDI
     * 
     * @return
     */
    public boolean isJndi();
    
    public void setJndi(boolean jndi);
    
    /**
     * @return the JNDI name 
     */
    public String getJndiName();
    
    /**
     * set the JNDI name
     * @param jndiName
     */
    public void setJndiName(String jndiName);

    
    /**
     * @return filename of property file containing DDL statements
     */
    public String getPropertyFileNameDDL();
    /**
     * sets the filename
     * @param fileName
     */
    public void   setPropertyFileNameDDL (String fileName);
    
    /**
     * @return filename of property file containing DML statements
     */
    public String getPropertyFileNameDML();
    /**
     * sets the filename
     * @param fileName
     */
    public void   setPropertyFileNameDML (String fileName);
    
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
