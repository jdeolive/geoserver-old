/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */


package org.geoserver.security.password;

import java.io.IOException;

import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.security.GeoserverUserGroupService;
import org.geoserver.security.impl.GeoserverUser;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Wrapper class for a {@link GeoserverUserGroupService} object
 * decoding the passwords.
 * 
 * This is needed for some authentication mechanisms, HTTP Digest
 * authentication as an example.
 * 
 * Decoding is only possible for {@link GeoserverUserPasswordEncoder}
 * objects of type {@link PasswordEncodingType#PLAIN} or
 * {@link PasswordEncodingType#ENCRYPT} 
 * 
 * @author christian
 *
 */
public class DecodingUserDetailsService implements UserDetailsService {

    protected GeoserverUserGroupService service;
    protected GeoserverUserPasswordEncoder encoder;
    /**
     * True if passwords can be decoded
     * 
     * @param service
     * @return
     */
    public static boolean canBeUsedFor(GeoserverUserGroupService service) {
        GeoserverUserPasswordEncoder enc = (GeoserverUserPasswordEncoder)
                GeoServerExtensions.bean(service.getPasswordEncoderName());
        return enc.getEncodingType()==PasswordEncodingType.PLAIN ||
               enc.getEncodingType()==PasswordEncodingType.ENCRYPT;
    }
    
    /**
     * Creates a new Instance
     * @param service
     * @return
     * @throws IOException
     */
    public static DecodingUserDetailsService newInstance(GeoserverUserGroupService service) throws IOException {
        if (canBeUsedFor(service)==false)
            throw new IOException("Invalid password encoding type");
        DecodingUserDetailsService decodingService = new DecodingUserDetailsService();
        decodingService.setGeoserverUserGroupService(service);        
        return decodingService;
    }
     
    /**
     * Protected, use {@link #canBeUsedFor(GeoserverUserGroupService)} followed
     * by {@link #newInstance(GeoserverUserGroupService)}
     */
    protected DecodingUserDetailsService() {        
    }
    
    /**
     * sets the wrapped {@link GeoserverUserGroupService} objects
     * and prepares the {@link GeoserverUserPasswordEncoder}
     * 
     * @param service
     * @throws IOException
     */
    public void setGeoserverUserGroupService(GeoserverUserGroupService service) throws IOException {
        this.service=service;
        encoder = (GeoserverUserPasswordEncoder)
                GeoServerExtensions.bean(service.getPasswordEncoderName());
        encoder.initializeFor(service);
    }
    
    /**
     * loads the user and decodes the password to plain text.
     * 
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException,
            DataAccessException {
        GeoserverUser user = (GeoserverUser) service.loadUserByUsername(username);
        if (user==null) return null;
        if (encoder.isResponsibleForEncoding(user.getPassword()))
            user.setPassword(encoder.decode(user.getPassword()));
        return user;
    }

}
