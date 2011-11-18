/* Copyright (c) 2001 - 2008 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.security.config.impl;

import org.geoserver.security.config.FileBasedSecurityServiceConfig;
import org.geoserver.security.config.XMLBasedSecurityServiceConfig;

/**
 * Implementation of {@link FileBasedSecurityServiceConfig} and
 * {@link XMLBasedSecurityServiceConfig}
 * 
 * @author christian
 *
 */
public class XMLFileBasedSecurityServiceConfigImpl extends FileBasedSecurityServiceConfigImpl 
    implements FileBasedSecurityServiceConfig,XMLBasedSecurityServiceConfig {
    private static final long serialVersionUID = 1L;
    private boolean validating;

    /* (non-Javadoc)
     * @see org.geoserver.security.config.XMLBasedSecurityServiceConfig#isValidating()
     */
    public boolean isValidating() {
        return validating;
    }

    /* (non-Javadoc)
     * @see org.geoserver.security.config.XMLBasedSecurityServiceConfig#setValidating(boolean)
     */
    public void setValidating(boolean validating) {
        this.validating = validating;
    }
    
}
