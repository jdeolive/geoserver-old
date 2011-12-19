/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.web.security.config.details;

import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.geoserver.security.config.SecurityNamedServiceConfig;
import org.geoserver.security.config.impl.XMLFileBasedRoleServiceConfigImpl;
import org.geoserver.security.xml.XMLConstants;
import org.geoserver.web.security.config.SecurityNamedConfigModelHelper;

/**
 * A form component that can be used for xml configurations
 */
public class XMLRoleConfigDetailsPanel extends AbstractRoleDetailsPanel{
    private static final long serialVersionUID = 1L;
    protected CheckBox validating;
    protected TextField<String> fileName;
    protected TextField<Integer> checkInterval;

    
    public XMLRoleConfigDetailsPanel(String id, CompoundPropertyModel<SecurityNamedConfigModelHelper> model) {
        super(id,model);
    }

    @Override
    protected void initializeComponents() {
        super.initializeComponents();        
        fileName = new TextField<String>("config.fileName");
        fileName.setEnabled(configHelper.isNew());
        add(fileName);

        checkInterval = new TextField<Integer>("config.checkInterval", Integer.class);
        add(checkInterval);
        
        validating  = new CheckBox("config.validating");
        add(validating);
    };
        
    
    @Override
    protected SecurityNamedServiceConfig createNewConfigObject() {
        XMLFileBasedRoleServiceConfigImpl  config= new XMLFileBasedRoleServiceConfigImpl();
        config.setFileName(XMLConstants.FILE_RR);
        return config;        
    }
    
    @Override
    public void updateModel() {
        super.updateModel();
        validating.updateModel();
        fileName.updateModel();
        checkInterval.updateModel();
    }
}

