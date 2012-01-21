package org.geoserver.security.web.role;

import org.apache.wicket.util.tester.FormTester;
import org.geoserver.security.impl.GeoServerRole;
import org.geoserver.security.web.AbstractSecurityPage;
import org.geoserver.security.web.AbstractSecurityWicketTestSupport;
import org.geoserver.security.web.SecurityNamedServiceEditPage;

public class EditRolePageTest extends AbstractSecurityWicketTestSupport {

    EditRolePage page;

    public void testFill() throws Exception{
        initializeForXML();
        doTestFill();
    }
    
    public void testFill2() throws Exception{
        initializeForXML();
        doTestFill2();
    }

    protected void doTestFill() throws Exception {
        insertValues();        
        
        AbstractSecurityPage returnPage = initializeForRoleServiceNamed(getRoleServiceName());
        tester.startPage(page=new EditRolePage(getRoleServiceName(),gaService.getRoleByName("ROLE_WFS"),returnPage));        
        tester.assertRenderedPage(EditRolePage.class);
        
        assertFalse(tester.getComponentFromLastRenderedPage("roleForm:rolename").isEnabled());
        assertTrue(tester.getComponentFromLastRenderedPage("roleForm:roleparameditor").isEnabled());
        assertTrue(tester.getComponentFromLastRenderedPage("roleForm:parentRoles").isEnabled());
        tester.assertVisible("roleForm:save");

        tester.assertModelValue("roleForm:rolename", "ROLE_WFS");
        tester.assertModelValue("roleForm:parentRoles", "ROLE_AUTHENTICATED");
        
        FormTester form = tester.newFormTester("roleForm");
        int index =-1;
        for (String name : page.parentRoles.getChoices()) {
            index++;
            if ("".equals(name))
                break;
        }
        assertTrue (index >=0);
        form.select("parentRoles", index);
        
        
        tester.executeAjaxEvent("roleForm:roleparameditor:add", "onclick");
        form = tester.newFormTester("roleForm");
        
        
        form.setValue("roleparameditor:editortable:editor:1:key", "bbox");
        form.setValue("roleparameditor:editortable:editor:1:value", "10 10 20 20");
                
        form.submit("save");
        tester.assertRenderedPage(SecurityNamedServiceEditPage.class);
        tester.assertErrorMessages(new String[0]);
        
        GeoServerRole role = gaService.getRoleByName("ROLE_WFS");
        assertNotNull(role);
        assertEquals(1,role.getProperties().size());
        assertEquals("10 10 20 20",role.getProperties().get("bbox"));
        GeoServerRole parentRole = gaService.getParentRole(role);
        assertNull(parentRole);
                
    }
    
    protected void doTestFill2() throws Exception {
        insertValues();        
        
        AbstractSecurityPage returnPage = initializeForRoleServiceNamed(getRoleServiceName());
        tester.startPage(page=new EditRolePage(getRoleServiceName(),gaService.getRoleByName("ROLE_AUTHENTICATED"),returnPage));        
        tester.assertRenderedPage(EditRolePage.class);
        
        tester.assertModelValue("roleForm:rolename", "ROLE_AUTHENTICATED");
        tester.assertModelValue("roleForm:parentRoles", "");

        // role params are shown sorted by key
        tester.assertModelValue("roleForm:roleparameditor:editortable:editor:1:key", "bbox");
        tester.assertModelValue("roleForm:roleparameditor:editortable:editor:1:value", "lookupAtRuntime");
        tester.assertModelValue("roleForm:roleparameditor:editortable:editor:2:key", "employee");
        tester.assertModelValue("roleForm:roleparameditor:editortable:editor:2:value", "");
        
        tester.executeAjaxEvent("roleForm:roleparameditor:editortable:editor:2:remove", "onclick");
        FormTester form = tester.newFormTester("roleForm");
        form.submit("save");
        tester.assertRenderedPage(SecurityNamedServiceEditPage.class);

        GeoServerRole role = gaService.getRoleByName("ROLE_AUTHENTICATED");
        assertNotNull(role);
        assertEquals(1,role.getProperties().size());
        assertEquals("lookupAtRuntime",role.getProperties().get("bbox"));

    }
        
    public void testReadOnlyRoleService() throws Exception {
        initializeForXML();
        activateRORoleService();
        
        AbstractSecurityPage returnPage = initializeForRoleServiceNamed(getRORoleServiceName());
        tester.startPage(page=new EditRolePage(getRORoleServiceName(),GeoServerRole.ADMIN_ROLE,returnPage));
        tester.assertRenderedPage(EditRolePage.class);
        assertFalse(tester.getComponentFromLastRenderedPage("roleForm:rolename").isEnabled());
        assertFalse(tester.getComponentFromLastRenderedPage("roleForm:roleparameditor").isEnabled());
        assertFalse(tester.getComponentFromLastRenderedPage("roleForm:parentRoles").isEnabled());
        tester.assertInvisible("roleForm:save");
    }

}
