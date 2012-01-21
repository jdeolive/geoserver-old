/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.security.web.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.form.validation.AbstractFormValidator;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.PropertyModel;
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.security.AccessMode;
import org.geoserver.security.impl.DataAccessRule;
import org.geoserver.security.web.AbstractSecurityPage;
import org.geoserver.web.wicket.ParamResourceModel;

/**
 * Abstract page binding a {@link DataAccessRule}
 */
@SuppressWarnings("serial")
public abstract class AbstractDataAccessRulePage extends AbstractSecurityPage {

    
    List<AccessMode> MODES = Arrays.asList(AccessMode.READ, AccessMode.WRITE);

    DropDownChoice<String> workspace;

    DropDownChoice<String> layer;

    DropDownChoice<AccessMode> accessMode;

    DataRolesFormComponent rolesFormComponent;
    DataAccessRule model;

    Form<Serializable> form;
    SubmitLink saveLink;

    public AbstractDataAccessRulePage(DataAccessRule rule) {
        model =new DataAccessRule(rule);

        // build the form
        form = new Form<Serializable>("ruleForm");
        add(form);
        form.add (new EmptyRolesValidator());
        form.add(workspace = new DropDownChoice<String>("workspace", getWorkspaceNames()));
        workspace.setDefaultModel(new PropertyModel<String>(model, "workspace"));
        
        setOutputMarkupId(true);
        
        form.add(layer = new DropDownChoice<String>("layer", getLayerNames(rule.getWorkspace())));
        layer.setDefaultModel(new PropertyModel<String>(model,"layer"));
        
        form.add(accessMode = new DropDownChoice<AccessMode>("accessMode", MODES, new AccessModeRenderer()));
        accessMode.setDefaultModel(new PropertyModel<AccessMode>(model,"accessMode"));
        
        form.add(rolesFormComponent = new DataRolesFormComponent(rule,form));

        // build the submit/cancel
        form.add(new BookmarkablePageLink<DataAccessRule>("cancel", DataSecurityPage.class));
        saveLink=saveLink();
        form.add(saveLink);

        // add the validators
        workspace.setRequired(true);
        layer.setRequired(true);
        accessMode.setRequired(true);
    }

    SubmitLink saveLink() {
        return new SubmitLink("save") {
            @Override
            public void onSubmit() {
                onFormSubmit();
            }
        };
    }

    /**
     * Implements the actual save action
     */
    protected abstract void onFormSubmit();

    /**
     * Returns a sorted list of workspace names
     */
    ArrayList<String> getWorkspaceNames() {
        ArrayList<String> result = new ArrayList<String>();
        for (WorkspaceInfo ws : getCatalog().getWorkspaces()) {
            result.add(ws.getName());
        }
        Collections.sort(result);
        result.add(0, "*");
        return result;
    }

    /**
     * Returns a sorted list of layer names in the specified workspace (or * if the workspace is *)
     */
    ArrayList<String> getLayerNames(String workspaceName) {
        ArrayList<String> result = new ArrayList<String>();
        if (!workspaceName.equals("*")) {
            for (ResourceInfo r : getCatalog().getResources(ResourceInfo.class)) {
                if (r.getStore().getWorkspace().getName().equals(workspaceName))
                    result.add(r.getName());
            }
            Collections.sort(result);
        }
        result.add(0, "*");
        return result;
    }

    /**
     * Makes sure we see translated text, by the raw name is used for the model
     */
    class AccessModeRenderer implements IChoiceRenderer<AccessMode> {

        public Object getDisplayValue(AccessMode object) {
            return (String) new ParamResourceModel( object.name(), getPage())
                    .getObject();
        }

        public String getIdValue(AccessMode object, int index) {
            return object.name();
        }

    }

    
    class EmptyRolesValidator extends AbstractFormValidator {

        @Override
        public FormComponent<?>[] getDependentFormComponents() {
           return new FormComponent[] { workspace, layer, accessMode, rolesFormComponent };

        }

        @Override
        public void validate(Form<?> form) {
            if (form.findSubmittingButton() != saveLink) { // only validate on final submit
                return;
            }
            updateModels();
            DataAccessRule rule = new DataAccessRule(getWorkspace(),
                    getLayer(),getAccessMode(),
                    rolesFormComponent.getRolesNamesForStoring());
                if (rule.getRoles().isEmpty()) {
                    form.error(new ParamResourceModel("emptyRoles", getPage(),
                            rule.getKey()).getString());
                }
        }
    }

    protected String getWorkspace() {
        return workspace.getDefaultModelObjectAsString();
    }
    
    protected String getLayer() {
        return layer.getDefaultModelObjectAsString();
    }

    protected AccessMode getAccessMode() {
        return accessMode.getModelObject();
    }

    protected void updateModels() {
        workspace.updateModel();
        layer.updateModel();
        accessMode.updateModel();
        rolesFormComponent.updateModel();
    }
}
