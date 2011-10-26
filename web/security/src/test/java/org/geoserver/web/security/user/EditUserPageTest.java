package org.geoserver.web.security.user;

import java.util.SortedSet;

import org.apache.wicket.extensions.markup.html.form.palette.component.Recorder;
import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.security.impl.GeoserverRole;
import org.geoserver.security.impl.GeoserverUser;
import org.geoserver.security.impl.GeoserverUserGroup;
import org.geoserver.security.password.GeoserverPasswordEncoder;

public class EditUserPageTest extends AbstractUserPageTest {

    
    GeoserverUser current;

    public void testFill() throws Exception{
        initializeForXML();
        doTestFill();
    }

    protected void doTestFill() throws Exception {
        initializeForXML();
        insertValues();
        addAdditonalData();
        
        current = ugService.getUserByUsername("user1");
        initializeTester();
        tester.assertRenderedPage(EditUserPage.class);
        
        
        assertFalse(tester.getComponentFromLastRenderedPage("userForm:username").isEnabled());

        tester.assertModelValue("userForm:username", "user1");
        GeoserverPasswordEncoder encoder = (GeoserverPasswordEncoder)
                GeoServerExtensions.bean(ugService.getPasswordEncoderName());
        String enc = (String) tester.getComponentFromLastRenderedPage("userForm:password").getDefaultModelObject();
        assertTrue(encoder.isPasswordValid(enc,"11111", null));
        enc = (String) tester.getComponentFromLastRenderedPage("userForm:confirmPassword").getDefaultModelObject();
        assertTrue(encoder.isPasswordValid(enc,"11111", null));
        tester.assertModelValue("userForm:enabled", Boolean.TRUE);
        
        newFormTester();        
        form.setValue("enabled", false);
        addUserProperty("coord", "10 10");
        
        assertTrue(page.userRolesFormComponent.isEnabled());
        tester.assertComponent("userForm:roles:roles:recorder", Recorder.class);
                                
        addNewRole("ROLE_NEW");        
        tester.assertRenderedPage(EditUserPage.class);
        
        assignRole("ROLE_NEW");
        
        // reopen new role dialog again to ensure that the current state is not lost
        openCloseRolePanel(EditUserPage.class);
        assertCalculatedRoles(new String[] { "ROLE_AUTHENTICATED","ROLE_NEW","ROLE_WFS","ROLE_WMS" });
        
        addNewGroup("testgroup");
        assignGroup("testgroup");
        
        openCloseGroupPanel(EditUserPage.class);
        
        
        assertCalculatedRoles(new String[] { "ROLE_NEW" });
        //print(tester.getLastRenderedPage(),true,true);
        form.submit("save");
        
        tester.assertErrorMessages(new String[0]);
        tester.assertRenderedPage(UserPage.class);
        
        GeoserverUser user = ugService.getUserByUsername("user1");
        assertNotNull(user);
        assertFalse(user.isEnabled());
        
        assertEquals(1,user.getProperties().size());
        assertEquals("10 10",user.getProperties().get("coord"));
        SortedSet<GeoserverUserGroup> groupList = ugService.getGroupsForUser(user);
        assertEquals(1,groupList.size());
        assertTrue(groupList.contains(ugService.getGroupByGroupname("testgroup")));
                
        SortedSet<GeoserverRole> roleList = gaService.getRolesForUser("user1");
        assertEquals(1,roleList.size());
        assertTrue(roleList.contains(gaService.getRoleByName("ROLE_NEW")));
    }
    
    public void testReadOnlyUserGroupService() throws Exception {
        initializeForXML();
        doTestReadOnlyUserGroupService();
    }

