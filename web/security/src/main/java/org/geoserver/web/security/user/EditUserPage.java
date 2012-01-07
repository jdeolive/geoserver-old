/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.web.security.user;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.geoserver.security.GeoServerRoleStore;
import org.geoserver.security.GeoServerUserGroupStore;
import org.geoserver.security.impl.GeoServerRole;
import org.geoserver.security.impl.GeoServerUser;
import org.geoserver.security.impl.GeoServerUserGroup;
import org.geoserver.security.validation.RoleStoreValidationWrapper;
import org.geoserver.security.validation.UserGroupStoreValidationWrapper;
import org.geoserver.web.security.AbstractSecurityPage;

/**
 * Allows editing an existing user
 */
public class EditUserPage extends AbstractUserPage {

    public EditUserPage(String userGroupServiceName, GeoServerUser user,AbstractSecurityPage responsePage) {
        super(userGroupServiceName,new UserUIModel(user),user.getProperties(),responsePage);
        username.setEnabled(false);
    }

    @Override
    protected void onFormSubmit() throws IOException {
        GeoServerUser user = uiUser.toGeoserverUser(userGroupServiceName);
        GeoServerUserGroupStore ugStore=null;
        try {
            if (hasUserGroupStore(userGroupServiceName)) {
                ugStore = new UserGroupStoreValidationWrapper(
                        getUserGroupStore(userGroupServiceName));
    
                user.getProperties().clear();
                for (Entry<Object,Object> entry : userpropertyeditor.getProperties().entrySet())
                    user.getProperties().put(entry.getKey(),entry.getValue());
    
            
                ugStore.updateUser(user);
            
                Set<GeoServerUserGroup> added = new HashSet<GeoServerUserGroup>();
                Set<GeoServerUserGroup> removed = new HashSet<GeoServerUserGroup>();
                userGroupFormComponent.calculateAddedRemovedCollections(added, removed);
                for (GeoServerUserGroup g : added)
                    ugStore.associateUserToGroup(user, g);
                for (GeoServerUserGroup g : removed)
                    ugStore.disAssociateUserFromGroup(user,g);
                ugStore.store();
            }
        } catch (IOException ex) {
            try {ugStore.load(); } catch (IOException ex2) {};
            throw ex;
        }

        GeoServerRoleStore gaStore=null;
        try {
            if (hasRoleStore(getSecurityManager().getActiveRoleService().getName())) {                                
                 gaStore = getRoleStore(getSecurityManager().getActiveRoleService().getName());
                gaStore = new RoleStoreValidationWrapper(gaStore);
                Set<GeoServerRole> addedRoles = new HashSet<GeoServerRole>();
                Set<GeoServerRole> removedRoles = new HashSet<GeoServerRole>();
                userRolesFormComponent.calculateAddedRemovedCollections(addedRoles, removedRoles);
                for (GeoServerRole role : addedRoles)
                    gaStore.associateRoleToUser(role, user.getUsername());
                for (GeoServerRole role : removedRoles)
                    gaStore.disAssociateRoleFromUser(role, user.getUsername());                                                                               
                gaStore.store();
            }
        } catch (IOException ex) {
            try {gaStore.load(); } catch (IOException ex2) {};
            throw ex;
        }

            
    }

}
