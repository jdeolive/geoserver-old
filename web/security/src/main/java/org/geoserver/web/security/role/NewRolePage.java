/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.web.security.role;

import java.io.IOException;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.validation.AbstractFormValidator;
import org.apache.wicket.model.ResourceModel;
import org.geoserver.security.GeoserverGrantedAuthorityStore;
import org.geoserver.security.impl.GeoserverGrantedAuthority;
import org.geoserver.web.wicket.ParamResourceModel;


/**
 * Page for adding a new {@link GeoserverGrantedAuthority} object
 * 
 * @author christian
 *
 */
public class NewRolePage extends AbstractRolePage {

    
    public NewRolePage(Page responsePage) {
        super(new RoleUIModel("", "",null),new Properties(),responsePage);        
        form.add(new RoleConflictValidator());
    }

    
    public NewRolePage() {
        this (null);
        if (hasGrantedAuthorityStore()==false) {
            throw new RuntimeException("Workflow error, new role not possible for read only service");
        }        
    }
        

    class RoleConflictValidator extends AbstractFormValidator {

        private static final long serialVersionUID = 1L;


        @Override
        public FormComponent<?>[] getDependentFormComponents() {
            return new FormComponent[] { rolenameField };
        }

        @Override
        public void validate(Form<?> form) {
            if (form.findSubmittingButton() != saveLink) { // only validate on final submit
                return;
            }

            rolenameField.updateModel();
            String newName = uiRole.getRolename();            
            try {
                GeoserverGrantedAuthority role =
                    getSecurityManager().getActiveRoleService().getGrantedAuthorityByName(newName);
                if (role!=null) {
                    form.error(new ResourceModel("NewRolePage.roleConflict").getObject(),
                            Collections.singletonMap("role", (Object) newName));
                    
                }
            } catch(IOException e) {
                throw new RuntimeException(e);
            }
            
        }
        
    }

    
    
    @Override
    protected void onFormSubmit() {
        
        
        try {
            GeoserverGrantedAuthorityStore store = getGrantedAuthorityStore();
            GeoserverGrantedAuthority role = store.createGrantedAuthorityObject(uiRole.getRolename());
            
            role.getProperties().clear();
            for (Entry<Object,Object> entry : roleParamEditor.getProperties().entrySet())
                role.getProperties().put(entry.getKey(),entry.getValue());

            store.addGrantedAuthority(role);
                    
            GeoserverGrantedAuthority parentRole = null;
            if (uiRole.getParentrolename()!=null && uiRole.getParentrolename().length() > 0) {
                parentRole=store.getGrantedAuthorityByName(uiRole.getParentrolename());
            }
            store.setParentRole(role,parentRole);
            store.store();
            
            setActualResponsePage(RolePage.class);
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error occurred while saving role", e);
            error(new ParamResourceModel("saveError", getPage(), e.getMessage()));
        }
    }

}
