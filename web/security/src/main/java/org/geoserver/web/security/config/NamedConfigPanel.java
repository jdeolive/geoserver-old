/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.web.security.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.Filter;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.ValidationError;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.security.GeoServerSecurityManager;
import org.geoserver.security.GeoServerSecurityProvider;
import org.geoserver.web.GeoServerApplication;

/**
 * A form component that can be used to edit user to group assignments
 */
public class NamedConfigPanel extends Panel {
    private static final long serialVersionUID = 1L;
    protected TextField<String> name;
    protected DropDownChoice<String> className;
    Form<SecurityConfigModelHelper> form;
    protected Class<?> extensionPoint;
    protected Set<String> alreadyUsedNames=null;
    
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
    

    public NamedConfigPanel(String id,SecurityConfigModelHelper helper, Class<?> extensionPoint) {        
        super(id);

        this.extensionPoint=extensionPoint;

        CompoundPropertyModel<SecurityConfigModelHelper> model = new CompoundPropertyModel<SecurityConfigModelHelper>(helper);
        form = new Form<SecurityConfigModelHelper>("namedConfig",model); 
        add(form);
        
        name = new TextField<String>("config.name");        
        name.setEnabled(helper.isNew());
        name.setRequired(true);    
        name.add(new NameValidator());
        form.add(name);
        
        
        className = new DropDownChoice<String>("config.className",                  
            //new PropertyModel<String>(model, "config.className"),
            getImplementations()
        );
        className.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                // TODO
            }
        });
                
        className.setEnabled(helper.isNew());
        className.setRequired(true);
        className.setOutputMarkupId(true);
        form.add(className);                
    }
                        
    /**
     * Returns a list of implementations for 
     * {@link #extensionPoint} and initializes {@link #providerHelper} 
     * 
     * @return
     */
    protected List<String> getImplementations() {
        
        List<GeoServerSecurityProvider> list = new ArrayList<GeoServerSecurityProvider>( 
                GeoServerExtensions.extensions(GeoServerSecurityProvider.class));
        
        GeoServerSecurityManager manager = GeoServerApplication.get().getSecurityManager();
        
        try {
            List<String> result=new ArrayList<String>();
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
                    if (Filter.class.isAssignableFrom(prov.getFilterClass()))
                    result.add(aClass.getName());
                    if (alreadyUsedNames==null)
                        alreadyUsedNames=manager.listFilters();
                }                                                
            }
            return result;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
        
}
