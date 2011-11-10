/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.web.security.group;

import java.io.IOException;
import java.io.Serializable;

import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.PropertyModel;
import org.geoserver.security.impl.GeoserverUserGroup;
import org.geoserver.web.GeoServerApplication;
import org.geoserver.web.security.AbstractSecurityPage;

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

    protected AbstractGroupPage(String userGroupServiceName,GroupUIModel uiGroup,Page responsePage) {
        
        //final CompoundPropertyModel<GroupUIModel> groupModel = new CompoundPropertyModel<GroupUIModel>(uiGroup);
        
        // build the form
        //Form<CompoundPropertyModel<GroupUIModel>> form = 
                //new Form<CompoundPropertyModel<GroupUIModel>>("groupForm");
        //setDefaultModel(groupModel);        
        
        this.userGroupServiceName=userGroupServiceName;
        this.uiGroup=uiGroup;
        form =new Form<Serializable>("groupForm");                
        add(form);
                        
        // populate the form editing components
        groupnameField = new TextField<String>("groupname") {
            public boolean isRequired() {
                Form<?> form = getForm();
                return form.getRootForm().findSubmittingButton() == saveLink;
            }
        };        
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

    SubmitLink saveLink(final Page responsePage) {
        return new SubmitLink("save") {
            @Override
            public void onSubmit() {
                onFormSubmit();
                if (responsePage!=null) {
                    setResponsePage(responsePage);
                } else {
                    PageParameters params = new PageParameters();
                    params.put(ServiceNameKey, userGroupServiceName);
                    setResponsePage(GroupPage.class,params);
                }
            }
        };
    }
    
    /**
     * Mediates between the UI and the {@link GeoserverUserGroup}  class
     */
    static class GroupUIModel implements Serializable {
        private String groupname;

        private boolean enabled;


        public GroupUIModel(String groupname,boolean enabled) {
            this.groupname=groupname;
            this.enabled=enabled;
        }        

        public GeoserverUserGroup toGeoserverUserGroup(String userGroupServiceName) {
            GeoserverUserGroup group;
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
    protected abstract void onFormSubmit();

    

}
