/* Copyright (c) 2001 - 2008 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.security.config.impl;

import org.geoserver.security.config.FileBasedSecurityServiceConfig;

/**
 * Implementation of {@link FileBasedSecurityServiceConfig}
 * 
 * @author christian
 *
 */
public class FileBasedSecurityServiceConfigImpl extends SecurityNamedServiceConfigImpl implements FileBasedSecurityServiceConfig {
    private String fileName;
    private long checkInterval;
    public String getFileName() {
        return fileName;
    }
    /* (non-Javadoc)
     * @see org.geoserver.security.config.FileBasedSecurityServiceConfig#setFileName(java.lang.String)
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    /* (non-Javadoc)
     * @see org.geoserver.security.config.FileBasedSecurityServiceConfig#getCheckInterval()
     */
    public long getCheckInterval() {
        return checkInterval;
    }
    /* (non-Javadoc)
     * @see org.geoserver.security.config.FileBasedSecurityServiceConfig#setCheckInterval(long)
     */
    public void setCheckInterval(long checkInterval) {
        this.checkInterval = checkInterval;
    }

    
}
