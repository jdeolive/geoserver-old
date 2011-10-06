/* Copyright (c) 2001 - 2008 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.security.config;



/**
 * Interface for service based on a datasource
 * obtained from jndi
 * 
 * @author christian
 *
 */
public interface JdbcJndiSecurityServiceConfig  extends JdbcBaseSecurityServiceConfig {
    
    /**
     * @return the JNDI name 
     */
    public String getJndiName();
    
    /**
     * set the JNDI name
     * @param jndiName
     */
    public void setJndiName(String jndiName);

}
