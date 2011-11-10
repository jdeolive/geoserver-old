package org.geoserver.web.security.group;

import java.util.SortedSet;

import org.apache.wicket.util.tester.FormTester;
import org.geoserver.security.impl.GeoserverRole;
import org.geoserver.security.impl.GeoserverUserGroup;
import org.geoserver.web.security.AbstractSecurityWicketTestSupport;
import org.geoserver.web.security.role.NewRolePage;

public class EditGroupPageTest extends AbstractSecurityWicketTestSupport {

    EditGroupPage page;
    
    

    public void testFill() throws Exception{
        initializeForXML();
        doTestFill();
    }

    protected void doTestFill() throws Exception {
        insertValues();        
        
        tester.startPage(page=new EditGroupPage(getUserGroupServiceName(),
                ugService.getGroupByGroupname("group1")));        
        tester.assertRenderedPage(EditGroupPage.class);
        
        tester.assertRenderedPage(EditGroupPage.class);
        assertFalse(tester.getComponentFromLastRenderedPage("groupForm:groupname").isEnabled());
        assertTrue(tester.getComponentFromLastRenderedPage("groupForm:enabled").isEnabled());
        assertTrue(tester.getComponentFromLastRenderedPage("groupForm:roles").isEnabled());
        tester.assertVisible("groupForm:save");

        tester.assertModelValue("groupForm:groupname", "group1");
        tester.assertModelValue("groupForm:enabled", Boolean.TRUE);
        
        FormTester form = tester.newFormTester("groupForm");
        form.setValue("enabled", Boolean.FALSE);
        
        
                        // add a role on the fly
        form.submit("roles:addRole");
        tester.assertRenderedPage(NewRolePage.class);
        form=tester.newFormTester("roleForm");                
        form.setValue("rolename", "ROLE_NEW");
        form.submit("save");
        
        // assign the new role to the new group
        form=tester.newFormTester("groupForm");
        tester.assertRenderedPage(EditGroupPage.class);
        form.setValue("roles:roles:recorder", gaService.getRoleByName("ROLE_NEW").getAuthority());
        
        // reopen new role dialog again to ensure that the current state is not lost
        form.submit("roles:addRole");
        tester.assertRenderedPage(NewRolePage.class);
        tester.clickLink("roleForm:cancel");
        tester.assertRenderedPage(EditGroupPage.class);
        
        // now save
        form=tester.newFormTester("groupForm");
        form.submit("save");
        
        tester.assertErrorMessages(new String[0]);
        tester.assertRenderedPage(GroupPage.class);
        
        GeoserverUserGroup group = ugService.getGroupByGroupname("group1");
        assertNotNull(group);
        assertFalse(group.isEnabled());
        SortedSet<GeoserverRole> roleList = gaService.getRolesForGroup("group1");
        assertEquals(1,roleList.size());
        assertEquals("ROLE_NEW",roleList.iterator().next().getAuthority());
                
    }
    
    public void testReadOnlyUserGroupService() throws Exception {
        initializeForXML();
        doTestReadOnlyUserGroupService();
    }

    protected void doTestReadOnlyUserGroupService() throws Exception {
        insertValues();
        activateROUGService();
        
        tester.startPage(page=new EditGroupPage(getROUserGroupServiceName(),
                ugService.getGroupByGroupname("group1")));
        tester.assertRenderedPage(EditGroupPage.class);
        assertFalse(tester.getComponentFromLastRenderedPage("groupForm:groupname").isEnabled());
        assertFalse(tester.getComponentFromLastRenderedPage("groupForm:enabled").isEnabled());
        assertTrue(tester.getComponentFromLastRenderedPage("groupForm:roles").isEnabled());
        tester.assertVisible("groupForm:save");
        
        FormTester form=tester.newFormTester("groupForm");
        form.setValue("roles:roles:recorder", gaService.getRoleByName("ROLE_WFS").getAuthority());
        form.submit("save");
        
        SortedSet<GeoserverRole> roleList = gaService.getRolesForGroup("group1");
        assertEquals(1,roleList.size());
        assertEquals("ROLE_WFS",roleList.iterator().next().getAuthority());

    }
    
    public void testReadOnlyRoleService() throws Exception {
        initializeForXML();
        doTestReadOnlyRoleService();
    }

    protected void doTestReadOnlyRoleService() throws Exception {
        insertValues();
        
        activateRORoleService();
        
        tester.startPage(page=new EditGroupPage(getUserGroupServiceName(),ugService.getGroupByGroupname("group1")));
        tester.assertRenderedPage(EditGroupPage.class);
        assertFalse(tester.getComponentFromLastRenderedPage("groupForm:groupname").isEnabled());
        assertTrue(tester.getComponentFromLastRenderedPage("groupForm:enabled").isEnabled());
        assertFalse(tester.getComponentFromLastRenderedPage("groupForm:roles").isEnabled());
        tester.assertVisible("groupForm:save");
        
        FormTester form = tester.newFormTester("groupForm");
        form.setValue("enabled", Boolean.FALSE);
        form.submit("save");

        GeoserverUserGroup group = ugService.getGroupByGroupname("group1");
        assertNotNull(group);
        assertFalse(group.isEnabled());

    }
    
    public void testAllServicesReadOnly() throws Exception {
        initializeForXML();
        activateROUGService();
        activateRORoleService();
        
        tester.startPage(page=new EditGroupPage(getROUserGroupServiceName(),
                ugService.getGroupByGroupname("group1")));
        tester.assertRenderedPage(EditGroupPage.class);
        assertFalse(tester.getComponentFromLastRenderedPage("groupForm:groupname").isEnabled());
        assertFalse(tester.getComponentFromLastRenderedPage("groupForm:enabled").isEnabled());
        assertFalse(tester.getComponentFromLastRenderedPage("groupForm:roles").isEnabled());
        tester.assertInvisible("groupForm:save");
    }



}
