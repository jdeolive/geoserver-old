/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.security.impl;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.security.GeoserverGrantedAuthorityService;
import org.geoserver.security.GeoserverUserDetailsService;
import org.geoserver.security.GeoserverServiceFactory;
import org.geoserver.security.GeoserverStoreFactory;
import org.geoserver.security.GeoserverUserGroupService;
import org.geoserver.security.config.UserDetailsServiceConfig;
import org.geoserver.security.event.GrantedAuthorityLoadedEvent;
import org.geoserver.security.event.GrantedAuthorityLoadedListener;
import org.geoserver.security.event.UserGroupLoadedEvent;
import org.geoserver.security.event.UserGroupLoadedListener;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * An implementation of {@link GeoserverUserDetailsService}
 * 
 * @author christian
 *
 */
public class GeoserverUserDetailsServiceImpl implements GeoserverUserDetailsService,GrantedAuthorityLoadedListener,UserGroupLoadedListener {
    


    protected GeoserverGrantedAuthorityService grantedAuthoriyService;
    protected GeoserverUserGroupService userGroupService;
    
    public static GeoserverUserDetailsServiceImpl get() {
        return (GeoserverUserDetailsServiceImpl) GeoServerExtensions.bean("userDetailsService");
    }

    
    
    public GeoserverUserDetailsServiceImpl() throws IOException{
        super();
        Util.migrateIfNeccessary();
        UserDetailsServiceConfig config = Util.loadSecurityServiceConfig();
        initializeFrom(config);
    }
    
    
    /* (non-Javadoc)
     * @see org.springframework.security.core.userdetails.UserDetailsService#loadUserByUsername(java.lang.String)
     */
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException,
            DataAccessException {
        assertServicesNotNull();
        GeoserverUser user=null;
        try {
            user = userGroupService.getUserByUsername(username);
        } catch (IOException e) {
            throw new UsernameNotFoundException("User not found: " + username,e);
        }
        if (user == null)
            throw new UsernameNotFoundException("User not found: " + username);
        
        user.resetGrantedAuthorities();
        return user;
    }

    public void setGrantedAuthorityService(GeoserverGrantedAuthorityService service) {
        if (grantedAuthoriyService!=null)
            grantedAuthoriyService.unregisterGrantedAuthorityLoadedListener(this);
        
        grantedAuthoriyService=service;
        grantedAuthoriyService.registerGrantedAuthorityLoadedListener(this);
    }

    public GeoserverGrantedAuthorityService getGrantedAuthorityService() {
        return grantedAuthoriyService;
    }

    public boolean isGrantedAuthorityStore() {
        assertServicesNotNull();
        return GeoserverStoreFactory.Singleton.hasStoreFor(getGrantedAuthorityService());
        
    }

    public void setUserGroupService(GeoserverUserGroupService service) {
        if (userGroupService!=null)
            userGroupService.unregisterUserGroupLoadedListener(this);
        userGroupService=service;
        userGroupService.registerUserGroupLoadedListener(this);
    }

    public GeoserverUserGroupService getUserGroupService() {
        return userGroupService;
    }

    public boolean isUserGroupStore() {
        assertServicesNotNull();
        return GeoserverStoreFactory.Singleton.hasStoreFor(getUserGroupService());
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


    /* (non-Javadoc)
     * @see org.geoserver.security.event.UserGroupChangedListener#usersAndGroupsChanged(org.geoserver.security.event.UserGroupChangedEvent)
     */
    public void usersAndGroupsChanged(UserGroupLoadedEvent event) {
    }


    /* (non-Javadoc)
     * @see org.geoserver.security.event.GrantedAuthorityChangedListener#grantedAuthoritiesChanged(org.geoserver.security.event.GrantedAuthorityChangedEvent)
     */
    public void grantedAuthoritiesChanged(GrantedAuthorityLoadedEvent event) {
    }


    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverSecurityService#calculateGrantedAuthorities(org.geoserver.security.impl.GeoserverUser)
     */
    @Override
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
        
        return Collections.unmodifiableSortedSet(set2);
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


    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverSecurityService#initializeFrom(org.geoserver.security.config.SecurityServiceConfig)
     */
    @Override
    public void initializeFrom(UserDetailsServiceConfig config) throws IOException {
        setUserGroupService(
            GeoserverServiceFactory.Singleton.getUserGroupService(
                    config.getUserGroupServiceName()));
        
        setGrantedAuthorityService(
                GeoserverServiceFactory.Singleton.getGrantedAuthorityService(
                        config.getGrantedAuthorityServiceName()));
    }


    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverUserDetailsService#calculateGrantedAuthorities(org.geoserver.security.impl.GeoserverUserGroup)
     */
    public SortedSet<GeoserverGrantedAuthority> calculateGrantedAuthorities(GeoserverUserGroup group) throws IOException {
        
        SortedSet<GeoserverGrantedAuthority> roles = new TreeSet<GeoserverGrantedAuthority>();
        roles.addAll(getGrantedAuthorityService().getRolesForGroup(group.getGroupname()));
        addInheritedRoles(roles);
        return roles;
    }
    
    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverUserDetailsService#addInheritedRoles(java.util.SortedSet)
     */
    public void addInheritedRoles(Collection<GeoserverGrantedAuthority> coll) throws IOException {
        Set<GeoserverGrantedAuthority> inherited = new HashSet<GeoserverGrantedAuthority>();
        for (GeoserverGrantedAuthority role : coll)
            addParentRole(role, inherited);
        coll.addAll(inherited);        
    }
    
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
