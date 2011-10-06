/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.web.security.user;

import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.apache.wicket.Page;
import org.geoserver.security.GeoserverGrantedAuthorityStore;
import org.geoserver.security.GeoserverUserGroupStore;
import org.geoserver.security.impl.GeoserverGrantedAuthority;
import org.geoserver.security.impl.GeoserverUser;
import org.geoserver.security.impl.GeoserverUserGroup;
import org.geoserver.web.wicket.ParamResourceModel;

/**
 * Allows editing an existing user
 */
public class EditUserPage extends AbstractUserPage {

    public EditUserPage(GeoserverUser user) {
        this(user,null);
    }
    
    public EditUserPage(GeoserverUser user,Page responsePage) {
        super(new UserUIModel(user),user.getProperties(),responsePage);
        username.setEnabled(false);
    }

    @Override
    protected void onFormSubmit() {
        try {
            
            GeoserverUser user = uiUser.toGeoserverUser();
            if (hasUserGroupStore()) {
                GeoserverUserGroupStore ugStore = getUserGroupStore();                                        
                user.getProperties().clear();
                for (Entry<Object,Object> entry : userpropertyeditor.getProperties().entrySet())
                    user.getProperties().put(entry.getKey(),entry.getValue());

            
                ugStore.updateUser(user);
            
                Set<GeoserverUserGroup> added = new HashSet<GeoserverUserGroup>();
                Set<GeoserverUserGroup> removed = new HashSet<GeoserverUserGroup>();
                userGroupFormComponent.calculateAddedRemovedCollections(added, removed);
                for (GeoserverUserGroup g : added)
                    ugStore.associateUserToGroup(user, g);
                for (GeoserverUserGroup g : removed)
                    ugStore.disAssociateUserFromGroup(user,g);
                ugStore.store();
            }
            if (hasGrantedAuthorityStore()) {                                
                GeoserverGrantedAuthorityStore gaStore = getGrantedAuthorityStore();
                Set<GeoserverGrantedAuthority> addedRoles = new HashSet<GeoserverGrantedAuthority>();
                Set<GeoserverGrantedAuthority> removedRoles = new HashSet<GeoserverGrantedAuthority>();
                userRolesFormComponent.calculateAddedRemovedCollections(addedRoles, removedRoles);
                for (GeoserverGrantedAuthority role : addedRoles)
                    gaStore.associateRoleToUser(role, user.getUsername());
                for (GeoserverGrantedAuthority role : removedRoles)
                    gaStore.disAssociateRoleFromUser(role, user.getUsername());                                                                               
                gaStore.store();
            }
            
            setActualResponsePage(UserPage.class);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error occurred while saving user", e);
            error(new ParamResourceModel("saveError", getPage(), e.getMessage()));
        }
    }

}
