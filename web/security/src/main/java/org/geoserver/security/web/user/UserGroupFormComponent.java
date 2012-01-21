/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.security.web.user;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
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
import org.geoserver.security.impl.GeoServerUser;
import org.geoserver.security.impl.GeoServerUserGroup;
import org.geoserver.security.web.AbstractSecurityPage;
import org.geoserver.security.web.group.NewGroupPage;
import org.geoserver.web.GeoServerApplication;

/**
 * A form component that can be used to edit user to group assignments
 */
public class UserGroupFormComponent extends FormComponentPanel<Serializable> {
    private static final long serialVersionUID = 1L;


    Palette<GeoServerUserGroup> groupPalette;
    GeoServerUser user;
    Form<?> form;
    List<GeoServerUserGroup> selectedGroups;
    String userGroupServiceName;

    public List<GeoServerUserGroup> getSelectedGroups() {
        return selectedGroups;
    }

    public UserGroupFormComponent(String userGroupServiceName,GeoServerUser user, final Form<?> form ) {
        this(userGroupServiceName,user,form,null);
    }
    public UserGroupFormComponent(final String userGroupServiceName,GeoServerUser user, final Form<?> form, final IBehavior behavior ) {        
        super("groups");
        this.userGroupServiceName=userGroupServiceName;
        this.user=user;
        this.form=form;
                                                        
        try {
            selectedGroups=new ArrayList<GeoServerUserGroup>();
            selectedGroups.addAll(getSecurityManager().
                    loadUserGroupService(userGroupServiceName).getGroupsForUser(user));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        PropertyModel<List<GeoServerUserGroup>> model = new 
                PropertyModel<List<GeoServerUserGroup>> (this,"selectedGroups");

        
        
        LoadableDetachableModel<SortedSet<GeoServerUserGroup>> choicesModel = new 
                LoadableDetachableModel<SortedSet<GeoServerUserGroup>> () {
                    private static final long serialVersionUID = 1L;

                    @Override
                    protected SortedSet<GeoServerUserGroup> load() {                        
                        try {
                            SortedSet<GeoServerUserGroup> result=new TreeSet<GeoServerUserGroup>();
                            result.addAll(
                                getSecurityManager().loadUserGroupService(userGroupServiceName).getUserGroups());
                            
                            return result;
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }                                
                    }            
        };

        if (behavior==null) {
            groupPalette = new Palette<GeoServerUserGroup>(
                "groups", model,choicesModel,
                new ChoiceRenderer<GeoServerUserGroup>("groupname","groupname"), 10, false);
        } else {
            groupPalette = new Palette<GeoServerUserGroup>(
                    "groups", model,choicesModel,
                    new ChoiceRenderer<GeoServerUserGroup>("groupname","groupname"), 10, false) {
                        private static final long serialVersionUID = 1L;

                        @Override
                        protected Recorder<GeoServerUserGroup> newRecorderComponent() {                            
                            Recorder<GeoServerUserGroup> r= super.newRecorderComponent();
                            r.add(behavior);
                            return r;
                        }                                        
            };            
        }
         
                        
            
        groupPalette.setOutputMarkupId(true);
        add(groupPalette);
        
        SubmitLink addGroup = 
          new SubmitLink("addGroup",form) {
            private static final long serialVersionUID = 1L;
            @Override
            public void onSubmit() {
                setResponsePage(new NewGroupPage(userGroupServiceName,
                        (AbstractSecurityPage)this.getPage()));
            }            
          };
        add(addGroup);
                
    }

    GeoServerSecurityManager getSecurityManager() {
        return GeoServerApplication.get().getSecurityManager();
    }

    protected void calculateAddedRemovedCollections(Collection<GeoServerUserGroup> added, Collection<GeoServerUserGroup> removed) {
        SortedSet<GeoServerUserGroup> oldgroups;
        try {
            oldgroups = getSecurityManager().loadUserGroupService(userGroupServiceName).getGroupsForUser(user);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Iterator<GeoServerUserGroup> it = groupPalette.getSelectedChoices();
        
        removed.addAll(oldgroups);
        while (it.hasNext()) {
            GeoServerUserGroup group = it.next();
            if (oldgroups.contains(group)==false)
                added.add(group);
            else
                removed.remove(group);
        }
    }

    @Override
    public void updateModel() {
        groupPalette.getRecorderComponent().updateModel();
    }
    
    public Palette<GeoServerUserGroup> getGroupPalette() {
        return groupPalette;
    }
}
