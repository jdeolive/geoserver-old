/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.geoserver.security.password;

import java.security.InvalidKeyException;
import java.security.Security;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.geotools.util.logging.Logging;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.encoding.PasswordEncoder;

/**
 * Abstract base implementation, delegating the encoding 
 * to third party encoders implementing {@link PasswordEncoder}
 * 
 * @author christian
 *
 */
public abstract class AbstractGeoserverPasswordEncoder implements GeoserverPasswordEncoder {

    protected PasswordEncoder delegate = null;
    protected Object lock = new Object();
    private String prefix;
    
    static protected Logger LOGGER = Logging.getLogger("org.geoserver.security");
    static protected Boolean StrongCryptographyAvailable = null;
    
    

    
    /**
     * Checks if strong encryption is available 
     * by trying to encrypt with AES 256 Bit
     * 
     * @return
     */
    public static boolean isStrongCryptographyAvailable() {
        if (StrongCryptographyAvailable!=null)
            return StrongCryptographyAvailable;
        
        KeyGenerator kgen;
        try {
            kgen = KeyGenerator.getInstance("AES");
            kgen.init(256);
            SecretKey skey = kgen.generateKey();
            byte[] raw = skey.getEncoded();
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES");

            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            cipher.doFinal("This is just an example".getBytes());            
            StrongCryptographyAvailable = true;
            LOGGER.info("Strong cryptograhpy is available");
        } catch (InvalidKeyException e) {
            StrongCryptographyAvailable = false; 
            LOGGER.warning("Strong cryptograhpy is NOT available"+
            "\nDownload and install of policy files recommended"+
            "\nfrom http://www.oracle.com/technetwork/java/javase/downloads/jce-6-download-429243.html");
        } catch (Exception ex) {
            LOGGER.warning("Strong cryptograhpy is NOT available"+            
            "\nUnexpected error: "+ex.getMessage());
            StrongCryptographyAvailable =false; //should not happen
        }
        return StrongCryptographyAvailable;
    }
    
    
    public AbstractGeoserverPasswordEncoder() {
        Security.addProvider(new BouncyCastleProvider());
    }
    
    /**
     * @return the concrete {@link PasswordEncoder} object
     */
    abstract protected PasswordEncoder getActualEncoder();
    
    protected PasswordEncoder getDelegate() {
        
        if (delegate!=null)
            return delegate;
        
        synchronized (lock) {
            if (delegate!=null)
                return delegate;
            delegate=getActualEncoder();            
        }
        return delegate;
    }
    
    @Override
    public String encodePassword(String rawPass, Object salt) throws DataAccessException {
        StringBuffer buff = new StringBuffer(getPrefix()).append(GeoserverPasswordEncoder.PREFIX_DELIMTER); 
        buff.append(getDelegate().encodePassword(rawPass, salt));
        return buff.toString();
    }

    @Override
    public boolean isPasswordValid(String encPass, String rawPass, Object salt)
            throws DataAccessException {
        String encPass2 = removePrefix(encPass);
        return getDelegate().isPasswordValid(encPass2, rawPass, salt);
    }


    @Override
    public abstract PasswordEncodingType getEncodingType();

    protected String removePrefix(String encPass) {
        return encPass.replaceFirst(getPrefix()+GeoserverPasswordEncoder.PREFIX_DELIMTER, "");
    }
    
    /**
     * @param encPass
     * @return true if this encoder has encoded encPass
     */
    public boolean isResponsibleForEncoding(String encPass) {
        if (encPass==null) return false;        
        return encPass.startsWith(getPrefix()+GeoserverPasswordEncoder.PREFIX_DELIMTER);
    }
    
    
    public String decode(String encPass) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("decoding passwords not supported");
    }
    
    public String getPrefix() {
        return prefix;
    }


    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

}
