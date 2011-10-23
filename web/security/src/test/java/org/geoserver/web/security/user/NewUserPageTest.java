package org.geoserver.web.security.user;

import java.util.SortedSet;

import org.apache.wicket.extensions.markup.html.form.palette.component.Recorder;
import org.apache.wicket.feedback.FeedbackMessage;
import org.geoserver.security.impl.GeoserverGrantedAuthority;
import org.geoserver.security.impl.GeoserverUser;
import org.geoserver.security.impl.GeoserverUserGroup;

public class NewUserPageTest extends AbstractUserPageTest {

    protected void initializeTester() {
        tester.startPage(page=new NewUserPage());
    }
    
    public void testFill() throws Exception{
        initializeForXML();
        doTestFill();
    }
    
    public void testFillJDBC() throws Exception{
        initializeForJDBC();
        doTestFill();
    }


    protected void doTestFill() throws Exception {
        insertValues();
        initializeTester();
        tester.assertRenderedPage(NewUserPage.class);
        
        newFormTester();
        form.setValue("username", "testuser");
        form.setValue("password", "pwd");
        form.setValue("confirmPassword", "pwd");

        
        assertTrue(page.uiUser.isEnabled());
        form.setValue("enabled", false);

        addUserProperty("coord", "10 10");
        
        assertTrue(page.userRolesFormComponent.isEnabled());
        tester.assertComponent("userForm:roles:roles:recorder", Recorder.class);
                                
        addNewRole("ROLE_NEW");        
        tester.assertRenderedPage(NewUserPage.class);
        
        assignRole("ROLE_NEW");
        
        // reopen new role dialog again to ensure that the current state is not lost
        openCloseRolePanel(NewUserPage.class);
        
        
        addNewGroup("testgroup");
        assignGroup("testgroup");
        
        openCloseGroupPanel(NewUserPage.class);
        
        assertCalculatedRoles(new String[] { "ROLE_NEW" });        
        form.submit("save");
        
        tester.assertErrorMessages(new String[0]);
        tester.assertRenderedPage(UserPage.class);
        
        GeoserverUser user = ugService.getUserByUsername("testuser");
        assertNotNull(user);
        assertFalse(user.isEnabled());
        
        assertEquals(1,user.getProperties().size());
        assertEquals("10 10",user.getProperties().get("coord"));
        SortedSet<GeoserverUserGroup> groupList = ugService.getGroupsForUser(user);
        assertEquals(1,groupList.size());
        assertEquals("testgroup",groupList.iterator().next().getGroupname());
        
        SortedSet<GeoserverGrantedAuthority> roleList = gaService.getRolesForUser("testuser");
        assertEquals(1,roleList.size());
        assertEquals("ROLE_NEW",roleList.iterator().next().getAuthority());
        
    }

    
    public void testFill3() throws Exception{
        initializeForXML();
        doTestFill3();
    }
    
    public void testFill3JDBC() throws Exception{
        initializeForJDBC();
        doTestFill3();
    }
    
    
    protected void doTestFill3() throws Exception {
        insertValues();
        initializeTester();
        tester.assertRenderedPage(NewUserPage.class);
        
        newFormTester();
        form.setValue("username", "testuser");
        form.setValue("password", "pwd");
        form.setValue("confirmPassword", "pwd");
        
        // TODO: this is a dummy call for the formtester to store 
        // the above vaules in the model, otherwise we would
        // lose the values due to the assingRole call 
        openCloseGroupPanel(NewUserPage.class);
        
        assignRole("ROLE_WMS");
        assertCalculatedRoles(new String[] { "ROLE_AUTHENTICATED","ROLE_WMS" });
                

                
        form.submit("save");

        
        tester.assertErrorMessages(new String[0]);
        tester.assertRenderedPage(UserPage.class);
        
        GeoserverUser user = ugService.getUserByUsername("testuser");
        assertNotNull(user);
        assertTrue(user.isEnabled());
        
        SortedSet<GeoserverUserGroup> groupList = ugService.getGroupsForUser(user);
        assertEquals(0,groupList.size());
        
        SortedSet<GeoserverGrantedAuthority> roleList = gaService.getRolesForUser("testuser");
        assertEquals(1,roleList.size());
        assertTrue(roleList.contains(gaService.createGrantedAuthorityObject("ROLE_WMS")));

        user = (GeoserverUser) ugService.loadUserByUsername("testuser");
        assertEquals(2,user.getAuthorities().size());
        assertTrue(user.getAuthorities().contains(gaService.createGrantedAuthorityObject("ROLE_AUTHENTICATED")));
        assertTrue(user.getAuthorities().contains(gaService.createGrantedAuthorityObject("ROLE_WMS")));
        
    }
    
    public void testFill2() throws Exception{
        initializeForXML();
        doTestFill2();
    }
    
    public void testFill2JDBC() throws Exception{
        initializeForJDBC();
        doTestFill2();
    }
    
    
    protected void doTestFill2() throws Exception {
        insertValues();
        addAdditonalData();
        initializeTester();
        tester.assertRenderedPage(NewUserPage.class);
        
        newFormTester();
        form.setValue("username", "testuser");
        form.setValue("password", "pwd");
        form.setValue("confirmPassword", "pwd");
        
        // TODO: this is a dummy call for the formtester to store 
        // the above vaules in the model, otherwise we would
        // lose the values due to the assingGroup call 
        openCloseGroupPanel(NewUserPage.class);
        
        assignGroup("group1");
        assertCalculatedRoles(new String[] { "ROLE_AUTHENTICATED","ROLE_WFS","ROLE_WMS" });
                

                
        form.submit("save");

        
        tester.assertErrorMessages(new String[0]);
        tester.assertRenderedPage(UserPage.class);
        
        GeoserverUser user = ugService.getUserByUsername("testuser");
        assertNotNull(user);
        assertTrue(user.isEnabled());
        
        SortedSet<GeoserverUserGroup> groupList = ugService.getGroupsForUser(user);
        assertEquals(1,groupList.size());
        assertEquals("group1",groupList.iterator().next().getGroupname());
        
        SortedSet<GeoserverGrantedAuthority> roleList = gaService.getRolesForUser("testuser");
        assertEquals(0,roleList.size());
        
        user = (GeoserverUser) ugService.loadUserByUsername("testuser");
        assertEquals(3,user.getAuthorities().size());
        assertTrue(user.getAuthorities().contains(gaService.createGrantedAuthorityObject("ROLE_AUTHENTICATED")));
        assertTrue(user.getAuthorities().contains(gaService.createGrantedAuthorityObject("ROLE_WFS")));
        assertTrue(user.getAuthorities().contains(gaService.createGrantedAuthorityObject("ROLE_WMS")));
        
    }


    
    
    public void testUserNameConflict() throws Exception {        
        initializeForXML();
        insertValues();        
        
        initializeTester();
        newFormTester();
        form.setValue("username", "user1");
        form.submit("save");
        
        assertTrue(testErrorMessagesWithRegExp(".*user1.*"));
        tester.getMessages(FeedbackMessage.ERROR);
        tester.assertRenderedPage(NewUserPage.class);
    }

    public void testInvalidWorkflow() throws Exception{
        initializeForXML();
        activateROUGService();        
        boolean fail = true;
        try {
            initializeTester();
        } catch (RuntimeException ex) {
            fail = false;
        }
        if (fail)
            fail("No runtime exception for read only UserGroupService");
    }
    
    public void testPasswordsDontMatch() throws Exception {
        super.doTestPasswordsDontMatch(NewUserPage.class);
    }

}
