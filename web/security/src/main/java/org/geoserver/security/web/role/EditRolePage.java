/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.security.web.role;

import java.io.IOException;
import java.util.Map.Entry;

import org.geoserver.security.GeoServerRoleStore;
import org.geoserver.security.impl.GeoServerRole;
import org.geoserver.security.validation.RoleStoreValidationWrapper;
import org.geoserver.security.web.AbstractSecurityPage;

/**
 * Page for editing a  {@link GeoServerRole} object
 * 
 * @author christian
 *
 */
public class EditRolePage extends AbstractRolePage {

    public EditRolePage(String roleServiceName,GeoServerRole role,AbstractSecurityPage responsePage) {
        // parent role name not known at this moment, parent
        // constructor will do the job 
        super(roleServiceName,new RoleUIModel(role.getAuthority(), null,role.getUserName()), 
                role.getProperties(),responsePage);        
        rolenameField.setEnabled(false);
        
        // do we have a personalized role
        if (role.getUserName()!=null ) {
            roleParamEditor.setEnabled(false);
            parentRoles.setEnabled(false);
            saveLink.setVisibilityAllowed(false);
        }

    }
        
            
    @Override
    protected void onFormSubmit() throws IOException{
        
        if (hasRoleStore(roleServiceName)==false) {
            throw new RuntimeException("Invalid workflow, cannot store in a read only role service");
        }
        GeoServerRoleStore store=null;
        try {
            store = new RoleStoreValidationWrapper(
                    getRoleStore(roleServiceName));
            
            GeoServerRole role = store.getRoleByName(uiRole.getRolename());
            
            role.getProperties().clear();
    
            
            for (Entry<Object,Object> entry : roleParamEditor.getProperties().entrySet())
              role.getProperties().put(entry.getKey(),entry.getValue());
            
            store.updateRole(role);
                    
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
