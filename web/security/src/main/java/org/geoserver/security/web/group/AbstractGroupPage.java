/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.security.web.group;

import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Level;

import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.PropertyModel;
import org.geoserver.security.impl.GeoServerUserGroup;
import org.geoserver.security.validation.AbstractSecurityException;
import org.geoserver.security.web.AbstractSecurityPage;
import org.geoserver.web.GeoServerApplication;
import org.geoserver.web.wicket.ParamResourceModel;

/**
 * Allows creation of a new user in users.properties
 */
@SuppressWarnings("serial")
public abstract class AbstractGroupPage extends AbstractSecurityPage {
    TextField<String> groupnameField;
    protected GroupRolesFormComponent groupRolesFormComponent;
    protected GroupUIModel uiGroup;
    protected SubmitLink saveLink;
    protected Form<Serializable> form;
    protected String userGroupServiceName;

    protected AbstractGroupPage(String userGroupServiceName,GroupUIModel uiGroup,AbstractSecurityPage responsePage) {
        
        
        this.userGroupServiceName=userGroupServiceName;
        this.uiGroup=uiGroup;
        form =new Form<Serializable>("groupForm");                
        add(form);
                        
        // populate the form editing components
//        groupnameField = new TextField<String>("groupname") {
//            public boolean isRequired() {
//                Form<?> form = getForm();
//                return form.getRootForm().findSubmittingButton() == saveLink;
//            }
//        };
        
        groupnameField = new TextField<String>("groupname");
        
        boolean hasUserGroupStore = hasUserGroupStore(userGroupServiceName);
        groupnameField.setEnabled(hasUserGroupStore);
        groupnameField.setDefaultModel(new PropertyModel<GroupUIModel>(uiGroup, "groupname"));
        form.add(groupnameField);
                
        CheckBox enable = new CheckBox("enabled");
        enable.setDefaultModel(new PropertyModel<GroupUIModel>(uiGroup, "enabled"));
        enable.setEnabled(hasUserGroupStore);
        form.add(enable);

        
        form.add(groupRolesFormComponent =new GroupRolesFormComponent(
                uiGroup.toGeoserverUserGroup(userGroupServiceName),form));
        groupRolesFormComponent.setEnabled(hasRoleStore(getSecurityManager().getActiveRoleService().getName()));
        
        // build the submit/cancel
        form.add(getCancelLink(responsePage));
        saveLink = saveLink(responsePage);
        form.add(saveLink);
        saveLink.setVisible(hasUserGroupStore || hasRoleStore(getSecurityManager().getActiveRoleService().getName()));
        
        
    }

    SubmitLink saveLink(final AbstractSecurityPage responsePage) {
        return new SubmitLink("save") {
            @Override
            public void onSubmit() {
                try {
                    onFormSubmit();
                    responsePage.setDirty(true);
                    setResponsePage(responsePage);
                } catch (IOException e) {
                    if (e.getCause() instanceof AbstractSecurityException) {
                        error(e.getCause());
                    } else {
                        error(new ParamResourceModel("saveError", getPage(), e.getMessage()).getObject());
                    }
                    LOGGER.log(Level.SEVERE, "Error occurred while saving group", e);
                }

            }
        };
    }
    
    /**
     * Mediates between the UI and the {@link GeoServerUserGroup}  class
     */
    static class GroupUIModel implements Serializable {
        private String groupname;

        private boolean enabled;


        public GroupUIModel(String groupname,boolean enabled) {
            this.groupname=groupname;
            this.enabled=enabled;
        }        

        public GeoServerUserGroup toGeoserverUserGroup(String userGroupServiceName) {
            GeoServerUserGroup group;
            try {
                group = GeoServerApplication.get().getSecurityManager()
                    .loadUserGroupService(userGroupServiceName).
                    createGroupObject(groupname,enabled);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return group;
        }
        
        public String getGroupname() {
            return groupname;
        }

        public void setGroupname(String groupname) {
            this.groupname = groupname;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

    }

    
    /**
     * Implements the actual save action
     */
    protected abstract void onFormSubmit() throws IOException;

    

}
