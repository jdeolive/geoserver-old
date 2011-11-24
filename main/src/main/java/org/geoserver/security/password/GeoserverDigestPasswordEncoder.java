/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.geoserver.security.password;

import java.io.IOException;

import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.security.GeoserverUserGroupService;
import org.jasypt.spring.security3.PasswordEncoder;
import org.jasypt.util.password.StrongPasswordEncryptor;

/**
 * Password encoder which uses digest encoding
 * This encoder cannot be used for authentication mechanisms
 * needing the plain text password. (Http digest authentication 
 * as an example)
 * 
 * The salt parameter is not used, this implementation
 * computes a random salt as default. 
 * 
 * {@link #isPasswordValid(String, String, Object)}
 * {@link #encodePassword(String, Object)}

 * 
 * @author christian
 *
 */
public class GeoserverDigestPasswordEncoder extends AbstractGeoserverPasswordEncoder implements GeoserverUserPasswordEncoder {

        
    public final static String BeanName="digestPasswordEncoder";

    public static GeoserverDigestPasswordEncoder get() {
        return (GeoserverDigestPasswordEncoder)
                GeoServerExtensions.bean(BeanName);        
    }
    
    @Override
    protected PasswordEncoder getActualEncoder() {
        PasswordEncoder encoder = new PasswordEncoder();
        encoder.setPasswordEncryptor(new StrongPasswordEncryptor());
        return encoder;
    }

    @Override
    public PasswordEncodingType getEncodingType() {
        return PasswordEncodingType.DIGEST;
    }



    @Override
    public void initializeFor(GeoserverUserGroupService service) throws IOException {
        return;
    }

    
    
}
