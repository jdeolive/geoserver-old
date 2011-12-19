/* Copyright (c) 2001 - 2008 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.security.validation;

import java.io.IOException;

import org.geoserver.security.GeoserverUserGroupService;
import org.geoserver.security.GeoserverUserGroupStore;
import org.geoserver.security.impl.GeoserverUser;
import org.geoserver.security.impl.GeoserverUserGroup;
import org.geoserver.security.password.PasswordValidationException;



/**
 * 
 * This class is a validation wrapper for {@link GeoserverUserGroupStore}
 * 
 * Usage:
 * <code>
 * GeoserverUserGroupStore valStore = new UserGroupStoreValidationWrapper(store);
 * valStore.addUser(..);
 * valStore.store()
 * </code>
 * 
 * Since the {@link GeoserverUserGroupStore} interface does not allow to 
 * throw {@link UserGroupServiceException} objects directly, these objects
 * a wrapped into an IOException. Use {@link IOException#getCause()} to
 * get the proper exception.
 * 
 * 
 * @author christian
 *
 */


public class UserGroupStoreValidationWrapper extends UserGroupServiceValidationWrapper implements GeoserverUserGroupStore{

   

    /**
     * Creates a wrapper object. 
     * 
     * @param store
     * 
     */    
    public UserGroupStoreValidationWrapper(GeoserverUserGroupStore store) {
        super(store);
    }

    GeoserverUserGroupStore getStore() {
        return (GeoserverUserGroupStore) service;
    }
    
    public void initializeFromService(GeoserverUserGroupService service) throws IOException {
        getStore().initializeFromService(service);
    }

    public void clear() throws IOException {
        getStore().clear();
    }



    public void addUser(GeoserverUser user) throws IOException, PasswordValidationException {
        checkNotExistingUserName(user.getUsername());
        getStore().addUser(user);
    }
     
    public void updateUser(GeoserverUser user) throws IOException, PasswordValidationException {
        checkExistingUserName(user.getUsername());
        getStore().updateUser(user);
    }

    public boolean removeUser(GeoserverUser user) throws IOException {
        return getStore().removeUser(user);
    }

    public void addGroup(GeoserverUserGroup group) throws IOException {
        checkNotExistingGroupName(group.getGroupname());
        getStore().addGroup(group);
    }


    public void updateGroup(GeoserverUserGroup group) throws IOException {
        checkExistingGroupName(group.getGroupname());
        getStore().updateGroup(group);
    }


    public boolean removeGroup(GeoserverUserGroup group) throws IOException {
        return getStore().removeGroup(group);
    }

    public void store() throws IOException {
        getStore().store();
    }



    public void associateUserToGroup(GeoserverUser user, GeoserverUserGroup group)
            throws IOException {
        checkExistingUserName(user.getUsername());
        checkExistingGroupName(group.getGroupname());
        getStore().associateUserToGroup(user, group);
    }

    public void disAssociateUserFromGroup(GeoserverUser user, GeoserverUserGroup group)
            throws IOException {
        checkExistingUserName(user.getUsername());
        checkExistingGroupName(group.getGroupname());
        getStore().disAssociateUserFromGroup(user, group);
    }



    public boolean isModified() {
        return getStore().isModified();
    }


}
