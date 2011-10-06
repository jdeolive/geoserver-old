/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.geoserver.security.concurrent;

import java.io.IOException;

import org.geoserver.security.GeoserverGrantedAuthorityService;
import org.geoserver.security.GeoserverGrantedAuthorityStore;
import org.geoserver.security.impl.GeoserverGrantedAuthority;

/**
 * This is a wrapper class for a {@link GeoserverGrantedAuthorityStore}
 * Thsi wrapper protects internal data structures using read/write locks
 * 
 * @author christian
 *
 */
public class LockingGrantedAuthorityStore extends LockingGrantedAuthorityService implements GeoserverGrantedAuthorityStore{

    /**
     * Constructor for the locking wrapper
     * 
     * @param store
     */
    public LockingGrantedAuthorityStore(GeoserverGrantedAuthorityStore store) {
        super(store);
    }
    
    /**
     * @return the wrapped store
     */
    public GeoserverGrantedAuthorityStore getStore() {
        return (GeoserverGrantedAuthorityStore) super.getService();
    }

    
    /**
     * WRITE_LOCK
     * @see org.geoserver.security.GeoserverGrantedAuthorityStore#addGrantedAuthority(org.geoserver.security.impl.GeoserverGrantedAuthority)
     */
    public void addGrantedAuthority(GeoserverGrantedAuthority role) throws IOException{
        writeLock();
        try {
            getStore().addGrantedAuthority(role);
        } finally {
            writeUnLock();
        }
    }

    /**
     * WRITE_LOCK
     * @see org.geoserver.security.GeoserverGrantedAuthorityStore#updateGrantedAuthority(org.geoserver.security.impl.GeoserverGrantedAuthority)
     */
    public void updateGrantedAuthority(GeoserverGrantedAuthority role) throws IOException{
        writeLock();
        try {
            getStore().updateGrantedAuthority(role);
        } finally {
            writeUnLock();
        }            
    }

    /**
     * WRITE_LOCK
     * @see org.geoserver.security.GeoserverGrantedAuthorityStore#removeGrantedAuthority(org.geoserver.security.impl.GeoserverGrantedAuthority)
     */
    public boolean removeGrantedAuthority(GeoserverGrantedAuthority role) throws IOException{
        writeLock();
        try {
            return getStore().removeGrantedAuthority(role);
        } finally {
            writeUnLock();
        }            
   }

    /**
     * WRITE_LOCK
     * @see org.geoserver.security.GeoserverGrantedAuthorityStore#associateRoleToGroup(org.geoserver.security.impl.GeoserverGrantedAuthority, java.lang.String)
     */
    public void associateRoleToGroup(GeoserverGrantedAuthority role, String groupname) throws IOException{
        writeLock();
        try {
            getStore().associateRoleToGroup(role,groupname);
        } finally {
            writeUnLock();
        }            
    }

    /**
     * WRITE_LOCK
     * @see org.geoserver.security.GeoserverGrantedAuthorityStore#disAssociateRoleFromGroup(org.geoserver.security.impl.GeoserverGrantedAuthority, java.lang.String)
     */
    public void disAssociateRoleFromGroup(GeoserverGrantedAuthority role, String groupname) throws IOException{
        writeLock();
        try {
            getStore().disAssociateRoleFromGroup(role, groupname);
        } finally {
            writeUnLock();
        }            
    }

    /**
     * WRITE_LOCK
     * @see org.geoserver.security.GeoserverGrantedAuthorityStore#associateRoleToUser(org.geoserver.security.impl.GeoserverGrantedAuthority, java.lang.String)
     */
    public void associateRoleToUser(GeoserverGrantedAuthority role, String username) throws IOException{
        writeLock();
        try {
            getStore().associateRoleToUser(role, username);
        } finally {
            writeUnLock();
        }            
    }

    /**
     * WRITE_LOCK
     * @see org.geoserver.security.GeoserverGrantedAuthorityStore#disAssociateRoleFromUser(org.geoserver.security.impl.GeoserverGrantedAuthority, java.lang.String)
     */
    public void disAssociateRoleFromUser(GeoserverGrantedAuthority role, String username) throws IOException{
        writeLock();
        try {
            getStore().disAssociateRoleFromUser(role, username);
        } finally {
            writeUnLock();
        }            
    }

    /**
     * WRITE_LOCK
     * @see org.geoserver.security.GeoserverGrantedAuthorityStore#store()
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
     * @see org.geoserver.security.GeoserverGrantedAuthorityStore#isModified()
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
     * @see org.geoserver.security.GeoserverGrantedAuthorityStore#setParentRole(org.geoserver.security.impl.GeoserverGrantedAuthority, org.geoserver.security.impl.GeoserverGrantedAuthority)
     */
    public void setParentRole(GeoserverGrantedAuthority role, GeoserverGrantedAuthority parentRole) throws IOException{
        writeLock();
        try {
            getStore().setParentRole(role, parentRole);
        } finally {
            writeUnLock();
        }            
    }

    /**
     * WRITE_LOCK
     * @see org.geoserver.security.GeoserverGrantedAuthorityStore#clear()
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
     * @see org.geoserver.security.GeoserverGrantedAuthorityStore#initializeFromService(org.geoserver.security.GeoserverGrantedAuthorityService)
     */
    public void initializeFromService(GeoserverGrantedAuthorityService service) throws IOException{
        writeLock();
        try {
            getStore().initializeFromService(service);
        } finally {
            writeUnLock();
        }            
    }


}
