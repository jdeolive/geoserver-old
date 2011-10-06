package org.geoserver.web.security.group;


import java.lang.reflect.Method;
import java.util.SortedSet;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.geoserver.security.impl.GeoserverUserGroup;
import org.geoserver.web.security.AbstractListPageTest;
import org.geoserver.web.wicket.GeoServerDataProvider.Property;

public class GroupListPageTest extends AbstractListPageTest<GeoserverUserGroup> {
    boolean withRoles=false;
        
    protected Class<? extends Page> listPageClass() {
        return GroupPage.class;
    }

    
    
    protected Class<? extends Page> editPageClass() {
        return EditGroupPage.class;
    }

    
    
        
    protected Class<? extends Page> newPageClass() {
        return NewGroupPage.class;
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

    public void testRemoveWithRolesXML() throws Exception {
        withRoles=true;
        initializeForXML();
        insertValues();
        addAdditonalData();
        doRemove("headerPanel:removeSelectedWithRoles");
    }
    
    public void testRemoveWithRolesJDBC() throws Exception {
        withRoles=true;
        initializeForJDBC();
        insertValues();
        addAdditonalData();
        doRemove("headerPanel:removeSelectedWithRoles");
    }

    public void testRemoveJDBC() throws Exception {
        initializeForJDBC();
        insertValues();
        addAdditonalData();
        doRemove("headerPanel:removeSelected");
    }

}
