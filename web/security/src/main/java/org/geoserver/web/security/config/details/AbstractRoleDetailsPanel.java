/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.web.security.config.details;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.CompoundPropertyModel;
import org.geoserver.security.impl.GeoServerRole;
import org.geoserver.web.security.config.SecurityNamedConfigModelHelper;

/**
 * A form component for role services
 * 
 * @author christian
 *
 */
public abstract class AbstractRoleDetailsPanel extends AbstractNamedConfigDetailsPanel {
    private static final long serialVersionUID = 1L;
    
    List<String> rolesList;

    protected DropDownChoice<String> adminRoleName;

    
    public AbstractRoleDetailsPanel(String id, CompoundPropertyModel<SecurityNamedConfigModelHelper> model) {
        super(id,model);                
    }

    @Override
    protected void initializeComponents() {

        
        rolesList = new ArrayList<String>();
        adminRoleName =  
                new DropDownChoice<String>("config.adminRoleName",rolesList);
        adminRoleName.setNullValid(true);        

        
        if (configHelper.isNew()==false) {
            String serviceName = configHelper.getConfig().getName();
            try {
                SortedSet<GeoServerRole> roles = 
                        getSecurityManager().loadRoleService(serviceName).getRoles();
                for (GeoServerRole role : roles) 
                    rolesList.add(role.getAuthority());
            } catch (IOException e) {
                // do nothing, service not available
            }
            adminRoleName.setEnabled(rolesList.size()>0);
        } else {
            adminRoleName.setEnabled(false);
        }                                        
        add(adminRoleName);

    }
    
    @Override
    public void updateModel() {
        if (adminRoleName.isEnabled())
            adminRoleName.updateModel();
    }
                                                    
}
