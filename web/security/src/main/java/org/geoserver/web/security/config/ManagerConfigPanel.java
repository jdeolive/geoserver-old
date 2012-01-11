/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.web.security.config;
   
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.extensions.markup.html.form.palette.Palette;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.model.util.SetModel;
import org.geoserver.security.GeoServerSecurityManager;
import org.geoserver.security.config.SecurityManagerConfig;
import org.geoserver.security.password.AbstractGeoserverPasswordEncoder;
import org.geoserver.security.password.GeoServerPasswordEncoder;
import org.geoserver.web.GeoServerApplication;


public class ManagerConfigPanel extends Panel {
    
    private static final long serialVersionUID = 1L;
    protected Form<SecurityConfigModelHelper> form;
    protected Palette<String> authProviders;
    protected List<String> encoderList = new ArrayList<String>();
    protected List<String> disabledEncoders = new ArrayList<String>();
    protected CheckBox anonymousAuth,encryptingUrlParams;
    DropDownChoice<String> roleServices,passwordEncrypters;
    
    public ManagerConfigPanel(String id) {
        super(id);

        SecurityManagerConfig config = null;
        List<String> roleServicesList = null;
        SetModel<String> allProviders = null;        
        
        try {
            config = getSecurityManager().loadSecurityConfig();
            roleServicesList = new ArrayList<String>();
            roleServicesList.addAll(getSecurityManager().listRoleServices());
            allProviders = new SetModel<String>();        
            allProviders.setObject(getSecurityManager().listAuthenticationProviders());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        
        SecurityConfigModelHelper helper= new SecurityConfigModelHelper(config, false);
        CompoundPropertyModel<SecurityConfigModelHelper> model = new 
                CompoundPropertyModel<SecurityConfigModelHelper>(helper);
        
        
        if (AbstractGeoserverPasswordEncoder.isStrongCryptographyAvailable()==false)
            warn(new ResourceModel("security.noStrongEncryption").getObject());
        else 
            info(new ResourceModel("security.strongEncryption").getObject());
        
        String formWicketId= this.getClass().getSimpleName()+"Form";
        form = new Form<SecurityConfigModelHelper>(formWicketId,model);
        add(form);                
        
        form.add(anonymousAuth=new CheckBox("config.anonymousAuth"));
        form.add(encryptingUrlParams=new CheckBox("config.encryptingUrlParams"));
        
        IChoiceRenderer<String> identityRender = new IChoiceRenderer<String>() {

            private static final long serialVersionUID = 1L;

            @Override
            public Object getDisplayValue(String object) {
                return object;
            }

            @Override
            public String getIdValue(String object, int index) {
                return object;
            }
        };
 
        
        roleServices = 
                new DropDownChoice<String>("config.roleServiceName",roleServicesList,identityRender); 

        roleServices.setNullValid(false);
        form.add(roleServices);

        List<GeoServerPasswordEncoder> encoders = 
            getSecurityManager().loadPasswordEncoders(null, true, null);
        
        encoderList = new ArrayList<String>();
        disabledEncoders = new ArrayList<String>();
        for (GeoServerPasswordEncoder encoder : encoders) {
            encoderList.add(encoder.getBeanName());
            if (AbstractGeoserverPasswordEncoder.isStrongCryptographyAvailable()==false
                   && encoder.isAvailableWithoutStrongCryptogaphy()==false) {
                disabledEncoders.add(encoder.getBeanName());
            }
        }
        
        passwordEncrypters =  
                new DropDownChoice<String>("config.configPasswordEncrypterName",encoderList,
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
        form.add(passwordEncrypters);
        
        
        authProviders=new Palette<String>("config.authProviderNames",
                new ListModel<String>(config.getAuthProviderNames()),
                allProviders, identityRender, 10, true);
        form.add(authProviders);
        form.add(saveLink());
    }
    
    
    SubmitLink saveLink() {
        return new SubmitLink("save") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onSubmit() {
                SecurityConfigModelHelper helper =ManagerConfigPanel.this.form.getModelObject();
                // Workaround, Palette seems not to work with CompoundPropertyModel
                Iterator<String> it = authProviders.getSelectedChoices();
                SecurityManagerConfig config = (SecurityManagerConfig) helper.getConfig(); 
                config.getAuthProviderNames().clear();
                while (it.hasNext()) {
                    config.getAuthProviderNames().add(it.next());
                }
                // end workaround
                if (helper.hasChanges()) {
                    try {
                        getSecurityManager().saveSecurityConfig(
                                (SecurityManagerConfig) helper.getConfig());
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }

            }
        };
    }

    GeoServerSecurityManager getSecurityManager() {
        return GeoServerApplication.get().getSecurityManager();
    }
    
    
}
