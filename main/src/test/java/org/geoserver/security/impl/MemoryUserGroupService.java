/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.geoserver.security.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.SortedSet;
import java.util.TreeMap;

import org.geoserver.security.GeoserverUserGroupStore;
import org.geoserver.security.config.SecurityNamedServiceConfig;


/**
 * 
 *  Implementation for testing
 *  uses serialization into a byte array
 * 
 * @author christian
 *
 */
public class MemoryUserGroupService extends AbstractUserGroupService {

    byte[] byteArray;
    
    public MemoryUserGroupService() {
    }

    @Override
    public boolean canCreateStore() {
        return true;
    }

    @Override
    public GeoserverUserGroupStore createStore() throws IOException {
        MemoryUserGroupStore store = new MemoryUserGroupStore();
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
            userMap = (TreeMap<String,GeoserverUser>) oin.readObject();
            groupMap =(TreeMap<String,GeoserverUserGroup>) oin.readObject();
            user_groupMap = (TreeMap<GeoserverUser,SortedSet<GeoserverUserGroup>>)oin.readObject();
            group_userMap = (TreeMap<GeoserverUserGroup,SortedSet<GeoserverUser>>)oin.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
    }

    @Override
    public GeoserverUser createUserObject(String username,String password, boolean isEnabled) throws IOException{
        GeoserverUser user = new MemoryGeoserverUser(username, this);
        user.setEnabled(isEnabled);
        user.setPassword(password);
        return user;
     }
     
    @Override
     public GeoserverUserGroup createGroupObject(String groupname, boolean isEnabled) throws IOException{
         GeoserverUserGroup group = new MemoryGeoserverUserGroup(groupname);
         group.setEnabled(isEnabled);
         return group;
     }

    @Override
    public void initializeFromConfig(SecurityNamedServiceConfig config) throws IOException {
        this.name=config.getName();
    }

}
