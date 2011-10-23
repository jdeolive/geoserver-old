/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.geoserver.security.concurrent;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;

import javax.management.relation.RoleStatus;


import org.geoserver.security.GeoserverRoleService;
import org.geoserver.security.GeoserverRoleStore;
import org.geoserver.security.config.SecurityNamedServiceConfig;
import org.geoserver.security.event.RoleLoadedEvent;
import org.geoserver.security.event.RoleLoadedListener;
import org.geoserver.security.impl.GeoserverRole;

/**
 * This is a wrapper class for a {@link GeoserverRoleService}.
 * This wrapper protects internal data structures using read/write locks
 * 
 * @author christian
 *
 */
public class LockingRoleService extends AbstractLockingService implements
        GeoserverRoleService,RoleLoadedListener {

    protected Set<RoleLoadedListener> listeners = 
        Collections.synchronizedSet(new HashSet<RoleLoadedListener>());

    /**
     * Constructor for the locking wrapper
     * 
     * @param service
     */
    public LockingRoleService(GeoserverRoleService service) {
        super(service);
        service.registerRoleLoadedListener(this);
    }

    /**
     * @return the wrapped service
     */
    public GeoserverRoleService getService() {
        return (GeoserverRoleService) super.getService();
    }

    @Override
    public GeoserverRoleStore createStore() throws IOException {
        GeoserverRoleStore store = getService().createStore();
        return store != null ? new LockingRoleStore(store) : null;
    }

    /** 
     * WRITE_LOCK
     * @see org.geoserver.security.GeoserverRoleService#load()
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
     * @see org.geoserver.security.GeoserverRoleService#getRolesForUser(java.lang.String)
     */
    public SortedSet<GeoserverRole> getRolesForUser(String username) throws IOException {
        readLock();
        try {
            return getService().getRolesForUser(username);
        } finally {
            readUnLock();
        }            
    }

    /**
     * READ_LOCK
     * @see org.geoserver.security.GeoserverRoleService#getRolesForGroup(java.lang.String)
     */
    public SortedSet<GeoserverRole> getRolesForGroup(String groupname) throws IOException{
        readLock();
        try {
            return getService().getRolesForGroup(groupname) ;
        } finally {
            readUnLock();
        }
            
    }

    /**
     * READ_LOCK
     * @see org.geoserver.security.GeoserverRoleService#getRoles()
     */
    public SortedSet<GeoserverRole> getRoles() throws IOException{
        readLock();
        try {
            return getService().getRoles();
        } finally {
            readUnLock();
        }            
    }

    /**
     * READ_LOCK
     * @see org.geoserver.security.GeoserverRoleService#createRoleObject(java.lang.String)
     */
    public GeoserverRole createRoleObject(String role) throws IOException{
        readLock();
        try {
            return getService().createRoleObject(role);
        } finally {
            readUnLock();
        }            
    }

    /**
     * READ_LOCK
     * @see org.geoserver.security.GeoserverRoleService#getParentRole(org.geoserver.security.impl.GeoserverRole)
     */
    public GeoserverRole getParentRole(GeoserverRole role) throws IOException{
        readLock();
        try {
            return getService().getParentRole(role);
        } finally {
            readUnLock();
        }            
    }

    /**
     * READ_LOCK
     * @see org.geoserver.security.GeoserverRoleService#getRoleByName(java.lang.String)
     */
    public GeoserverRole getRoleByName(String role) throws IOException {
        readLock();
        try {
            return getService().getRoleByName(role);
        } finally {
            readUnLock();
        }                    
    }

    
    /**
     * Fire {@link RoleLoadedEvent} for all listeners 
     */
    protected void fireRoleChangedEvent() {        
        RoleLoadedEvent event = new RoleLoadedEvent(this);
        for (RoleLoadedListener listener : listeners) {
            listener.rolesChanged(event);
        }
        
    }

    /**
     * NO_LOCK
     * @see org.geoserver.security.GeoserverRoleService#registerRoleLoadedListener(org.geoserver.security.event.RoleLoadedListener)
     */
    public void registerRoleLoadedListener(RoleLoadedListener listener) {
        listeners.add(listener);
    }

    /**
     * NO_LOCK
     * @see org.geoserver.security.GeoserverRoleService#unregisterRoleLoadedListener(org.geoserver.security.event.RoleLoadedListener)
     */
    public void unregisterRoleLoadedListener(RoleLoadedListener listener) {
        listeners.remove(listener);
    }

    /**
     * NO_LOCK
     *  
     */
    public void rolesChanged(RoleLoadedEvent event) {
        // release the locks to avoid deadlock situations
//        if (rwl.isWriteLockedByCurrentThread())
//            writeUnLock();
//        else 
//            readUnLock();
        fireRoleChangedEvent();
    }


    /**
     * READ_LOCK
     * @see org.geoserver.security.GeoserverRoleService#getGroupNamesForRole(org.geoserver.security.impl.GeoserverRole)
     */
    public SortedSet<String> getGroupNamesForRole(GeoserverRole role) throws IOException{
        readLock();
        try {
            return getService().getGroupNamesForRole(role);
        } finally {
            readUnLock();
        }            
    }

    /**
     * READ_LOCK
     * @see org.geoserver.security.GeoserverRoleService#getUserNamesForRole(org.geoserver.security.impl.GeoserverRole)
     */
    public SortedSet<String> getUserNamesForRole(GeoserverRole role) throws IOException{
        readLock();
        try {
            return getService().getUserNamesForRole(role);
        } finally {
            readUnLock();
        }            
    }
    
    /**
     * READ_LOCK
     * @see org.geoserver.security.GeoserverRoleService#getParentMappings()
     */
    public  Map<String,String> getParentMappings() throws IOException {
        readLock();
        try {
            return getService().getParentMappings();
        } finally {
            readUnLock();
        }            
    }
    
    /**
     * READ_LOCK
     * @see org.geoserver.security.GeoserverRoleService#personalizeRoleParams(java.lang.String, java.util.Properties, java.lang.String, java.util.Properties)
     */
    public  Properties personalizeRoleParams (String roleName,Properties roleParams, 
            String userName,Properties userProps) throws IOException {
        
        readLock();
        try {
            return getService().personalizeRoleParams(roleName, roleParams, userName, userProps);
        } finally {
            readUnLock();
        }            
    }

    /**
     * WRITE_LOCK
     * @see org.geoserver.security.GeoserverRoleService#initializeFromConfig(org.geoserver.security.config.SecurityNamedServiceConfig)
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

}
