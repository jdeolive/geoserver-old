/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.geoserver.security.password;

import org.jasypt.spring.security3.PBEPasswordEncoder;
import org.jasypt.util.text.BasicTextEncryptor;
import org.springframework.security.authentication.encoding.PasswordEncoder;

/**
 * Password Encoder using symmetric encryption
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
public class GeoserverPBEPasswordEncoder extends AbstractGeoserverPasswordEncoder {

    BasicTextEncryptor encrypter;
    @Override
    protected PasswordEncoder getActualEncoder() {
        PBEPasswordEncoder encoder = new PBEPasswordEncoder();
        // TODO, Cannot use due to US export restrictions
        //encrypter = new StrongTextEncryptor();
        encrypter = new BasicTextEncryptor();
        String masterPassword = MasterPasswordProvider.get().getMasterPassword();
        if (masterPassword==null)
            throw new RuntimeException( this.getClass().getName()+ " needs a master password");
        encrypter.setPassword(masterPassword);
        encoder.setTextEncryptor(encrypter);        
        return encoder;
    }

    @Override
    public PasswordEncoding getEncodingType() {
        return PasswordEncoding.ENCRYPT;
    }
    
    public String getPrefix() {
        return "crypt1";
    }

    public String decode(String encPass) throws UnsupportedOperationException {
        String encPass2= removePrefix(encPass);
        if (encrypter==null) { // not initialized
            getDelegate();
        }
        return encrypter.decrypt(encPass2);
    }

}
