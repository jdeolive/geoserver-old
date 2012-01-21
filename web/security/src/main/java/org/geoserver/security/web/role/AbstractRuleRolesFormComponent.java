/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.security.web.role;


import java.util.HashSet;
import java.util.Set;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.geoserver.security.impl.GeoServerRole;

/**
 * A form component that can be used to edit user/rule role lists
 */
@SuppressWarnings("serial")
public abstract class AbstractRuleRolesFormComponent<T> extends AbstractRolesFormComponent<T> {

    protected AjaxCheckBox hasAnyBox;
    protected boolean hasAny;

    public AbstractRuleRolesFormComponent(String id, T rootObject,  final boolean isRequired, Form<?> form) {
        super(id, rootObject, isRequired, form, null);
    }


    public AbstractRuleRolesFormComponent(String id, T rootObject,  final boolean isRequired, Form<?> form,IBehavior behavior) {
        
        super(id,rootObject,isRequired,form,behavior);
                
        hasAnyBox=new AjaxCheckBox("hasany", new PropertyModel<Boolean>(this, "hasAny")) {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                if (hasAny) {
                    rolePalette.setEnabled(false);                    
                }
                else {
                    rolePalette.setEnabled(true);                    
                }
                target.addComponent(rolePalette);
                
                 
            }
        };                
        add(hasAnyBox);
        if (hasStoredAnyRole(rootObject)) {
            rolePalette.setEnabled(false);
            rolePalette.add(new AttributeAppender("disabled", true, new Model<String>("disabled"), " "));
            hasAnyBox.setDefaultModelObject(Boolean.TRUE);
        }
        else {
            rolePalette.setEnabled(true);
            rolePalette.add(new AttributeAppender("enabled", true, new Model<String>("enabled"), " "));
            hasAnyBox.setDefaultModelObject(Boolean.FALSE);
        }    

    }
    
    public abstract boolean hasStoredAnyRole(T rootObject); 
    
    public boolean hasAnyRole() {
        return (Boolean) hasAnyBox.getDefaultModelObject();
    }
    
    public  Set<GeoServerRole> getRolesForStoring() {
        Set<GeoServerRole> result = new HashSet<GeoServerRole>();
        if (hasAnyRole())
            result.add(GeoServerRole.HASANY_ROLE);
        else 
            result.addAll(rolePalette.getModelCollection());
        return result;
    }
        
    public Set<String> getRolesNamesForStoring() {
        Set<String> result = new HashSet<String>();
        for (GeoServerRole role : getRolesForStoring())
            result.add(role.getAuthority());
        return result;
    }
    
    
    public boolean isHasAny() {
        return hasAny;
    }

    public void setHasAny(boolean hasAny) {
        this.hasAny = hasAny;
    }

    @Override
    public void updateModel() {
        super.updateModel();
        hasAnyBox.updateModel();
    }

}
