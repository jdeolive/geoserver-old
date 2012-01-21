package org.geoserver.security.web.user;

import org.apache.wicket.Page;
import org.apache.wicket.util.tester.FormTester;
import org.geoserver.security.web.AbstractSecurityWicketTestSupport;
import org.geoserver.security.web.group.NewGroupPage;
import org.geoserver.security.web.role.NewRolePage;

public abstract class AbstractUserPageTest extends AbstractSecurityWicketTestSupport {

    protected AbstractUserPage page;
    protected FormTester form; 
    
    protected abstract void initializeTester();
        
    public void testReadOnlyRoleService() throws Exception{
        initializeForXML();
        activateRORoleService();        
        initializeTester();
        assertFalse(page.userRolesFormComponent.isEnabled());
    }


    protected void doTestPasswordsDontMatch(Class <? extends Page> pageClass) throws Exception {
        initializeForXML();
        initializeTester();
        newFormTester();
        form.setValue("username", "user");
        form.setValue("password", "pwd1");
        form.setValue("confirmPassword", "pwd2");
        form.submit("save");
        
        assertTrue(testErrorMessagesWithRegExp(".*[Pp]assword.*"));
        tester.assertRenderedPage(pageClass);
    }
    
    protected void newFormTester() {
        form = tester.newFormTester("userForm");
    }

    protected void addNewRole(String roleName) {
        // add a role on the fly
        form.submit("roles:addRole");
        tester.assertRenderedPage(NewRolePage.class);
        FormTester roleform=tester.newFormTester("roleForm");                
        roleform.setValue("rolename", "ROLE_NEW");
        roleform.submit("save");
        
        newFormTester();
    }
    
    protected void addNewGroup(String groupName) {
        // add a role on the fly
        form.submit("groups:addGroup");
        tester.assertRenderedPage(NewGroupPage.class);
        FormTester groupform=tester.newFormTester("groupForm");                
        groupform.setValue("groupname", groupName);
        groupform.submit("save");        
        newFormTester();
    }

    
    protected void assignRole(String roleName) throws Exception {
        form.setValue("roles:roles:recorder", gaService.getRoleByName(roleName).getAuthority());
        tester.executeAjaxEvent("userForm:roles:roles:recorder", "onchange");
        newFormTester();
    }
    
    protected void assignGroup(String groupName) throws Exception {
        form.setValue("groups:groups:recorder", ugService.getGroupByGroupname(groupName).getGroupname());
        tester.executeAjaxEvent("userForm:groups:groups:recorder", "onchange");
        newFormTester();
    }

    
    protected void openCloseRolePanel(Class<? extends Page> responseClass ) {
        form.submit("roles:addRole");
        tester.assertRenderedPage(NewRolePage.class);
        tester.clickLink("roleForm:cancel");
        tester.assertRenderedPage(responseClass);                
        newFormTester();
    }
    
    protected void openCloseGroupPanel(Class<? extends Page> responseClass) {
        form.submit("groups:addGroup");
        tester.assertRenderedPage(NewGroupPage.class);
        tester.clickLink("groupForm:cancel");
        tester.assertRenderedPage(responseClass);                
        newFormTester();
    }
    
    protected void addUserProperty(String key, String value) {
        tester.executeAjaxEvent("userForm:userpropertyeditor:add", "onclick");
        newFormTester();
                
        form.setValue("userpropertyeditor:editortable:editor:1:key", key);
        form.setValue("userpropertyeditor:editortable:editor:1:value", value);                       
    }
    
    protected void assertCalculatedRoles( String[] roles) throws Exception{
        for (int i = 0; i < roles.length;i++ )
        tester.assertModelValue("userForm:calculatedrolesContainer:calculatedroles:"+i,
                gaService.getRoleByName(roles[i]));
    }

}
