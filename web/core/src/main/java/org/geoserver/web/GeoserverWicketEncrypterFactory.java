/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

/**
 * Password Encoder for encrypting url params
 * 
 * @author christian
 *
 */

package org.geoserver.web;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.logging.Logger;

import javax.crypto.Cipher;

import org.apache.wicket.util.crypt.AbstractCrypt;
import org.apache.wicket.util.crypt.ICrypt;
import org.apache.wicket.util.crypt.ICryptFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.security.password.AbstractGeoserverPasswordEncoder;
import org.geoserver.security.password.KeyStoreProvider;
import org.geotools.util.logging.Logging;
import org.jasypt.encryption.pbe.StandardPBEByteEncryptor;

/**
 * Encryptor factory for apache wicket
 * 
 * @author christian
 *
 */
public class GeoserverWicketEncrypterFactory implements ICryptFactory {
    
    static ICryptFactory Factory;
    static protected Logger LOGGER = Logging.getLogger("org.geoserver.security");    
    protected ICrypt encryptor;
    
    class NoCrypt implements ICrypt {

        @Override
        public String decryptUrlSafe(String text) {
            return text;
        }

        @Override
        public String encryptUrlSafe(String plainText) {
            return plainText;
        }

        @Override
        public void setKey(String key) {
        }
        
    };
    
    class CryptImpl extends AbstractCrypt {
        protected StandardPBEByteEncryptor enc;
        
        CryptImpl (String password) {            
            enc = new StandardPBEByteEncryptor();            
            enc.setPassword(password);
            
            if (GeoServerApplication.get().getSecurityManager().isStrongEncryptionAvailable()) {
                enc.setProvider(new BouncyCastleProvider());
                enc.setAlgorithm("PBEWITHSHA256AND128BITAES-CBC-BC");
            }
            else // US export restrictions
                enc.setAlgorithm("PBEWITHMD5ANDDES");
        }
        @Override
        protected byte[] crypt(byte[] input, int mode) throws GeneralSecurityException {
            if (mode==Cipher.ENCRYPT_MODE) {
                return enc.encrypt(input);
            } else {
                return enc.decrypt(input);
            }
        }
    };
    
    /**
     * Look up in the Spring Context for an implementation
     * of {@link ICryptFactory}
     * if nothing found use this default.
     * @return
     */
    public static ICryptFactory get() {
        if (Factory!=null) return Factory;
        Factory = GeoServerExtensions.bean(ICryptFactory.class);
        if (Factory==null)
            Factory=new GeoserverWicketEncrypterFactory();
        return Factory;
    }
    
    /**
     * Sets up the encryptor using  {@link KeyStoreProvider#URLPARAMKEY} as
     * alias for  the key store
     * 
     * if no key is found, a message is written to the  log file and
     * and encryption is disabled  
     */
    protected GeoserverWicketEncrypterFactory()  {        
       String password=null;
       try {            
           password = KeyStoreProvider.get().getUrlParamKey();
           if (password != null) {
               encryptor= new CryptImpl(password);
               return;
           }
       } catch (IOException e) {
       }
       LOGGER.severe("No key with alias: " +  KeyStoreProvider.URLPARAMKEY +
               "\nfound in: "+KeyStoreProvider.get().getKeyStoreProvderFile().getAbsolutePath()+
               "\nFalling back to no encryption for url parameters");                      
       encryptor=new NoCrypt();
   }

    @Override
    public ICrypt newCrypt() {
        return encryptor;
    }
  
 }
