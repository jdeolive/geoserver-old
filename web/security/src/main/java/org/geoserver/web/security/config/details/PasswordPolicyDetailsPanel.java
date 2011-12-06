/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.web.security.config.details;

import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.geoserver.security.config.SecurityNamedServiceConfig;
import org.geoserver.security.config.impl.PasswordPolicyConfigImpl;
import org.geoserver.web.security.config.SecurityNamedConfigModelHelper;

/**
 * A form component for password policies
 * 
 * @author christian
 *
 */
public  class PasswordPolicyDetailsPanel extends AbstractNamedConfigDetailsPanel {
    private static final long serialVersionUID = 1L;
    
    protected CheckBox digitRequired,uppercaseRequired,lowercaseRequired;
    protected RequiredTextField<Integer> minLength,maxLength;


    
    public PasswordPolicyDetailsPanel(String id, CompoundPropertyModel<SecurityNamedConfigModelHelper> model) {
        super(id,model);
    }

    @Override
    protected void initializeComponents() {

        
        add(digitRequired=new CheckBox("config.digitRequired"));
        add(uppercaseRequired=new CheckBox("config.uppercaseRequired"));
        add(lowercaseRequired=new CheckBox("config.lowercaseRequired"));
        add(minLength=new RequiredTextField<Integer>("config.minLength"));
        add(maxLength=new RequiredTextField<Integer>("config.maxLength"));
        

    }

    @Override
    protected SecurityNamedServiceConfig createNewConfigObject() {
        return new PasswordPolicyConfigImpl();
    }
     
    @Override
    public void updateModel() {
        digitRequired.updateModel();
        uppercaseRequired.updateModel();
        lowercaseRequired.updateModel();
        minLength.updateModel();
        maxLength.updateModel();
    }
    
}
