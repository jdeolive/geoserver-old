/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */


package org.geoserver.web.security.role;



import java.io.IOException;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.model.StringResourceModel;
import org.geoserver.security.impl.GeoserverRole;
import org.geoserver.web.ComponentBuilder;
import org.geoserver.web.FormTestPage;
import org.geoserver.web.security.AbstractConfirmRemovalPanelTest;

public class ConfirmRemovalRolePanelTest extends AbstractConfirmRemovalPanelTest<GeoserverRole> {
    private static final long serialVersionUID = 1L;

    protected void setupPanel(final List<GeoserverRole> roots)  {
        
        tester.startPage(new FormTestPage(new ComponentBuilder() {
            private static final long serialVersionUID = 1L;

            public Component buildComponent(String id) {
                return new ConfirmRemovalRolePanel(id, roots.toArray(new GeoserverRole[roots.size()])) {
                    @Override
                    protected StringResourceModel canRemove(GeoserverRole data) {
                        SelectionRoleRemovalLink link = new SelectionRoleRemovalLink("XXX",null,null);
                        return link.canRemove(data);
                    }

                    private static final long serialVersionUID = 1L;                    
                };
            }
        }));
    }
    
    public void testRemoveRoleJDBC() throws Exception {
        initializeForJDBC();
        removeObject();
        
    }
    public void testRemoveRoleXML() throws Exception {
        initializeForXML();
        removeObject();                                       
    }
    
    

    @Override
    protected GeoserverRole getRemoveableObject() throws Exception{
        GeoserverRole role =  gaService.getRoleByName("ROLE_NEW");
        if (role == null) {
            gaStore.addRole(role =gaStore.createRoleObject("ROLE_NEW"));
            gaStore.store();
        }
        return role;    
    }

    @Override
    protected GeoserverRole getProblematicObject() throws Exception {
        return gaService.getRoleByName(
                GeoserverRole.ADMIN_ROLE.getAuthority());
    }

    @Override
    protected String getProblematicObjectRegExp() throws Exception{
        return ".*"+getProblematicObject().getAuthority()+".*";
    }

    @Override
    protected String getRemoveableObjectRegExp() throws Exception{
        return ".*"+getRemoveableObject().getAuthority()+".*";
    }    


}
