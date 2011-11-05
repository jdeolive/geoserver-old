/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.geoserver.security.impl;

import java.io.IOException;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * This class is common helper for
 * {@link AbstractUserGroupService} and {@link AbstractUserGroupStore} 
 * to avoid code duplication
 * 
 * @author christian
 *
 */
public class UserGroupStoreHelper{
    public TreeMap<String, GeoserverUser> userMap = new TreeMap<String,GeoserverUser>();
    public TreeMap<String, GeoserverUserGroup>groupMap = new TreeMap<String,GeoserverUserGroup>();
    public TreeMap<GeoserverUserGroup, SortedSet<GeoserverUser>>group_userMap =
        new TreeMap<GeoserverUserGroup, SortedSet<GeoserverUser>>();
    public TreeMap<GeoserverUser, SortedSet<GeoserverUserGroup>> user_groupMap =
        new TreeMap<GeoserverUser, SortedSet<GeoserverUserGroup>>();     

    
    public GeoserverUser getUserByUsername(String username) throws IOException {
        return  userMap.get(username);

    }

    public GeoserverUserGroup getGroupByGroupname(String groupname) throws IOException {
        return  groupMap.get(groupname);
    }


    public SortedSet<GeoserverUser> getUsers() throws IOException{
        
        SortedSet<GeoserverUser> users = new TreeSet<GeoserverUser>();
        users.addAll(userMap.values());
        return Collections.unmodifiableSortedSet(users);
    }
    
    public SortedSet<GeoserverUserGroup> getUserGroups() throws IOException{
        
        SortedSet<GeoserverUserGroup> groups = new TreeSet<GeoserverUserGroup>();
        groups.addAll(groupMap.values());
        return Collections.unmodifiableSortedSet(groups);
    }
    
    public  SortedSet<GeoserverUserGroup> getGroupsForUser (GeoserverUser user) throws IOException{        
        SortedSet<GeoserverUserGroup> groups = user_groupMap.get(user);
        if  (groups==null) 
            groups =  new TreeSet<GeoserverUserGroup>();
        return Collections.unmodifiableSortedSet(groups);
    }
    
    
    public  SortedSet<GeoserverUser> getUsersForGroup (GeoserverUserGroup group) throws IOException{
        SortedSet<GeoserverUser> users = group_userMap.get(group);
        if  (users==null) 
            users= new TreeSet<GeoserverUser>();
        return Collections.unmodifiableSortedSet(users);
    }
    
    public void clearMaps() {
        userMap.clear();
        groupMap.clear();
        user_groupMap.clear();
        group_userMap.clear();
    }



}
