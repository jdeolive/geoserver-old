/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.web.security.role;

import java.io.IOException;
import java.util.Map.Entry;

import org.geoserver.security.GeoserverRoleStore;
import org.geoserver.security.impl.GeoserverRole;
import org.geoserver.security.validation.RoleStoreValidationWrapper;
import org.geoserver.web.security.AbstractSecurityPage;


/**
 * Page for editing a  {@link GeoserverRole} object
 * 
 * @author christian
 *
 */
public class EditRolePage extends AbstractRolePage {

    public EditRolePage(String roleServiceName,GeoserverRole role,AbstractSecurityPage responsePage) {
        // parentrole name not known at this moment, parent
        // constructor will do the job 
        super(roleServiceName,new RoleUIModel(role.getAuthority(), null,role.getUserName()), 
                role.getProperties(),responsePage);        
        rolenameField.setEnabled(false);
        
        // do we hava a personalized role
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
        
        GeoserverRoleStore store = new RoleStoreValidationWrapper(
                getRoleStore(roleServiceName));
        
        GeoserverRole role = store.getRoleByName(uiRole.getRolename());
        
        role.getProperties().clear();

        
      for (Entry<Object,Object> entry : roleParamEditor.getProperties().entrySet())
          role.getProperties().put(entry.getKey(),entry.getValue());
        
        store.updateRole(role);
                
        GeoserverRole parentRole = null;
        if (uiRole.getParentrolename()!=null && uiRole.getParentrolename().length() > 0) {
            parentRole=store.getRoleByName(uiRole.getParentrolename());
        }
        store.setParentRole(role,parentRole);
        store.store();            
    }

}
