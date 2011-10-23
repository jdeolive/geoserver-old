/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.web.security.user;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.validation.AbstractFormValidator;
import org.apache.wicket.model.ResourceModel;
import org.geoserver.security.GeoserverRoleStore;
import org.geoserver.security.GeoserverUserGroupStore;
import org.geoserver.security.impl.GeoserverRole;
import org.geoserver.security.impl.GeoserverUser;
import org.geoserver.security.impl.GeoserverUserGroup;
import org.geoserver.web.wicket.ParamResourceModel;


/**
 * Allows creation of a new user in users.properties
 */
public class NewUserPage extends AbstractUserPage {

    public NewUserPage() {
        this(null);
    }   
    public NewUserPage(Page responsePage) {
       super(new UserUIModel(),new Properties(),responsePage);       
       form.add(new UserConflictValidator());
       if (hasUserGroupStore()==false) {
           throw new RuntimeException("Workflow error, new role not possible for read only service");
       }

    }
    
    
    class UserConflictValidator extends AbstractFormValidator {

        private static final long serialVersionUID = 1L;


        @Override
        public FormComponent<?>[] getDependentFormComponents() {
            return new FormComponent[] { username };
        }

        @Override
        public void validate(Form<?> form) {
            if (form.findSubmittingButton() != saveLink) { // only validate on final submit
                return;
            }

            username.updateModel();
            String newName = uiUser.getUsername();            
            try {
                GeoserverUser user = 
                    getSecurityManager().getActiveUserGroupService().getUserByUsername(newName);
                if (user != null) {
                    form.error(new ResourceModel("NewUserPage.userConflict").getObject(),
                            Collections.singletonMap("user", (Object) newName));
                    
                }
            } catch(IOException e) {
                throw new RuntimeException(e);
            }
            
        }
        
    }

    
    
    @Override
    protected void onFormSubmit() {
        try {
             
            
            GeoserverUserGroupStore ugStore = getUserGroupStore();
            GeoserverUser user =uiUser.toGeoserverUser();             
            user.getProperties().clear();
            for (Entry<Object,Object> entry : userpropertyeditor.getProperties().entrySet())
                user.getProperties().put(entry.getKey(),entry.getValue());

            ugStore.addUser(user);
                    
            Iterator<GeoserverUserGroup> it =userGroupFormComponent.groupPalette.getSelectedChoices();
            while (it.hasNext()) {
                ugStore.associateUserToGroup(user, it.next());
            }
            
            if (hasRoleStore()) {
                GeoserverRoleStore gaStore = getRoleStore();
                Iterator<GeoserverRole> roleIt =userRolesFormComponent.
                        getRolePalette().getSelectedChoices();
                while (roleIt.hasNext()) {
                    gaStore.associateRoleToUser(roleIt.next(), user.getUsername());
                }
                gaStore.store();
            }
            
                                    
            ugStore.store();
            
            
            setActualResponsePage(UserPage.class);
        } catch(Exception e) {
            LOGGER.log(Level.SEVERE, "Error occurred while saving user", e);
            error(new ParamResourceModel("saveError", getPage(), e.getMessage()));
        }
    }
}
