/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.web.security.group;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.logging.Level;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.validation.AbstractFormValidator;
import org.apache.wicket.model.ResourceModel;
import org.geoserver.security.GeoserverRoleStore;
import org.geoserver.security.GeoserverUserGroupStore;
import org.geoserver.security.impl.GeoserverRole;
import org.geoserver.security.impl.GeoserverUserGroup;
import org.geoserver.web.wicket.ParamResourceModel;

public class NewGroupPage extends AbstractGroupPage {

    public NewGroupPage() {
        this(null);
    }   
    
    public NewGroupPage(Page responsePage) {
        super(new GroupUIModel("", true),responsePage);
        //groupnameField.add(new GroupConflictValidator());
        form.add(new GroupConflictValidator());
        if (hasUserGroupStore()==false) {
            throw new RuntimeException("Workflow error, new role not possible for read only service");
        }
        
    }
        
    
    /**
     * Checks the group is not a new one
     */
    class GroupConflictValidator extends AbstractFormValidator {

        private static final long serialVersionUID = 1L;


        @Override
        public FormComponent<?>[] getDependentFormComponents() {
            return new FormComponent[] { groupnameField };
        }

        @Override
        public void validate(Form<?> form) {
            if (form.findSubmittingButton() != saveLink) { // only validate on final submit
                return;
            }

            groupnameField.updateModel();
            String newName = uiGroup.getGroupname();            
            try {
                GeoserverUserGroup group =
                    getSecurityManager().getActiveUserGroupService().getGroupByGroupname(newName);
                if (group!=null) {
                    form.error(new ResourceModel("NewGroupPage.groupConflict").getObject(),
                            Collections.singletonMap("group", (Object) newName));
                    
                }
            } catch(IOException e) {
                throw new RuntimeException(e);
            }
            
        }
        
    }

    
    @Override
    protected void onFormSubmit() {
        
        try {
            GeoserverUserGroupStore store = getUserGroupStore();
            GeoserverUserGroup group = store.createGroupObject(
                    uiGroup.getGroupname(),uiGroup.isEnabled());
            store.addGroup(group);
            store.store();
            
            if (hasRoleStore()) {
                GeoserverRoleStore gaStore = getRoleStore();
                Iterator<GeoserverRole> roleIt =groupRolesFormComponent.
                    getRolePalette().getSelectedChoices();
                while (roleIt.hasNext()) {
                    gaStore.associateRoleToGroup(roleIt.next(), group.getGroupname());
                }
                gaStore.store();
            }
                            
            setActualResponsePage(GroupPage.class);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error occurred while saving group", e);
            error(new ParamResourceModel("saveError", getPage(), e.getMessage()));
        }
    }

}
