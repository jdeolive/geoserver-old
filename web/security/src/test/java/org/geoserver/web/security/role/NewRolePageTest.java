package org.geoserver.web.security.role;

import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.util.tester.FormTester;
import org.geoserver.security.impl.GeoserverRole;
import org.geoserver.web.security.AbstractSecurityWicketTestSupport;

public class NewRolePageTest extends AbstractSecurityWicketTestSupport {

    NewRolePage page;
    
    
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
        tester.startPage(page=new NewRolePage());
        
        tester.assertRenderedPage(NewRolePage.class);
        
        
        FormTester form = tester.newFormTester("roleForm");
        form.setValue("rolename", "ROLE_TEST");
        
        int index =-1;
        for (String name : page.parentRoles.getChoices()) {
            index++;
            if ("ROLE_AUTHENTICATED".equals(name))
                break;
        }
        assertTrue (index >=0);
        form.select("parentRoles", index);
        
        
        tester.executeAjaxEvent("roleForm:roleparameditor:add", "onclick");
        form = tester.newFormTester("roleForm");
        //print(tester.getLastRenderedPage(),true,true);
        
        form.setValue("roleparameditor:editortable:editor:1:key", "bbox");
        form.setValue("roleparameditor:editortable:editor:1:value", "10 10 20 20");
                
        form.submit("save");
        
        tester.assertErrorMessages(new String[0]);
        tester.assertRenderedPage(RolePage.class);
        
        GeoserverRole role = gaService.getRoleByName("ROLE_TEST");
        assertNotNull(role);
        assertEquals(1,role.getProperties().size());
        assertEquals("10 10 20 20",role.getProperties().get("bbox"));
        GeoserverRole parentRole = gaService.getParentRole(role);
        assertNotNull(parentRole);
        assertEquals("ROLE_AUTHENTICATED",parentRole.getAuthority());
        
    }
    
    public void testRoleNameConflict() throws Exception {
        initializeForXML();
        insertValues();        
        tester.startPage(page=new NewRolePage());
        
        FormTester form = tester.newFormTester("roleForm");
        form.setValue("rolename", "ROLE_WFS");
        form.submit("save");
        
        assertTrue(testErrorMessagesWithRegExp(".*ROLE_WFS.*"));
        tester.getMessages(FeedbackMessage.ERROR);
        tester.assertRenderedPage(NewRolePage.class);
    }

    public void testInvalidWorkflow() throws Exception{
        initializeForXML();
        activateROGAService();
        boolean fail = true;
        try {
            tester.startPage(page=new NewRolePage());
        } catch (RuntimeException ex) {
            fail = false;
        }
        if (fail)
            fail("No runtime exception for read only RoleService");
    }

}
