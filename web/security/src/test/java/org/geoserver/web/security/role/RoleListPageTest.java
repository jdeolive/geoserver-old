package org.geoserver.web.security.role;


import java.lang.reflect.Method;
import java.util.SortedSet;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.geoserver.security.impl.GeoserverGrantedAuthority;
import org.geoserver.web.security.AbstractListPageTest;
import org.geoserver.web.wicket.GeoServerDataProvider.Property;

public class RoleListPageTest extends AbstractListPageTest<GeoserverGrantedAuthority> {
    
     public static final String SECOND_COLUM_PATH="itemProperties:1:component:link";
    


    
    protected Class<? extends Page> listPageClass() {
        return RolePage.class;
    }

        
    protected Class<? extends Page> editPageClass() {
        return EditRolePage.class;
    }

    
    public void testEditParentRole() throws Exception {
        initializeForXML();
        insertValues();
        
        tester.startPage(listPageClass());
                   
        GeoserverGrantedAuthority role = gaService.getGrantedAuthorityByName("ROLE_AUTHENTICATED");
        assertNotNull(role);
        Component c = getFromList(SECOND_COLUM_PATH, role,RoleListProvider.PARENTROLENAME); 
        assertNotNull(c);
        tester.clickLink(c.getPageRelativePath());
        
        tester.assertRenderedPage(editPageClass());
        assertTrue(checkEditForm(role.getAuthority()));
                
    }
    
        
    protected Class<? extends Page> newPageClass() {
        return NewRolePage.class;
    }


    @Override
    protected String getSearchString() throws Exception{
        GeoserverGrantedAuthority role = 
                gaService.getGrantedAuthorityByName(
                GeoserverGrantedAuthority.ADMIN_ROLE.getAuthority());
        assertNotNull(role);
        return role.getAuthority();
    }


    @Override
    protected Property<GeoserverGrantedAuthority> getEditProperty() {
        return RoleListProvider.ROLENAME;
    }


    @Override
    protected boolean checkEditForm(String objectString) {
        return objectString.equals( 
                tester.getComponentFromLastRenderedPage("roleForm:rolename").getDefaultModelObject());
    }
    
    public void testReadOnlyService() throws Exception{
        initializeForXML();
        tester.startPage(listPageClass());
        tester.assertVisible(getRemoveLink().getPageRelativePath());
        tester.assertVisible(getAddLink().getPageRelativePath());
        
        activateROGAService();
        
        tester.startPage(listPageClass());
        tester.assertInvisible(getRemoveLink().getPageRelativePath());
        tester.assertInvisible(getAddLink().getPageRelativePath());        
    }
    
    
    @Override
    protected void simulateDeleteSubmit() throws Exception {
                    
        SelectionRoleRemovalLink link = (SelectionRoleRemovalLink) getRemoveLink();
        Method m = link.delegate.getClass().getDeclaredMethod("onSubmit", AjaxRequestTarget.class,Component.class);
        m.invoke(link.delegate, null,null);
        
        SortedSet<GeoserverGrantedAuthority> roles = gaService.getRoles();
        assertEquals(0,roles.size(),4);
        assertTrue(roles.contains(GeoserverGrantedAuthority.ADMIN_ROLE));
    }
    
    public void testRemoveJDBC() throws Exception {
        initializeForJDBC();
        insertValues();
        addAdditonalData();
        doRemove("headerPanel:removeSelected");
    }

    @Override
    protected void doRemove(String pathForLink) throws Exception {
        GeoserverGrantedAuthority newRole = gaStore.createGrantedAuthorityObject("NEW_ROLE");
        gaStore.addGrantedAuthority(newRole);
        gaStore.store();
        assertEquals(5,gaService.getRoles().size());
        
        super.doRemove(pathForLink);
    }
}
