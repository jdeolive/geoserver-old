/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.security.web.service;

import java.util.ArrayList;
import java.util.logging.Level;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.validation.AbstractFormValidator;
import org.apache.wicket.model.Model;
import org.geoserver.security.impl.ServiceAccessRule;
import org.geoserver.security.impl.ServiceAccessRuleDAO;
import org.geoserver.web.wicket.ParamResourceModel;

/**
 * Adds a new rule to the data access set
 */
@SuppressWarnings("serial")
public class NewServiceAccessRulePage extends AbstractServiceAccessRulePage {

	public NewServiceAccessRulePage() {
		super(new ServiceAccessRule());
	        service.add(new AjaxFormComponentUpdatingBehavior("onchange") {            
	            @Override
	            protected void onUpdate(AjaxRequestTarget target) {
	                method.setChoices(new Model<ArrayList<String>>(getMethod(service.getConvertedInput())));
	                method.modelChanged();
	                target.addComponent(method);
	            }
	        });
	        method.setOutputMarkupId(true);		
		form.add(new DuplicateRuleValidator());
	}

	@Override
	protected void onFormSubmit() {
		try {		        
			ServiceAccessRule rule = new ServiceAccessRule( 
			                model.getService(),
					model.getMethod(),
					rolesFormComponent.getRolesNamesForStoring());
			ServiceAccessRuleDAO dao = ServiceAccessRuleDAO.get();
			dao.addRule(rule);
			dao.storeRules();
			setResponsePage(ServiceAccessRulePage.class);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error occurred while saving service rule", e);
			error(new ParamResourceModel("saveError", getPage(), e.getMessage()));
		}
	}

//	private String parseRole(String modelObjectAsString) {
//		return modelObjectAsString.substring(1,
//				modelObjectAsString.length() - 1);
//	}

	/**
	 * Checks the same rule has not been entered before
	 * 
	 * @author aaime
	 * 
	 */
	class DuplicateRuleValidator extends AbstractFormValidator {
		public void validate(Form<?> form) {
		        if (form.findSubmittingButton() != saveLink) { // only validate on final submit
	                    return;
	                }
		    
		        updateModels();
			ServiceAccessRule rule = new ServiceAccessRule( 
			                model.getService(),
                                        model.getMethod(),
					rolesFormComponent.getRolesNamesForStoring());
			if (ServiceAccessRuleDAO.get().getRules().contains(rule)) {
				form.error(new ParamResourceModel("duplicateRule", getPage(),
						rule.getKey()).getString());
			}
		}

		public FormComponent<?>[] getDependentFormComponents() {
			return new FormComponent[] { service, method, rolesFormComponent };
		}
	}
	

}
