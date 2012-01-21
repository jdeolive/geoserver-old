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
import org.geoserver.web.security.config.SecurityNamedConfigModelHelper;
import org.geoserver.web.wicket.SimpleChoiceRenderer;

/**
 * Abstract base class for authentication provider details
 * 
 * @author christian
 *
 */
public abstract class AbstractAuthenticationProviderDetailsPanel extends AbstractNamedConfigDetailsPanel {
    private static final long serialVersionUID = 1L;
    
    List<String> ugList;
    protected DropDownChoice<String> userGroupService;

    
    public AbstractAuthenticationProviderDetailsPanel(String id, CompoundPropertyModel<SecurityNamedConfigModelHelper> model) {
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
                        
        userGroupService =  
                new DropDownChoice<String>("config.userGroupServiceName",ugList, new SimpleChoiceRenderer());
        //userGroupService.setNullValid(false);
        userGroupService.setEnabled(ugList.size()>0);
        add(userGroupService);

    }

    
    @Override
    public void updateModel() {
        if (userGroupService.isEnabled())
            userGroupService.updateModel();
    }
}
