/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.security.web.group;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.geoserver.security.GeoServerRoleStore;
import org.geoserver.security.GeoServerUserGroupStore;
import org.geoserver.security.impl.GeoServerRole;
import org.geoserver.security.impl.GeoServerUserGroup;
import org.geoserver.security.validation.RoleStoreValidationWrapper;
import org.geoserver.security.validation.UserGroupStoreValidationWrapper;
import org.geoserver.security.web.AbstractSecurityPage;

public class EditGroupPage extends AbstractGroupPage {

    
    public EditGroupPage(String userGroupServiceName,GeoServerUserGroup group,AbstractSecurityPage responsePage) {
        super(userGroupServiceName,new GroupUIModel(group.getGroupname(), group.isEnabled()),responsePage);
        groupnameField.setEnabled(false);
    }

    @Override
    protected void onFormSubmit() throws IOException {
        
                    
        GeoServerUserGroup group = getUserGroupService(userGroupServiceName).getGroupByGroupname(uiGroup.getGroupname());

        GeoServerUserGroupStore store = null;
        try {
            if (hasUserGroupStore(userGroupServiceName)) {
                store = new UserGroupStoreValidationWrapper(                     
                        getUserGroupStore(userGroupServiceName));            
                group.setEnabled(uiGroup.isEnabled());
                store.updateGroup(group);
                store.store();
            };   
        } catch (IOException ex) {
            try {store.load(); } catch (IOException ex2) {};
            throw ex;
        }

        GeoServerRoleStore gaStore = null;
        try {
            if (hasRoleStore(getSecurityManager().getActiveRoleService().getName())) {
                gaStore = getRoleStore(getSecurityManager().getActiveRoleService().getName());
                gaStore = new RoleStoreValidationWrapper(gaStore);                   
                Set<GeoServerRole> addedRoles = new HashSet<GeoServerRole>();
                Set<GeoServerRole> removedRoles = new HashSet<GeoServerRole>();
                groupRolesFormComponent.calculateAddedRemovedCollections(addedRoles, removedRoles);
                for (GeoServerRole role : addedRoles)
                    gaStore.associateRoleToGroup(role, group.getGroupname());
                for (GeoServerRole role : removedRoles)
                    gaStore.disAssociateRoleFromGroup(role, group.getGroupname());        
                gaStore.store();
            }        
        } catch (IOException ex) {
            try {gaStore.load(); } catch (IOException ex2) {};
            throw ex;
        }

    }

}
