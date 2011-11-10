package org.geoserver.web.security.group;


import java.lang.reflect.Method;
import java.util.SortedSet;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.geoserver.security.impl.GeoserverUserGroup;
import org.geoserver.web.security.AbstractListPageTest;
import org.geoserver.web.wicket.GeoServerDataProvider.Property;

public class GroupListPageTest extends AbstractListPageTest<GeoserverUserGroup> {
    boolean withRoles=false;
        
    protected Page listPage(PageParameters params ) {
        if (params==null)
            params = getParamsForService(getUserGroupServiceName());
        return new  GroupPage(params);
    }
    protected Page newPage(Object...params) {
        if (params.length==0)
            return new  NewGroupPage(getUserGroupServiceName());
        else
            return new  NewGroupPage((String) params[0]);
    }
    protected Page editPage(Object...params) {
        if (params.length==0) {
            return new  EditGroupPage(
                    getUserGroupServiceName(),
                    new GeoserverUserGroup("dummygroup"));            
        }
        if (params.length==1)
            return new  EditGroupPage(
                    getUserGroupServiceName(),
                    (GeoserverUserGroup) params[0]);
        else
            return new  EditGroupPage( (String) params[0],
                    (GeoserverUserGroup) params[1]);                    
    }


    @Override
    protected String getSearchString() throws Exception{
         GeoserverUserGroup g = ugService.getGroupByGroupname("admins");
         assertNotNull(g);
         return g.getGroupname();
    }


    @Override
    protected Property<GeoserverUserGroup> getEditProperty() {
        return GroupListProvider.GROUPNAME;
    }


    @Override
    protected boolean checkEditForm(String objectString) {
        return objectString.equals( 
                tester.getComponentFromLastRenderedPage("groupForm:groupname").getDefaultModelObject());
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
        SelectionGroupRemovalLink link =   
                (SelectionGroupRemovalLink) (withRoles ?  getRemoveLinkWithRoles() : getRemoveLink());
        Method m = link.delegate.getClass().getDeclaredMethod("onSubmit", AjaxRequestTarget.class,Component.class);
        m.invoke(link.delegate, null,null);
        
        SortedSet<GeoserverUserGroup> groups = ugService.getUserGroups();
        assertTrue(groups.size()==0);
        
        if (withRoles)
            assertTrue(gaService.getRolesForGroup("group1").size()==0);
        else    
            assertTrue(gaService.getRolesForGroup("group1").size()==2);
    }

    public void testRemoveWithRoles() throws Exception {
        withRoles=true;
        initializeForXML();
        insertValues();
        addAdditonalData();
        doRemove("headerPanel:removeSelectedWithRoles");
    }
    
}
