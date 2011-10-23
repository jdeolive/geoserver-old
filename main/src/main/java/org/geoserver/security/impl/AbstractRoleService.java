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
import org.geoserver.security.GeoserverRoleService;
import org.geoserver.security.GeoserverRoleStore;
import org.geoserver.security.event.RoleLoadedEvent;
import org.geoserver.security.event.RoleLoadedListener;


/**
 * Default in memory implementation for {@link GeoserverRoleService}
 * 
 * @author Christian
 *
 */
public abstract class AbstractRoleService extends AbstractGeoServerSecurityService 
    implements GeoserverRoleService {
    
    protected TreeMap<String,GeoserverRole> roleMap =
        new TreeMap<String,GeoserverRole>();    
    protected TreeMap<String, SortedSet<GeoserverRole>>group_roleMap =
        new TreeMap<String, SortedSet<GeoserverRole>>();
    protected TreeMap<String, SortedSet<GeoserverRole>> user_roleMap =
        new TreeMap<String, SortedSet<GeoserverRole>>();
    protected HashMap<GeoserverRole, GeoserverRole> role_parentMap =
        new HashMap<GeoserverRole, GeoserverRole>();
    
    protected Set<RoleLoadedListener> listeners = 
        Collections.synchronizedSet(new HashSet<RoleLoadedListener>());

    protected AbstractRoleService() {
    }

    
    @Override
    public GeoserverRoleStore createStore() throws IOException {
        //return null, subclasses can override if they support a store along with a service
        return null;
    }

    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverRoleService#registerRoleLoadedListener(org.geoserver.security.event.RoleLoadedListener)
     */
    public void registerRoleLoadedListener (RoleLoadedListener listener) {
        listeners.add(listener);
        
    }
    
    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverRoleService#unregisterRoleLoadedListener(org.geoserver.security.event.RoleLoadedListener)
     */
    public void unregisterRoleLoadedListener (RoleLoadedListener listener) {
        listeners.remove(listener);
    }

                
    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverRoleService#getRoles()
     */
    public SortedSet<GeoserverRole> getRoles()   throws IOException{
        
    
        SortedSet<GeoserverRole> result = new TreeSet<GeoserverRole>();
        result.addAll(roleMap.values());
        return Collections.unmodifiableSortedSet(result);
    }
            

    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverRoleService#load()
     */
    public void load() throws IOException{
        LOGGER.info("Start reloading roles for service named "+getName());
        // prevent concurrent write from store and
        // read from service
        synchronized (this) { 
            deserialize();
        }
        LOGGER.info("Reloading roles successful for service named "+getName());
        fireRoleLoadedEvent();
    }

    /**
     * Subclasses must implement this method 
     * Load role assignments from backend
     */
    protected abstract void deserialize() throws IOException;
        
            

    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverRoleService#getRolesForUser(java.lang.String)
     */
    public  SortedSet<GeoserverRole> getRolesForUser(String username)  throws IOException{
        SortedSet<GeoserverRole> roles = user_roleMap.get(username);
        if (roles==null)
            roles=new TreeSet<GeoserverRole>();
        return Collections.unmodifiableSortedSet(roles);
    }

    
    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverRoleService#getRolesForGroup(java.lang.String)
     */
    public  SortedSet<GeoserverRole> getRolesForGroup(String groupname)  throws IOException{
        SortedSet<GeoserverRole> roles = group_roleMap.get(groupname);
        if (roles==null)
            roles=new TreeSet<GeoserverRole>();
        return Collections.unmodifiableSortedSet(roles);
    }

    
    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverRoleService#createRoleObject(java.lang.String)
     */
    public GeoserverRole createRoleObject(String role)   throws IOException{
        return new GeoserverRole(role);
    }
    

    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverRoleService#getParentRole(org.geoserver.security.impl.GeoserverRole)
     */
    public GeoserverRole getParentRole(GeoserverRole role)   throws IOException{
        return role_parentMap.get(role);        
    }
    
    protected void checkRole(GeoserverRole role) {
        if (roleMap.containsKey(role.getAuthority())==false)
            throw new IllegalArgumentException("Role: " +  role.getAuthority()+ " does not exist");
    }
    
    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverRoleService#getRoleByName(java.lang.String)
     */
    public GeoserverRole getRoleByName(String role) throws  IOException {
            return roleMap.get(role);
    }
    
    /**
     * Fire {@link RoleLoadedEvent} for all listeners
     */
    protected void fireRoleLoadedEvent() {
        RoleLoadedEvent event = new RoleLoadedEvent(this);
        for (RoleLoadedListener listener : listeners) {
            listener.rolesChanged(event);
        }
    }    

    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverRoleService#getGroupNamesForRole(org.geoserver.security.impl.GeoserverRole)
     */
    public SortedSet<String> getGroupNamesForRole(GeoserverRole role) throws IOException {
        SortedSet<String> result = new TreeSet<String>();
        for (Entry<String,SortedSet<GeoserverRole>> entry : group_roleMap.entrySet()) {
            if (entry.getValue().contains(role))
                result.add(entry.getKey());
        }
        return Collections.unmodifiableSortedSet(result);
    }


    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverRoleService#getUserNamesForRole(org.geoserver.security.impl.GeoserverRole)
     */
    public SortedSet<String> getUserNamesForRole(GeoserverRole role) throws IOException{
        SortedSet<String> result = new TreeSet<String>();
        for (Entry<String,SortedSet<GeoserverRole>> entry : user_roleMap.entrySet()) {
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
     * @see org.geoserver.security.GeoserverRoleService#getParentMappings()
     */
    public  Map<String,String> getParentMappings() throws IOException {
        Map<String,String> parentMap = new HashMap<String,String>();
        for (GeoserverRole role: roleMap.values()) {
            GeoserverRole parentRole = role_parentMap.get(role); 
            parentMap.put(role.getAuthority(), 
                    parentRole == null ? null : parentRole.getAuthority());
        }
        return Collections.unmodifiableMap(parentMap);
    }

    /** (non-Javadoc)
     * @see org.geoserver.security.GeoserverRoleService#personalizeRoleParams(java.lang.String, java.util.Properties, java.lang.String, java.util.Properties)
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
