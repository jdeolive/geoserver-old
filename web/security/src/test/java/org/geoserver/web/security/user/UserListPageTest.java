package org.geoserver.web.security.user;


import java.lang.reflect.Method;
import java.util.SortedSet;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.geoserver.security.impl.GeoserverUser;
import org.geoserver.web.security.AbstractListPageTest;
import org.geoserver.web.wicket.GeoServerDataProvider.Property;

public class UserListPageTest extends AbstractListPageTest<GeoserverUser> {
    boolean withRoles=false;
    
    protected Class<? extends Page> listPageClass() {
        return UserPage.class;
    }

    
    
    protected Class<? extends Page> editPageClass() {
        return EditUserPage.class;
    }

    
    
        
    protected Class<? extends Page> newPageClass() {
        return NewUserPage.class;
    }


    @Override
    protected String getSearchString() throws Exception{
         GeoserverUser u = ugService.getUserByUsername("user1");
         assertNotNull(u);
         return u.getUsername();
    }


    @Override
    protected Property<GeoserverUser> getEditProperty() {
        return UserListProvider.USERNAME;
    }


    @Override
    protected boolean checkEditForm(String objectString) {
        return objectString.equals( 
                tester.getComponentFromLastRenderedPage("userForm:username").getDefaultModelObject());
    }
    
    public void testReadOnlyService() throws Exception {
        initializeForXML();
        tester.startPage(listPageClass());
        tester.assertVisible(getRemoveLink().getPageRelativePath());
        tester.assertVisible(getRemoveLinkWithRoles().getPageRelativePath());
        tester.assertVisible(getAddLink().getPageRelativePath());
        
        activateROGAService();
        tester.startPage(listPageClass());
        tester.assertVisible(getRemoveLink().getPageRelativePath());
        tester.assertInvisible(getRemoveLinkWithRoles().getPageRelativePath());
        tester.assertVisible(getAddLink().getPageRelativePath());
        
        activateROUGService();
        tester.startPage(listPageClass());
        tester.assertInvisible(getRemoveLink().getPageRelativePath());
        tester.assertInvisible(getAddLink().getPageRelativePath());
        tester.assertInvisible(getRemoveLinkWithRoles().getPageRelativePath());
    }

    @Override
    protected void simulateDeleteSubmit() throws Exception {
        SelectionUserRemovalLink link = 
                (SelectionUserRemovalLink) (withRoles ?  getRemoveLinkWithRoles() : getRemoveLink());
        Method m = link.delegate.getClass().getDeclaredMethod("onSubmit", AjaxRequestTarget.class,Component.class);
        m.invoke(link.delegate, null,null);
        
        SortedSet<GeoserverUser> users = ugService.getUsers();
        assertTrue(users.size()==0);
        if (withRoles)            
            assertTrue(gaService.getRolesForUser("user1").size()==0);
        else
            assertTrue(gaService.getRolesForUser("user1").size()==2);
    }

    public void testRemoveWithRoles() throws Exception {
        withRoles=true;
        initializeForXML();
        insertValues();
        addAdditonalData();
        doRemove("headerPanel:removeSelectedWithRoles");
    }
}
