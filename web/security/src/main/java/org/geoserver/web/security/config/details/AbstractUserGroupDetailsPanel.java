/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.web.security.config.details;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.security.GeoserverUserGroupService;
import org.geoserver.security.config.SecurityUserGroupServiceConfig;
import org.geoserver.security.password.AbstractGeoserverPasswordEncoder;
import org.geoserver.security.password.GeoserverDigestPasswordEncoder;
import org.geoserver.security.password.GeoserverUserPasswordEncoder;
import org.geoserver.security.password.PasswordEncodingType;
import org.geoserver.security.password.PasswordValidatorImpl;
import org.geoserver.web.security.config.SecurityNamedConfigModelHelper;

/**
 * A form component without any details
 * 
 * @author christian
 *
 */
public abstract class AbstractUserGroupDetailsPanel extends AbstractNamedConfigDetailsPanel {
    private static final long serialVersionUID = 1L;
    
        
    List<String> encoderList;
    List<String> disabledEncoders;
    List<String> passwordPolicies;

    protected DropDownChoice<String> passwordEncoderName,passwordPolicyName;

    
    public AbstractUserGroupDetailsPanel(String id, CompoundPropertyModel<SecurityNamedConfigModelHelper> model) {
        super(id,model);
        
    }

    @Override
    protected void initializeComponents() {

        SecurityUserGroupServiceConfig config = 
                (SecurityUserGroupServiceConfig) configHelper.getConfig();
        
        
        List<GeoserverUserPasswordEncoder> encoders = 
                GeoServerExtensions.extensions(GeoserverUserPasswordEncoder.class);
        
        encoderList = new ArrayList<String>();
        disabledEncoders = new ArrayList<String>();
        for (GeoserverUserPasswordEncoder encoder : encoders) {
            encoderList.add(encoder.getBeanName());
            if (AbstractGeoserverPasswordEncoder.isStrongCryptographyAvailable()==false
                   && encoder.isAvailableWithoutStrongCryptogaphy()==false) {
                disabledEncoders.add(encoder.getBeanName());
            }
        }
        
        // set defaults for a new service
        if (configHelper.isNew()) {                 
            config.setPasswordEncoderName(GeoserverDigestPasswordEncoder.BeanName);
            config.setPasswordPolicyName(PasswordValidatorImpl.DEFAULT_NAME);
        }
        
        passwordEncoderName =  
                new DropDownChoice<String>("config.passwordEncoderName",encoderList,
                        new IChoiceRenderer<String>() {
                            private static final long serialVersionUID = 1L;

                            @Override
                            public Object getDisplayValue(String object) {
                                return new ResourceModel("security."+object,object).getObject();
                            }

                            @Override
                            public String getIdValue(String object, int index) {
                                return object;
                            }
                        }                                                
                        ){
                    private static final long serialVersionUID = 1L;
            @Override
            protected boolean isDisabled(String object, int index, String selected) {
                return disabledEncoders.contains(object);
            }
        };                

        // enable/disable changing the password encoder for an existing service
        try {
            if (configHelper.isNew()==false) {                
                GeoserverUserPasswordEncoder encoder = (GeoserverUserPasswordEncoder) 
                    GeoServerExtensions.bean(config.getPasswordEncoderName());
                GeoserverUserGroupService service = 
                        getSecurityManager().loadUserGroupService(config.getName());
                // check if we have a write able service with digest encoding and
                // if there are already digested passwords
                boolean disabled = encoder.getEncodingType()==PasswordEncodingType.DIGEST &&                    
                        service.canCreateStore();
                        // TODO, to costly, need a method service.getCountUsers()            
                    //  &&service.getUsers().size()>0;                    
                passwordEncoderName.setEnabled(!disabled);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        add(passwordEncoderName);

        passwordPolicies = new ArrayList<String>();
        try {
            passwordPolicies.addAll(getSecurityManager().listPasswordValidators());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        passwordPolicyName =new DropDownChoice<String>("config.passwordPolicyName",passwordPolicies);
        add(passwordPolicyName);
    }     
    
    @Override
    public void updateModel() {
        super.updateModel();
        //isLockingNeeded.updateModel();
        passwordEncoderName.updateModel();
        passwordPolicyName.updateModel();        
    }
}
