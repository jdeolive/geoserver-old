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


import org.geoserver.security.GeoserverGrantedAuthorityService;
import org.geoserver.security.GeoserverGrantedAuthorityStore;
import org.geoserver.security.config.SecurityNamedServiceConfig;
import org.geoserver.security.event.GrantedAuthorityLoadedEvent;
import org.geoserver.security.event.GrantedAuthorityLoadedListener;
import org.geoserver.security.impl.GeoserverGrantedAuthority;

/**
 * This is a wrapper class for a {@link GeoserverGrantedAuthorityService}.
 * This wrapper protects internal data structures using read/write locks
 * 
 * @author christian
 *
 */
public class LockingGrantedAuthorityService extends AbstractLockingService implements
        GeoserverGrantedAuthorityService,GrantedAuthorityLoadedListener {

    protected Set<GrantedAuthorityLoadedListener> listeners = 
        Collections.synchronizedSet(new HashSet<GrantedAuthorityLoadedListener>());

    /**
     * Constructor for the locking wrapper
     * 
     * @param service
     */
    public LockingGrantedAuthorityService(GeoserverGrantedAuthorityService service) {
        super(service);
        service.registerGrantedAuthorityLoadedListener(this);
    }

    /**
     * @return the wrapped service
     */
    public GeoserverGrantedAuthorityService getService() {
        return (GeoserverGrantedAuthorityService) super.getService();
    }

    @Override
    public GeoserverGrantedAuthorityStore createStore() throws IOException {
        GeoserverGrantedAuthorityStore store = getService().createStore();
        return store != null ? new LockingGrantedAuthorityStore(store) : null;
    }

    /** 
     * WRITE_LOCK
     * @see org.geoserver.security.GeoserverGrantedAuthorityService#load()
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
     * @see org.geoserver.security.GeoserverGrantedAuthorityService#getRolesForUser(java.lang.String)
     */
    public SortedSet<GeoserverGrantedAuthority> getRolesForUser(String username) throws IOException {
        readLock();
        try {
            return getService().getRolesForUser(username);
        } finally {
            readUnLock();
        }            
    }

    /**
     * READ_LOCK
     * @see org.geoserver.security.GeoserverGrantedAuthorityService#getRolesForGroup(java.lang.String)
     */
    public SortedSet<GeoserverGrantedAuthority> getRolesForGroup(String groupname) throws IOException{
        readLock();
        try {
            return getService().getRolesForGroup(groupname) ;
        } finally {
            readUnLock();
        }
            
    }

    /**
     * READ_LOCK
     * @see org.geoserver.security.GeoserverGrantedAuthorityService#getRoles()
     */
    public SortedSet<GeoserverGrantedAuthority> getRoles() throws IOException{
        readLock();
        try {
            return getService().getRoles();
        } finally {
            readUnLock();
        }            
    }

    /**
     * READ_LOCK
     * @see org.geoserver.security.GeoserverGrantedAuthorityService#createGrantedAuthorityObject(java.lang.String)
     */
    public GeoserverGrantedAuthority createGrantedAuthorityObject(String role) throws IOException{
        readLock();
        try {
            return getService().createGrantedAuthorityObject(role);
        } finally {
            readUnLock();
        }            
    }

    /**
     * READ_LOCK
     * @see org.geoserver.security.GeoserverGrantedAuthorityService#getParentRole(org.geoserver.security.impl.GeoserverGrantedAuthority)
     */
    public GeoserverGrantedAuthority getParentRole(GeoserverGrantedAuthority role) throws IOException{
        readLock();
        try {
            return getService().getParentRole(role);
        } finally {
            readUnLock();
        }            
    }

    /**
     * READ_LOCK
     * @see org.geoserver.security.GeoserverGrantedAuthorityService#getGrantedAuthorityByName(java.lang.String)
     */
    public GeoserverGrantedAuthority getGrantedAuthorityByName(String role) throws IOException {
        readLock();
        try {
            return getService().getGrantedAuthorityByName(role);
        } finally {
            readUnLock();
        }                    
    }

    
    /**
     * Fire {@link GrantedAuthorityLoadedEvent} for all listeners 
     */
    protected void fireGrantedLoadedChangedEvent() {        
        GrantedAuthorityLoadedEvent event = new GrantedAuthorityLoadedEvent(this);
        for (GrantedAuthorityLoadedListener listener : listeners) {
            listener.grantedAuthoritiesChanged(event);
        }
        
    }

    /**
     * NO_LOCK
     * @see org.geoserver.security.GeoserverGrantedAuthorityService#registerGrantedAuthorityLoadedListener(org.geoserver.security.event.GrantedAuthorityLoadedListener)
     */
    public void registerGrantedAuthorityLoadedListener(GrantedAuthorityLoadedListener listener) {
        listeners.add(listener);
    }

    /**
     * NO_LOCK
     * @see org.geoserver.security.GeoserverGrantedAuthorityService#unregisterGrantedAuthorityLoadedListener(org.geoserver.security.event.GrantedAuthorityLoadedListener)
     */
    public void unregisterGrantedAuthorityLoadedListener(GrantedAuthorityLoadedListener listener) {
        listeners.remove(listener);
    }

    /**
     * NO_LOCK
     * @see org.geoserver.security.event.GrantedAuthorityChangedListener#grantedAuthoritiesChanged(org.geoserver.security.event.GrantedAuthorityChangedEvent)
     */
    public void grantedAuthoritiesChanged(GrantedAuthorityLoadedEvent event) {
        // release the locks to avoid deadlock situations
//        if (rwl.isWriteLockedByCurrentThread())
//            writeUnLock();
//        else 
//            readUnLock();
        fireGrantedLoadedChangedEvent();
    }


    /**
     * READ_LOCK
     * @see org.geoserver.security.GeoserverGrantedAuthorityService#getGroupNamesForRole(org.geoserver.security.impl.GeoserverGrantedAuthority)
     */
    public SortedSet<String> getGroupNamesForRole(GeoserverGrantedAuthority role) throws IOException{
        readLock();
        try {
            return getService().getGroupNamesForRole(role);
        } finally {
            readUnLock();
        }            
    }

    /**
     * READ_LOCK
     * @see org.geoserver.security.GeoserverGrantedAuthorityService#getUserNamesForRole(org.geoserver.security.impl.GeoserverGrantedAuthority)
     */
    public SortedSet<String> getUserNamesForRole(GeoserverGrantedAuthority role) throws IOException{
        readLock();
        try {
            return getService().getUserNamesForRole(role);
        } finally {
            readUnLock();
        }            
    }
    
    /**
     * READ_LOCK
     * @see org.geoserver.security.GeoserverGrantedAuthorityService#getParentMappings()
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
     * @see org.geoserver.security.GeoserverGrantedAuthorityService#personalizeRoleParams(java.lang.String, java.util.Properties, java.lang.String, java.util.Properties)
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
     * @see org.geoserver.security.GeoserverGrantedAuthorityService#initializeFromConfig(org.geoserver.security.config.SecurityNamedServiceConfig)
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
