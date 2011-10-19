/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.security;

import java.io.IOException;
import java.util.Collection;
import java.util.SortedSet;

import org.geoserver.security.config.UserDetailsServiceConfig;
import org.geoserver.security.impl.GeoserverGrantedAuthority;
import org.geoserver.security.impl.GeoserverUser;
import org.geoserver.security.impl.GeoserverUserGroup;
import org.springframework.security.core.userdetails.UserDetailsService;



/**
 * This interface aggregates an {@link GeoserverGrantedAuthorityService} and a
 * {@link GeoserverUserGroupService}.
 * 
 * If roles should be modifiable,  {@link GeoserverGrantedAuthorityStore} should be used.
 * The same holds true for {@link GeoserverUserGroupStore}
 * 
 * Objects implementing this class serve as {@link UserDetailsService} to the 
 * Spring Security Framework. 
 * 
 *   
 * @author christian
 *
 */
public interface GeoserverUserDetailsService extends UserDetailsService {

    /**
     * Initialize from {@link UserDetailsServiceConfig} object
     * 
     * @param config
     */
    //void initializeFrom(UserDetailsServiceConfig config) throws IOException;
    
    /**
     * Setter for the {@link GeoserverGrantedAuthorityService} or {@link GeoserverGrantedAuthorityStore} object
     * 
     * @param service
     */
    void setGrantedAuthorityService (GeoserverGrantedAuthorityService service);
    
    /**
     * @return {@link GeoserverGrantedAuthorityService} or {@link GeoserverGrantedAuthorityStore} object
     */
    GeoserverGrantedAuthorityService getGrantedAuthorityService ();
    
    /**
     * 
     * @return true, if roles can be modified
     */
    boolean isGrantedAuthorityStore();
    
    /**
     * Setter for the {@link GeoserverUserGroupService} or {@link GeoserverUserGroupStore} object
     * 
     * @param service
     */
    void setUserGroupService(GeoserverUserGroupService service);
    
    /**
     * @return {@link GeoserverUserGroupService} or {@link GeoserverUserGroupStore} object
     */
    GeoserverUserGroupService getUserGroupService();
    
    /**
     * @return true, if user and groups can be modified
     */
    boolean isUserGroupStore();
    
    
    /**
     * Calculate the Granted Authorities for a user
     * 
     * The algorithm
     * 
     * get the roles directly assigned to the user
     * get the groups of the user
     * for each "enabled" group, add the roles of the group
     * for all roles so far, search for ancestor roles and
     * add them to the set
     * 
     * After role calculation has finished, personalize each
     * role with role attributes if necessary
     * 
     * @param user
     * @return
     * @throws IOException
     */
    SortedSet<GeoserverGrantedAuthority> calculateGrantedAuthorities(GeoserverUser user) throws IOException;
    
    /**
     * Calculate the Granted Authorities for a group
     * including inherited roles
     * 
     * @param group
     * @return
     * @throws IOException
     */
    SortedSet<GeoserverGrantedAuthority> calculateGrantedAuthorities(GeoserverUserGroup group) throws IOException;
    
    /**
     * Adds inherited roles to a role set
     * 
     * @param coll
     * @throws IOException
     */
    void addInheritedRoles(Collection<GeoserverGrantedAuthority> coll) throws IOException;
    
    /**
     * Takes the role set for a user and
     * personalizes the roles (matching user properties
     * and role parameters)
     * 
     * @param user
     * @param roles
     * @return
     * @throws IOException
     */
    public SortedSet<GeoserverGrantedAuthority> personalizeRoles(GeoserverUser user, 
            Collection<GeoserverGrantedAuthority> roles) throws IOException;
}