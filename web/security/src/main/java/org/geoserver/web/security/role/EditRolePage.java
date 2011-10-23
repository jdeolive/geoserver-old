/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.web.security.role;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.apache.wicket.Page;
import org.geoserver.security.GeoserverRoleStore;
import org.geoserver.security.impl.GeoserverRole;
import org.geoserver.web.wicket.ParamResourceModel;


/**
 * Page for editing a  {@link GeoserverRole} object
 * 
 * @author christian
 *
 */
public class EditRolePage extends AbstractRolePage {

    public EditRolePage(GeoserverRole role) {
        this(role,null);
    }
    
    public EditRolePage(GeoserverRole role,Page responsePage) {
        // parentrole name not known at this moment, parent
        // constructor will do the job 
        super(new RoleUIModel(role.getAuthority(), null,role.getUserName()), 
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
    protected void onFormSubmit() {
        
        if (hasRoleStore()==false) {
            throw new RuntimeException("Invalid workflow, cannot store in a read only GA service");
        }
        
        try {
            GeoserverRoleStore store = getRoleStore();
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
            
            setActualResponsePage(RolePage.class);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error occurred while saving role", e);
            error(new ParamResourceModel("saveError", getPage(), e.getMessage()));
        }
    }

}
