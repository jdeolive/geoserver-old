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

import org.geoserver.security.GeoserverRoleStore;
import org.geoserver.security.config.SecurityNamedServiceConfig;


/**
 * 
 *  Implementation for testing
 *  uses serialization into a byte array
 * 
 * @author christian
 *
 */
public class MemoryRoleService extends AbstractRoleService {

    byte[] byteArray;


    @Override
    public boolean canCreateStore() {
        return true;
    }

    @Override
    public GeoserverRoleStore createStore() throws IOException {
        MemoryRoleStore store = new MemoryRoleStore();
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
            roleMap = (TreeMap<String,GeoserverRole>) oin.readObject();
            role_parentMap =(HashMap<GeoserverRole,GeoserverRole>) oin.readObject();
            user_roleMap = (TreeMap<String,SortedSet<GeoserverRole>>)oin.readObject();
            group_roleMap = (TreeMap<String,SortedSet<GeoserverRole>>)oin.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        }            
    }

    @Override
    public GeoserverRole createRoleObject(String role)
            throws IOException {
        return new MemoryGeoserverRole(role);
    }

    @Override
    public void initializeFromConfig(SecurityNamedServiceConfig config) throws IOException {
    }

}
