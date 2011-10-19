/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.geoserver.security.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeMap;

import org.geoserver.security.GeoserverGrantedAuthorityStore;
import org.geoserver.security.config.SecurityNamedServiceConfig;


/**
 * 
 *  Implementation for testing
 *  uses serialization into a byte array
 * 
 * @author christian
 *
 */
public class MemoryGrantedAuthorityService extends AbstractGrantedAuthorityService {

    byte[] byteArray;

    public MemoryGrantedAuthorityService(String name) {
        super(name);
        
    }

    @Override
    public boolean canCreateStore() {
        return true;
    }

    @Override
    public GeoserverGrantedAuthorityStore createStore() throws IOException {
        MemoryGrantedAuthorityStore store = new MemoryGrantedAuthorityStore(getName());
        store.initializeFromService(this);
        return store;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected void deserialize() throws IOException {
        clearMaps();
        if (byteArray==null) return;
        ByteArrayInputStream in = new ByteArrayInputStream(byteArray);
        ObjectInputStream oin = new ObjectInputStream(in);
        try {
            roleMap = (TreeMap<String,GeoserverGrantedAuthority>) oin.readObject();
            role_parentMap =(HashMap<GeoserverGrantedAuthority,GeoserverGrantedAuthority>) oin.readObject();
            user_roleMap = (TreeMap<String,SortedSet<GeoserverGrantedAuthority>>)oin.readObject();
            group_roleMap = (TreeMap<String,SortedSet<GeoserverGrantedAuthority>>)oin.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        }            
    }

    @Override
    public GeoserverGrantedAuthority createGrantedAuthorityObject(String role)
            throws IOException {
        return new MemoryGeoserverGrantedAuthority(role);
    }

    @Override
    public void initializeFromConfig(SecurityNamedServiceConfig config) throws IOException {
    }

}
