/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.geoserver.security.impl;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.geoserver.security.GeoserverGrantedAuthorityService;
import org.geoserver.security.GeoserverGrantedAuthorityStore;
import org.geoserver.security.GeoserverUserGroupService;
import org.geoserver.security.GeoserverUserGroupStore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public abstract class AbstractUserDetailsServiceTest extends AbstractSecurityServiceTest {

    
    protected GeoserverGrantedAuthorityService roleService;
    protected GeoserverUserGroupService usergroupService;
    protected GeoserverGrantedAuthorityStore roleStore;
    protected GeoserverUserGroupStore usergroupStore;
    

    
    
    protected void setServices(String serviceName) throws IOException{
        roleService=createGrantedAuthorityService(serviceName);
        usergroupService=createUserGroupService(serviceName);
        roleStore = createStore(roleService);
        usergroupStore =createStore(usergroupService);
        getSecurityManager().setActiveRoleService(roleService);
        getSecurityManager().setActiveUserGroupService(usergroupService);
    }
    
    public void testConfiguration() {
        try {
            setServices("config");
            assertEquals(roleService,getSecurityManager().getActiveRoleService());
            assertEquals(usergroupService,getSecurityManager().getActiveUserGroupService());
            assertTrue(roleService.canCreateStore());
            assertTrue(usergroupService.canCreateStore());
        } catch (IOException ex) {
            Assert.fail(ex.getMessage());
        }
    }
    
    public void testGrantedAuthorityCalculation() {
        try {

            setServices("rolecalulation");
            // populate with values
            insertValues(roleStore);
            insertValues(usergroupStore);
            
            String username = "theUser";
            GeoserverUser theUser = null;
            boolean fail=true;
            try {
                theUser = (GeoserverUser) usergroupService.loadUserByUsername(username);
            } catch (UsernameNotFoundException ex) {
                fail = false;
            }
            if (fail) {
                Assert.fail("No UsernameNotFoundException thrown");
            }
            
            theUser=usergroupStore.createUserObject(username, "", true);
            usergroupStore.addUser(theUser);
                                               
            GeoserverGrantedAuthority role = null;
            Set<GeoserverGrantedAuthority> roles = new HashSet<GeoserverGrantedAuthority>();
            
           // no roles
            checkGrantedAuthorities(username, roles);
                        
            // first direct role
            role=roleStore.createGrantedAuthorityObject("userrole1");
            roleStore.addGrantedAuthority(role);
            roleStore.associateRoleToUser(role, username);
            roles.add(role);
            checkGrantedAuthorities(username, roles);
            
            // second direct role
            role=roleStore.createGrantedAuthorityObject("userrole2");
            roleStore.addGrantedAuthority(role);
            roleStore.associateRoleToUser(role, username);
            roles.add(role);
            checkGrantedAuthorities(username, roles);

            // first role inherited by first group
            GeoserverUserGroup theGroup1=usergroupStore.createGroupObject("theGroup1",true);
            usergroupStore.addGroup(theGroup1);
            usergroupStore.associateUserToGroup(theUser, theGroup1);
            role=roleStore.createGrantedAuthorityObject("grouprole1a");
            roleStore.addGrantedAuthority(role);
            roleStore.associateRoleToGroup(role, "theGroup1");
            roles.add(role);
            checkGrantedAuthorities(username, roles);
            
            // second role inherited by first group
            role=roleStore.createGrantedAuthorityObject("grouprole1b");
            roleStore.addGrantedAuthority(role);
            roleStore.associateRoleToGroup(role, "theGroup1");
            roles.add(role);
            checkGrantedAuthorities(username, roles);

            // first role inherited by second group, but the group is disabled
            GeoserverUserGroup theGroup2=usergroupStore.createGroupObject("theGroup2",false);
            usergroupStore.addGroup(theGroup2);
            usergroupStore.associateUserToGroup(theUser, theGroup2);
            role=roleStore.createGrantedAuthorityObject("grouprole2a");
            roleStore.addGrantedAuthority(role);
            roleStore.associateRoleToGroup(role, "theGroup2");            
            checkGrantedAuthorities(username, roles);

            // enable the group
            theGroup2.setEnabled(true);
            usergroupStore.updateGroup(theGroup2);
            roles.add(role);
            checkGrantedAuthorities(username, roles);

            // check inheritance, first level
            GeoserverGrantedAuthority tmp = role;
            role=roleStore.createGrantedAuthorityObject("grouprole2aa");
            roleStore.addGrantedAuthority(role);
            roleStore.setParentRole(tmp, role);
            roles.add(role);
            checkGrantedAuthorities(username, roles);
            
            // check inheritance, second level
            tmp = role;
            role=roleStore.createGrantedAuthorityObject("grouprole2aaa");
            roleStore.addGrantedAuthority(role);
            roleStore.setParentRole(tmp, role);
            roles.add(role);
            checkGrantedAuthorities(username, roles);

            // remove second level
            tmp=roleStore.getGrantedAuthorityByName("grouprole2aa");
            roleStore.setParentRole(tmp, null);
            roles.remove(role);
            checkGrantedAuthorities(username, roles);
            
            // delete first level role
            roleStore.removeGrantedAuthority(tmp);
            roles.remove(tmp);
            checkGrantedAuthorities(username, roles);
            
            // delete second group
            usergroupStore.removeGroup(theGroup2);
            tmp=roleStore.getGrantedAuthorityByName("grouprole2a");
            roles.remove(tmp);
            checkGrantedAuthorities(username, roles);
            
            // remove role from first group
            tmp=roleStore.getGrantedAuthorityByName("grouprole1b");
            roleStore.disAssociateRoleFromGroup(tmp, theGroup1.getGroupname());
            roles.remove(tmp);
            checkGrantedAuthorities(username, roles);
            
            // remove role from user
            tmp=roleStore.getGrantedAuthorityByName("userrole2");
            roleStore.disAssociateRoleFromUser(tmp, theUser.getUsername());
            roles.remove(tmp);
            checkGrantedAuthorities(username, roles);
            
        } catch (IOException ex) {
            Assert.fail(ex.getMessage());
        }
    }
    
    public void testPersonalizedRoles() {
       try {
    
            setServices("personalizedRoles");
            // populate with values
            insertValues(roleStore);
            insertValues(usergroupStore);
            
            String username = "persUser";
            GeoserverUser theUser = null;
            
            theUser=usergroupStore.createUserObject(username, "", true);
            theUser.getProperties().put("propertyA", "A");
            theUser.getProperties().put("propertyB", "B");
            theUser.getProperties().put("propertyC", "C");
            usergroupStore.addUser(theUser);
                                               
            GeoserverGrantedAuthority role = null;
                        
            role=roleStore.createGrantedAuthorityObject("persrole1");
            role.getProperties().put("propertyA", "");
            role.getProperties().put("propertyX", "X");
            roleStore.addGrantedAuthority(role);
            roleStore.associateRoleToUser(role, username);
            
            role=roleStore.createGrantedAuthorityObject("persrole2");
            role.getProperties().put("propertyB", "");
            role.getProperties().put("propertyY", "Y");
            roleStore.addGrantedAuthority(role);
            roleStore.associateRoleToUser(role, username);
            
            syncbackends();
            
            UserDetails details = usergroupService.loadUserByUsername(username);
            
            Collection<GrantedAuthority> authColl = details.getAuthorities();
            
            for (GrantedAuthority auth : authColl) {
                role = (GeoserverGrantedAuthority) auth;
                if ("persrole1".equals(role.getAuthority())) {
                    assertEquals("A", role.getProperties().get("propertyA"));
                    assertEquals("X", role.getProperties().get("propertyX"));
                    
                    GeoserverGrantedAuthority anonymousRole = 
                        roleStore.getGrantedAuthorityByName(role.getAuthority());
                    
                    assertFalse(role.isAnonymous());
                    assertTrue(anonymousRole.isAnonymous());
                    assertFalse(role==anonymousRole);
                    assertFalse(role.equals(anonymousRole));
                    assertTrue(theUser.getUsername().equals(role.getUserName()));
                    assertNull(anonymousRole.getUserName());
                    
                } else if ("persrole2".equals(role.getAuthority())) {
                    assertEquals("B", role.getProperties().get("propertyB"));
                    assertEquals("Y", role.getProperties().get("propertyY"));                                        
                } else {
                    Assert.fail("Unknown role "+role.getAuthority() + "for user " + username);
                }                                        
            }
            
       } catch (IOException ex) {       
           Assert.fail(ex.getMessage());
       }                
    }
    
    protected void checkGrantedAuthorities(String username, Set<GeoserverGrantedAuthority> roles) throws IOException{
        syncbackends();
        UserDetails details = usergroupService.loadUserByUsername(username);
        Collection<GrantedAuthority> authColl = details.getAuthorities();
        assertEquals(roles.size(), authColl.size());
        for (GeoserverGrantedAuthority role : roles) {
            assertTrue(authColl.contains(role));
        }
    }
    
    protected void syncbackends() throws IOException{
        roleStore.store();
        usergroupStore.store();

    }
}
