/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.security.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.geoserver.security.GeoserverGrantedAuthorityService;
import org.geoserver.security.GeoserverGrantedAuthorityStore;
import org.geoserver.security.config.SecurityNamedServiceConfig;

/**
 * Abstract base class for role store implementations
 * 
 * @author christian
 *
 */
public abstract class AbstractGrantedAuthorityStore extends AbstractGrantedAuthorityService implements GeoserverGrantedAuthorityStore {

    /** logger */
    static Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.geoserver.security");
    
    private boolean modified=false;
    protected AbstractGrantedAuthorityService service;

    public AbstractGrantedAuthorityStore(String name) {
        super(name);
        
    }
    
    
    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverGrantedAuthorityStore#isModified()
     */
    public boolean isModified() {
        return modified;
    }

    /**
     * Setter for modified flag
     * @param value
     */
    public void setModified(Boolean value) {
        modified=value;
    }
    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverGrantedAuthorityStore#addGrantedAuthority(org.geoserver.security.impl.GeoserverGrantedAuthority)
     */
    public void addGrantedAuthority(GeoserverGrantedAuthority role)  throws IOException{
        
        
        if(roleMap.containsKey(role.getAuthority()))
            throw new IllegalArgumentException("The role " + role.getAuthority() + " already exists");
        else {
            roleMap.put(role.getAuthority(),role);
            setModified(true);
        }
    }
    

    
    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverGrantedAuthorityStore#updateGrantedAuthority(org.geoserver.security.impl.GeoserverGrantedAuthority)
     */
    public void updateGrantedAuthority(GeoserverGrantedAuthority role)  throws IOException {
        
        if(roleMap.containsKey(role.getAuthority())) {
            roleMap.put(role.getAuthority(),role);
            setModified(true);
        }
        else
            throw new IllegalArgumentException("The role " + role.getAuthority() + " does not exist");
    }
    

    
    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverGrantedAuthorityStore#removeGrantedAuthority(org.geoserver.security.impl.GeoserverGrantedAuthority)
     */
    public boolean removeGrantedAuthority(GeoserverGrantedAuthority role)  throws IOException{
        
        if (roleMap.containsKey(role.getAuthority())==false) // nothing to do
            return false;
        
        for (SortedSet<GeoserverGrantedAuthority> set: user_roleMap.values()) {
            set.remove(role);
        }
        for (SortedSet<GeoserverGrantedAuthority> set: group_roleMap.values()) {
            set.remove(role);
        }
        
        // role hierarchy
        role_parentMap.remove(role);
        Set<GeoserverGrantedAuthority> toBeRemoved = new HashSet<GeoserverGrantedAuthority>();
        for (Entry<GeoserverGrantedAuthority,GeoserverGrantedAuthority> entry : role_parentMap.entrySet()) {
            if (role.equals(entry.getValue()))
                toBeRemoved.add(entry.getKey());
        }
        for (GeoserverGrantedAuthority  ga : toBeRemoved) {
            role_parentMap.put(ga,null);
        }    
        
        // remove role
        roleMap.remove(role.getAuthority());
        setModified(true);
        return true;

    }


    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverGrantedAuthorityStore#store()
     */
    public void store() throws IOException {
        if (isModified()) {
            LOGGER.info("Start storing roles for service named "+getName());
         // prevent concurrent write from store and
         // read from service
            synchronized (service) { 
                serialize();
            }            
            setModified(false);
            LOGGER.info("Storing roles successful for service named "+getName());
            service.load(); // service must reload
        } else {
            LOGGER.info("Storing unnecessary, no change for roles");
        }        
        
    }
    
