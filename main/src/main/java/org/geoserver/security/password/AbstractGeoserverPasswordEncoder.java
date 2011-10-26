/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.geoserver.security.password;

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
    protected String beanName;
    protected Object lock = new Object();
    
    
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
    public void setBeanName(String name) {
        beanName=name;
    }
    
    public String getBeanName() {
        return beanName;
    }

    @Override
    public abstract PasswordEncoding getEncodingType();

    protected String removePrefix(String encPass) {
        return encPass.replaceFirst(getPrefix()+GeoserverPasswordEncoder.PREFIX_DELIMTER, "");
    }
    
    /**
     * @param encPass
     * @return true if this encoder has encoded encPass
     */
    public boolean isResponsibleForEncoding(String encPass) {
        if (encPass==null) return false;
        getDelegate(); // ensure initalization
        return encPass.startsWith(getPrefix()+GeoserverPasswordEncoder.PREFIX_DELIMTER);
    }
    
    
    public String decode(String encPass) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("decoding passwords not supported");
    }
}
