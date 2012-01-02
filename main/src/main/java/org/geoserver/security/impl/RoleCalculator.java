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

import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.security.GeoServerSecurityManager;
import org.geoserver.security.GeoserverRoleService;
import org.geoserver.security.GeoserverUserGroupService;

/**
 * Helper Object for role calculations 
 * 
 * @author christian
 *
 */
public class RoleCalculator  {
    

    protected GeoserverRoleService roleService;
    protected GeoserverUserGroupService userGroupService;

    /**
     * Constructor
     * 
     * @param userGroupService
     * @param roleService
     */
    public RoleCalculator (GeoserverUserGroupService userGroupService,GeoserverRoleService roleService) {
        this.userGroupService=userGroupService;
        this.roleService=roleService;
        assertServicesNotNull();
    }
        
    public void setRoleService(GeoserverRoleService service) {
        roleService=service;
        assertServicesNotNull();

    }

    public GeoserverRoleService getRoleService() {
        return roleService;
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
        if (roleService==null) {
            throw new RuntimeException("role service Service is null");
        }
    }


    /**
     * Calculate the {@link GeoserverRole} objects for a user
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
     * If the user has the admin role of the active role service,
     * {@link GeoserverRole#ADMIN_ROLE} is also included in the set. 
     * 
     * @param user
     * @return
     * @throws IOException
     */  
    public SortedSet<GeoserverRole> calculateRoles(GeoserverUser user)
            throws IOException {
        
        Set<GeoserverRole> set1 = new HashSet<GeoserverRole>();
        
        // alle roles for the user
        set1.addAll(getRoleService().getRolesForUser(user.getUsername()));
        addInheritedRoles(set1);
        
        // add all roles for enabled groups
        for (GeoserverUserGroup group : 
            getUserGroupService().getGroupsForUser(user)) {
            if (group.isEnabled())
                set1.addAll(calculateRoles(group));
        }
        
       // personalize roles
        SortedSet<GeoserverRole> set2 = 
                personalizeRoles(user, set1);
        
        // if the user has the admin role of the role service the 
        // GeoserverRole.ADMIN_ROLE must also be in the set
        GeoserverRole adminRole = GeoServerExtensions.bean(GeoServerSecurityManager.class).getActiveRoleService().getAdminRole();
        if (adminRole!=null) {
            String adminRoleName = adminRole.getAuthority();
            if (adminRoleName != null && adminRoleName.length()> 0 
                    && (adminRoleName.equals(GeoserverRole.ADMIN_ROLE.getAuthority())==false)) {
                if (set2.contains(adminRole)) {
                    set2.add(GeoserverRole.ADMIN_ROLE);
                }
            }
        }
        return set2;
    }
    
    /**
     * Collects the ascendents for a {@link GeoserverRole} object
     * 
     * @param role
     * @param inherited
     */
    protected void addParentRole(GeoserverRole role,Collection<GeoserverRole> inherited) throws IOException{
        GeoserverRole parentRole = 
            getRoleService().getParentRole(role);
        if (parentRole==null) 
            return; // end of recursion
        
        if (inherited.contains(parentRole))
            return; // end of recursion
        
        inherited.add(parentRole);
        // recursion
        addParentRole(parentRole, inherited);
    }

    /**
     * Calculate the {@link GeoserverRole} objects for a group
     * including inherited roles
     * 
     * @param group
     * @return
     * @throws IOException
     */
    public SortedSet<GeoserverRole> calculateRoles(GeoserverUserGroup group) throws IOException {
        
        SortedSet<GeoserverRole> roles = new TreeSet<GeoserverRole>();
        roles.addAll(getRoleService().getRolesForGroup(group.getGroupname()));
        addInheritedRoles(roles);
        return roles;
    }
    
    /**
     * Adds inherited roles to a role set
     * 
     * @param coll
     * @throws IOException
     */
    public void addInheritedRoles(Collection<GeoserverRole> coll) throws IOException {
        Set<GeoserverRole> inherited = new HashSet<GeoserverRole>();
        for (GeoserverRole role : coll)
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
    public SortedSet<GeoserverRole> personalizeRoles(GeoserverUser user, Collection<GeoserverRole> roles) throws IOException{
        SortedSet<GeoserverRole> set = new TreeSet<GeoserverRole>();
        for (GeoserverRole role : roles) {
            Properties personalizedProps = getRoleService().personalizeRoleParams(
                    role.getAuthority(), role.getProperties(), 
                    user.getUsername(), user.getProperties());
            if (personalizedProps==null) {
                set.add(role);
            } else { // create personalized role
                GeoserverRole pRole = getRoleService().createRoleObject(role.getAuthority());
                pRole.setUserName(user.getUsername());
                for (Object key : personalizedProps.keySet())
                    pRole.getProperties().put(key, personalizedProps.get(key));
                set.add(pRole);
            }                
        }
        return set;        
    }
}





