/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.security;

import java.io.IOException;

import org.geoserver.security.impl.GeoserverUser;
import org.geoserver.security.impl.GeoserverUserGroup;

/**
 * A class implementing this interface implements a backend for
 * user and group management. The store always operates on a
 * {@link GeoserverUserGroupService} object.
 * 
 * @author christian
 *
 */
public interface GeoserverUserGroupStore extends GeoserverUserGroupService {


    
    /**
     * Initializes itself from a service for future 
     * store modifications concerning this service 
     * 
     * @param service
     */
    public void initializeFromService(GeoserverUserGroupService service) throws IOException;

    /**
     * discards all entries
     * 
     * @throws IOException
     */
    public abstract void clear() throws IOException;

    
    /**
     * Adds a user 
     * @param user
     */
    public abstract void addUser(GeoserverUser user)  throws IOException;

    /**
     * Updates a user 
     * @param user
     */
    public abstract void updateUser(GeoserverUser user)  throws IOException;

    /**
     * Removes the specified user 
     * @param user
     * @return
     */
    public abstract boolean removeUser(GeoserverUser user)  throws IOException;
    
    /**
     * Adds a group 
     * @param group
     */
    public abstract void addGroup(GeoserverUserGroup group)  throws IOException;

    /**
     * Updates a group 
     * @param group
     */
    public abstract void updateGroup(GeoserverUserGroup group)  throws IOException;

    /**
     * Removes the specified group. 
     * 
     * @param group
     * @return
     */
    public abstract boolean removeGroup(GeoserverUserGroup group)  throws IOException;


    /**
     * Synchronizes all changes with the backend store.On success, 
     * the associated {@link GeoserverUserGroupService} object should
     * be loaded
     */
    public abstract void store() throws IOException;

    /**
     * Associates a user with a group, on success
     * 
     * @param user
     * @param group
     */
    public void associateUserToGroup(GeoserverUser user, GeoserverUserGroup group)  throws IOException;
    
    /**
     * Disassociates a user from a group, on success
     * 
     * 
     * @param user
     * @param group
     */
    public void disAssociateUserFromGroup(GeoserverUser user, GeoserverUserGroup group)  throws IOException;

                            
    /**
     * returns true if there are pending modifications
     * not written to the backend store
     * 
     * @return true/false
     */
    public boolean isModified();
 
}