/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.security.web.role;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.Properties;

import org.geoserver.security.GeoServerRoleStore;
import org.geoserver.security.impl.GeoServerRole;
import org.geoserver.security.validation.RoleStoreValidationWrapper;
import org.geoserver.security.web.AbstractSecurityPage;

/**
 * Page for adding a new {@link GeoServerRole} object
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
        
        GeoServerRoleStore store = null;
        try {
            store = new RoleStoreValidationWrapper(
                    getRoleStore(roleServiceName));
    
            GeoServerRole role = store.createRoleObject(uiRole.getRolename());
            
            role.getProperties().clear();
            for (Entry<Object,Object> entry : roleParamEditor.getProperties().entrySet())
                role.getProperties().put(entry.getKey(),entry.getValue());
    
            store.addRole(role);
                    
            GeoServerRole parentRole = null;
            if (uiRole.getParentrolename()!=null && uiRole.getParentrolename().length() > 0) {
                parentRole=store.getRoleByName(uiRole.getParentrolename());
            }
            store.setParentRole(role,parentRole);
            store.store();
        } catch (IOException ex) {
            try {store.load(); } catch (IOException ex2) {};
            throw ex;
        }
                                    
    }

}
