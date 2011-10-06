/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.web.security.group;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import org.apache.wicket.Page;
import org.geoserver.security.GeoserverGrantedAuthorityStore;
import org.geoserver.security.GeoserverUserGroupStore;
import org.geoserver.security.impl.GeoserverGrantedAuthority;
import org.geoserver.security.impl.GeoserverUserGroup;
import org.geoserver.web.wicket.ParamResourceModel;

public class EditGroupPage extends AbstractGroupPage {

    public EditGroupPage(GeoserverUserGroup group) {
        this(group,null);
    }
    
    public EditGroupPage(GeoserverUserGroup group,Page responsePage) {
        super(new GroupUIModel(group.getGroupname(), group.isEnabled()),responsePage);
        groupnameField.setEnabled(false);
    }

    @Override
    protected void onFormSubmit() {
        
        
        //GroupUIModel uiGroup = (GroupUIModel) getDefaultModelObject();
        try {
            
            GeoserverUserGroup group = getUserGroupService().getGroupByGroupname(uiGroup.getGroupname());
            
            if (hasUserGroupStore()) {
                GeoserverUserGroupStore store = getUserGroupStore();            
                group.setEnabled(uiGroup.isEnabled());
                store.updateGroup(group);
                store.store();
            };   

            if (hasGrantedAuthorityStore()) {
                GeoserverGrantedAuthorityStore gaStore = getGrantedAuthorityStore();
                Set<GeoserverGrantedAuthority> addedRoles = new HashSet<GeoserverGrantedAuthority>();
                Set<GeoserverGrantedAuthority> removedRoles = new HashSet<GeoserverGrantedAuthority>();
                groupRolesFormComponent.calculateAddedRemovedCollections(addedRoles, removedRoles);
                for (GeoserverGrantedAuthority role : addedRoles)
                    gaStore.associateRoleToGroup(role, group.getGroupname());
                for (GeoserverGrantedAuthority role : removedRoles)
                    gaStore.disAssociateRoleFromGroup(role, group.getGroupname());
            
                gaStore.store();
            }            
            setActualResponsePage(GroupPage.class);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error occurred while saving group", e);
            error(new ParamResourceModel("saveError", getPage(), e.getMessage()));
        }
    }

}
