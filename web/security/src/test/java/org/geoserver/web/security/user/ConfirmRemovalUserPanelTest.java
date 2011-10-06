/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */


package org.geoserver.web.security.user;



import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.geoserver.security.impl.GeoserverGrantedAuthority;
import org.geoserver.security.impl.GeoserverUser;
import org.geoserver.web.ComponentBuilder;
import org.geoserver.web.FormTestPage;
import org.geoserver.web.security.AbstractConfirmRemovalPanelTest;

public class ConfirmRemovalUserPanelTest extends AbstractConfirmRemovalPanelTest<GeoserverUser> {
    private static final long serialVersionUID = 1L;

    boolean disassociateRoles = false;
    
    protected void setupPanel(final List<GeoserverUser> roots) {
        tester.startPage(new FormTestPage(new ComponentBuilder() {
            private static final long serialVersionUID = 1L;

            public Component buildComponent(String id) {
                Model<Boolean> model = new Model<Boolean>(disassociateRoles);
                return new ConfirmRemovalUserPanel(id, model,roots.toArray(new GeoserverUser[roots.size()])) {
                    @Override
                    protected StringResourceModel canRemove(GeoserverUser data) {
                        SelectionUserRemovalLink link = new SelectionUserRemovalLink("XXX",null,null,disassociateRoles);
                        return link.canRemove(data);
                    }

                    private static final long serialVersionUID = 1L;                    
                };
            }
        }));
    }
    
    public void testRemoveUserJDBC() throws Exception {
        disassociateRoles=false;
        initializeForJDBC();
        removeObject();
        
    }
    public void testRemoveUserXML() throws Exception {
        disassociateRoles=false;
        initializeForXML();
        removeObject();                                       
    }

    public void testRemoveUserWithRolesJDBC() throws Exception {
        disassociateRoles=true;
        initializeForJDBC();
        removeObject();
        
    }
    public void testRemoveUserWithRolesXML() throws Exception {
        disassociateRoles=true;
        initializeForXML();
        removeObject();                                       
    }

    

    @Override
    protected GeoserverUser getRemoveableObject() throws Exception{
            return ugService.getUserByUsername("admin");
    }

    @Override
    protected GeoserverUser getProblematicObject() throws Exception {
        return null;
    }

    @Override
    protected String getProblematicObjectRegExp() throws Exception{
        return "";
    }

    @Override
    protected String getRemoveableObjectRegExp() throws Exception{
        if (disassociateRoles)
            return ".*"+getRemoveableObject().getUsername()+".*" +
            		GeoserverGrantedAuthority.ADMIN_ROLE +".*";
        else    
            return ".*"+getRemoveableObject().getUsername()+".*";
    }    


}
