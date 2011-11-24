/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.web.security.config.details;

import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.model.IModel;
import org.geoserver.security.config.SecurityNamedServiceConfig;
import org.geoserver.security.config.impl.XMLFileBasedSecurityServiceConfigImpl;
import org.geoserver.web.security.config.SecurityNamedConfigModelHelper;

/**
 * A form component that can be used for xml configurations
 */
public class XMLNamedConfigPanel extends FormComponentPanel<SecurityNamedConfigModelHelper>{
    private static final long serialVersionUID = 1L;
    protected CheckBox validating;
    
    public XMLNamedConfigPanel(String id, IModel<SecurityNamedConfigModelHelper> model) {
        super(id,model);
        SecurityNamedConfigModelHelper helper = model.getObject();
        if (helper.isNew()) {
            XMLFileBasedSecurityServiceConfigImpl newConfig = new XMLFileBasedSecurityServiceConfigImpl(); 
            SecurityNamedServiceConfig old =  helper.getConfig();
            newConfig.setName(old.getName());
            newConfig.setClassName(old.getClassName());
            model.setObject(new SecurityNamedConfigModelHelper(newConfig, true));
        }
        
        validating  = new CheckBox("config.validating");
        add(validating);
    }
                            
}
