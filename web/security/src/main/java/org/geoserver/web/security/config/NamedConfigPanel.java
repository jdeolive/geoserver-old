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
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.ValidationError;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.security.GeoServerAuthenticationProvider;
import org.geoserver.security.GeoServerSecurityFilter;
import org.geoserver.security.GeoServerSecurityManager;
import org.geoserver.security.GeoServerSecurityProvider;
import org.geoserver.security.GeoserverRoleService;
import org.geoserver.security.GeoserverUserGroupService;
import org.geoserver.security.config.PasswordPolicyConfig;
import org.geoserver.security.config.SecurityAuthProviderConfig;
import org.geoserver.security.config.SecurityNamedServiceConfig;
import org.geoserver.security.config.SecurityRoleServiceConfig;
import org.geoserver.security.config.SecurityUserGoupServiceConfig;
import org.geoserver.security.password.PasswordValidator;
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
    protected Set<String> alreadyUsedNames=null;
    protected CompoundPropertyModel<SecurityNamedConfigModelHelper> model;
    
    /**
     * Validates service name for new services
     * 
     * @author christian
     *
     */           
    class NameValidator extends AbstractValidator<String> {

        private static final long serialVersionUID = 1L;
                

        @Override
        protected void onValidate(IValidatable<String> validatable) {
            //name.updateModel();
            String newName = validatable.getValue();
            if (alreadyUsedNames.contains(newName)) {
                ValidationError error = new ValidationError();
                error.setMessage(new ResourceModel(NamedConfigPanel.class.getSimpleName()+".nameConflict").getObject());
                error.setVariable("service", (Object) newName);
                validatable.error(error);
            }
        }
        
    };
    

    public NamedConfigPanel(String id,SecurityNamedConfigModelHelper helper, Class<?> extensionPoint, AbstractSecurityPage responsePage) {        
        super(id);

        this.extensionPoint=extensionPoint;

        model = new CompoundPropertyModel<SecurityNamedConfigModelHelper>(helper);
        form = new Form<SecurityNamedConfigModelHelper>("namedConfig",model); 
        add(form);        
        name = new TextField<String>("config.name");        
        name.setEnabled(helper.isNew());
        name.setRequired(true);    
        name.add(new NameValidator());
        form.add(name);
        
        List<String> classNames = getImplementations();
        if (helper.isNew() && classNames.size()>=1) {
            helper.getConfig().setClassName(classNames.get(0));                
        }
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
                comp.setOutputMarkupId(true);
                old.replaceWith(comp);                
                //comp.setMarkupId(old.getMarkupId());
                //form.addOrReplace(comp);
                target.addComponent(comp);                         
            }
        });

        implClass.setEnabled(helper.isNew && classNames.size()>1);
        
        if (helper.getConfig().getClassName() == null ||
            helper.getConfig().getClassName().length()==0) {    
            form.add(new NamedConfigDetailsEmptyPanel(DETAILS_WICKET_ID, model));            
        } else {
            form.add(getConfigDetailsPanel(helper.getConfig().getClassName()));
        }        
        form.get(DETAILS_WICKET_ID).setOutputMarkupId(true);
                
        implClass.setRequired(true);        
        implClass.setOutputMarkupId(true);
        implClass.setNullValid(false);
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
        
        GeoServerSecurityManager manager = GeoServerApplication.get().getSecurityManager();
        
        try {
            Set<String> result=new HashSet<String>();
            for (GeoServerSecurityProvider prov : list) {            
                Class<?> aClass = prov.getAuthenticationProviderClass();            
                if (aClass!=null) {
                    if (extensionPoint.isAssignableFrom(prov.getAuthenticationProviderClass()))
                    result.add(aClass.getName());
                    if (alreadyUsedNames==null)
                        alreadyUsedNames=manager.listAuthenticationProviders();
                }
                aClass = prov.getPasswordValidatorClass();            
                if (aClass!=null) {
                    if (extensionPoint.isAssignableFrom(prov.getPasswordValidatorClass()))
                    result.add(aClass.getName());
                    if (alreadyUsedNames==null)
                        alreadyUsedNames=manager.listPasswordValidators();
                }
                aClass = prov.getRoleServiceClass();            
                if (aClass!=null) {
                    if (extensionPoint.isAssignableFrom(prov.getRoleServiceClass()))
                    result.add(aClass.getName());
                    if (alreadyUsedNames==null)
                        alreadyUsedNames=manager.listRoleServices();
                }
                
                aClass = prov.getUserGroupServiceClass();            
                if (aClass!=null) {
                    if (extensionPoint.isAssignableFrom(prov.getUserGroupServiceClass()))
                    result.add(aClass.getName());
                    if (alreadyUsedNames==null)
                        alreadyUsedNames=manager.listUserGroupServices();
                }
                
                aClass = prov.getFilterClass();            
                if (aClass!=null) {
                    if (extensionPoint.isAssignableFrom(prov.getFilterClass()))
                    result.add(aClass.getName());
                    if (alreadyUsedNames==null)
                        alreadyUsedNames=manager.listFilters();
                }                                                
            }
            List<String> resList = new ArrayList<String>();
            resList.addAll(result);
            return resList;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    SubmitLink saveLink(final AbstractSecurityPage responsePage) {
        return new SubmitLink("save") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onSubmit() {
                setResponsePage(responsePage);
                if (model.getObject().hasChanges()) {
                    responsePage.setDirty(true);
                    try {
                        saveConfiguration();
                    } catch (IOException e) {
                        form.error(e.getMessage());
                        LOGGER.log(Level.SEVERE,e.getMessage(),e);
                        //error(new ParamResourceModel("saveError", getPage(), e.getMessage()));
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
            if (panel!=null)
                return panel;
        }
        throw new RuntimeException("No details panel for "+className);
    }

    protected void saveConfiguration() throws IOException {
        
        GeoServerSecurityManager manager = GeoServerApplication.get().getSecurityManager();
        
        if (GeoServerAuthenticationProvider.class.isAssignableFrom(extensionPoint)) {
            manager.saveAuthenticationProvider((SecurityAuthProviderConfig) model.getObject().getConfig());
        }
        if (GeoserverUserGroupService.class.isAssignableFrom(extensionPoint)) {
            manager.saveUserGroupService((SecurityUserGoupServiceConfig)model.getObject().getConfig());
        }
        if (GeoserverRoleService.class.isAssignableFrom(extensionPoint)) {
            manager.saveRoleService((SecurityRoleServiceConfig) model.getObject().getConfig());
        }
        if (PasswordValidator.class.isAssignableFrom(extensionPoint)) {
            manager.savePasswordPolicy((PasswordPolicyConfig) model.getObject().getConfig());
        }
        if (GeoServerSecurityFilter.class.isAssignableFrom(extensionPoint)) {
            manager.saveFilter(model.getObject().getConfig());
        }
    }

    
}
