/* Copyright (c) 2001 - 2008 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */


package org.geoserver.web.security.config;


import org.geoserver.security.config.SecurityConfig;

/**
 * 
 * 
 * 
 * @author christian
 *
 */
public  class SecurityConfigModelHelper extends AbstractConfigModelHelper<SecurityConfig>{

    private static final long serialVersionUID = 1L;
    
    
    public SecurityConfigModelHelper(SecurityConfig config, boolean isNew) {
        super(config,isNew);
    }
    
}
