/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.security;

import java.io.IOException;

import org.geoserver.security.impl.GeoserverGrantedAuthority;

/**
 * A class implementing this interface is capable of storing
 * roles to a backend. The store always operates on a
 * {@link GeoserverGrantedAuthorityService} object.
 * 
 * 
 * @author christian
 *
 */
public interface GeoserverGrantedAuthorityStore extends GeoserverGrantedAuthorityService {

    
    
    /**
     * Initializes itself from a service for future 
     * store modifications concerning this service 
     * 
     * @param service
     */
    public void initializeFromService(GeoserverGrantedAuthorityService service) throws IOException;

    
    /**
     * discards all entries
     * 
     * @throws IOException
     */
    public abstract void clear() throws IOException;

    
    /**
     * Adds a role 
     * @param role
     */
    public abstract void addGrantedAuthority(GeoserverGrantedAuthority role)  throws IOException;

    /**
     * Updates a role 
     * @param role
     */
    public abstract void updateGrantedAuthority(GeoserverGrantedAuthority role)  throws IOException;

    /**
     * Removes the specified GeoserverGrantedAuthority role 
     * @param role
     * @return
     */
    public abstract boolean removeGrantedAuthority(GeoserverGrantedAuthority role)  throws IOException;


    /**
     * Associates a role with a group. 
     *   
     * @param role
     * @param groupname
     */
    public void associateRoleToGroup(GeoserverGrantedAuthority role, String groupname)  throws IOException;
    
    /**
     * Disassociates a role from a group.
     * 
     * @param role
     * @param groupname
     */
    public void disAssociateRoleFromGroup(GeoserverGrantedAuthority role, String groupname)  throws IOException;


    /**
     * Associates a role with a user,
     * 
     * @param role
     * @param username
     */
    public void associateRoleToUser(GeoserverGrantedAuthority role, String username)  throws IOException;
    
    /**
     * Disassociates a role from a user.
     * 
     * @param role
     * @param groupname
     */
    public void disAssociateRoleFromUser(GeoserverGrantedAuthority role, String username)  throws IOException;
    
    
        
    /**
     * Synchronizes all changes with the backend store.  
     * On success, the associated service object should be reloaded
     */
    public abstract void store() throws IOException;
                

    /**
     * returns true if there are pending modifications
     * not written to the backend store
     * 
     * @return true/false
     */
    public boolean isModified();
    
    /**
     * Sets the parent role, the method must check if parentRole is not equal
     * to role and if parentRole is not contained in the descendants of role
     * 
     * This code sequence will do the job
     * <code>
     *   RoleHierarchyHelper helper = new RoleHierarchyHelper(getParentMappings());
     *   if (helper.isValidParent(role.getAuthority(), 
     *           parentRole==null ? null : parentRole.getAuthority())==false)
     *       throw new IOException(parentRole.getAuthority() +
     *               " is not a valid parent for " + role.getAuthority());    
     * </code>
     * 
     * @param role
     * @param parentRole, may be null to remove a parent
     */
    public void setParentRole(GeoserverGrantedAuthority role, GeoserverGrantedAuthority parentRole)  throws IOException;
    
    
}