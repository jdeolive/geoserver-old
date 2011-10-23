/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.geoserver.security.impl;


import java.io.File;
import java.io.IOException;

import org.geoserver.data.test.LiveData;
import org.geoserver.data.test.TestData;
import org.geoserver.security.GeoserverRoleService;
import org.geoserver.security.GeoserverRoleStore;
import org.geoserver.security.GeoserverUserGroupService;
import org.geoserver.security.GeoserverUserGroupStore;
import org.geoserver.test.GeoServerAbstractTestSupport;


/**
 * Base test class
 * 
 * @author christian
 *
 */
public abstract class AbstractSecurityServiceTest extends GeoServerAbstractTestSupport {

    public GeoserverUserGroupService createUserGroupService(String name) throws IOException {
        return null;
    }
    public GeoserverRoleService createRoleService(String name) throws IOException {
        return null;
    }

    public GeoserverUserGroupStore createStore(GeoserverUserGroupService service) throws IOException {
        return service.createStore();
    }
    public GeoserverRoleStore createStore(GeoserverRoleService service) throws IOException {
        return service.createStore(); 
    }
    protected void checkEmpty(GeoserverRoleService roleService) throws IOException {
        assertEquals(0, roleService.getRoles().size());
    }
    public void insertValues(GeoserverRoleStore roleStore) throws IOException {
        
        GeoserverRole role_admin = 
            roleStore.createRoleObject(GeoserverRole.ADMIN_ROLE.getAuthority());        
        GeoserverRole role_auth = 
            roleStore.createRoleObject("ROLE_AUTHENTICATED" );
        GeoserverRole role_wfs = 
            roleStore.createRoleObject("ROLE_WFS");
        GeoserverRole role_wms = 
            roleStore.createRoleObject("ROLE_WMS");
                        
        role_auth.getProperties().put("employee","");
        role_auth.getProperties().put("bbox","lookupAtRuntime");
                        
        roleStore.addRole(role_admin);
        roleStore.addRole(role_auth);
        roleStore.addRole(role_wfs);
        roleStore.addRole(role_wms);
        
        roleStore.setParentRole(role_wms, role_auth);
        roleStore.setParentRole(role_wfs, role_auth);
    
                
        
        roleStore.associateRoleToUser(role_admin, "admin");
        roleStore.associateRoleToUser(role_wms, "user1");
        roleStore.associateRoleToUser(role_wfs, "user1");
        
        roleStore.associateRoleToGroup(role_wms, "g_wms");
        roleStore.associateRoleToGroup(role_wfs, "g_wfs");
        roleStore.associateRoleToGroup(role_wms, "g_all");
        roleStore.associateRoleToGroup(role_wfs, "g_all");
        
    }
    public void removeValues(GeoserverRoleStore roleStore) throws IOException {
        GeoserverRole role_auth = 
            roleStore.createRoleObject("ROLE_AUTHENTICATED" );
        GeoserverRole role_wfs = roleStore.getRoleByName("ROLE_WFS");
        roleStore.removeRole(role_wfs);
        roleStore.removeRole(role_auth);
    }
    public void modifyValues(GeoserverRoleStore roleStore) throws IOException {
        
        GeoserverRole role_auth = roleStore.getRoleByName("ROLE_AUTHENTICATED");
        GeoserverRole role_wfs = roleStore.getRoleByName("ROLE_WFS");
        GeoserverRole role_wms = roleStore.getRoleByName("ROLE_WMS");
        
        role_auth.getProperties().remove("bbox");
        role_auth.getProperties().setProperty("employee","4711");
        roleStore.updateRole(role_auth);
        
        role_wms.getProperties().setProperty("envelope", "10 10 20 20");
        roleStore.updateRole(role_wms);
        
        roleStore.disAssociateRoleFromGroup(role_wfs, "g_all");
        roleStore.disAssociateRoleFromUser(role_wfs, "user1");
        roleStore.setParentRole(role_wms, null);
        roleStore.setParentRole(role_wfs, role_wms);
    }
    protected void checkValuesRemoved(GeoserverRoleService roleService) throws IOException {
        GeoserverRole role_admin = roleService.getRoleByName(
                GeoserverRole.ADMIN_ROLE.getAuthority());
        GeoserverRole role_wms = roleService.getRoleByName("ROLE_WMS");
    
        assertEquals(2, roleService.getRoles().size());
        assertTrue(roleService.getRoles().contains(role_admin));        
        assertTrue(roleService.getRoles().contains(role_wms));
        
        assertNull(roleService.getParentRole(role_wms));
        assertEquals(1, roleService.getRolesForUser("user1").size());
        assertTrue(roleService.getRolesForUser("user1").contains(role_wms));
        
        assertEquals(0, roleService.getRolesForGroup("g_wfs").size());
        assertEquals(1, roleService.getRolesForGroup("g_all").size());
        assertTrue(roleService.getRolesForGroup("g_all").contains(role_wms));
    }
    protected void checkValuesModified(GeoserverRoleService roleService) throws IOException {
        GeoserverRole role_auth = roleService.getRoleByName("ROLE_AUTHENTICATED");
        GeoserverRole role_wms = roleService.getRoleByName("ROLE_WMS");
        GeoserverRole role_wfs = roleService.getRoleByName("ROLE_WFS");
        
        assertEquals(1,role_auth.getProperties().size());
        assertEquals("4711", role_auth.getProperties().get("employee"));
        assertEquals(1,role_wms.getProperties().size());
        assertEquals("10 10 20 20", role_wms.getProperties().get("envelope"));
        assertEquals(0,role_wfs.getProperties().size());
        
        for (GeoserverRole role : roleService.getRoles()) {
            if ("ROLE_AUTHENTICATED".equals(role.getAuthority())) {
                assertEquals(1,role.getProperties().size());
                assertEquals("4711", role.getProperties().get("employee"));                
            }
            else if ("ROLE_WMS".equals(role.getAuthority())) {
                assertEquals(1,role.getProperties().size());
                assertEquals("10 10 20 20", role.getProperties().get("envelope"));
            } else {
                assertEquals(0,role.getProperties().size());
            }
                
        }
        assertEquals(1,roleService.getGroupNamesForRole(role_wfs).size());
        assertTrue(roleService.getGroupNamesForRole(role_wfs).contains("g_wfs"));
        assertEquals(0,roleService.getUserNamesForRole(role_wfs).size());
        
        assertEquals(1,roleService.getRolesForGroup("g_all").size());
        assertTrue(roleService.getRolesForGroup("g_all").contains(role_wms));
        GeoserverRole role = roleService.getRolesForGroup("g_all").iterator().next();
        assertEquals(1,role.getProperties().size());
        assertEquals("10 10 20 20", role.getProperties().get("envelope"));
        
        assertEquals(1,roleService.getRolesForUser("user1").size());
        assertTrue(roleService.getRolesForUser("user1").contains(role_wms));
        role = roleService.getRolesForUser("user1").iterator().next();
        assertEquals(1,role.getProperties().size());
        assertEquals("10 10 20 20", role.getProperties().get("envelope"));
        
        assertNull(roleService.getParentRole(role_wms));
        assertEquals(role_wms,roleService.getParentRole(role_wfs));
    }
    protected void checkValuesInserted(GeoserverRoleService roleService) throws IOException {
    
        GeoserverRole role_auth = roleService.getRoleByName("ROLE_AUTHENTICATED");
        GeoserverRole role_wfs = roleService.getRoleByName("ROLE_WFS");
        GeoserverRole role_wms = roleService.getRoleByName("ROLE_WMS");
        GeoserverRole role_admin = roleService.getRoleByName(
                GeoserverRole.ADMIN_ROLE.getAuthority());
    
        
        assertEquals(4, roleService.getRoles().size());
        assertTrue(roleService.getRoles().contains(role_admin));
        assertTrue(roleService.getRoles().contains(role_auth));
        assertTrue(roleService.getRoles().contains(role_wfs));
        assertTrue(roleService.getRoles().contains(role_wms));
                
        
        assertNull (roleService.getRoleByName("xxx"));
        
        for (GeoserverRole role : roleService.getRoles() ) {
            if (role_auth.getAuthority().equals(role.getAuthority())) {
                assertEquals(2,role.getProperties().size());
                assertEquals(role.getProperties().getProperty("employee"),"");
                assertEquals(role.getProperties().getProperty("bbox"),"lookupAtRuntime");
            } else {
                assertEquals(0,role.getProperties().size());
            }
            
        }
        
        
        assertEquals(0,role_admin.getProperties().size());
        assertEquals(0,role_wfs.getProperties().size());
        assertEquals(0,role_wms.getProperties().size());
        
        assertEquals(2,role_auth.getProperties().size());
        assertEquals(role_auth.getProperties().getProperty("employee"),"");
        assertEquals(role_auth.getProperties().getProperty("bbox"),"lookupAtRuntime");
        
        
        
        assertNull(roleService.getParentRole(role_admin));
        assertNull(roleService.getParentRole(role_auth));
        assertEquals(role_auth,roleService.getParentRole(role_wms));
        assertEquals(role_auth,roleService.getParentRole(role_wfs));
        assertEquals(2,roleService.getParentRole(role_wfs).getProperties().size());
        assertEquals(roleService.getParentRole(role_wfs).getProperties().getProperty("employee"),"");
        assertEquals(roleService.getParentRole(role_wfs).getProperties().getProperty("bbox"),"lookupAtRuntime");
        
        
    
        assertEquals(0, roleService.getRolesForUser("xxx").size());
        assertEquals(1, roleService.getRolesForUser("admin").size());
        assertTrue(roleService.getRolesForUser("admin").contains(GeoserverRole.ADMIN_ROLE));
        
        assertEquals(2, roleService.getRolesForUser("user1").size());
        assertTrue(roleService.getRolesForUser("user1").contains(role_wfs));
        assertTrue(roleService.getRolesForUser("user1").contains(role_wms));
        
        assertEquals(0, roleService.getRolesForGroup("xxx").size());
        
        assertEquals(1, roleService.getRolesForGroup("g_wfs").size());
        assertTrue(roleService.getRolesForGroup("g_wfs").contains(role_wfs));
        
        assertEquals(1, roleService.getRolesForGroup("g_wms").size());
        assertTrue(roleService.getRolesForGroup("g_wms").contains(role_wms));
    
        assertEquals(2, roleService.getRolesForGroup("g_all").size());
        assertTrue(roleService.getRolesForGroup("g_all").contains(role_wfs));
        assertTrue(roleService.getRolesForGroup("g_all").contains(role_wms));
        
        assertEquals(1,roleService.getUserNamesForRole(GeoserverRole.ADMIN_ROLE).size());
        assertTrue(roleService.getUserNamesForRole(GeoserverRole.ADMIN_ROLE).contains("admin"));        
        assertEquals(1,roleService.getUserNamesForRole(role_wfs).size());
        assertTrue(roleService.getUserNamesForRole(role_wfs).contains("user1"));
        assertEquals(1,roleService.getUserNamesForRole(role_wms).size());
        assertTrue(roleService.getUserNamesForRole(role_wms).contains("user1"));
        assertEquals(0,roleService.getUserNamesForRole(
                roleService.createRoleObject("xxx")).size());
    
        assertEquals(2,roleService.getGroupNamesForRole(role_wfs).size());
        assertTrue(roleService.getGroupNamesForRole(role_wfs).contains("g_wfs"));
        assertTrue(roleService.getGroupNamesForRole(role_wfs).contains("g_all"));
    
        
        assertEquals(2,roleService.getGroupNamesForRole(role_wms).size());
        assertTrue(roleService.getGroupNamesForRole(role_wms).contains("g_wms"));
        assertTrue(roleService.getGroupNamesForRole(role_wms).contains("g_all"));
    
        assertEquals(0,roleService.getGroupNamesForRole(
                roleService.createRoleObject("xxx")).size());
        
        assertEquals (4,roleService.getParentMappings().size());
        assertNull (roleService.getParentMappings().get(
                GeoserverRole.ADMIN_ROLE.getAuthority()));
        assertNull (roleService.getParentMappings().get(role_auth.getAuthority()));
        assertEquals(roleService.getParentMappings().get(role_wfs.getAuthority()), role_auth.getAuthority());
        assertEquals(roleService.getParentMappings().get(role_wms.getAuthority()), role_auth.getAuthority());
        
    }
    protected void checkEmpty(GeoserverUserGroupService userService) throws IOException {
        assertEquals(0, userService.getUsers().size());
        assertEquals(0, userService.getUserGroups().size());
    }
    protected void checkValuesInserted(GeoserverUserGroupService userGroupService) throws IOException {
        assertEquals(4, userGroupService.getUsers().size());
        
        GeoserverUser admin = GeoserverUser.createDefaultAdmin();
        GeoserverUser user1 = (GeoserverUser) userGroupService.getUserByUsername("user1");
        GeoserverUser user2 = (GeoserverUser) userGroupService.getUserByUsername("user2");
        GeoserverUser disableduser = (GeoserverUser) userGroupService.getUserByUsername("disableduser");
        
        assertNull(userGroupService.getUserByUsername("xxx"));
    
        assertTrue(userGroupService.getUsers().contains(admin));
        assertTrue(userGroupService.getUsers().contains(user1));
        assertTrue(userGroupService.getUsers().contains(user2));
        assertTrue(userGroupService.getUsers().contains(disableduser));
        // check if properties are loaded too
        for (GeoserverUser user : userGroupService.getUsers() ) {
            if (user2.getUsername().equals(user.getUsername())) {
                assertEquals(2,user.getProperties().size());
                assertEquals(user.getProperties().getProperty("mail"),"user2@gmx.com");
                assertEquals(user.getProperties().getProperty("tel"),"12-34-38");                
            } else {
                assertEquals(0,user.getProperties().size());
            }
        }
        
        assertTrue(admin.isEnabled());
        assertTrue(user1.isEnabled());
        assertTrue(user1.isEnabled());
        assertFalse(disableduser.isEnabled());
        
        assertEquals("geoserver",admin.getPassword());
        assertEquals("11111",user1.getPassword());
        assertEquals("22222",user2.getPassword());
        assertEquals("",disableduser.getPassword());
        
        assertEquals(0,admin.getProperties().size());
        assertEquals(0,user1.getProperties().size());
        assertEquals(0,disableduser.getProperties().size());
        
        assertEquals(2,user2.getProperties().size());        
        assertEquals(user2.getProperties().getProperty("mail"),"user2@gmx.com");
        assertEquals(user2.getProperties().getProperty("tel"),"12-34-38");
    
        assertEquals(3, userGroupService.getUserGroups().size());
        GeoserverUserGroup admins = userGroupService.getGroupByGroupname("admins");
        GeoserverUserGroup group1 = userGroupService.getGroupByGroupname("group1");
        GeoserverUserGroup disabledgroup = userGroupService.getGroupByGroupname("disabledgroup");
        
        assertNull(userGroupService.getGroupByGroupname("yyy"));
        
        assertTrue(userGroupService.getUserGroups().contains(admins));
        assertTrue(userGroupService.getUserGroups().contains(group1));
        assertTrue(userGroupService.getUserGroups().contains(disabledgroup));
    
        assertTrue(admins.isEnabled());
        assertTrue(group1.isEnabled());
        assertFalse(disabledgroup.isEnabled());
        
        assertEquals(2,userGroupService.getUsersForGroup(group1).size());
        assertTrue(userGroupService.getUsersForGroup(group1).contains(user1));
        assertTrue(userGroupService.getUsersForGroup(group1).contains(user2));
        // check if properties are loaded too
        for (GeoserverUser user : userGroupService.getUsersForGroup(group1) ) {
            if (user2.getUsername().equals(user.getUsername())) {
                assertEquals(2,user.getProperties().size());
                assertEquals(user.getProperties().getProperty("mail"),"user2@gmx.com");
                assertEquals(user.getProperties().getProperty("tel"),"12-34-38");                
            } else {
                assertEquals(0,user.getProperties().size());
            }
        }
    
        assertEquals(1,userGroupService.getUsersForGroup(admins).size());
        assertTrue(userGroupService.getUsersForGroup(admins).contains(admin));
        
        assertEquals(1,userGroupService.getUsersForGroup(disabledgroup).size());
        assertTrue(userGroupService.getUsersForGroup(disabledgroup).contains(disableduser));
        
        assertEquals(1,userGroupService.getGroupsForUser(admin).size());
        assertTrue(userGroupService.getGroupsForUser(admin).contains(admins));
    
        assertEquals(1,userGroupService.getGroupsForUser(user1).size());
        assertTrue(userGroupService.getGroupsForUser(user1).contains(group1));
        
        assertEquals(1,userGroupService.getGroupsForUser(user2).size());
        assertTrue(userGroupService.getGroupsForUser(user2).contains(group1));
    
        assertEquals(1,userGroupService.getGroupsForUser(disableduser).size());
        assertTrue(userGroupService.getGroupsForUser(disableduser).contains(disabledgroup));
    
    
    }
    protected void checkValuesModified(GeoserverUserGroupService userGroupService) throws IOException {
        GeoserverUser disableduser = userGroupService.getUserByUsername("disableduser");
        assertTrue(disableduser.isEnabled());
        assertEquals("hallo", disableduser.getPassword());
        assertEquals(1, disableduser.getProperties().size());
        assertEquals("miller", disableduser.getProperties().getProperty("lastname"));
        
        GeoserverUser user2 = userGroupService.getUserByUsername("user2");
        assertEquals(1, user2.getProperties().size());
        assertEquals("11-22-33", user2.getProperties().getProperty("tel"));
        
        GeoserverUserGroup disabledgroup = userGroupService.getGroupByGroupname("disabledgroup");
        assertTrue(disabledgroup.isEnabled());
    
        GeoserverUserGroup group1 = userGroupService.getGroupByGroupname("group1");
        GeoserverUser user1 = userGroupService.getUserByUsername("user1");
        assertEquals(1,userGroupService.getUsersForGroup(group1).size());
        assertTrue(userGroupService.getUsersForGroup(group1).contains(user1));
    
        assertEquals(0,userGroupService.getGroupsForUser(user2).size());
    }
    protected void checkValuesRemoved(GeoserverUserGroupService userGroupService) throws IOException {
        
        GeoserverUser admin = GeoserverUser.createDefaultAdmin();
        GeoserverUser user1 = (GeoserverUser) userGroupService.getUserByUsername("user1");
        GeoserverUser disableduser = (GeoserverUser) userGroupService.getUserByUsername("disableduser");
    
        assertEquals(3, userGroupService.getUsers().size());
        assertTrue(userGroupService.getUsers().contains(admin));
        assertTrue(userGroupService.getUsers().contains(user1));
        assertTrue(userGroupService.getUsers().contains(disableduser));
        
        GeoserverUserGroup admins = userGroupService.getGroupByGroupname("admins");
        GeoserverUserGroup group1 = userGroupService.getGroupByGroupname("group1");
        assertEquals(2, userGroupService.getUserGroups().size());
        assertTrue(userGroupService.getUserGroups().contains(admins));
        assertTrue(userGroupService.getUserGroups().contains(group1));
    
        assertEquals(0, userGroupService.getGroupsForUser(disableduser).size());
        assertEquals(1, userGroupService.getUsersForGroup(group1).size());
        assertTrue(userGroupService.getUsersForGroup(group1).contains(user1));
    }
    public void insertValues(GeoserverUserGroupStore userGroupStore) throws IOException {
                
        GeoserverUser admin = userGroupStore.createUserObject(GeoserverUser.AdminName, 
                GeoserverUser.AdminPasword, GeoserverUser.AdminEnabled);
        GeoserverUser user1 = userGroupStore.createUserObject("user1", "11111", true);
        GeoserverUser user2 = userGroupStore.createUserObject("user2", "22222", true);
        GeoserverUser disableduser = userGroupStore.createUserObject("disableduser", "", false);
        
        user2.getProperties().put("mail","user2@gmx.com");
        user2.getProperties().put("tel","12-34-38");
                
        userGroupStore.addUser(admin);
        userGroupStore.addUser(user1);
        userGroupStore.addUser(user2);
        userGroupStore.addUser(disableduser);
        
        GeoserverUserGroup admins = userGroupStore.createGroupObject("admins", true);
        GeoserverUserGroup group1 = userGroupStore.createGroupObject("group1",true);
        GeoserverUserGroup disabledgroup = userGroupStore.createGroupObject("disabledgroup",false);
        
        userGroupStore.addGroup(admins);
        userGroupStore.addGroup(group1);
        userGroupStore.addGroup(disabledgroup);
        
        userGroupStore.associateUserToGroup(admin, admins);
        userGroupStore.associateUserToGroup(user1, group1);
        userGroupStore.associateUserToGroup(user2, group1);
        userGroupStore.associateUserToGroup(disableduser, disabledgroup);
    
    }
    public void modifyValues(GeoserverUserGroupStore userGroupStore) throws IOException {
        GeoserverUser disableduser = userGroupStore.getUserByUsername("disableduser");
        disableduser.setEnabled(true);
        disableduser.setPassword("hallo");
        disableduser.getProperties().put("lastname","miller");
        userGroupStore.updateUser(disableduser);
        
        GeoserverUser user2 = userGroupStore.getUserByUsername("user2");
        user2.getProperties().remove("mail");
        user2.getProperties().put("tel", "11-22-33");
        userGroupStore.updateUser(user2);        
        
        GeoserverUserGroup disabledgroup = userGroupStore.getGroupByGroupname("disabledgroup");
        disabledgroup.setEnabled(true);
        userGroupStore.updateGroup(disabledgroup);
                
        GeoserverUserGroup group1 = userGroupStore.getGroupByGroupname("group1");
        userGroupStore.disAssociateUserFromGroup(user2, group1);
        
    }
    public void removeValues(GeoserverUserGroupStore userGroupStore) throws IOException {
        GeoserverUser user2 = userGroupStore.getUserByUsername("user2");
        userGroupStore.removeUser(user2);
        GeoserverUserGroup disabledGroup = userGroupStore.getGroupByGroupname("disabledgroup");
        userGroupStore.removeGroup(disabledGroup);
    }
    
    @Override
    protected TestData buildTestData() throws Exception {
        
        File data = new File("./src/test/resources/datadir");        
        return new LiveData(data);
    }
    
    /**
     * Indicates if the test is a JDBC test
     * All test are based on the fact that locking appears
     * at row level.
     * This is not true for JDBC databases
     * - Locking may be based on blocks
     * - Since only a few records are in the table,
     *   the whole table my be locked. (lock escalation)
     *   
     * Use this method to avoid to aggressive checks  
     * 
     * 
     * @return 
     */
    protected boolean isJDBCTest() {
        return false;
    }

}
