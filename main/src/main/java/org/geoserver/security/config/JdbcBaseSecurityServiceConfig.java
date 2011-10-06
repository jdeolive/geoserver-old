/* Copyright (c) 2001 - 2008 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.security.config;



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
     * @return filename of property file containig DDL statements
     */
    public String getPropertyFileNameDDL();
    /**
     * sets the filename
     * @param fileName
     */
    public void   setPropertyFileNameDDL (String fileName);
    
    /**
     * @return filename of property file containig DML statements
     */
    public String getPropertyFileNameDML();
    /**
     * sets the filename
     * @param fileName
     */
    public void   setPropertyFileNameDML (String fileName);
}
