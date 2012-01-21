/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.security.web.service;

import java.util.logging.Level;

import org.geoserver.security.impl.ServiceAccessRule;
import org.geoserver.security.impl.ServiceAccessRuleDAO;
import org.geoserver.web.wicket.ParamResourceModel;

/**
 * Edits an existing rule
 */
public class EditServiceAccessRulePage extends AbstractServiceAccessRulePage {
    
    public String methodName,serviceName;
    
    public EditServiceAccessRulePage(ServiceAccessRule rule) {
        super(rule);
        // save the names, DropdownChoices have a strange behavior
        // if the are read only, the select model object is empty
        methodName = rule.getMethod();
        serviceName = rule.getService();
        method.setEnabled(false);        
        service.setEnabled(false);        
    }

    @Override
    protected String getMethodName() {
        return methodName;
    }
    @Override
    protected String getServiceName() {
        return serviceName;
    }

    
    @Override
    protected void onFormSubmit() {
        try {
            ServiceAccessRuleDAO dao = ServiceAccessRuleDAO.get();
                        
            ServiceAccessRule storedRule=null;
            for (ServiceAccessRule rule : dao.getRules()) {
                if (serviceName.equals(rule.getService()) && 
                        methodName.equals(rule.getMethod())) {
                    storedRule=rule;
                    break;
                }
            }
            storedRule.getRoles().clear();
            storedRule.getRoles().addAll(rolesFormComponent.getRolesNamesForStoring());
            dao.storeRules();
            setResponsePage(ServiceAccessRulePage.class);
        } catch(Exception e) {
            LOGGER.log(Level.SEVERE, "Error occurred while saving rule ", e);
            error(new ParamResourceModel("saveError", getPage(), e.getMessage()));
        }
    }
    

}
