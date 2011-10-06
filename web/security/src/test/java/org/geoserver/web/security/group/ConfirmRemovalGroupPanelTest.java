/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */


package org.geoserver.web.security.group;



import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.geoserver.security.impl.GeoserverUserGroup;
import org.geoserver.web.ComponentBuilder;
import org.geoserver.web.FormTestPage;
import org.geoserver.web.security.AbstractConfirmRemovalPanelTest;

public class ConfirmRemovalGroupPanelTest extends AbstractConfirmRemovalPanelTest<GeoserverUserGroup> {
    private static final long serialVersionUID = 1L;

    boolean disassociateRoles = false;
    
    protected void setupPanel(final List<GeoserverUserGroup> roots) {
        tester.startPage(new FormTestPage(new ComponentBuilder() {
            private static final long serialVersionUID = 1L;

            public Component buildComponent(String id) {
                Model<Boolean> model = new Model<Boolean>(disassociateRoles);
                return new ConfirmRemovalGroupPanel(id, model,roots.toArray(new GeoserverUserGroup[roots.size()])) {
                    @Override
                    protected StringResourceModel canRemove(GeoserverUserGroup data) {
                        SelectionGroupRemovalLink link = new SelectionGroupRemovalLink("XXX",null,null,disassociateRoles);
                        return link.canRemove(data);
                    }

                    private static final long serialVersionUID = 1L;                    
                };
            }
        }));
    }
    
    public void testRemoveGroupJDBC() throws Exception {
        disassociateRoles=false;
        initializeForJDBC();
        removeObject();
        
    }
    public void testRemoveGroupXML() throws Exception {
        disassociateRoles=false;
        initializeForXML();
        removeObject();                                       
    }

    public void testRemoveGroupWithRolesJDBC() throws Exception {
        disassociateRoles=true;
        initializeForJDBC();
        removeObject();
        
    }
    public void testRemoveGroupWithRolesXML() throws Exception {
        disassociateRoles=true;
        initializeForXML();
        removeObject();                                       
    }

    

    @Override
    protected GeoserverUserGroup getRemoveableObject() throws Exception{
        if (disassociateRoles)
            return ugService.createGroupObject("g_all", true);
        else
            return ugService.getGroupByGroupname("group1");
    }

    @Override
    protected GeoserverUserGroup getProblematicObject() throws Exception {
        return null;
    }

    @Override
    protected String getProblematicObjectRegExp() throws Exception{
        return "";
    }

    @Override
    protected String getRemoveableObjectRegExp() throws Exception{
        if (disassociateRoles)
            return ".*"+getRemoveableObject().getGroupname()+".*ROLE_WMS.*";
        else    
            return ".*"+getRemoveableObject().getGroupname()+".*";
    }    


}
