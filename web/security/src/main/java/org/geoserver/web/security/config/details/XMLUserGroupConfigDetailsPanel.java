/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.web.security.config.details;

import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.model.IModel;
import org.geoserver.security.config.SecurityNamedServiceConfig;
import org.geoserver.security.config.impl.XMLFileBasedUserGroupServiceConfigImpl;
import org.geoserver.web.security.config.SecurityNamedConfigModelHelper;

/**
 * A form component that can be used for xml configurations
 */
public class XMLUserGroupConfigDetailsPanel extends AbstractUserGroupDetailsPanel{
    private static final long serialVersionUID = 1L;
    protected CheckBox validating;
    protected RequiredTextField<String> fileName;
    protected RequiredTextField<Integer> checkInterval;
    
    public XMLUserGroupConfigDetailsPanel(String id, IModel<SecurityNamedConfigModelHelper> model) {
        super(id,model);
    }

    @Override
    protected void initializeComponents() {
        super.initializeComponents();
        fileName = new RequiredTextField<String>("config.fileName");
        add(fileName);

        checkInterval = new RequiredTextField<Integer>("config.checkInterval", Integer.class);
        add(checkInterval);
        
        validating  = new CheckBox("config.validating");
        add(validating);
        
    };
        
    
    @Override
    protected SecurityNamedServiceConfig createNewConfigObject() {
        return new XMLFileBasedUserGroupServiceConfigImpl();
    }
                            
}
