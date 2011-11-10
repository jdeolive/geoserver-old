package org.geoserver.web.security.user;


import java.lang.reflect.Method;
import java.util.SortedSet;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.geoserver.security.impl.GeoserverUser;
import org.geoserver.security.impl.GeoserverUserGroup;
import org.geoserver.web.security.AbstractListPageTest;
import org.geoserver.web.security.group.EditGroupPage;
import org.geoserver.web.wicket.GeoServerDataProvider.Property;

public class UserListPageTest extends AbstractListPageTest<GeoserverUser> {
    boolean withRoles=false;
    
    protected Page listPage(PageParameters params ) {
        if (params==null)
            params=getParamsForService(getUserGroupServiceName());
        return new  UserPage(params);
    }
    protected Page newPage(Object...params) {
        if (params.length==0)
            return new  NewUserPage(getUserGroupServiceName());
        else
            return new  NewUserPage((String) params[0]);
    }
    protected Page editPage(Object...params) {
        if (params.length==0) {
            return new  EditUserPage(
                    getUserGroupServiceName(),
                    new GeoserverUser("dummyuser"));            
        }

        if (params.length==1)
            return new  EditUserPage(
                    getUserGroupServiceName(),
                    (GeoserverUser) params[0]);
        else
            return new  EditUserPage( (String) params[0],
                    (GeoserverUser) params[1]);                    
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
        tester.startPage(listPage(null));
        tester.assertVisible(getRemoveLink().getPageRelativePath());
        tester.assertVisible(getRemoveLinkWithRoles().getPageRelativePath());
        tester.assertVisible(getAddLink().getPageRelativePath());
        
        activateRORoleService();
        tester.startPage(listPage(null));
        tester.assertVisible(getRemoveLink().getPageRelativePath());
        tester.assertInvisible(getRemoveLinkWithRoles().getPageRelativePath());
        tester.assertVisible(getAddLink().getPageRelativePath());
        
        activateROUGService();
        tester.startPage(listPage(getParamsForService(getROUserGroupServiceName())));
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
