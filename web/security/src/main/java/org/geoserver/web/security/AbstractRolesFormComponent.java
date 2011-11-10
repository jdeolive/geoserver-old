/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.web.security;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.extensions.markup.html.form.palette.Palette;
import org.apache.wicket.extensions.markup.html.form.palette.component.Recorder;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.geoserver.security.GeoServerSecurityManager;
import org.geoserver.security.impl.GeoserverRole;
import org.geoserver.web.GeoServerApplication;
import org.geoserver.web.security.role.NewRolePage;

/**
 * A form component that can be used to edit user/rule role lists
 */
@SuppressWarnings("serial")
public abstract class AbstractRolesFormComponent<T> extends FormComponentPanel<Serializable> {


    protected Palette<GeoserverRole> rolePalette;
    protected T rootObject;
    protected List <GeoserverRole> selectedRoles; 
    protected Form<?> form;
    protected String roleServiceName;
    
    public AbstractRolesFormComponent(String id, T rootObject,   final boolean isRequired, Form<?> form) {
        this(id,rootObject,isRequired,form,null);
    }
    
    public AbstractRolesFormComponent(String id, T rootObject,   final boolean isRequired, Form<?> form,final IBehavior behavior) {
        
        super(id);
        this.roleServiceName=getSecurityManager().getActiveRoleService().getName();
        this.rootObject=rootObject;
        this.form=form;
                
        selectedRoles=getStoredGrantedAuthorities(rootObject);        
        PropertyModel<List<GeoserverRole>> model = new 
                PropertyModel<List<GeoserverRole>> (this,"selectedRoles");
                
        
        LoadableDetachableModel<SortedSet<GeoserverRole>> choicesModel = new 
                LoadableDetachableModel<SortedSet<GeoserverRole>> () {
                    @Override
                    protected SortedSet<GeoserverRole> load() {                        
                        try {
                            SortedSet<GeoserverRole> result = new TreeSet<GeoserverRole>();
                            result.addAll(getSecurityManager().getActiveRoleService().getRoles());
                            return result;
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }                                
                    }            
        };


     
        
        rolePalette = new Palette<GeoserverRole>(
                "roles", model,choicesModel,
                new ChoiceRenderer<GeoserverRole>("authority","authority"), 10, false) {
            // trick to force the palette to have at least one selected elements
            // tried with a nicer validator but it's not used at all, the required thing
            // instead is working (don't know why...)
            protected Recorder<GeoserverRole> newRecorderComponent() {
                Recorder<GeoserverRole> rec = super.newRecorderComponent();                
                if (isRequired)
                    rec.setRequired(true);                
                if (behavior!=null) 
                    rec.add(behavior);
                return rec;
            }            
        };
       
                                    
        rolePalette.setOutputMarkupId(true);
        add(rolePalette);

        SubmitLink addRole =
          new SubmitLink("addRole",form) {
          @Override
          public void onSubmit() {              
              setResponsePage(new NewRolePage(roleServiceName,this.getPage()));
          }            
        }; 
        add(addRole);        
        addRole.setVisible(getSecurityManager().getActiveRoleService().canCreateStore());
    }

    public GeoServerSecurityManager getSecurityManager() {
        return GeoServerApplication.get().getSecurityManager();
    }

    public Palette<GeoserverRole> getRolePalette() {
	return rolePalette;
    }
    
    abstract protected List<GeoserverRole> getStoredGrantedAuthorities (T rootObject);
    
    public void calculateAddedRemovedCollections(Collection<GeoserverRole> added, Collection<GeoserverRole> removed) {
        List<GeoserverRole> oldroles;
        oldroles = getStoredGrantedAuthorities(rootObject);
        Iterator<GeoserverRole> it = rolePalette.getSelectedChoices();
        
        removed.addAll(oldroles);
        while (it.hasNext()) {
            GeoserverRole role = it.next();
            if (oldroles.contains(role)==false)
                added.add(role);
            else
                removed.remove(role);
        }
    }
    
    public List<GeoserverRole> getSelectedRoles() {
        return selectedRoles;
    }

    public void setSelectedRoles(List<GeoserverRole> selectedRoles) {
        this.selectedRoles = selectedRoles;
    }

    @Override
    public void updateModel()
    {
        rolePalette.getRecorderComponent().updateModel();
    }
        
}
