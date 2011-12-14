/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.geoserver.security.password;

import org.geoserver.platform.GeoServerExtensions;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.authentication.encoding.PlaintextPasswordEncoder;

/**
 * Configuration password encoder which encodes nothing
 * 
 * @author christian
 *
 */
public class GeoserverConfigPlainTextPasswordEncoder extends AbstractGeoserverPasswordEncoder implements GeoserverConfigPasswordEncoder {
    
    public final static String BeanName="plainTextConfigPasswordEncoder";
    
    public static GeoserverConfigPlainTextPasswordEncoder get() {
          return (GeoserverConfigPlainTextPasswordEncoder)
                    GeoServerExtensions.bean(BeanName);        
    }

    
    @Override
    protected PasswordEncoder getActualEncoder() {
        return new PlaintextPasswordEncoder();
    }

    @Override
    public PasswordEncodingType getEncodingType() {
        return PasswordEncodingType.PLAIN;
    }
    
    public String decode(String encPass) throws UnsupportedOperationException {
        return removePrefix(encPass);
    }





}
