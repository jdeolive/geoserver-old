package org.geoserver.web.security.role;


import java.lang.reflect.Method;
import java.util.List;
import java.util.SortedSet;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.geoserver.security.impl.GeoserverRole;
import org.geoserver.web.security.AbstractListPageTest;
import org.geoserver.web.wicket.GeoServerDataProvider.Property;

public class RoleListPageTest extends AbstractListPageTest<GeoserverRole> {
    
    public static final String SECOND_COLUM_PATH="itemProperties:1:component:link";
    

    protected Page listPage(PageParameters params) {
        if (params==null)
            params = getParamsForService(getRoleServiceName()); 
       return new  RolePanel(params);
    }
    protected Page newPage(Object...params) {
        if (params.length==0)
            return new  NewRolePage(getSecurityManager().getActiveRoleService().getName());
        else
            return new  NewRolePage((String) params[0]);
    }
    protected Page editPage(Object...params) {
        if (params.length==0) {
            return new  EditRolePage(
                    getSecurityManager().getActiveRoleService().getName(),
                    GeoserverRole.ADMIN_ROLE);            
        }
        if (params.length==1)
            return new  EditRolePage(
                    getSecurityManager().getActiveRoleService().getName(),
                    (GeoserverRole) params[0]);
        else
            return new  EditRolePage((String) params[0],
                    (GeoserverRole) params[1]);
    }


    
    
    public void testEditParentRole() throws Exception {
        initializeForXML();
        insertValues();
        
        tester.startPage(listPage(null));
                   
        GeoserverRole role = gaService.getRoleByName("ROLE_AUTHENTICATED");
        assertNotNull(role);
        List<Property<GeoserverRole>> props = new RoleListProvider(getRoleServiceName()).getProperties();
        Property<GeoserverRole> parentProp = null;
        for (Property<GeoserverRole> prop: props) {
            if (RoleListProvider.ParentPropertyName.equals(prop.getName())){
                parentProp=prop;
                break;
            }
        }
        Component c = getFromList(SECOND_COLUM_PATH, role,parentProp); 
        assertNotNull(c);
        tester.clickLink(c.getPageRelativePath());
        
        tester.assertRenderedPage(EditRolePage.class);
        assertTrue(checkEditForm(role.getAuthority()));
                
    }
    
        


    @Override
    protected String getSearchString() throws Exception{
        GeoserverRole role = 
                gaService.getRoleByName(
                GeoserverRole.ADMIN_ROLE.getAuthority());
        assertNotNull(role);
        return role.getAuthority();
    }


    @Override
    protected Property<GeoserverRole> getEditProperty() {
        return RoleListProvider.ROLENAME;
    }


    @Override
    protected boolean checkEditForm(String objectString) {
        return objectString.equals( 
                tester.getComponentFromLastRenderedPage("roleForm:rolename").getDefaultModelObject());
    }
    
    public void testReadOnlyService() throws Exception{
        initializeForXML();
        tester.startPage(listPage(null));
        tester.assertVisible(getRemoveLink().getPageRelativePath());
        tester.assertVisible(getAddLink().getPageRelativePath());
        
        activateRORoleService();
        
        tester.startPage(listPage(getParamsForService(getRORoleServiceName())));
        tester.assertInvisible(getRemoveLink().getPageRelativePath());
        tester.assertInvisible(getAddLink().getPageRelativePath());        
    }
    
    
    @Override
    protected void simulateDeleteSubmit() throws Exception {
                    
        SelectionRoleRemovalLink link = (SelectionRoleRemovalLink) getRemoveLink();
        Method m = link.delegate.getClass().getDeclaredMethod("onSubmit", AjaxRequestTarget.class,Component.class);
        m.invoke(link.delegate, null,null);
        
        SortedSet<GeoserverRole> roles = gaService.getRoles();
        assertEquals(0,roles.size(),4);
        assertTrue(roles.contains(GeoserverRole.ADMIN_ROLE));
    }

    @Override
    protected void doRemove(String pathForLink) throws Exception {
        GeoserverRole newRole = gaStore.createRoleObject("NEW_ROLE");
        gaStore.addRole(newRole);
        gaStore.store();
        assertEquals(5,gaService.getRoles().size());
        
        super.doRemove(pathForLink);
    }
}
