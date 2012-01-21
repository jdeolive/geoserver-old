/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.security.web.data;

import java.util.logging.Level;

import org.geoserver.security.AccessMode;
import org.geoserver.security.impl.DataAccessRule;
import org.geoserver.security.impl.DataAccessRuleDAO;
import org.geoserver.web.wicket.ParamResourceModel;

/**
 * Edits an existing rule
 */
public class EditDataAccessRulePage extends AbstractDataAccessRulePage {

    String savedWorkspace,savedLayer;
    AccessMode savedAccessMode;
    
    public EditDataAccessRulePage(DataAccessRule rule) {
        super(rule);
        savedWorkspace=rule.getWorkspace();
        savedLayer=rule.getLayer();
        savedAccessMode=rule.getAccessMode();
        
        // disabled drop down choices have a strange behavior
        // for gettint the model object
        workspace.setEnabled(false);
        layer.setEnabled(false);
        accessMode.setEnabled(false);
    }

    @Override
    protected void onFormSubmit() {
        try {
            DataAccessRuleDAO dao = DataAccessRuleDAO.get();            
            
            DataAccessRule storedRule=null;
            for (DataAccessRule rule : dao.getRules()) {
                if (rule.getWorkspace().equals(savedWorkspace) &&
                    rule.getLayer().equals(savedLayer) &&    
                    rule.getAccessMode().equals(savedAccessMode)    
                        ) {
                    storedRule=rule;
                    break;
                }
            }
            storedRule.getRoles().clear();
            storedRule.getRoles().addAll(rolesFormComponent.getRolesNamesForStoring());
            dao.storeRules();
            
            setResponsePage(DataSecurityPage.class);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error occurred while saving rule ", e);
            error(new ParamResourceModel("saveError", getPage(), e.getMessage()));
        }
    }

    protected String getWorkspace() {
        return savedWorkspace;
    }
    
    protected String getLayer() {
        return savedLayer;
    }

    protected AccessMode getAccessMode() {
        return savedAccessMode;
    }

}
