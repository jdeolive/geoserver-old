/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.security;

import java.io.IOException;
import java.util.SortedSet;

import org.geoserver.security.config.SecurityNamedServiceConfig;
import org.geoserver.security.event.UserGroupLoadedEvent;
import org.geoserver.security.event.UserGroupLoadedListener;
import org.geoserver.security.impl.GeoserverUser;
import org.geoserver.security.impl.GeoserverUserGroup;
import org.springframework.dao.DataAccessException;

/**
 * This interface is an extenstion to {@link UserDetailsService}
 * 
 * A class implementing this interface implements a read only backend for
 * user and group management
 * 
 * @author christian
 *
 */
public interface GeoserverUserGroupService  {
    
    
    /**
     * Initialize from config object
     * 
     * @param config
     * @throws IOException
     */
    public void initializeFromConfig(SecurityNamedServiceConfig config) throws IOException;
    
    /**
     * @return the name of this service
     */
    String getName();
    
    
    /**
     * Register for notifications on load
     * 
     * @param listener
     */
    void registerUserGroupLoadedListener (UserGroupLoadedListener listener);
    
    /**
     * Unregister for notifications on store/load
     * 
     * @param listener
     */
    void unregisterUserGroupLoadedListener (UserGroupLoadedListener listener);


    /**
     * Returns the the group object, null if not found
     * 
     * @param groupname
     * @return null if group not found
     * @throws DataAccessException
     */
    public GeoserverUserGroup getGroupByGroupname(String groupname) throws IOException;
    
    /**
     * Returns the the user object, null if not found
     * 
     * @param username
     * @return null if user not found
     * @throws DataAccessException
     */
    public GeoserverUser getUserByUsername(String username) throws IOException;

   

    /**
     * Create a user object. Implementations can use subclasses of {@link GeoserverUser}
     * 
     * @param username
     * @param password
     * @param isEnabled
     * @return
     */
    public GeoserverUser createUserObject(String username,String password, boolean isEnabled)  throws IOException;
    
    /**
     * Create a user object. Implementations can use classes implementing  {@link GeoserverUserGroup}
     * 
     * @param groupname
     * @param password
     * @param isEnabled
     * @return
     */
    public GeoserverUserGroup createGroupObject(String groupname, boolean isEnabled)  throws IOException;
    
    /**
     * Returns the list of users. 
     * 
     * @return a collection which cannot be modified
     */
    public abstract SortedSet<GeoserverUser> getUsers()  throws IOException;
    
    /**
     * Returns the list of GeoserverUserGroups. 
     * 
     * @return a collection which cannot be modified
     */
    public abstract SortedSet<GeoserverUserGroup> getUserGroups()  throws IOException;

          
    
    /**
     * get users for a group
     * 
     * @param group
     * @return a collection which cannot be modified
     */
    public  SortedSet<GeoserverUser> getUsersForGroup (GeoserverUserGroup group)  throws IOException;
    
    /**
     * get the groups for a user, an implementation not 
     * supporting user groups returns an empty collection
     * 
     * @param user
     * @return a collection which cannot be modified
     */
    public  SortedSet<GeoserverUserGroup> getGroupsForUser (GeoserverUser user)  throws IOException;

                
    /**
     * load from backendstore. On success,
     * a  {@link UserGroupLoadedEvent} should  be triggered 
     */
    public abstract void load() throws IOException;

}