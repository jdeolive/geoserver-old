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
import java.util.SortedSet;
import java.util.TreeMap;


/**
 * Implementation for testing
 * uses serialization into a byte array
 * 
 * @author christian
 *
 */
public class MemoryUserGroupStore extends AbstractUserGroupStore {
    
    
    public MemoryUserGroupStore(String name) {
        super(name);        
    }


    @Override
    protected void serialize() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream oout = new ObjectOutputStream(out);
        oout.writeObject(userMap);
        oout.writeObject(groupMap);
        oout.writeObject(user_groupMap);
        oout.writeObject(group_userMap);
        ((MemoryUserGroupService) service).byteArray=out.toByteArray();
        oout.close();            
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void deserialize() throws IOException {
        clearMaps();
        byte[] bytes = ((MemoryUserGroupService) service).byteArray;
        if (bytes==null) {
            setModified(false);
            return;
        }
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);                
        ObjectInputStream oin = new ObjectInputStream(in);
        try {
            userMap = (TreeMap<String,GeoserverUser>) oin.readObject();
            groupMap =(TreeMap<String,GeoserverUserGroup>) oin.readObject();
            user_groupMap = (TreeMap<GeoserverUser,SortedSet<GeoserverUserGroup>>)oin.readObject();
            group_userMap = (TreeMap<GeoserverUserGroup,SortedSet<GeoserverUser>>)oin.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
        setModified(false);
    }
    

    @Override
    public GeoserverUser createUserObject(String username,String password, boolean isEnabled) throws IOException{
        GeoserverUser user = new MemoryGeoserverUser(username);
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

    

}
