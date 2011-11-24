/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.geoserver.security.password;


import org.jasypt.spring.security3.PasswordEncoder;
import org.springframework.beans.factory.BeanNameAware;

/**
 * {@link PasswordEncoder} implementations useable for 
 * encoding passwords in configuration file. This is a
 * marker interface
 * 
 * @author christian
 *
 */
public interface GeoserverConfigPasswordEncoder extends GeoserverPasswordEncoder, BeanNameAware {
    
        
}
