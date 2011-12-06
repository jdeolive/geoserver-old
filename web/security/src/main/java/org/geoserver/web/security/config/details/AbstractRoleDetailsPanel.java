/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.web.security.config.details;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.CompoundPropertyModel;
import org.geoserver.security.GeoserverRoleService;
import org.geoserver.security.config.SecurityRoleServiceConfig;
import org.geoserver.security.impl.GeoserverRole;
import org.geoserver.security.xml.XMLRoleService;
import org.geoserver.web.security.config.SecurityNamedConfigModelHelper;

/**
 * A form component without any details
 * 
 * @author christian
 *
 */
public abstract class AbstractRoleDetailsPanel extends AbstractNamedConfigDetailsPanel {
    private static final long serialVersionUID = 1L;
    
    protected CheckBox isLockingNeeded;    
    List<String> rolesList;

    protected DropDownChoice<String> adminRoleName;

    
    public AbstractRoleDetailsPanel(String id, CompoundPropertyModel<SecurityNamedConfigModelHelper> model) {
        super(id,model);
    }

    @Override
    protected void initializeComponents() {

        SecurityRoleServiceConfig config = 
                (SecurityRoleServiceConfig) configHelper.getConfig();
        
        // for the default service, locking is needed
        if (XMLRoleService.DEFAULT_NAME.equals(config.getName()))
            config.setLockingNeeded(true);
        add(isLockingNeeded=new CheckBox("config.lockingNeeded"));
        if (XMLRoleService.DEFAULT_NAME.equals(config.getName()))
            isLockingNeeded.setEnabled(false); // 
        
        
        rolesList = new ArrayList<String>();
        
        if (configHelper.isNew()==false) { 
            try {
                GeoserverRoleService service = getSecurityManager().loadRoleService(config.getName());
                for (GeoserverRole role : service.getRoles()) {
                    rolesList.add(role.getAuthority());
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        
        
        
        adminRoleName =  
                new DropDownChoice<String>("config.adminRoleName",rolesList);
        adminRoleName.setNullValid(true);
        adminRoleName.setEnabled(rolesList.size()>0);
        add(adminRoleName);

    }
                                                    
}
