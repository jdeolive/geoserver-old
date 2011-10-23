package org.geoserver.web.security.group;

import java.util.SortedSet;

import org.apache.wicket.extensions.markup.html.form.palette.component.Recorder;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.util.tester.FormTester;
import org.geoserver.security.impl.GeoserverRole;
import org.geoserver.security.impl.GeoserverUserGroup;
import org.geoserver.web.security.AbstractSecurityWicketTestSupport;
import org.geoserver.web.security.role.NewRolePage;

public class NewGroupPageTest extends AbstractSecurityWicketTestSupport {

    NewGroupPage page;
    
    
    public void testFill() throws Exception{
        initializeForXML();
        doTestFill();
    }
    
    public void testFillJDBC() throws Exception{
        initializeForJDBC();
        doTestFill();
    }


    protected void doTestFill() throws Exception {
        initializeForXML();
        insertValues();        
        tester.startPage(page=new NewGroupPage());        
        tester.assertRenderedPage(NewGroupPage.class);
        
        FormTester form = tester.newFormTester("groupForm");
        form.setValue("groupname", "testgroup");
        
        assertTrue(page.uiGroup.isEnabled());
        form.setValue("enabled", false);

        
        assertTrue(page.groupRolesFormComponent.isEnabled());
        tester.assertComponent("groupForm:roles:roles:recorder", Recorder.class);

                
        // add a role on the fly
        form.submit("roles:addRole");
        tester.assertRenderedPage(NewRolePage.class);
        form=tester.newFormTester("roleForm");                
        form.setValue("rolename", "ROLE_NEW");
        form.submit("save");
        
        // assign the new role to the new group
        form=tester.newFormTester("groupForm");
        tester.assertRenderedPage(NewGroupPage.class);
        form.setValue("roles:roles:recorder", gaService.getRoleByName("ROLE_NEW").getAuthority());
        
        // reopen new role dialog again to ensure that the current state is not lost
        form.submit("roles:addRole");
        tester.assertRenderedPage(NewRolePage.class);
        tester.clickLink("roleForm:cancel");
        tester.assertRenderedPage(NewGroupPage.class);
        
        // now save
        form=tester.newFormTester("groupForm");
        form.submit("save");
        
        tester.assertErrorMessages(new String[0]);
        tester.assertRenderedPage(GroupPage.class);
        
        GeoserverUserGroup group = ugService.getGroupByGroupname("testgroup");
        assertNotNull(group);
        assertFalse(group.isEnabled());
        SortedSet<GeoserverRole> roleList = gaService.getRolesForGroup("testgroup");
        assertEquals(1,roleList.size());
        assertEquals("ROLE_NEW",roleList.iterator().next().getAuthority());
        
    }
    
    public void testGroupNameConflict() throws Exception {
        initializeForXML();
        insertValues();        
        tester.startPage(page=new NewGroupPage());
        
        FormTester form = tester.newFormTester("groupForm");
        form.setValue("groupname", "group1");
        form.submit("save");
        
        assertTrue(testErrorMessagesWithRegExp(".*group1.*"));
        tester.getMessages(FeedbackMessage.ERROR);
        tester.assertRenderedPage(NewGroupPage.class);
    }

    public void testInvalidWorkflow() throws Exception{
        initializeForXML();
        activateROUGService();
        boolean fail = true;
        try {
            tester.startPage(page=new NewGroupPage());
        } catch (RuntimeException ex) {
            fail = false;
        }
        if (fail)
            fail("No runtime exception for read only UserGroupService");
    }
    
    public void testReadOnlyRoleService() throws Exception{
        initializeForXML();
        activateROGAService();
        tester.startPage(page=new NewGroupPage());
        assertFalse(page.groupRolesFormComponent.isEnabled());
        
        FormTester form = tester.newFormTester("groupForm");
        form.setValue("groupname", "testgroup");
        form.submit("save");
        
        GeoserverUserGroup group = ugService.getGroupByGroupname("testgroup");
        assertNotNull(group);
        assertTrue(group.isEnabled());
        SortedSet<GeoserverRole> roleList = gaService.getRolesForGroup("testgroup");
        assertEquals(0,roleList.size());
    }

   
}
