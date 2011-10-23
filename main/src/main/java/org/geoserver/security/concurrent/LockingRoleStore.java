/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.geoserver.security.concurrent;

import java.io.IOException;

import org.geoserver.security.GeoserverRoleService;
import org.geoserver.security.GeoserverRoleStore;
import org.geoserver.security.impl.GeoserverRole;

/**
 * This is a wrapper class for a {@link GeoserverRoleStore}
 * Thsi wrapper protects internal data structures using read/write locks
 * 
 * @author christian
 *
 */
public class LockingRoleStore extends LockingRoleService implements GeoserverRoleStore{

    /**
     * Constructor for the locking wrapper
     * 
     * @param store
     */
    public LockingRoleStore(GeoserverRoleStore store) {
        super(store);
    }
    
    /**
     * @return the wrapped store
     */
    public GeoserverRoleStore getStore() {
        return (GeoserverRoleStore) super.getService();
    }

    
    /**
     * WRITE_LOCK
     * @see org.geoserver.security.GeoserverRoleStore#addRole(org.geoserver.security.impl.GeoserverRole)
     */
    public void addRole(GeoserverRole role) throws IOException{
        writeLock();
        try {
            getStore().addRole(role);
        } finally {
            writeUnLock();
        }
    }

    /**
     * WRITE_LOCK
     * @see org.geoserver.security.GeoserverRoleStore#updateRole(org.geoserver.security.impl.GeoserverRole)
     */
    public void updateRole(GeoserverRole role) throws IOException{
        writeLock();
        try {
            getStore().updateRole(role);
        } finally {
            writeUnLock();
        }            
    }

    /**
     * WRITE_LOCK
     * @see org.geoserver.security.GeoserverRoleStore#removeRole(org.geoserver.security.impl.GeoserverRole)
     */
    public boolean removeRole(GeoserverRole role) throws IOException{
        writeLock();
        try {
            return getStore().removeRole(role);
        } finally {
            writeUnLock();
        }            
   }

    /**
     * WRITE_LOCK
     * @see org.geoserver.security.GeoserverRoleStore#associateRoleToGroup(org.geoserver.security.impl.GeoserverRole, java.lang.String)
     */
    public void associateRoleToGroup(GeoserverRole role, String groupname) throws IOException{
        writeLock();
        try {
            getStore().associateRoleToGroup(role,groupname);
        } finally {
            writeUnLock();
        }            
    }

    /**
     * WRITE_LOCK
     * @see org.geoserver.security.GeoserverRoleStore#disAssociateRoleFromGroup(org.geoserver.security.impl.GeoserverRole, java.lang.String)
     */
    public void disAssociateRoleFromGroup(GeoserverRole role, String groupname) throws IOException{
        writeLock();
        try {
            getStore().disAssociateRoleFromGroup(role, groupname);
        } finally {
            writeUnLock();
        }            
    }

    /**
     * WRITE_LOCK
     * @see org.geoserver.security.GeoserverRoleStore#associateRoleToUser(org.geoserver.security.impl.GeoserverRole, java.lang.String)
     */
    public void associateRoleToUser(GeoserverRole role, String username) throws IOException{
        writeLock();
        try {
            getStore().associateRoleToUser(role, username);
        } finally {
            writeUnLock();
        }            
    }

    /**
     * WRITE_LOCK
     * @see org.geoserver.security.GeoserverRoleStore#disAssociateRoleFromUser(org.geoserver.security.impl.GeoserverRole, java.lang.String)
     */
    public void disAssociateRoleFromUser(GeoserverRole role, String username) throws IOException{
        writeLock();
        try {
            getStore().disAssociateRoleFromUser(role, username);
        } finally {
            writeUnLock();
        }            
    }

    /**
     * WRITE_LOCK
     * @see org.geoserver.security.GeoserverRoleStore#store()
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
     * READ_LOCK
     * @see org.geoserver.security.GeoserverRoleStore#isModified()
     */
    public boolean isModified() {
        readLock();
        try {
            return getStore().isModified() ;
        } finally {
            readUnLock();
        }
    }

    /**
     * WRITE_LOCK
     * @see org.geoserver.security.GeoserverRoleStore#setParentRole(org.geoserver.security.impl.GeoserverRole, org.geoserver.security.impl.GeoserverRole)
     */
    public void setParentRole(GeoserverRole role, GeoserverRole parentRole) throws IOException{
        writeLock();
        try {
            getStore().setParentRole(role, parentRole);
        } finally {
            writeUnLock();
        }            
    }

    /**
     * WRITE_LOCK
     * @see org.geoserver.security.GeoserverRoleStore#clear()
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
     * @see org.geoserver.security.GeoserverRoleStore#initializeFromService(org.geoserver.security.GeoserverRoleService)
     */
    public void initializeFromService(GeoserverRoleService service) throws IOException{
        writeLock();
        try {
            getStore().initializeFromService(service);
        } finally {
            writeUnLock();
        }            
    }


}
