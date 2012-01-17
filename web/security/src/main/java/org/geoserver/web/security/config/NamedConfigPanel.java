/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.web.security.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.security.GeoServerAuthenticationProvider;
import org.geoserver.security.GeoServerSecurityFilter;
import org.geoserver.security.GeoServerSecurityManager;
import org.geoserver.security.GeoServerSecurityProvider;
import org.geoserver.security.GeoServerRoleService;
import org.geoserver.security.GeoServerUserGroupService;
import org.geoserver.security.config.PasswordPolicyConfig;
import org.geoserver.security.config.SecurityAuthProviderConfig;
import org.geoserver.security.config.SecurityRoleServiceConfig;
import org.geoserver.security.config.SecurityUserGroupServiceConfig;
import org.geoserver.security.password.PasswordValidator;
import org.geoserver.security.validation.SecurityConfigException;
import org.geoserver.security.validation.SecurityConfigValidator;
import org.geoserver.web.GeoServerApplication;
import org.geoserver.web.security.AbstractSecurityPage;
import org.geoserver.web.security.config.details.AbstractNamedConfigDetailsPanel;
import org.geoserver.web.security.config.details.NamedConfigDetailsEmptyPanel;
import org.geoserver.web.security.config.details.NamedConfigDetailsPanelProvider;
import org.geoserver.web.wicket.ParamResourceModel;
import org.geotools.util.logging.Logging;

/**
 * A form component that can be used to edit user to group assignments
 */
public class NamedConfigPanel extends Panel {
    
    static Logger LOGGER = Logging.getLogger("org.geoserver.security");
    public static final String DETAILS_WICKET_ID = "details";
    private static final long serialVersionUID = 1L;
    protected TextField<String> name;
    protected DropDownChoice<String> implClass;
    protected Form<SecurityNamedConfigModelHelper> form;
    protected Class<?> extensionPoint;
    protected CompoundPropertyModel<SecurityNamedConfigModelHelper> model;
    
    

    public NamedConfigPanel(String id,SecurityNamedConfigModelHelper helper, Class<?> extensionPoint, AbstractSecurityPage responsePage) {        
        super(id);

        this.extensionPoint=extensionPoint;

        model = new CompoundPropertyModel<SecurityNamedConfigModelHelper>(helper);
        form = new Form<SecurityNamedConfigModelHelper>("namedConfig",model); 
        add(form);        
        name = new TextField<String>("config.name");        
        name.setEnabled(helper.isNew());   
        name.setRequired(false);
        form.add(name);
        
        List<String> classNames = getImplementations();
//        if (helper.isNew() && classNames.size()>=1) {
//            helper.getConfig().setClassName(classNames.get(0));                
//        }
        implClass = new DropDownChoice<String>("config.className",                  
            //new PropertyModel<String>(model.getObject(), "config.className"),                
            classNames,
            new IChoiceRenderer<String>() {
                private static final long serialVersionUID = 1L;

                @Override
                public Object getDisplayValue(String className) {
                    return new ResourceModel("security."+className,
                            className).getObject();
                }

                @Override
                public String getIdValue(String className, int index) {
                    return className;
                }
                
            }); 
            /*{
                private static final long serialVersionUID = 9004791493341302097L;
                @Override
                public void updateModel() {
                    NamedConfigPanel.this.model.getObject().
                        getConfig().setClassName(getConvertedInput());
                }
            };*/
            
        
        
        implClass.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                Component old = form.get(DETAILS_WICKET_ID);
                Component comp = getConfigDetailsPanel(
//                        implClass.getModel().getObject());
                        NamedConfigPanel.this.model.getObject().getConfig().getClassName());
                old.replaceWith(comp);
                comp.setOutputMarkupId(true);
                //comp.setMarkupId(old.getMarkupId());
                //form.addOrReplace(comp);
                target.addComponent(comp);
                //target.addComponent(old);
            }
        });

        implClass.setEnabled(helper.isNew());
        
