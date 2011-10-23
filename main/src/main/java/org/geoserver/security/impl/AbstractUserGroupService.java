/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.security.impl;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.geoserver.security.GeoserverUserGroupService;
import org.geoserver.security.GeoserverUserGroupStore;
import org.geoserver.security.event.UserGroupLoadedEvent;
import org.geoserver.security.event.UserGroupLoadedListener;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Standard implementation of {@link GeoserverUserGroupService}
 * 
 * @author christian
 *
 */
public abstract class AbstractUserGroupService extends AbstractGeoServerSecurityService
    implements GeoserverUserGroupService {
    
    protected TreeMap<String, GeoserverUser> userMap = new TreeMap<String,GeoserverUser>();
    protected TreeMap<String, GeoserverUserGroup>groupMap = new TreeMap<String,GeoserverUserGroup>();
    protected TreeMap<GeoserverUserGroup, SortedSet<GeoserverUser>>group_userMap =
        new TreeMap<GeoserverUserGroup, SortedSet<GeoserverUser>>();
    protected TreeMap<GeoserverUser, SortedSet<GeoserverUserGroup>> user_groupMap =
        new TreeMap<GeoserverUser, SortedSet<GeoserverUserGroup>>();     
    
    protected Set<UserGroupLoadedListener> listeners = 
        Collections.synchronizedSet(new HashSet<UserGroupLoadedListener>());
    
    protected AbstractUserGroupService() {
    }

    protected AbstractUserGroupService(String name) {
        super(name);
    }

    @Override
    public GeoserverUserGroupStore createStore() throws IOException {
        //return null, subclasses can override if they support a store along with a service
        return null;
    }

    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverUserGroupService#registerUserGroupChangedListener(org.geoserver.security.event.UserGroupChangedListener)
     */
    public void registerUserGroupLoadedListener (UserGroupLoadedListener listener) {
        listeners.add(listener);
    }
    
    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverUserGroupService#unregisterUserGroupChangedListener(org.geoserver.security.event.UserGroupChangedListener)
     */
    public void unregisterUserGroupLoadedListener (UserGroupLoadedListener listener) {
        listeners.remove(listener);
        
    }

    
    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverUserGroupService#getUserByUsername(java.lang.String)
     */
    public GeoserverUser getUserByUsername(String username) throws IOException {
        return  userMap.get(username);

    }

    public GeoserverUserGroup getGroupByGroupname(String groupname) throws IOException {
        return  groupMap.get(groupname);
    }
    

    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverUserGroupService#getUsers()
     */
    public SortedSet<GeoserverUser> getUsers() throws IOException{
        
        SortedSet<GeoserverUser> users = new TreeSet<GeoserverUser>();
        users.addAll(userMap.values());
        return Collections.unmodifiableSortedSet(users);
    }
    
    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverUserGroupService#getUserGroups()
     */
    public SortedSet<GeoserverUserGroup> getUserGroups() throws IOException{
        
        SortedSet<GeoserverUserGroup> groups = new TreeSet<GeoserverUserGroup>();
        groups.addAll(groupMap.values());
        return Collections.unmodifiableSortedSet(groups);
    }

    
    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverUserGroupService#createUserObject(java.lang.String, java.lang.String, boolean)
     */
    public GeoserverUser createUserObject(String username,String password, boolean isEnabled) throws IOException{
       GeoserverUser user = new GeoserverUser(username);
       user.setEnabled(isEnabled);
       user.setPassword(password);
       return user;
    }
    
    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverUserGroupService#createGroupObject(java.lang.String, boolean)
     */
    public GeoserverUserGroup createGroupObject(String groupname, boolean isEnabled) throws IOException{
        GeoserverUserGroup group = new GeoserverUserGroup(groupname);
        group.setEnabled(isEnabled);
        return group;
    }
    
    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverUserGroupService#getGroupsForUser(org.geoserver.security.impl.GeoserverUser)
     */
    public  SortedSet<GeoserverUserGroup> getGroupsForUser (GeoserverUser user) throws IOException{        
        SortedSet<GeoserverUserGroup> groups = user_groupMap.get(user);
        if  (groups==null) 
            groups =  new TreeSet<GeoserverUserGroup>();
        return Collections.unmodifiableSortedSet(groups);
    }
    
    
    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverUserGroupService#getUsersForGroup(org.geoserver.security.impl.GeoserverUserGroup)
     */
    public  SortedSet<GeoserverUser> getUsersForGroup (GeoserverUserGroup group) throws IOException{
        SortedSet<GeoserverUser> users = group_userMap.get(group);
        if  (users==null) 
            users= new TreeSet<GeoserverUser>();
        return Collections.unmodifiableSortedSet(users);
    }

    protected void checkUser(GeoserverUser user) throws IOException{
        if (userMap.containsKey(user.getUsername())==false)
            throw new IOException("User: " +  user.getUsername()+ " does not exist");
    }
    
    protected void checkGroup(GeoserverUserGroup group) throws IOException{
        if (groupMap.containsKey(group.getGroupname())==false)
            throw new IOException("Group: " +  group.getGroupname()+ " does not exist");
    }

    /**
     * Subclasses must implement this method 
     * Load user groups  from backend
     */
    protected abstract void deserialize() throws IOException;

    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverUserGroupService#load()
     */
    public void load() throws IOException{
        LOGGER.info("Start reloading user/grous for service named "+getName());
        // prevent concurrent write from store and
        // read from service
        synchronized (this) { 
            deserialize();
        }
        LOGGER.info("Reloading user/groups successful for service named "+getName());
        fireUserGroupLoadedEvent();

    }

    /**
     * Fire {@link UserGroupLoadedEvent} for all listeners
     */
    protected void fireUserGroupLoadedEvent() {
        UserGroupLoadedEvent event = new UserGroupLoadedEvent(this);
        for (UserGroupLoadedListener listener : listeners) {
            listener.usersAndGroupsChanged(event);
        }
    }
    
    /**
     * internal use, clear the maps
     */
    protected void clearMaps() {
        userMap.clear();
        groupMap.clear();
        user_groupMap.clear();
        group_userMap.clear();
    }

    /**
     * The root configuration for the user group service.
     */
    public File getConfigRoot() throws IOException {
        return new File(getSecurityManager().getUserGroupRoot(), getName());
    }

    /* (non-Javadoc)
     * @see org.springframework.security.core.userdetails.UserDetailsService#loadUserByUsername(java.lang.String)
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException,
            DataAccessException {
        GeoserverUser user=null;
        try {
            user = getUserByUsername(username);            
            if (user==null)
                throw new UsernameNotFoundException(userNotFoundMessage(username));
            RoleCalculator calculator = new RoleCalculator(this, 
                    getSecurityManager().getActiveRoleService());
            user.setAuthorities(calculator.calculateGrantedAuthorities(user));
        } catch (IOException e) {
            throw new UsernameNotFoundException(userNotFoundMessage(username),e);
        }        
        
        return user;
    }
    
    protected String userNotFoundMessage(String username) {
        return "User  "+username + 
                " not found in usergroupservice: "+getName();
    }
}
