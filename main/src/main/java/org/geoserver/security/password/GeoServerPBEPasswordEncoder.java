/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.security.password;

import java.io.IOException;

import org.geoserver.security.GeoServerUserGroupService;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.spring.security3.PBEPasswordEncoder;
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
public class GeoServerPBEPasswordEncoder extends AbstractGeoserverPasswordEncoder {

    StandardPBEStringEncryptor encrypter;

    private String providerName,algorithm;
    private String keyAliasInKeyStore = KeyStoreProvider.CONFIGPASSWORDKEY;

    @Override
    public void initializeFor(GeoServerUserGroupService service) throws IOException {
        if (KeyStoreProvider.get().hasUserGRoupKey(service.getName())==false) {
            throw new IOException("No key alias: " +
                    KeyStoreProvider.get().aliasForGroupService(service.getName())+
                    "\nin key store: " + KeyStoreProvider.get().getKeyStoreProvderFile().getAbsolutePath());
        }
        
        keyAliasInKeyStore=
                KeyStoreProvider.get().aliasForGroupService(service.getName());

    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }


    public String getAlgorithm() {
        return algorithm;
    }


    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getKeyAliasInKeyStore() {
        return keyAliasInKeyStore;
    }

    @Override
    protected PasswordEncoder getActualEncoder() {
        
        String password=null;
        try {
            if (KeyStoreProvider.get().containsAlias(getKeyAliasInKeyStore())==false)
                throw new IOException();
            password = new String (KeyStoreProvider.get().getSecretKey(getKeyAliasInKeyStore()).getEncoded());
        } catch (IOException e) {
            throw new RuntimeException( "Cannot find alias: "+getKeyAliasInKeyStore() +
                    " in "+ KeyStoreProvider.get().getKeyStoreProvderFile().getAbsolutePath());
        }            

        PBEPasswordEncoder encoder = new PBEPasswordEncoder();
        encrypter = new StandardPBEStringEncryptor();
        
        encrypter.setPassword(password);
        if (getProviderName()!=null && getProviderName().isEmpty()==false)
            encrypter.setProviderName(getProviderName());
        encrypter.setAlgorithm(getAlgorithm());
        encoder.setPbeStringEncryptor(encrypter);
        return encoder;
    }

    @Override
    public PasswordEncodingType getEncodingType() {
        return PasswordEncodingType.ENCRYPT;
    }
    

    public String decode(String encPass) throws UnsupportedOperationException {
        String encPass2= removePrefix(encPass);
        if (encrypter==null) { // not initialized
            getDelegate();
        }
        return encrypter.decrypt(encPass2);
    }
}
