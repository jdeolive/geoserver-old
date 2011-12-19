/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.web.security.config.details;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.geoserver.security.config.PasswordPolicyConfig;
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
    protected AjaxCheckBox unlimitedComp;
    protected Label maxLengthLabel; 
    protected TextField<Integer> minLength,maxLength;    
    int savedMaxLength;

    

    public PasswordPolicyDetailsPanel(String id, CompoundPropertyModel<SecurityNamedConfigModelHelper> model) {
        super(id,model);
    }

    @Override
    protected void initializeComponents() {

        
        add(digitRequired=new CheckBox("config.digitRequired"));
        add(uppercaseRequired=new CheckBox("config.uppercaseRequired"));
        add(lowercaseRequired=new CheckBox("config.lowercaseRequired"));
        add(minLength=new TextField<Integer>("config.minLength"));
        add(maxLength=new TextField<Integer>("config.maxLength"));
        add(maxLengthLabel = new Label("maxLengthLabel", new StringResourceModel("maxLength", this,null)));
        
        maxLength.setOutputMarkupPlaceholderTag(true);
        maxLengthLabel.setOutputMarkupPlaceholderTag(true);
        
        boolean unlimtedLength =((PasswordPolicyConfig)configHelper.getConfig()).getMaxLength()==-1;
        maxLength.setVisible(!unlimtedLength);
        maxLengthLabel.setVisible(!unlimtedLength);
        
        IModel<Boolean> unlimitedModel = new IModel<Boolean>() {
            private static final long serialVersionUID = 1L;
            boolean value;
            @Override
            public void detach() {
            }
            
            @Override
            public void setObject(Boolean object) {
                value=object;
            }
            
            @Override
            public Boolean getObject() {
                return value;
            }
        };

        unlimitedModel.setObject(unlimtedLength);
        savedMaxLength=((PasswordPolicyConfig)configHelper.getConfig()).getMaxLength();
        
        unlimitedComp=new AjaxCheckBox("unlimited", unlimitedModel )
        {
            private static final long serialVersionUID = 1L;
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                Boolean value = getModelObject();
                maxLength.setVisible(!value);
                maxLengthLabel.setVisible(!value);
                //PasswordPolicyConfig pwConfig = (PasswordPolicyConfig) configHelper.getConfig();
                if (value) {                                        
                    maxLength.setModelObject(-1);
                }
                else {
                    maxLength.setModelObject(savedMaxLength);
                }    
                target.addComponent(maxLength);
                target.addComponent(maxLengthLabel);
            }
        };
        add(unlimitedComp);
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
        maxLength.setConvertedInput(maxLength.getModelObject());
        maxLength.updateModel();
        unlimitedComp.updateModel();
    }


}
