package org.geoserver.web.security.user;


import java.lang.reflect.Method;
import java.util.SortedSet;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.geoserver.security.impl.GeoserverUser;
import org.geoserver.web.security.AbstractSecurityPage;
import org.geoserver.web.security.AbstractTabbedListPageTest;
import org.geoserver.web.security.config.UserGroupTabbedPage;
import org.geoserver.web.wicket.GeoServerDataProvider.Property;

public class UserListPageTest extends AbstractTabbedListPageTest<GeoserverUser> {
    boolean withRoles=false;
    
    protected AbstractSecurityPage listPage(String serviceName ) {
        UserGroupTabbedPage result = (UserGroupTabbedPage) 
                initializeForUGServiceNamed(serviceName);
        tester.clickLink(getTabbedPanelPath()+":tabs-container:tabs:1:link", true);
        return result;
    }
    protected AbstractSecurityPage newPage(AbstractSecurityPage page,Object...params) {
        if (params.length==0)
            return new  NewUserPage(getUserGroupServiceName(),page);
        else
            return new  NewUserPage((String) params[0],page);
    }
    protected AbstractSecurityPage editPage(AbstractSecurityPage page,Object...params) {
        if (params.length==0) {
            return new  EditUserPage(
                    getUserGroupServiceName(),
                    new GeoserverUser("dummyuser"),page);            
        }

        if (params.length==1)
            return new  EditUserPage(
                    getUserGroupServiceName(),
                    (GeoserverUser) params[0],page);
        else
            return new  EditUserPage( (String) params[0],
                    (GeoserverUser) params[1],page);                    
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
        tester.startPage(listPage(getUserGroupServiceName()));
        tester.assertVisible(getRemoveLink().getPageRelativePath());
        tester.assertVisible(getRemoveLinkWithRoles().getPageRelativePath());
        tester.assertVisible(getAddLink().getPageRelativePath());
        
        activateRORoleService();
        tester.startPage(listPage(getUserGroupServiceName()));
        tester.assertVisible(getRemoveLink().getPageRelativePath());
        tester.assertInvisible(getRemoveLinkWithRoles().getPageRelativePath());
        tester.assertVisible(getAddLink().getPageRelativePath());
        
        activateROUGService();
        tester.startPage(listPage(getROUserGroupServiceName()));
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
        doRemove(getTabbedPanelPath()+":panel:removeSelectedWithRoles");
    }
    
    @Override
    protected String getTabbedPanelPath() {
        return "UserGroupTabbedPage";
    }
    @Override
    protected String getServiceName() {
        return getUserGroupServiceName();
    }

}
