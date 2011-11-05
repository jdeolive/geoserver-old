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
    
    
    protected Set<UserGroupLoadedListener> listeners = 
        Collections.synchronizedSet(new HashSet<UserGroupLoadedListener>());
    protected String passwordEncoderName,passwordValidatorName;
    protected UserGroupStoreHelper helper;
    
    protected AbstractUserGroupService() {
        helper=new UserGroupStoreHelper();
    }

    @Override
    public String getPasswordEncoderName() {
        return passwordEncoderName;
    }
    
    @Override
    public String getPasswordValidatorName() {
        return passwordValidatorName;
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
        return helper.getUserByUsername(username);

    }

    public GeoserverUserGroup getGroupByGroupname(String groupname) throws IOException {
        return  helper.getGroupByGroupname(groupname);
    }
    

    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverUserGroupService#getUsers()
     */
    public SortedSet<GeoserverUser> getUsers() throws IOException{
        return helper.getUsers();
        
    }
    
    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverUserGroupService#getUserGroups()
     */
    public SortedSet<GeoserverUserGroup> getUserGroups() throws IOException{
        return helper.getUserGroups();
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
        return helper.getGroupsForUser(user);
    }
    
    
    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverUserGroupService#getUsersForGroup(org.geoserver.security.impl.GeoserverUserGroup)
     */
    public  SortedSet<GeoserverUser> getUsersForGroup (GeoserverUserGroup group) throws IOException{
        return helper.getUsersForGroup(group);
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
        helper.clearMaps();
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
            user.setAuthorities(calculator.calculateRoles(user));
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
