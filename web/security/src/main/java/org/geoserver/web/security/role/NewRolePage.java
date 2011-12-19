/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.web.security.role;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.Properties;

import org.geoserver.security.GeoserverRoleStore;
import org.geoserver.security.impl.GeoserverRole;
import org.geoserver.security.validation.RoleStoreValidationWrapper;
import org.geoserver.web.security.AbstractSecurityPage;


/**
 * Page for adding a new {@link GeoserverRole} object
 * 
 * @author christian
 *
 */
public class NewRolePage extends AbstractRolePage {

    
    public NewRolePage(String roleServiceName,AbstractSecurityPage responsePage) {
        super(roleServiceName,new RoleUIModel("", "",null),new Properties(),responsePage);        
        if (hasRoleStore(roleServiceName)==false) {
            throw new RuntimeException("Workflow error, new role not possible for read only service");
        }

    }

    
    @Override
    protected void onFormSubmit() throws IOException {
        
        
        GeoserverRoleStore store = new RoleStoreValidationWrapper(
                getRoleStore(roleServiceName));

        GeoserverRole role = store.createRoleObject(uiRole.getRolename());
        
        role.getProperties().clear();
        for (Entry<Object,Object> entry : roleParamEditor.getProperties().entrySet())
            role.getProperties().put(entry.getKey(),entry.getValue());

        store.addRole(role);
                
        GeoserverRole parentRole = null;
        if (uiRole.getParentrolename()!=null && uiRole.getParentrolename().length() > 0) {
            parentRole=store.getRoleByName(uiRole.getParentrolename());
        }
        store.setParentRole(role,parentRole);
        store.store();
                        
            
    }

}
