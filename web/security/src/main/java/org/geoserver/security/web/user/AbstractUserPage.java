/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.security.web.user;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.validation.EqualInputValidator;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.geoserver.security.GeoServerRoleService;
import org.geoserver.security.GeoServerUserGroupService;
import org.geoserver.security.impl.GeoServerRole;
import org.geoserver.security.impl.GeoServerUser;
import org.geoserver.security.impl.GeoServerUserGroup;
import org.geoserver.security.impl.RoleCalculator;
import org.geoserver.security.validation.AbstractSecurityException;
import org.geoserver.security.web.AbstractSecurityPage;
import org.geoserver.security.web.role.EditRolePage;
import org.geoserver.security.web.role.RoleListProvider;
import org.geoserver.web.GeoServerApplication;
import org.geoserver.web.wicket.GeoServerDataProvider.Property;
import org.geoserver.web.wicket.ParamResourceModel;
import org.geoserver.web.wicket.SimpleAjaxLink;
import org.geoserver.web.wicket.property.PropertyEditorFormComponent;

/**
 * Allows creation of a new user in users.properties
 */
public abstract class AbstractUserPage extends AbstractSecurityPage {
    protected TextField<String> username;
    protected UserGroupFormComponent userGroupFormComponent;
    protected UserRolesFormComponent userRolesFormComponent;
    protected PropertyEditorFormComponent userpropertyeditor;
    protected ListView<GeoServerRole> calculatedRoles;
    protected UserUIModel uiUser;
    protected Form<Serializable> form;
    protected SubmitLink saveLink;
    protected WebMarkupContainer calculatedrolesContainer;
    protected String userGroupServiceName;

    protected AbstractUserPage(String userGroupServiceName,UserUIModel uiUser,Properties properties, AbstractSecurityPage responsePage) {
        this.userGroupServiceName=userGroupServiceName;
        this.uiUser=uiUser;
        // build the form
        form = new Form<Serializable>("userForm");                
        add(form);
        
        // populate the form editing components
//        username = new TextField<String>("username",
//                new PropertyModel<String>(uiUser, "username")) {
//                    private static final long serialVersionUID = 1L;
//                    public boolean isRequired() {
//                        Form<?> form = getForm();
//                        return form.getRootForm().findSubmittingButton() == saveLink;
//                    }
//            };
            
        username = new TextField<String>("username",
                    new PropertyModel<String>(uiUser, "username")); 
            
        form.add(username);
        boolean hasUserGroupStore = hasUserGroupStore(userGroupServiceName);
        username.setEnabled(hasUserGroupStore);
        
        CheckBox enable = new CheckBox("enabled",
                new PropertyModel<Boolean>(uiUser, "enabled"));        
        form.add(enable);
        enable.setEnabled(hasUserGroupStore);

        PasswordTextField pw1 = new PasswordTextField("password",
                new PropertyModel<String>(uiUser, "password")) {
            private static final long serialVersionUID = 1L;
            public boolean isRequired() {
                Form<?> form = Form.findForm(this);
                if (form==null) return false;
                return form.getRootForm().findSubmittingButton() == saveLink;
            }
        };
        form.add(pw1);
        pw1.setResetPassword(false);        
        pw1.setEnabled(hasUserGroupStore);
        
        PasswordTextField pw2 = new PasswordTextField("confirmPassword",
                new PropertyModel<String>(uiUser, "confirmPassword")) {
            private static final long serialVersionUID = 1L;
            public boolean isRequired() {
                Form<?> form = Form.findForm(this);
                if (form==null) return false;
                return form.getRootForm().findSubmittingButton() == saveLink;
            }
        };
        form.add(pw2);
        pw2.setResetPassword(false);                
        pw2.setEnabled(hasUserGroupStore);
        
        
        LoadableDetachableModel<List<GeoServerRole>> caclulatedRolesModel = new 
                LoadableDetachableModel<List<GeoServerRole>> () {
                    private static final long serialVersionUID = 1L;                                       
                    @Override
                    protected List<GeoServerRole> load() {                        
                            return getCalculatedroles();
                    }            
        };


                
        calculatedRoles = new ListView<GeoServerRole>
        ("calculatedroles", caclulatedRolesModel) {
                private static final long serialVersionUID = 1L;
                protected void populateItem(ListItem<GeoServerRole> item) {
                    item.add(editRoleLink("calculatedrole", item.getModel(), RoleListProvider.ROLENAME));
                }
        };
        calculatedRoles.setOutputMarkupId(true);
        
        //form.add(calculatedRoles);
        calculatedrolesContainer=new WebMarkupContainer("calculatedrolesContainer");
        calculatedrolesContainer.setOutputMarkupId(true);
        calculatedrolesContainer.add(calculatedRoles);
        form.add(calculatedrolesContainer);

                        
        GeoServerUser tmpUser = uiUser.toGeoserverUser(userGroupServiceName);
        
        form.add(userpropertyeditor=new PropertyEditorFormComponent("userpropertyeditor",properties));        
        userpropertyeditor.setEnabled(hasUserGroupStore);
        form.add(userGroupFormComponent = new UserGroupFormComponent(userGroupServiceName,tmpUser,form,getCalculatedRolesBehavior()));
        userGroupFormComponent.setEnabled(hasUserGroupStore);
        form.add(userRolesFormComponent =new UserRolesFormComponent(tmpUser,form,getCalculatedRolesBehavior()));
        userRolesFormComponent.setEnabled(hasRoleStore(getSecurityManager().getActiveRoleService().getName()));
                                       
        // build the submit/cancel
        form.add(getCancelLink(responsePage));
        form.add(saveLink=saveLink(responsePage));
        saveLink.setVisible(hasUserGroupStore || hasRoleStore(getSecurityManager().getActiveRoleService().getName()));
        
                               
        // add the validators
        form.add(new EqualInputValidator(pw1, pw2) {
            private static final long serialVersionUID = 1L;
            @Override
            public void validate(Form<?> form) {
                if (form.findSubmittingButton() != saveLink) { // only validate on final submit
                    return;
                }
                super.validate(form);
            }
            @Override
            protected String resourceKey() {
                return "AbstractUserPage.passwordMismatch";
            }                        
        });
        
    }

