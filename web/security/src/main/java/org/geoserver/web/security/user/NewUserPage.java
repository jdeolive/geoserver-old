/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.web.security.user;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;

import org.geoserver.security.GeoserverRoleStore;
import org.geoserver.security.GeoserverUserGroupStore;
import org.geoserver.security.impl.GeoserverRole;
import org.geoserver.security.impl.GeoserverUser;
import org.geoserver.security.impl.GeoserverUserGroup;
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
        GeoserverUserGroupStore ugStore = new UserGroupStoreValidationWrapper(
                getUserGroupStore(userGroupServiceName));
        GeoserverUser user =uiUser.toGeoserverUser(userGroupServiceName);             
        user.getProperties().clear();
        for (Entry<Object,Object> entry : userpropertyeditor.getProperties().entrySet())
            user.getProperties().put(entry.getKey(),entry.getValue());

        ugStore.addUser(user);
                
        Iterator<GeoserverUserGroup> it =userGroupFormComponent.groupPalette.getSelectedChoices();
        while (it.hasNext()) {
            ugStore.associateUserToGroup(user, it.next());
        }
        
        if (hasRoleStore(getSecurityManager().getActiveRoleService().getName())) {
            GeoserverRoleStore gaStore = getRoleStore(getSecurityManager().getActiveRoleService().getName());
            gaStore = new RoleStoreValidationWrapper(gaStore);
            Iterator<GeoserverRole> roleIt =userRolesFormComponent.
                    getRolePalette().getSelectedChoices();
            while (roleIt.hasNext()) {
                gaStore.associateRoleToUser(roleIt.next(), user.getUsername());
            }
            gaStore.store();
        }
        
                                
        ugStore.store();
    }
}
