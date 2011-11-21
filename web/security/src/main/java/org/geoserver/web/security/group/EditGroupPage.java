/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.web.security.group;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import org.geoserver.security.GeoserverRoleStore;
import org.geoserver.security.GeoserverUserGroupStore;
import org.geoserver.security.impl.GeoserverRole;
import org.geoserver.security.impl.GeoserverUserGroup;
import org.geoserver.web.security.AbstractSecurityPage;
import org.geoserver.web.wicket.ParamResourceModel;

public class EditGroupPage extends AbstractGroupPage {

    
    public EditGroupPage(String userGroupServiceName,GeoserverUserGroup group,AbstractSecurityPage responsePage) {
        super(userGroupServiceName,new GroupUIModel(group.getGroupname(), group.isEnabled()),responsePage);
        groupnameField.setEnabled(false);
    }

    @Override
    protected void onFormSubmit() {
        
        
        //GroupUIModel uiGroup = (GroupUIModel) getDefaultModelObject();
        try {
            
            GeoserverUserGroup group = getUserGroupService(userGroupServiceName).getGroupByGroupname(uiGroup.getGroupname());
            
            if (hasUserGroupStore(userGroupServiceName)) {
                GeoserverUserGroupStore store = getUserGroupStore(userGroupServiceName);            
                group.setEnabled(uiGroup.isEnabled());
                store.updateGroup(group);
                store.store();
            };   

            if (hasRoleStore(getSecurityManager().getActiveRoleService().getName())) {
                GeoserverRoleStore gaStore = getRoleStore(getSecurityManager().getActiveRoleService().getName());
                Set<GeoserverRole> addedRoles = new HashSet<GeoserverRole>();
                Set<GeoserverRole> removedRoles = new HashSet<GeoserverRole>();
                groupRolesFormComponent.calculateAddedRemovedCollections(addedRoles, removedRoles);
                for (GeoserverRole role : addedRoles)
                    gaStore.associateRoleToGroup(role, group.getGroupname());
                for (GeoserverRole role : removedRoles)
                    gaStore.disAssociateRoleFromGroup(role, group.getGroupname());
            
                gaStore.store();
            }            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error occurred while saving group", e);
            error(new ParamResourceModel("saveError", getPage(), e.getMessage()));
        }
    }

}
