/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.security.web.role;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.geoserver.security.impl.GeoServerRole;
import org.geoserver.security.impl.RoleHierarchyHelper;
import org.geoserver.security.validation.AbstractSecurityException;
import org.geoserver.security.web.AbstractSecurityPage;
import org.geoserver.web.wicket.ParamResourceModel;
import org.geoserver.web.wicket.property.PropertyEditorFormComponent;

/**
 * Allows creation of a new user in users.properties
 */
@SuppressWarnings("serial")
public abstract class AbstractRolePage extends AbstractSecurityPage {
    TextField<String> rolenameField;
    PropertyEditorFormComponent roleParamEditor;
    DropDownChoice<String> parentRoles;
    SubmitLink saveLink;
    RoleUIModel uiRole;
    Form<Serializable> form;
    String roleServiceName;

    protected AbstractRolePage(String roleService,RoleUIModel uiRole,Properties properties,AbstractSecurityPage responsePage ) {

        this.roleServiceName=roleService;

        this.uiRole=uiRole;
        prepareForHierarchy(uiRole);
                  
        form = new Form<Serializable>("roleForm");        
        add(form);
        

        Label descriptionLabel=new Label("roledescription");        
        if (uiRole.username==null || uiRole.username.length()==0)
            descriptionLabel.setDefaultModel(
                    new StringResourceModel(AbstractRolePage.class.getSimpleName()+".anonymousRole",null));
        else
            descriptionLabel.setDefaultModel(
                    new StringResourceModel(AbstractRolePage.class.getSimpleName()+".personalizedRole",null,
                            new Object[] {uiRole.username}  ));
        form.add(descriptionLabel);
        
        // populate the form editing components
//        rolenameField = new TextField<String>("rolename") {
//                public boolean isRequired() {
//                    Form<?> form = getForm();
//                    return form.getRootForm().findSubmittingButton() == saveLink;
//                }
//        };
        
        rolenameField = new TextField<String>("rolename");
        boolean hasRoleStore = hasRoleStore(roleServiceName);
        rolenameField.setDefaultModel(new PropertyModel<RoleUIModel>(uiRole, "rolename"));
        rolenameField.setEnabled(hasRoleStore);
        form.add(rolenameField);
        
        
        parentRoles=new DropDownChoice<String>(
                "parentRoles", new PropertyModel<String>(uiRole, "parentrolename"), uiRole.getPossibleParentRoleNames());
        parentRoles.setEnabled(hasRoleStore);
        form.add(parentRoles);
        
        
        roleParamEditor = new PropertyEditorFormComponent("roleparameditor",properties);
        roleParamEditor.setEnabled(hasRoleStore);
        form.add(roleParamEditor);
                        
        // build the submit/cancel        
        form.add(getCancelLink(responsePage));
        form.add(saveLink=saveLink(responsePage));
        saveLink.setVisibilityAllowed(hasRoleStore);
                
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
                    LOGGER.log(Level.SEVERE, "Error occurred while saving role", e);
                }

            }
        };
    }
    
    /**
     * Prepare the model for hierarchy handling
     * 
     * @param uimodel
     */
    protected void prepareForHierarchy(RoleUIModel uimodel)  {
        uimodel.setPossibleParentRoleNames(new ArrayList<String>());
        uimodel.getPossibleParentRoleNames().add(""); // no parent        
        try {
            Map<String,String> parentMappings = getRoleService(roleServiceName).getParentMappings();  
            
            if (uimodel.getRolename() !=null && uimodel.getRolename().length()>0) { // rolename given
                RoleHierarchyHelper helper = new RoleHierarchyHelper(parentMappings);
                Set<String> invalidParents = new HashSet<String>();
                invalidParents.addAll(helper.getDescendants(uimodel.getRolename()));
                invalidParents.add(uimodel.getRolename()); 
                for (String existingRole :parentMappings.keySet()) {
                    if (invalidParents.contains(existingRole)==false) 
                        uimodel.getPossibleParentRoleNames().add(existingRole);
                }    
                uimodel.setParentrolename(parentMappings.get(uimodel.getRolename()));
                if (uimodel.getParentrolename()==null) 
                    uimodel.setParentrolename("");
            } else {  // no rolename given, we are creating a new one
                uimodel.getPossibleParentRoleNames().addAll(parentMappings.keySet());
                uimodel.setParentrolename("");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    
    /**
     * Implements the actual save action
     */
    protected abstract void onFormSubmit() throws IOException;
    
    /**
     * Mediates between the UI and the {@link GeoServerRole}  class
     */
    static class RoleUIModel implements Serializable {
        private String rolename;
        private String parentrolename;
        private String username;
        private List<String> possibleParentRoleNames;
        
        public String getUsername() {
            return username;
        }


        public void setUsername(String username) {
            this.username = username;
        }
       
        public RoleUIModel(String rolename,String parentrolename,String username) {
            this.rolename=rolename;
            this.parentrolename=parentrolename;
            this.username=username;
        }

        
        public String getParentrolename() {
            return parentrolename;
        }


        public void setParentrolename(String parentrolename) {
            this.parentrolename = parentrolename;
        }
        
        public String getRolename() {
            return rolename;
        }

        public void setRolename(String rolename) {
            this.rolename = rolename;
        }

        public List<String> getPossibleParentRoleNames() {
            return possibleParentRoleNames;
        }


        public void setPossibleParentRoleNames(List<String> possibleParentRoleNames) {
            this.possibleParentRoleNames = possibleParentRoleNames;
        }


    }

    

}
