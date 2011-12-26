/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.web.security.config.details;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.CompoundPropertyModel;
import org.geoserver.security.GeoserverRoleService;
import org.geoserver.security.config.SecurityRoleServiceConfig;
import org.geoserver.security.impl.GeoserverRole;
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

//        SecurityRoleServiceConfig config = 
//                (SecurityRoleServiceConfig) configHelper.getConfig();
        
        
        
        rolesList = new ArrayList<String>();
        rolesList.add(GeoserverRole.ADMIN_ROLE.getAuthority());
        
                        
        adminRoleName =  
                new DropDownChoice<String>("config.adminRoleName",rolesList);
        adminRoleName.setNullValid(false);
        adminRoleName.setEnabled(rolesList.size()>0);
        add(adminRoleName);

    }
    
    @Override
    public void updateModel() {
        if (adminRoleName.isEnabled())
            adminRoleName.updateModel();
    }
                                                    
}
