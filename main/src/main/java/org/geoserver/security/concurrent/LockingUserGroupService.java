/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.geoserver.security.concurrent;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

import org.geoserver.security.GeoserverUserGroupService;
import org.geoserver.security.GeoserverUserGroupStore;
import org.geoserver.security.config.SecurityNamedServiceConfig;
import org.geoserver.security.event.UserGroupLoadedEvent;
import org.geoserver.security.event.UserGroupLoadedListener;
import org.geoserver.security.impl.GeoserverUser;
import org.geoserver.security.impl.GeoserverUserGroup;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * This is a wrapper class for a {@link GeoserverUserGroupService}
 * This wrapper protects internal data structures using read/write locks
 * 
 * @author christian
 *
 */
public class LockingUserGroupService extends AbstractLockingService implements
        GeoserverUserGroupService, UserGroupLoadedListener {

    protected Set<UserGroupLoadedListener> listeners = 
        Collections.synchronizedSet(new HashSet<UserGroupLoadedListener>());

    /**
     * Constructor for the locking wrapper
     * 
     * @param service
     */
    public LockingUserGroupService(GeoserverUserGroupService service) {
        super(service);
        service.registerUserGroupLoadedListener(this);
    }
    
    /**
     * @return the wrapped service
     */
    public GeoserverUserGroupService getService() {
        return (GeoserverUserGroupService) super.getService();
    }

    @Override
    public GeoserverUserGroupStore createStore() throws IOException {
        GeoserverUserGroupStore store = getService().createStore();
        return store != null ? new LockingUserGroupStore(store) : null;
    }

    /**
     * READ_LOCK
     * @see org.geoserver.security.GeoserverUserGroupService#getGroupByGroupname(java.lang.String)
     */
    public GeoserverUserGroup getGroupByGroupname(String groupname) throws IOException{
        readLock();
        try {
            return getService().getGroupByGroupname(groupname);
        } finally {
            readUnLock();
        }            
    }


    /**
     * READ_LOCK
     * @see org.geoserver.security.GeoserverUserGroupService#createUserObject(java.lang.String, java.lang.String, boolean)
     */
    public GeoserverUser createUserObject(String username, String password, boolean isEnabled) throws IOException{
        readLock();
        try {
            return getService().createUserObject(username, password, isEnabled);
        } finally {
            readUnLock();
        }            
    }

    /**
     * WRITE_LOCK
     * @see org.geoserver.security.GeoserverUserGroupService#load()
     */
    public void load() throws IOException {
        writeLock();
        try {
            getService().load();
        } finally {
            writeUnLock();
        }            
    }

    /**
     * READ_LOCK
     * @see org.geoserver.security.GeoserverUserGroupService#getUserByUsername(java.lang.String)
     */
    public GeoserverUser getUserByUsername(String username) throws IOException {
        readLock();
        try {
            return getService().getUserByUsername(username);
        } finally {
            readUnLock();
        }            
    }


    /**
     * READ_LOCK
     * @see org.geoserver.security.GeoserverUserGroupService#createGroupObject(java.lang.String, boolean)
     */
    public GeoserverUserGroup createGroupObject(String groupname, boolean isEnabled) throws IOException{
        readLock();
        try {        
            return getService().createGroupObject(groupname, isEnabled);
        } finally {
            readUnLock();
        }            
    }

    /**
     * READ_LOCK
     * @see org.geoserver.security.GeoserverUserGroupService#getUsers()
     */
    public SortedSet<GeoserverUser> getUsers() throws IOException{
        readLock();
        try {
            return getService().getUsers();
        } finally {
            readUnLock();
        }            
    }

    /**
     * READ_LOCK
     * @see org.geoserver.security.GeoserverUserGroupService#getUserGroups()
     */
    public SortedSet<GeoserverUserGroup> getUserGroups() throws IOException{
        readLock();
        try {
            return getService().getUserGroups();
        } finally {
            readUnLock();
        }            
    }

    /**
     * READ_LOCK
     * @see org.geoserver.security.GeoserverUserGroupService#getUsersForGroup(org.geoserver.security.impl.GeoserverUserGroup)
     */
    public SortedSet<GeoserverUser> getUsersForGroup(GeoserverUserGroup group) throws IOException{
        readLock();
        try {
            return getService().getUsersForGroup(group);
        } finally {
            readUnLock();
        }            
    }

    /**
     * READ_LOCK
     * @see org.geoserver.security.GeoserverUserGroupService#getGroupsForUser(org.geoserver.security.impl.GeoserverUser)
     */
    public SortedSet<GeoserverUserGroup> getGroupsForUser(GeoserverUser user) throws IOException{
        readLock();
        try {
            return getService().getGroupsForUser(user);
        } finally {
            readUnLock();
        }            
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
     * NO_LOCK
     * @see org.geoserver.security.GeoserverUserGroupService#registerUserGroupChangedListener(org.geoserver.security.event.UserGroupChangedListener)
     */
    public void registerUserGroupLoadedListener(UserGroupLoadedListener listener) {
        listeners.add(listener);
    }

    /**
     * NO_LOCK
     * @see org.geoserver.security.GeoserverUserGroupService#unregisterUserGroupChangedListener(org.geoserver.security.event.UserGroupChangedListener)
     */
    public void unregisterUserGroupLoadedListener(UserGroupLoadedListener listener) {
        listeners.remove(listener);
    }

    /**
     * NO_LOCK
     * @see org.geoserver.security.event.UserGroupChangedListener#usersAndGroupsChanged(org.geoserver.security.event.UserGroupChangedEvent)
     */
    public void usersAndGroupsChanged(UserGroupLoadedEvent event) {
//        if (rwl.isWriteLockedByCurrentThread())
//            writeUnLock();
//        else
//            readUnLock();
        fireUserGroupLoadedEvent();
    }

    /**
     * WRITE_LOCK
     * @see org.geoserver.security.GeoserverUserGroupService#initializeFromConfig(org.geoserver.security.config.SecurityNamedServiceConfig)
     */
    @Override
    public void initializeFromConfig(SecurityNamedServiceConfig config) throws IOException {
        writeLock();
        try {
            getService().initializeFromConfig(config);
        } finally {
            writeUnLock();
        }
    }

    
    /** 
     * READ_LOCK
     * @see org.springframework.security.core.userdetails.UserDetailsService#loadUserByUsername(java.lang.String)
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException,
            DataAccessException {
        readLock();
        try {
            return getService().loadUserByUsername(username);
        } finally {
            readUnLock();
        }            
    }

    /**
     * NO_LOCK
     */
    @Override
    public String getPasswordEncoderName() {
        return getService().getPasswordEncoderName();
    }
    
    /**
     * NO_LOCK
     */
    @Override
    public String getPasswordValidatorName() {
        return getService().getPasswordValidatorName();
    }


}
