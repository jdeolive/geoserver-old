/* Copyright (c) 2001 - 2008 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.security.config;

import java.io.File;

import org.geoserver.security.file.FileWatcher;


/**
 * Interface for service based on a file
 * 
 * @author christian
 *
 */
public interface FileBasedSecurityServiceConfig  extends SecurityNamedServiceConfig {
    /**
     * @return the filename
     */
    public String getFileName();
    /**
     * set the filename
     *       
     * @param fileName
     */
    public void setFileName(String fileName);
    /**
     * 
     * @return the check intervall in millisecs
     */
    public long getCheckInterval();
    /**
     * Sets the check interval (milliSecs) for checking
     * if the file has changed (needed for cluster
     * deployments or if the file is modified
     * externally)
     *  
     * A value of > 0 causes
     * {@link FileWatcher} object to be created
     * 
     * A value of <= 0 disables this feature
     * 
     * Hint: the granularity of {@link File} last access time
     * is often a second, values < 1000 may not have the desired
     * effect.
     * 
     * @param value
     */
    public void setCheckInterval(long milliSecs);

}
