/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.web.security.group;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.geoserver.security.GeoserverRoleStore;
import org.geoserver.security.GeoserverUserGroupStore;
import org.geoserver.security.impl.GeoserverRole;
import org.geoserver.security.impl.GeoserverUserGroup;
import org.geoserver.security.validation.RoleStoreValidationWrapper;
import org.geoserver.security.validation.UserGroupStoreValidationWrapper;
import org.geoserver.web.security.AbstractSecurityPage;

public class EditGroupPage extends AbstractGroupPage {

    
    public EditGroupPage(String userGroupServiceName,GeoserverUserGroup group,AbstractSecurityPage responsePage) {
        super(userGroupServiceName,new GroupUIModel(group.getGroupname(), group.isEnabled()),responsePage);
        groupnameField.setEnabled(false);
    }

    @Override
    protected void onFormSubmit() throws IOException {
        
                    
        GeoserverUserGroup group = getUserGroupService(userGroupServiceName).getGroupByGroupname(uiGroup.getGroupname());
        
        if (hasUserGroupStore(userGroupServiceName)) {
            GeoserverUserGroupStore store = new UserGroupStoreValidationWrapper(                     
                    getUserGroupStore(userGroupServiceName));            
            group.setEnabled(uiGroup.isEnabled());
            store.updateGroup(group);
            store.store();
        };   

        if (hasRoleStore(getSecurityManager().getActiveRoleService().getName())) {
            GeoserverRoleStore gaStore = getRoleStore(getSecurityManager().getActiveRoleService().getName());
            gaStore = new RoleStoreValidationWrapper(gaStore);                   
            Set<GeoserverRole> addedRoles = new HashSet<GeoserverRole>();
            Set<GeoserverRole> removedRoles = new HashSet<GeoserverRole>();
            groupRolesFormComponent.calculateAddedRemovedCollections(addedRoles, removedRoles);
            for (GeoserverRole role : addedRoles)
                gaStore.associateRoleToGroup(role, group.getGroupname());
            for (GeoserverRole role : removedRoles)
                gaStore.disAssociateRoleFromGroup(role, group.getGroupname());
        
            gaStore.store();
        }            
    }

}
