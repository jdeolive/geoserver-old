/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.geoserver.security.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeMap;

/**
 * Implementation for testing
 * uses serialization into a byte array
 * 
 * @author christian
 *
 */
public class MemoryGrantedAuthorityStore extends AbstractGrantedAuthorityStore {
    
    
    
    public MemoryGrantedAuthorityStore(String name) {
        super(name);        
    }


    @Override
    protected void serialize() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream oout = new ObjectOutputStream(out);
        oout.writeObject(roleMap);
        oout.writeObject(role_parentMap);
        oout.writeObject(user_roleMap);
        oout.writeObject(group_roleMap);
        ((MemoryGrantedAuthorityService)service).byteArray=out.toByteArray();
        oout.close();            
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void deserialize() throws IOException {
        clearMaps();
        byte[] bytes = ((MemoryGrantedAuthorityService) service).byteArray;
        if (bytes==null) {
            setModified(false);
            return;
        }
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        ObjectInputStream oin = new ObjectInputStream(in);
        try {
            roleMap = (TreeMap<String,GeoserverGrantedAuthority>) oin.readObject();
            role_parentMap =(HashMap<GeoserverGrantedAuthority,GeoserverGrantedAuthority>) oin.readObject();
            user_roleMap = (TreeMap<String,SortedSet<GeoserverGrantedAuthority>>)oin.readObject();
            group_roleMap = (TreeMap<String,SortedSet<GeoserverGrantedAuthority>>)oin.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
        setModified(false);
    }
    
    @Override
    public GeoserverGrantedAuthority createGrantedAuthorityObject(String role)
            throws IOException {
        return new MemoryGeoserverGrantedAuthority(role);
    }


}