    SubmitLink saveLink(final AbstractSecurityPage responsePage) {
        return new SubmitLink("save") {
            private static final long serialVersionUID = 1L;

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
                    LOGGER.log(Level.SEVERE, "Error occurred while saving user", e);
                }
            }
        };
    }
    
    
    Component editRoleLink(String id, IModel<GeoServerRole> itemModel, Property<GeoServerRole> property) {
        return new SimpleAjaxLink(id, itemModel, property.getModel(itemModel)) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onClick(AjaxRequestTarget target) {                
                setResponsePage(new EditRolePage(
                        getSecurityManager().getActiveRoleService().getName(),
                        (GeoServerRole) getDefaultModelObject(), 
                        (AbstractSecurityPage) this.getPage()));
            }
        };
    }

    protected IBehavior getCalculatedRolesBehavior() {
        return new AjaxFormComponentUpdatingBehavior("onchange") {            
               private static final long serialVersionUID = 1L;
               @Override
               protected void onUpdate(AjaxRequestTarget target) {
                   AbstractUserPage.this.calculatedRoles.modelChanged();
                   target.addComponent(AbstractUserPage.this.calculatedrolesContainer);
               }
        };
    }
    
    public List<GeoServerRole> getCalculatedroles() {
        List<GeoServerRole> tmpList = new ArrayList<GeoServerRole>();
        List<GeoServerRole> resultList = new ArrayList<GeoServerRole>();
        try {
            GeoServerUserGroupService ugService= getSecurityManager().loadUserGroupService(userGroupServiceName);
            GeoServerRoleService gaService = getSecurityManager().getActiveRoleService();
            RoleCalculator calc = new RoleCalculator(ugService, gaService);
            tmpList.addAll(userRolesFormComponent.getSelectedRoles());
            calc.addInheritedRoles(tmpList);
            for (GeoServerUserGroup group : userGroupFormComponent.getSelectedGroups()) {
                if (group.isEnabled())
                    tmpList.addAll(calc.calculateRoles(group));
            }
            resultList.addAll(calc.personalizeRoles(uiUser.toGeoserverUser(userGroupServiceName), tmpList));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Collections.sort(resultList);
        return resultList;
    }
    
    
    /**
     * Implements the actual save action
     */
    protected abstract void onFormSubmit() throws IOException;
    
    /**
     * Mediates between the UI and the Spring User class
     */
    static class UserUIModel implements Serializable {
        private static final long serialVersionUID = 1L;
        private String username;

        /**
         * Will be used to check if the user edited the pw in case the pw is encrypted with a one
         * way only (digest) encryption algorithm
         */
        private String originalPassword;

        private String password;

        private String confirmPassword;
        

        private boolean enabled;

        /**
         * Maps a {@link GeoServerUser} into something that maps 1-1 with the UI
         * 
         * @param user
         */
        public UserUIModel(GeoServerUser user) {
            this.username = user.getUsername();
            this.originalPassword = user.getPassword();
            this.password = this.originalPassword;
            this.confirmPassword = this.originalPassword;            
            this.enabled = user.isEnabled();
        }

        /**
         * Prepares for an emtpy new user
         */
        public UserUIModel() {
            this.username = "";
            this.originalPassword = "";
            this.password = "";
            this.confirmPassword = "";
            this.enabled = true;
        }

        /**
         * Converts this UI view back into an Spring {@link GeoServerUser} object
         * 
         * @return
         */
        public GeoServerUser toGeoserverUser(String userGroupServiceName) {
            GeoServerUser user;
            try {
                user = GeoServerApplication.get().getSecurityManager()
                    .loadUserGroupService(userGroupServiceName).createUserObject(username, password, enabled);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return user;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getOriginalPassword() {
            return originalPassword;
        }

        public void setOriginalPassword(String originalPassword) {
            this.originalPassword = originalPassword;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getConfirmPassword() {
            return confirmPassword;
        }

        public void setConfirmPassword(String confirmPassword) {
            this.confirmPassword = confirmPassword;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

    }
    
}