//        if (helper.getConfig().getClassName() == null ||
//            helper.getConfig().getClassName().length()==0) {    
//            form.add(new NamedConfigDetailsEmptyPanel(DETAILS_WICKET_ID, model));            
//        } else {
//            form.add(getConfigDetailsPanel(helper.getConfig().getClassName()));
//        }

        if (helper.isNew())
              form.add(new NamedConfigDetailsEmptyPanel(DETAILS_WICKET_ID, model));
        else
          form.add(getConfigDetailsPanel(helper.getConfig().getClassName()));

        
        form.get(DETAILS_WICKET_ID).setOutputMarkupId(true);                
        //implClass.setRequired(true);        
        implClass.setOutputMarkupId(true);
        //implClass.setNullValid(false);
        implClass.setNullValid(true);
        form.add(implClass);
        
        form.add(getCancelLink(responsePage));
        form.add(saveLink(responsePage));        

    }
                        
    /**
     * Returns a list of implementations for 
     * {@link #extensionPoint}  
     * 
     * @return
     */
    protected List<String> getImplementations() {
        
        List<GeoServerSecurityProvider> list = new ArrayList<GeoServerSecurityProvider>( 
                GeoServerExtensions.extensions(GeoServerSecurityProvider.class));
                
        Set<String> result=new HashSet<String>();
        for (GeoServerSecurityProvider prov : list) {            
            Class<?> aClass = prov.getAuthenticationProviderClass();            
            if (aClass!=null) {
                if (extensionPoint.isAssignableFrom(prov.getAuthenticationProviderClass()))
                result.add(aClass.getName());
            }
            aClass = prov.getPasswordValidatorClass();            
            if (aClass!=null) {
                if (extensionPoint.isAssignableFrom(prov.getPasswordValidatorClass()))
                result.add(aClass.getName());
            }
            aClass = prov.getRoleServiceClass();            
            if (aClass!=null) {
                if (extensionPoint.isAssignableFrom(prov.getRoleServiceClass()))
                result.add(aClass.getName());
            }
            
            aClass = prov.getUserGroupServiceClass();            
            if (aClass!=null) {
                if (extensionPoint.isAssignableFrom(prov.getUserGroupServiceClass()))
                result.add(aClass.getName());
            }
            
            aClass = prov.getFilterClass();            
            if (aClass!=null) {
                if (extensionPoint.isAssignableFrom(prov.getFilterClass()))
                result.add(aClass.getName());
            }                                                
        }
        List<String> resList = new ArrayList<String>();
        resList.addAll(result);
        return resList;
    }
    
    SubmitLink saveLink(final AbstractSecurityPage responsePage) {
        return new SubmitLink("save") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onSubmit() {                
                if (model.getObject().hasChanges()) {                    
                    try {                        
                        saveConfiguration();
                        responsePage.setDirty(true);
                        setResponsePage(responsePage);
                    } catch (SecurityConfigException se) {    
                      error(new ParamResourceModel("security."+se.getErrorId()
                              , null, se.getArgs()).getObject());
                    } catch (IOException e) {
                        form.error(e.getMessage());
                        LOGGER.log(Level.SEVERE,e.getMessage(),e);                        
                    }                                                
                }
            }
        };
    }
        
    public Link<Page> getCancelLink(final AbstractSecurityPage returnPage) {
        return new Link<Page>("cancel") {
            private static final long serialVersionUID = 1L;
            @Override
            public void onClick() {
                returnPage.setDirty(false); 
                setResponsePage(returnPage);
            }            
        }; 
    }

    AbstractNamedConfigDetailsPanel getConfigDetailsPanel(String className) {
        AbstractNamedConfigDetailsPanel panel = null;
        List<NamedConfigDetailsPanelProvider> providers =
                GeoServerExtensions.extensions(NamedConfigDetailsPanelProvider.class);
        for (NamedConfigDetailsPanelProvider provider : providers) {
            panel=provider.getDetailsPanel(className, DETAILS_WICKET_ID, model);
            if (panel!=null) {
                panel.setOutputMarkupId(true);
                return panel;                
            }
        }
        throw new RuntimeException("No details panel for "+className);
    }

    protected void saveConfiguration() throws IOException,SecurityConfigException {
        
        GeoServerSecurityManager manager = GeoServerApplication.get().getSecurityManager();
        SecurityNamedConfigModelHelper helper = model.getObject();        
        if (helper.isNew()) {
            SecurityConfigValidator val = new SecurityConfigValidator(manager);
            val.validateAddNamedService(extensionPoint, helper.getConfig());
        }
        
        if (GeoServerAuthenticationProvider.class.isAssignableFrom(extensionPoint)) {
            manager.saveAuthenticationProvider((SecurityAuthProviderConfig) helper.getConfig());
        }
        if (GeoServerUserGroupService.class.isAssignableFrom(extensionPoint)) {
            manager.saveUserGroupService((SecurityUserGroupServiceConfig)helper.getConfig());
        }
        if (GeoServerRoleService.class.isAssignableFrom(extensionPoint)) {
            manager.saveRoleService((SecurityRoleServiceConfig) helper.getConfig());
        }
        if (PasswordValidator.class.isAssignableFrom(extensionPoint)) {
            manager.savePasswordPolicy((PasswordPolicyConfig) helper.getConfig());
        }
        if (GeoServerSecurityFilter.class.isAssignableFrom(extensionPoint)) {
            manager.saveFilter(helper.getConfig());
        }
    }

    
}
