/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.geoserver.security.password;

import java.io.IOException;
import java.security.Security;
import java.util.logging.Logger;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.geoserver.security.GeoServerUserGroupService;
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
public abstract class AbstractGeoserverPasswordEncoder implements GeoServerPasswordEncoder {

    static protected Logger LOGGER = Logging.getLogger("org.geoserver.security");

    protected PasswordEncoder delegate = null;
    protected Object lock = new Object();
    protected String beanName;
    private boolean availableWithoutStrongCryptogaphy;
    private boolean reversible = true;
    private String prefix;

    public String getName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    /**
     * Does nothing, subclases may override.
     */
    public void initializeFor(GeoServerUserGroupService service) throws IOException {
    }

   
    public AbstractGeoserverPasswordEncoder() {
        setAvailableWithoutStrongCryptogaphy(true);
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
        StringBuffer buff = new StringBuffer();
        if (getPrefix() != null) {
            buff.append(getPrefix()).append(GeoServerPasswordEncoder.PREFIX_DELIMTER);
        }
        
        buff.append(getDelegate().encodePassword(rawPass, salt));
        return buff.toString();
    }

    @Override
    public boolean isPasswordValid(String encPass, String rawPass, Object salt)
            throws DataAccessException {
        String encPass2 = getPrefix() != null ? removePrefix(encPass) : encPass;
        return getDelegate().isPasswordValid(encPass2, rawPass, salt);
    }


    @Override
    public abstract PasswordEncodingType getEncodingType();

    protected String removePrefix(String encPass) {
        return encPass.replaceFirst(getPrefix()+GeoServerPasswordEncoder.PREFIX_DELIMTER, "");
    }
    
    /**
     * @param encPass
     * @return true if this encoder has encoded encPass
     */
    public boolean isResponsibleForEncoding(String encPass) {
        if (encPass==null) return false;        
        return encPass.startsWith(getPrefix()+GeoServerPasswordEncoder.PREFIX_DELIMTER);
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

    public boolean isAvailableWithoutStrongCryptogaphy() {
        return availableWithoutStrongCryptogaphy;
    }


    public void setAvailableWithoutStrongCryptogaphy(boolean availableWithoutStrongCryptogaphy) {
        this.availableWithoutStrongCryptogaphy = availableWithoutStrongCryptogaphy;
    }

    public boolean isReversible() {
        return reversible;
    }

    public void setReversible(boolean reversible) {
        this.reversible = reversible;
    }
}
