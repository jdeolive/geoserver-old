/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.security.web.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.form.validation.AbstractFormValidator;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.PropertyModel;
import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.platform.Service;
import org.geoserver.security.impl.DataAccessRule;
import org.geoserver.security.impl.ServiceAccessRule;
import org.geoserver.security.web.AbstractSecurityPage;
import org.geoserver.web.wicket.ParamResourceModel;

/**
 * Abstract page binding a {@link DataAccessRule}
 */
@SuppressWarnings("serial")
public abstract class AbstractServiceAccessRulePage extends AbstractSecurityPage {

    DropDownChoice<String> service;

    DropDownChoice<String> method;

    ServiceRolesFormComponent rolesFormComponent;

    Form<Serializable> form;
    SubmitLink saveLink;
    ServiceAccessRule model;

    public AbstractServiceAccessRulePage(ServiceAccessRule rule) {
        
        //setDefaultModel(new CompoundPropertyModel<ServiceAccessRule>(new ServiceAccessRule(rule)));

        model = new ServiceAccessRule(rule);
        // build the form
        form = new Form<Serializable>("ruleForm");
        add(form);
        form.add(new EmptyRolesValidator());
        
        form.add(service = new DropDownChoice<String>("service", getServiceNames()));
        service.setDefaultModel(new PropertyModel<String>(model, "service"));
//        service.add(new AjaxFormComponentUpdatingBehavior("onchange") {            
//            @Override
//            protected void onUpdate(AjaxRequestTarget target) {
//                method.setChoices(new Model<ArrayList<String>>(getMethod(service.getConvertedInput())));
//                method.modelChanged();
//                target.addComponent(method);
//            }
//        });
        setOutputMarkupId(true);
        form.add(method = new DropDownChoice<String>("method", getMethod(model.getService())));
        method.setDefaultModel(new PropertyModel<String>(model, "method"));
        
        
        form.add(rolesFormComponent = new ServiceRolesFormComponent(model,form));

        // build the submit/cancel
        form.add(new BookmarkablePageLink<ServiceAccessRulePage>
                ("cancel", ServiceAccessRulePage.class));
        
        saveLink=saveLink();
        form.add(saveLink);

        // add the validators
        service.setRequired(true);
        method.setRequired(true);
    }

    SubmitLink saveLink() {
        return new SubmitLink("save") {
            @Override
            public void onSubmit() {
                onFormSubmit();
            }
        };
    }

    /**
     * Implements the actual save action
     */
    protected abstract void onFormSubmit();

    /**
     * Returns a sorted list of workspace names
     */
    ArrayList<String> getServiceNames() {
        ArrayList<String> result = new ArrayList<String>();
        for (Service ows : GeoServerExtensions.extensions(Service.class)) {
            if (!result.contains(ows.getId()))
                result.add(ows.getId());
        }
        Collections.sort(result);
        result.add(0, "*");

        return result;
    }

    /**
     * Returns a sorted list of layer names in the specified workspace (or * if the workspace is *)
     */
    ArrayList<String> getMethod(String service) {
        ArrayList<String> result = new ArrayList<String>();
        boolean flag = true;
        for (Service ows : GeoServerExtensions.extensions(Service.class)) {
            if (service.equals(ows.getId()) && !result.contains(ows.getOperations()) && flag) {
                flag = false;
                result.addAll(ows.getOperations());
            }
        }
        Collections.sort(result);
        result.add(0, "*");
        return result;
    }
    
    class EmptyRolesValidator extends AbstractFormValidator {

        @Override
        public FormComponent<?>[] getDependentFormComponents() {
           return new FormComponent[] { service, method, rolesFormComponent };

        }

        @Override
        public void validate(Form<?> form) {
            
        if (form.findSubmittingButton() != saveLink) { // only validate on final submit
                return;
            }       

       updateModels(); 
       ServiceAccessRule rule = new ServiceAccessRule(                      
              getServiceName(),getMethodName(),                    
              rolesFormComponent.getRolesNamesForStoring());
                        
        if (rule.getRoles().isEmpty()) {
            form.error(new ParamResourceModel("emptyRoles", getPage(),
                  rule.getKey()).getString());
            }
        }
    }
    
    protected void updateModels() {
        service.updateModel();
        method.updateModel();
        rolesFormComponent.updateModel();
    }
    protected String getMethodName() {
        return method.getDefaultModelObjectAsString();
    }
    protected String getServiceName() {
        return service.getDefaultModelObjectAsString();
    }

}
