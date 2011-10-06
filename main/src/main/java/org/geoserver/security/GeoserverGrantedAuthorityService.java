/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.security;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.SortedSet;

import org.geoserver.security.config.SecurityNamedServiceConfig;
import org.geoserver.security.event.GrantedAuthorityLoadedEvent;
import org.geoserver.security.event.GrantedAuthorityLoadedListener;
import org.geoserver.security.impl.GeoserverGrantedAuthority;

/**
 * A class implementing this interface is capable of reading 
 * role assignments from a backend.
 * 
 * 
 * @author christian
 *
 */
public interface GeoserverGrantedAuthorityService  {
    
    
    /**
     * Initialize from configuration object
     * @param config
     * @throws IOException
     */
    public void initializeFromConfig(SecurityNamedServiceConfig config) throws IOException;
    
    /**
     * @return the name of this service
     */
    String getName();
    
    
    /**
     * Register for notifications on load
     * 
     * @param listener
     */
    void registerGrantedAuthorityLoadedListener (GrantedAuthorityLoadedListener listener);
    
    /**
     * Unregister for notifications on load
     * 
     * @param listener
     */
    void unregisterGrantedAuthorityLoadedListener (GrantedAuthorityLoadedListener listener);

    /**
     * Get group names for a {@link GeoserverGrantedAuthority} object
     * Hierarchical roles are not considered
     * 
     * @param role
     * @return collection which cannot be modified
     */
    SortedSet<String> getGroupNamesForRole(GeoserverGrantedAuthority role) throws IOException;

    /**
     * Get user names for a {@link GeoserverGrantedAuthority} object
     * Hierarchical roles are not considered
     * 
     * @param role
     * @return collection which cannot be modified
     */
    SortedSet<String> getUserNamesForRole(GeoserverGrantedAuthority role) throws IOException;

    
    /**
     * Get the roles for the user
     * Hierarchical roles are not considered
     * 
     * @param username
     * @return a collection which cannot be modified
     */
    public abstract SortedSet<GeoserverGrantedAuthority> getRolesForUser(String username) throws IOException;

    
    /**
     * Get the roles for the group
     * Hierarchical roles are not considered
     * 
     * @param groupname
     * @return a collection which cannot be modified
     */
    public abstract SortedSet<GeoserverGrantedAuthority> getRolesForGroup(String groupname) throws IOException;


    /**
     * Get the list of roles currently known by users (implementations must provide
     * the admin role "ROLE_ADMINISTRATOR") 
     * 
     * @return a collection which cannot be modified
     */
    
    public abstract SortedSet<GeoserverGrantedAuthority> getRoles() throws IOException;

    
    /**
     * returns a role name -> parent role name mapping for the all
     * {@link GeoserverGrantedAuthority} objects.
     * 
     * This method should be used by clients if they have to build
     * a tree structure
     * 
     * @return a collection which cannot be modified
     * @throws IOException
     */
    public abstract Map<String,String> getParentMappings() throws IOException;
        
    /**
     * Creates a {@link GeoserverGrantedAuthority} object . Implementations
     * can use their special classes derived from {@link GeoserverGrantedAuthority}
     * 
     * @param role
     * @return
     */
    public GeoserverGrantedAuthority createGrantedAuthorityObject(String role) throws IOException;
    
    
    /**
     * Get the parent {@link GeoserverGrantedAuthority} object
     * @param role
     * @return the parent role or null
     */
    public GeoserverGrantedAuthority getParentRole(GeoserverGrantedAuthority role)  throws IOException;
    
    /**
     * Loads a {@link GeoserverGrantedAuthority} by name
     * @param role
     * @return
     * @throws null if the role is not found
     */
    public GeoserverGrantedAuthority getGrantedAuthorityByName(String role) throws  IOException;

    /**
     * load from backend store. On success,
     * a  {@link GrantedAuthorityLoadedEvent} should must be triggered 
     */
    public abstract void load() throws IOException;
    
    
    /**
     * This is a callback for personalized roles
     * Example:
     * Role employee has a property "employeeNumber", which has 
     * no value or a default value. "employeeNumber" is also called a 
     * role parameter in this context.
     * 
     * A user "harry" has assigned the role employee and
     * has a user property "empNr" with the value 4711 
     *  
     * Now, this method should create a {@link Properties}
     * object containing the the property "employeeNumber"
     * with the value 4711.
     * 
     * A GIS example could be a BBOX for specific user to
     * restrict his access to the wms service  
     * 
     * @param roleName, the name of the role
     * 
     * @param roleParams, the params for the role from
     * {@link GeoserverGrantedAuthorityService}
     * 
     * @param userName, the user name
     * @param userProps. the properties of the user from
     * {@link GeoserverUserGroupService}
     * 
     * @return null for no personalization, the personalized
     * properties otherwise 
     * @throws IOException
     */
    public abstract Properties personalizeRoleParams (String roleName,Properties roleParams, 
            String userName,Properties userProps) throws IOException;
        
}