    /**
     * Subclasses must implement this method 
     * Save role assignments to backend
     */
    protected abstract void serialize() throws IOException;

    
    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverGrantedAuthorityStore#disAssociateRoleFromGroup(org.geoserver.security.impl.GeoserverGrantedAuthority, java.lang.String)
     */
    public void disAssociateRoleFromGroup(GeoserverGrantedAuthority role, String groupname) throws IOException{
        SortedSet<GeoserverGrantedAuthority> roles = group_roleMap.get(groupname);
        if (roles!=null && roles.contains(role)) {
            roles.remove(role);
            setModified(true);
        }
    }

    
    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverGrantedAuthorityStore#associateRoleToGroup(org.geoserver.security.impl.GeoserverGrantedAuthority, java.lang.String)
     */
    public void associateRoleToGroup(GeoserverGrantedAuthority role, String groupname) throws IOException{
        SortedSet<GeoserverGrantedAuthority> roles = group_roleMap.get(groupname);
        if (roles == null) {
            roles=new TreeSet<GeoserverGrantedAuthority>();
            group_roleMap.put(groupname, roles);
        }
        if (roles.contains(role)==false) { // something changed ?
            roles.add(role);
            setModified(true);
        }
    }
    

    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverGrantedAuthorityStore#associateRoleToUser(org.geoserver.security.impl.GeoserverGrantedAuthority, java.lang.String)
     */
    public void associateRoleToUser(GeoserverGrantedAuthority role, String username) throws IOException{
        SortedSet<GeoserverGrantedAuthority> roles = user_roleMap.get(username);
        if (roles == null) {
            roles=new TreeSet<GeoserverGrantedAuthority>();
            user_roleMap.put(username, roles);
        }
        if (roles.contains(role)==false) { // something changed
            roles.add(role);
            setModified(true);
        }
            
    }
    
    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverGrantedAuthorityStore#disAssociateRoleFromUser(org.geoserver.security.impl.GeoserverGrantedAuthority, java.lang.String)
     */
    public void disAssociateRoleFromUser(GeoserverGrantedAuthority role, String username) throws IOException{
        SortedSet<GeoserverGrantedAuthority> roles = user_roleMap.get(username);
        if (roles!=null && roles.contains(role)) {
            roles.remove(role);
            setModified(true);
        }
    }


    
    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverGrantedAuthorityStore#setParentRole(org.geoserver.security.impl.GeoserverGrantedAuthority, org.geoserver.security.impl.GeoserverGrantedAuthority)
     */
    public void setParentRole(GeoserverGrantedAuthority role, GeoserverGrantedAuthority parentRole) throws IOException{
        
        RoleHierarchyHelper helper = new RoleHierarchyHelper(getParentMappings());
        if (helper.isValidParent(role.getAuthority(), 
                parentRole==null ? null : parentRole.getAuthority())==false)
            throw new IOException(parentRole.getAuthority() +
                    " is not a valid parent for " + role.getAuthority());    
        
        checkRole(role);
        if (parentRole==null) {
            role_parentMap.remove(role);
        } else {
            checkRole(parentRole);
            role_parentMap.put(role,parentRole);
        }
        setModified(true);
    }
    
    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverGrantedAuthorityStore#clear()
     */
    public void clear() throws IOException {
        clearMaps();
        setModified(true);
    }
    
    /**
     * Make a deep copy (using serialization) from the
     * service to the store.
     *  
     * 
     * @see org.geoserver.security.impl.AbstractGrantedAuthorityService#deserialize()
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void deserialize() throws IOException {
        
        // make a deep copy of the maps using serialization
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream oout = new ObjectOutputStream(out);
        oout.writeObject(service.roleMap);
        oout.writeObject(service.role_parentMap);
        oout.writeObject(service.user_roleMap);
        oout.writeObject(service.group_roleMap);
        byte[] byteArray=out.toByteArray();
        oout.close();            

        
        clearMaps();        
        ByteArrayInputStream in = new ByteArrayInputStream(byteArray);
        ObjectInputStream oin = new ObjectInputStream(in);
        try {
            roleMap = (TreeMap<String,GeoserverGrantedAuthority>) oin.readObject();
            role_parentMap =(HashMap<GeoserverGrantedAuthority,GeoserverGrantedAuthority>) oin.readObject();
            user_roleMap = (TreeMap<String,SortedSet<GeoserverGrantedAuthority>>)oin.readObject();
            group_roleMap = (TreeMap<String,SortedSet<GeoserverGrantedAuthority>>)oin.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
        setModified(false);
    }

    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverGrantedAuthorityStore#initializeFromService(org.geoserver.security.GeoserverGrantedAuthorityService)
     */
    @Override
    public void initializeFromService(GeoserverGrantedAuthorityService service)
            throws IOException {
        this.service=(AbstractGrantedAuthorityService)service;
        deserialize();
    }
    
    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverGrantedAuthorityService#initializeFromConfig(org.geoserver.security.config.SecurityNamedServiceConfig)
     */
    @Override
    public void initializeFromConfig(SecurityNamedServiceConfig config) throws IOException {
        // Do nothing
    }

}
