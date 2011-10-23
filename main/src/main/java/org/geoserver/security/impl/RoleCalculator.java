/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.security.impl;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.geoserver.security.GeoserverGrantedAuthorityService;
import org.geoserver.security.GeoserverUserGroupService;

/**
 * Helper Object for role calculations 
 * 
 * @author christian
 *
 */
public class RoleCalculator  {
    

    protected GeoserverGrantedAuthorityService grantedAuthoriyService;
    protected GeoserverUserGroupService userGroupService;

    /**
     * Constructor
     * 
     * @param userGroupService
     * @param grantedAuthoriyService
     */
    public RoleCalculator (GeoserverUserGroupService userGroupService,GeoserverGrantedAuthorityService grantedAuthoriyService) {
        this.userGroupService=userGroupService;
        this.grantedAuthoriyService=grantedAuthoriyService;
        assertServicesNotNull();
    }
        
    public void setGrantedAuthorityService(GeoserverGrantedAuthorityService service) {
        grantedAuthoriyService=service;
        assertServicesNotNull();

    }

    public GeoserverGrantedAuthorityService getGrantedAuthorityService() {
        return grantedAuthoriyService;
    }


    public void setUserGroupService(GeoserverUserGroupService service) {
        userGroupService=service;
        assertServicesNotNull();
   }

    public GeoserverUserGroupService getUserGroupService() {
        return userGroupService;
    }


    /**
     * Check if the services are not null
     * 
     */
    protected void assertServicesNotNull() {
        if (userGroupService==null ) {
            throw new RuntimeException("User/Group service is null");
        }
        if (grantedAuthoriyService==null) {
            throw new RuntimeException("Granted authoritry service Service is null");
        }
    }


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
    public SortedSet<GeoserverGrantedAuthority> calculateGrantedAuthorities(GeoserverUser user)
            throws IOException {
        
        Set<GeoserverGrantedAuthority> set1 = new HashSet<GeoserverGrantedAuthority>();
        
        // alle roles for the user
        set1.addAll(getGrantedAuthorityService().getRolesForUser(user.getUsername()));
        addInheritedRoles(set1);
        
        // add all roles for enabled groups
        for (GeoserverUserGroup group : 
            getUserGroupService().getGroupsForUser(user)) {
            if (group.isEnabled())
                set1.addAll(calculateGrantedAuthorities(group));
        }
        
       // personalize roles
        SortedSet<GeoserverGrantedAuthority> set2 = 
                personalizeRoles(user, set1);
        
        return set2;
    }
    
    /**
     * Collects the ascendents for a {@link GeoserverGrantedAuthority} object
     * 
     * @param role
     * @param inherited
     */
    protected void addParentRole(GeoserverGrantedAuthority role,Collection<GeoserverGrantedAuthority> inherited) throws IOException{
        GeoserverGrantedAuthority parentRole = 
            getGrantedAuthorityService().getParentRole(role);
        if (parentRole==null) 
            return; // end of recursion
        
        if (inherited.contains(parentRole))
            return; // end of recursion
        
        inherited.add(parentRole);
        // recursion
        addParentRole(parentRole, inherited);
    }

    /**
     * Calculate the Granted Authorities for a group
     * including inherited roles
     * 
     * @param group
     * @return
     * @throws IOException
     */
    public SortedSet<GeoserverGrantedAuthority> calculateGrantedAuthorities(GeoserverUserGroup group) throws IOException {
        
        SortedSet<GeoserverGrantedAuthority> roles = new TreeSet<GeoserverGrantedAuthority>();
        roles.addAll(getGrantedAuthorityService().getRolesForGroup(group.getGroupname()));
        addInheritedRoles(roles);
        return roles;
    }
    
    /**
     * Adds inherited roles to a role set
     * 
     * @param coll
     * @throws IOException
     */
    public void addInheritedRoles(Collection<GeoserverGrantedAuthority> coll) throws IOException {
        Set<GeoserverGrantedAuthority> inherited = new HashSet<GeoserverGrantedAuthority>();
        for (GeoserverGrantedAuthority role : coll)
            addParentRole(role, inherited);
        coll.addAll(inherited);        
    }

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
    public SortedSet<GeoserverGrantedAuthority> personalizeRoles(GeoserverUser user, Collection<GeoserverGrantedAuthority> roles) throws IOException{
        SortedSet<GeoserverGrantedAuthority> set = new TreeSet<GeoserverGrantedAuthority>();
        for (GeoserverGrantedAuthority role : roles) {
            Properties personalizedProps = getGrantedAuthorityService().personalizeRoleParams(
                    role.getAuthority(), role.getProperties(), 
                    user.getUsername(), user.getProperties());
            if (personalizedProps==null) {
                set.add(role);
            } else { // create personalized role
                GeoserverGrantedAuthority pRole = getGrantedAuthorityService().createGrantedAuthorityObject(role.getAuthority());
                pRole.setUserName(user.getUsername());
                for (Object key : personalizedProps.keySet())
                    pRole.getProperties().put(key, personalizedProps.get(key));
                set.add(pRole);
            }                
        }
        return set;        
    }
}





