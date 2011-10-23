/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.security.impl;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.geoserver.security.GeoserverGrantedAuthorityService;
import org.geoserver.security.GeoserverGrantedAuthorityStore;
import org.geoserver.security.event.GrantedAuthorityLoadedEvent;
import org.geoserver.security.event.GrantedAuthorityLoadedListener;


/**
 * Default in memory implementation for {@link GeoserverGrantedAuthorityService}
 * 
 * @author Christian
 *
 */
public abstract class AbstractGrantedAuthorityService extends AbstractGeoServerSecurityService 
    implements GeoserverGrantedAuthorityService {
    
    protected TreeMap<String,GeoserverGrantedAuthority> roleMap =
        new TreeMap<String,GeoserverGrantedAuthority>();    
    protected TreeMap<String, SortedSet<GeoserverGrantedAuthority>>group_roleMap =
        new TreeMap<String, SortedSet<GeoserverGrantedAuthority>>();
    protected TreeMap<String, SortedSet<GeoserverGrantedAuthority>> user_roleMap =
        new TreeMap<String, SortedSet<GeoserverGrantedAuthority>>();
    protected HashMap<GeoserverGrantedAuthority, GeoserverGrantedAuthority> role_parentMap =
        new HashMap<GeoserverGrantedAuthority, GeoserverGrantedAuthority>();
    
    protected Set<GrantedAuthorityLoadedListener> listeners = 
        Collections.synchronizedSet(new HashSet<GrantedAuthorityLoadedListener>());

    protected AbstractGrantedAuthorityService() {
    }

    protected AbstractGrantedAuthorityService(String name) {
        super(name);
    }
    
    @Override
    public GeoserverGrantedAuthorityStore createStore() throws IOException {
        //return null, subclasses can override if they support a store along with a service
        return null;
    }

    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverGrantedAuthorityService#registerGrantedAuthorityChangedListener(org.geoserver.security.event.GrantedAuthorityChangedListener)
     */
    public void registerGrantedAuthorityLoadedListener (GrantedAuthorityLoadedListener listener) {
        listeners.add(listener);
        
    }
    
    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverGrantedAuthorityService#unregisterGrantedAuthorityLoadedListener(org.geoserver.security.event.GrantedAuthorityLoadedListener)
     */
    public void unregisterGrantedAuthorityLoadedListener (GrantedAuthorityLoadedListener listener) {
        listeners.remove(listener);
    }

                
    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverGrantedAuthorityService#getRoles()
     */
    public SortedSet<GeoserverGrantedAuthority> getRoles()   throws IOException{
        
    
        SortedSet<GeoserverGrantedAuthority> result = new TreeSet<GeoserverGrantedAuthority>();
        result.addAll(roleMap.values());
        return Collections.unmodifiableSortedSet(result);
    }
            

    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverGrantedAuthorityService#load()
     */
    public void load() throws IOException{
        LOGGER.info("Start reloading roles for service named "+getName());
        // prevent concurrent write from store and
        // read from service
        synchronized (this) { 
            deserialize();
        }
        LOGGER.info("Reloading roles successful for service named "+getName());
        fireGrantedAuthorityLoadedEvent();
    }

    /**
     * Subclasses must implement this method 
     * Load role assignments from backend
     */
    protected abstract void deserialize() throws IOException;
        
            

    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverGrantedAuthorityService#getRolesForUser(java.lang.String)
     */
    public  SortedSet<GeoserverGrantedAuthority> getRolesForUser(String username)  throws IOException{
        SortedSet<GeoserverGrantedAuthority> roles = user_roleMap.get(username);
        if (roles==null)
            roles=new TreeSet<GeoserverGrantedAuthority>();
        return Collections.unmodifiableSortedSet(roles);
    }

    
    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverGrantedAuthorityService#getRolesForGroup(java.lang.String)
     */
    public  SortedSet<GeoserverGrantedAuthority> getRolesForGroup(String groupname)  throws IOException{
        SortedSet<GeoserverGrantedAuthority> roles = group_roleMap.get(groupname);
        if (roles==null)
            roles=new TreeSet<GeoserverGrantedAuthority>();
        return Collections.unmodifiableSortedSet(roles);
    }

    
    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverGrantedAuthorityService#createGrantedAuthorityObject(java.lang.String)
     */
    public GeoserverGrantedAuthority createGrantedAuthorityObject(String role)   throws IOException{
        return new GeoserverGrantedAuthority(role);
    }
    

    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverGrantedAuthorityService#getParentRole(org.geoserver.security.impl.GeoserverGrantedAuthority)
     */
    public GeoserverGrantedAuthority getParentRole(GeoserverGrantedAuthority role)   throws IOException{
        return role_parentMap.get(role);        
    }
    
    protected void checkRole(GeoserverGrantedAuthority role) {
        if (roleMap.containsKey(role.getAuthority())==false)
            throw new IllegalArgumentException("Role: " +  role.getAuthority()+ " does not exist");
    }
    
    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverGrantedAuthorityService#getGrantedAuthorityByName(java.lang.String)
     */
    public GeoserverGrantedAuthority getGrantedAuthorityByName(String role) throws  IOException {
            return roleMap.get(role);
    }
    
    /**
     * Fire {@link GrantedAuthorityLoadedEvent} for all listeners
     */
    protected void fireGrantedAuthorityLoadedEvent() {
        GrantedAuthorityLoadedEvent event = new GrantedAuthorityLoadedEvent(this);
        for (GrantedAuthorityLoadedListener listener : listeners) {
            listener.grantedAuthoritiesChanged(event);
        }
    }    

    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverGrantedAuthorityService#getGroupNamesForRole(org.geoserver.security.impl.GeoserverGrantedAuthority)
     */
    public SortedSet<String> getGroupNamesForRole(GeoserverGrantedAuthority role) throws IOException {
        SortedSet<String> result = new TreeSet<String>();
        for (Entry<String,SortedSet<GeoserverGrantedAuthority>> entry : group_roleMap.entrySet()) {
            if (entry.getValue().contains(role))
                result.add(entry.getKey());
        }
        return Collections.unmodifiableSortedSet(result);
    }


    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverGrantedAuthorityService#getUserNamesForRole(org.geoserver.security.impl.GeoserverGrantedAuthority)
     */
    public SortedSet<String> getUserNamesForRole(GeoserverGrantedAuthority role) throws IOException{
        SortedSet<String> result = new TreeSet<String>();
        for (Entry<String,SortedSet<GeoserverGrantedAuthority>> entry : user_roleMap.entrySet()) {
            if (entry.getValue().contains(role))
                result.add(entry.getKey());
        }
        return Collections.unmodifiableSortedSet(result);        
    }
    
    /**
     * internal use, clear the maps
     */
    protected void clearMaps() {
        roleMap.clear();
        role_parentMap.clear();
        group_roleMap.clear();
        user_roleMap.clear();
    }
    
    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverGrantedAuthorityService#getParentMappings()
     */
    public  Map<String,String> getParentMappings() throws IOException {
        Map<String,String> parentMap = new HashMap<String,String>();
        for (GeoserverGrantedAuthority role: roleMap.values()) {
            GeoserverGrantedAuthority parentRole = role_parentMap.get(role); 
            parentMap.put(role.getAuthority(), 
                    parentRole == null ? null : parentRole.getAuthority());
        }
        return Collections.unmodifiableMap(parentMap);
    }

    /** (non-Javadoc)
     * @see org.geoserver.security.GeoserverGrantedAuthorityService#personalizeRoleParams(java.lang.String, java.util.Properties, java.lang.String, java.util.Properties)
     * 
     * Default implementation: if a user property name equals a role property name, 
     * take the value from to user property and use it for the role property. 
     * 
     */
    public  Properties personalizeRoleParams (String roleName,Properties roleParams, 
            String userName,Properties userProps) throws IOException {
        Properties props = null;
        
        // this is true if the set is modified --> common 
        // property names exist
        
        props = new Properties();
        boolean personalized=false;
        
        for (Object key: roleParams.keySet()) {
            if (userProps.containsKey(key)) {
                props.put(key, userProps.get(key));
                personalized=true;
            }
            else
                props.put(key,roleParams.get(key));
        }
        return personalized ?  props : null;
    }

    /**
     * The root configuration for the role service.
     */
    public File getConfigRoot() throws IOException {
        return new File(getSecurityManager().getRoleRoot(), getName());
    }
}
