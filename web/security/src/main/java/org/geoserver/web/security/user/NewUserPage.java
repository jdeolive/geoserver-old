/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.web.security.user;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;

import org.geoserver.security.GeoServerRoleStore;
import org.geoserver.security.GeoServerUserGroupStore;
import org.geoserver.security.impl.GeoServerRole;
import org.geoserver.security.impl.GeoServerUser;
import org.geoserver.security.impl.GeoServerUserGroup;
import org.geoserver.security.validation.RoleStoreValidationWrapper;
import org.geoserver.security.validation.UserGroupStoreValidationWrapper;
import org.geoserver.web.security.AbstractSecurityPage;


/**
 * Allows creation of a new user in users.properties
 */
public class NewUserPage extends AbstractUserPage {

    public NewUserPage(String userGroupServiceName,AbstractSecurityPage responsePage) {
       super(userGroupServiceName,new UserUIModel(),new Properties(),responsePage);       
       if (hasUserGroupStore(userGroupServiceName)==false) {
           throw new RuntimeException("Workflow error, new user not possible for read only service");
       }

    }
    
    @Override
    protected void onFormSubmit() throws IOException {
        GeoServerUserGroupStore ugStore = new UserGroupStoreValidationWrapper(
                getUserGroupStore(userGroupServiceName));
        GeoServerUser user =uiUser.toGeoserverUser(userGroupServiceName);
        try {
            user.getProperties().clear();
            for (Entry<Object,Object> entry : userpropertyeditor.getProperties().entrySet())
                user.getProperties().put(entry.getKey(),entry.getValue());
    
            ugStore.addUser(user);
                    
            Iterator<GeoServerUserGroup> it =userGroupFormComponent.groupPalette.getSelectedChoices();
            while (it.hasNext()) {
                ugStore.associateUserToGroup(user, it.next());
            }
            ugStore.store();
        } catch (IOException ex) {
            try {ugStore.load(); } catch (IOException ex2) {};
            throw ex;
        }

        GeoServerRoleStore gaStore = null;
        try {
            if (hasRoleStore(getSecurityManager().getActiveRoleService().getName())) {
                gaStore = getRoleStore(getSecurityManager().getActiveRoleService().getName());
                gaStore = new RoleStoreValidationWrapper(gaStore);
                Iterator<GeoServerRole> roleIt =userRolesFormComponent.
                        getRolePalette().getSelectedChoices();
                while (roleIt.hasNext()) {
                    gaStore.associateRoleToUser(roleIt.next(), user.getUsername());
                }
                gaStore.store();
            }
        } catch (IOException ex) {
            try {gaStore.load(); } catch (IOException ex2) {};
            throw ex;
        }                                        
    }
}
