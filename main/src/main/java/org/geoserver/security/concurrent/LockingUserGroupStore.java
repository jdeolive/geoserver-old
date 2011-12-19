/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.geoserver.security.concurrent;

import java.io.IOException;

import org.geoserver.security.GeoserverUserGroupService;
import org.geoserver.security.GeoserverUserGroupStore;
import org.geoserver.security.impl.GeoserverUser;
import org.geoserver.security.impl.GeoserverUserGroup;
import org.geoserver.security.validation.PasswordValidationException;

/**
 * This is a wrapper class for a {@link GeoserverUserGroupStore}
 * protected internal data structures using read/write locks
 * 
 * @author christian
 *
 */
public class LockingUserGroupStore extends LockingUserGroupService implements GeoserverUserGroupStore{

    /**
     * Constructor for the locking wrapper
     * 
     * @param store
     */
    public LockingUserGroupStore(GeoserverUserGroupStore store) {
        super(store);
    }
    
    /**
     * @return the wrapped store
     */
    public GeoserverUserGroupStore getStore() {
        return (GeoserverUserGroupStore) super.getService();
    }

    /**
     * WRITE_LOCK
     * @see org.geoserver.security.GeoserverUserGroupStore#addUser(org.geoserver.security.impl.GeoserverUser)
     */
    public void addUser(GeoserverUser user) throws IOException{
        writeLock();
        try {
            getStore().addUser(user);
        } finally {
            writeUnLock();
        }
    }

    /**
     * WRITE_LOCK
     * @see org.geoserver.security.GeoserverUserGroupStore#updateUser(org.geoserver.security.impl.GeoserverUser)
     */
    public void updateUser(GeoserverUser user) throws IOException{
        writeLock();
        try {
            getStore().updateUser(user);
        } finally {
            writeUnLock();
        }            
    }

    /**
     * WRITE_LOCK
     * @see org.geoserver.security.GeoserverUserGroupStore#removeUser(org.geoserver.security.impl.GeoserverUser)
     */
    public boolean removeUser(GeoserverUser user) throws IOException{
        writeLock();
        try {
            return getStore().removeUser(user);
        } finally {
            writeUnLock();
        }
            
    }

    /**
     * WRITE_LOCK
     * @see org.geoserver.security.GeoserverUserGroupStore#addGroup(org.geoserver.security.impl.GeoserverUserGroup)
     */
    public void addGroup(GeoserverUserGroup group) throws IOException{
        writeLock();
        try {
            getStore().addGroup(group);
        } finally {
            writeUnLock();
        }
            
    }

    /**
     * WRITE_LOCK
     * @see org.geoserver.security.GeoserverUserGroupStore#updateGroup(org.geoserver.security.impl.GeoserverUserGroup)
     */
    public void updateGroup(GeoserverUserGroup group) throws IOException{
        writeLock();
        try {
            getStore().updateGroup(group);
        } finally {
            writeUnLock();
        }
            
    }

    /**
     * WRITE_LOCK
     * @see org.geoserver.security.GeoserverUserGroupStore#removeGroup(org.geoserver.security.impl.GeoserverUserGroup)
     */
    public boolean removeGroup(GeoserverUserGroup group) throws IOException{
        writeLock();
        try {
            return getStore().removeGroup(group);
        } finally {
            writeUnLock();
        }
            
    }

    /**
     * WRITE_LOCK
     * @see org.geoserver.security.GeoserverUserGroupStore#store()
     */
    public void store() throws IOException {
        writeLock();
        try {
            getStore().store();
        } finally {
            writeUnLock();
        }            
    }

    /**
     * WRITE_LOCK
     * @see org.geoserver.security.GeoserverUserGroupStore#associateUserToGroup(org.geoserver.security.impl.GeoserverUser, org.geoserver.security.impl.GeoserverUserGroup)
     */
    public void associateUserToGroup(GeoserverUser user, GeoserverUserGroup group) throws IOException{
        writeLock();
        try {
            getStore().associateUserToGroup(user, group);
        } finally {
            writeUnLock();
        }            
    }

    /**
     * WRITE_LOCK
     * @see org.geoserver.security.GeoserverUserGroupStore#disAssociateUserFromGroup(org.geoserver.security.impl.GeoserverUser, org.geoserver.security.impl.GeoserverUserGroup)
     */
    public void disAssociateUserFromGroup(GeoserverUser user, GeoserverUserGroup group) throws IOException{
        writeLock();
        try {
            getStore().disAssociateUserFromGroup(user, group);
        } finally {
            writeUnLock();
        }            
    }

    /**
     * READ_LOCK
     * @see org.geoserver.security.GeoserverUserGroupStore#isModified()
     */
    public boolean isModified() {
        readLock();
        try {
            return getStore().isModified();
        } finally {
            readUnLock();
        }
    }
    /**
     * WRITE_LOCK
     * @see org.geoserver.security.GeoserverUserGroupStore#clear()
     */
    public void clear() throws IOException {
        writeLock();
        try {
            getStore().clear();
        } finally {
            writeUnLock();
        }            
    }

    /**
     * WRITE_LOCK
     * @see org.geoserver.security.GeoserverUserGroupStore#initializeFromService(org.geoserver.security.GeoserverUserGroupService)
     */
    public void initializeFromService(GeoserverUserGroupService service) throws IOException {
        writeLock();
        try {
            getStore().initializeFromService(service);
        } finally {
            writeUnLock();
        }            
    }

    

}
