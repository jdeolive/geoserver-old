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
import org.geoserver.security.UsernamePasswordAuthenticationProviderConfig;
import org.geoserver.security.config.SecurityNamedServiceConfig;
import org.geoserver.web.security.config.SecurityNamedConfigModelHelper;

/**
 * A form component for role services
 * 
 * @author christian
 *
 */
public  class UsernamePasswordDetailsPanel extends AbstractNamedConfigDetailsPanel {
    private static final long serialVersionUID = 1L;
    
    List<String> ugList;
    protected DropDownChoice<String> userGroupService;

    
    public UsernamePasswordDetailsPanel(String id, CompoundPropertyModel<SecurityNamedConfigModelHelper> model) {
        super(id,model);
    }

    @Override
    protected void initializeComponents() {

                        
        ugList = new ArrayList<String>();        
        try {
            ugList.addAll(getSecurityManager().listUserGroupServices());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (configHelper.isNew() && ugList.size()>0) {
            UsernamePasswordAuthenticationProviderConfig config = 
                (UsernamePasswordAuthenticationProviderConfig) configHelper.getConfig();
            config.setUserGroupServiceName(ugList.get(0));
        }
                        
        userGroupService =  
                new DropDownChoice<String>("config.userGroupServiceName",ugList);
        userGroupService.setNullValid(false);
        userGroupService.setEnabled(ugList.size()>0);
        add(userGroupService);

    }

    @Override
    protected   SecurityNamedServiceConfig createNewConfigObject() {
        return new UsernamePasswordAuthenticationProviderConfig();
    }
}
