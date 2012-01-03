/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.web.security.group;

import java.io.IOException;
import java.util.Iterator;

import org.geoserver.security.GeoserverRoleStore;
import org.geoserver.security.GeoserverUserGroupStore;
import org.geoserver.security.impl.GeoserverRole;
import org.geoserver.security.impl.GeoserverUserGroup;
import org.geoserver.security.validation.RoleStoreValidationWrapper;
import org.geoserver.security.validation.UserGroupStoreValidationWrapper;
import org.geoserver.web.security.AbstractSecurityPage;

public class NewGroupPage extends AbstractGroupPage {

    
    public NewGroupPage(String userGroupServiceName,AbstractSecurityPage responsePage) {
        super(userGroupServiceName,new GroupUIModel("", true),responsePage);
                
        if (hasUserGroupStore(userGroupServiceName)==false) {
            throw new RuntimeException("Workflow error, new user not possible for read only service");
        }
        
    }
                
    @Override
    protected void onFormSubmit() throws IOException {
        GeoserverUserGroupStore store=null;
        GeoserverUserGroup group=null;
        try {
            store = new UserGroupStoreValidationWrapper(
                    getUserGroupStore(userGroupServiceName));
            group = store.createGroupObject(
                    uiGroup.getGroupname(),uiGroup.isEnabled());
            store.addGroup(group);
            store.store();
        } catch (IOException ex) {
            try {store.load(); } catch (IOException ex2) {};
            throw ex;
        }

        GeoserverRoleStore gaStore=null;
        try {
            if (hasRoleStore(getSecurityManager().getActiveRoleService().getName())) {
                gaStore = getRoleStore(getSecurityManager().getActiveRoleService().getName());
                gaStore = new RoleStoreValidationWrapper(gaStore);
                Iterator<GeoserverRole> roleIt =groupRolesFormComponent.
                    getRolePalette().getSelectedChoices();
                while (roleIt.hasNext()) {
                    gaStore.associateRoleToGroup(roleIt.next(), group.getGroupname());
                }
                gaStore.store();
            }
        } catch (IOException ex) {
            try {gaStore.load(); } catch (IOException ex2) {};
            throw ex;
        }

                        
    }

}