    protected void doTestReadOnlyUserGroupService() throws Exception {
        insertValues();
        activateROUGService();
        
        current = ugService.getUserByUsername("user1");
        initializeTester();
        tester.assertRenderedPage(EditUserPage.class);
        
        assertFalse(tester.getComponentFromLastRenderedPage("userForm:username").isEnabled());
        assertFalse(tester.getComponentFromLastRenderedPage("userForm:password").isEnabled());
        assertFalse(tester.getComponentFromLastRenderedPage("userForm:confirmPassword").isEnabled());               
        assertFalse(tester.getComponentFromLastRenderedPage("userForm:enabled").isEnabled());
        assertTrue(tester.getComponentFromLastRenderedPage("userForm:roles").isEnabled());
        assertFalse(tester.getComponentFromLastRenderedPage("userForm:groups").isEnabled());
        assertFalse(tester.getComponentFromLastRenderedPage("userForm:userpropertyeditor").isEnabled());
        tester.assertVisible("userForm:save");
        
        newFormTester();
        assignRole(GeoserverRole.ADMIN_ROLE.getAuthority());
        newFormTester();        
        form.submit("save");
        
        SortedSet<GeoserverRole> roleList = gaService.getRolesForUser("user1");
        assertEquals(1,roleList.size());
        assertTrue(roleList.contains(gaService.getRoleByName(GeoserverRole.ADMIN_ROLE.getAuthority())));

    }
    
    public void testReadOnlyRoleService() throws Exception {
        initializeForXML();
        doTestReadOnlyRoleService();
    }

    protected void doTestReadOnlyRoleService() throws Exception {
        insertValues();        
        activateROGAService();        
        current = ugService.getUserByUsername("user1");
        initializeTester();
        tester.assertRenderedPage(EditUserPage.class);
        assertFalse(tester.getComponentFromLastRenderedPage("userForm:username").isEnabled());
        assertTrue(tester.getComponentFromLastRenderedPage("userForm:password").isEnabled());
        assertTrue(tester.getComponentFromLastRenderedPage("userForm:confirmPassword").isEnabled());               
        assertTrue(tester.getComponentFromLastRenderedPage("userForm:enabled").isEnabled());
        assertFalse(tester.getComponentFromLastRenderedPage("userForm:roles").isEnabled());
        assertTrue(tester.getComponentFromLastRenderedPage("userForm:groups").isEnabled());
        assertTrue(tester.getComponentFromLastRenderedPage("userForm:userpropertyeditor").isEnabled());

        
        tester.assertVisible("userForm:save");
        
        newFormTester();
        form.setValue("enabled", Boolean.FALSE);
        form.submit("save");

        GeoserverUser user = ugService.getUserByUsername("user1");
        assertNotNull(user);
        assertFalse(user.isEnabled());

    }
    
    public void testAllServicesReadOnly() throws Exception {
        initializeForXML();
        insertValues();
        activateROUGService();
        activateROGAService();
        
        current = ugService.getUserByUsername("user1");
        initializeTester();
        tester.assertRenderedPage(EditUserPage.class);
        assertFalse(tester.getComponentFromLastRenderedPage("userForm:username").isEnabled());
        assertFalse(tester.getComponentFromLastRenderedPage("userForm:password").isEnabled());
        assertFalse(tester.getComponentFromLastRenderedPage("userForm:confirmPassword").isEnabled());
        assertFalse(tester.getComponentFromLastRenderedPage("userForm:enabled").isEnabled());
        assertFalse(tester.getComponentFromLastRenderedPage("userForm:roles").isEnabled());
        assertFalse(tester.getComponentFromLastRenderedPage("userForm:groups").isEnabled());
        assertFalse(tester.getComponentFromLastRenderedPage("userForm:userpropertyeditor").isEnabled());
        tester.assertInvisible("userForm:save");
    }

    @Override
    protected void initializeTester() {
        tester.startPage(page=new EditUserPage(current));
    }

    public void testPasswordsDontMatch() throws Exception {
        initializeForXML();
        insertValues();
        current = ugService.getUserByUsername("user1");
        super.doTestPasswordsDontMatch(EditUserPage.class);
    }



}
