/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.geoserver.security.password;

import java.io.IOException;

import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.security.GeoserverUserGroupService;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.authentication.encoding.PlaintextPasswordEncoder;

/**
 * Password encoder which encodes nothing
 * 
 * @author christian
 *
 */
public class GeoserverPlainTextPasswordEncoder extends AbstractGeoserverPasswordEncoder implements GeoserverUserPasswordEncoder {
    
    public final static String BeanName="plainTextPasswordEncoder";
    protected String beanName;
    
    public static GeoserverPlainTextPasswordEncoder get() {
          return (GeoserverPlainTextPasswordEncoder)
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


    @Override
    public void setBeanName(String name) {
        beanName=name;
    }


    @Override
    public void initializeFor(GeoserverUserGroupService service) throws IOException {
        // do nothing
    }


    @Override
    public String getNameReference() {
        return beanName;
    }

}
