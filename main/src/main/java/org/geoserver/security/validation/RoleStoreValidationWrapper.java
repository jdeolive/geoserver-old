/* Copyright (c) 2001 - 2008 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.security.validation;

import java.io.IOException;

import org.geoserver.security.GeoserverRoleService;
import org.geoserver.security.GeoserverRoleStore;
import org.geoserver.security.GeoserverUserGroupService;
import org.geoserver.security.impl.GeoserverRole;



/**
 * 
 * This class is a validation wrapper for {@link GeoserverRoleStore}
 * 
 * Usage:
 * <code>
 * GeoserverRoleStore valStore = new RoleStoreValidationWrapper(store);
 * valStore.addRole(..);
 * valStore.store()
 * </code>
 * 
 * Since the {@link GeoserverRoleStore} interface does not allow to 
 * throw {@link RoleServiceException} objects directly, these objects
 * a wrapped into an IOException. Use {@link IOException#getCause()} to
 * get the proper exception.
 * 
 * 
 * @author christian
 *
 */
public class RoleStoreValidationWrapper extends RoleServiceValidationWrapper implements GeoserverRoleStore{

    
    /**
     * @see RoleServiceValidationWrapper
     */    
    public RoleStoreValidationWrapper(GeoserverRoleStore store, boolean checkAgainstRules, 
            GeoserverUserGroupService ...services) {
        super(store,checkAgainstRules, services);
    }

    /**
     * @see RoleServiceValidationWrapper
     */    
    public RoleStoreValidationWrapper(GeoserverRoleStore store, GeoserverUserGroupService ...services) {
        super(store,services);
    }
        
    GeoserverRoleStore getStore() {
        return (GeoserverRoleStore) service;
    }

    public void initializeFromService(GeoserverRoleService aService) throws IOException {
        getStore().initializeFromService(aService);
    }

    public void clear() throws IOException {
        getStore().clear();
    }


    public void addRole(GeoserverRole role) throws IOException {
        checkNotExistingRoleName(role.getAuthority());
        getStore().addRole(role);
    }


    public void updateRole(GeoserverRole role) throws IOException {
        checkExistingRoleName(role.getAuthority());
        getStore().updateRole(role);
    }

    public boolean removeRole(GeoserverRole role) throws IOException {
        checkRemovalOfAdminRole(role);
        checkRoleIsUsed(role);
        return getStore().removeRole(role);
    }

    public void associateRoleToGroup(GeoserverRole role, String groupname) throws IOException {
        checkExistingRoleName(role.getAuthority());
        checkValidGroupName(groupname);
        getStore().associateRoleToGroup(role, groupname);
    }


    public void disAssociateRoleFromGroup(GeoserverRole role, String groupname) throws IOException {
        checkExistingRoleName(role.getAuthority());
        checkValidGroupName(groupname);
        getStore().disAssociateRoleFromGroup(role, groupname);
    }

    public void associateRoleToUser(GeoserverRole role, String username) throws IOException {
        checkExistingRoleName(role.getAuthority());
        checkValidUserName(username);
        getStore().associateRoleToUser(role, username);
    }
    public void disAssociateRoleFromUser(GeoserverRole role, String username) throws IOException {
        checkExistingRoleName(role.getAuthority());
        checkValidUserName(username);
        getStore().disAssociateRoleFromUser(role, username);
    }


    public void store() throws IOException {
        getStore().store();
    }


    public boolean isModified() {
        return getStore().isModified();
    }


    public void setParentRole(GeoserverRole role, GeoserverRole parentRole) throws IOException {
        checkExistingRoleName(role.getAuthority());
        if (parentRole!=null)
            checkExistingRoleName(parentRole.getAuthority());
        getStore().setParentRole(role, parentRole);
    }

}
