/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.security.web.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.markup.html.form.Form;
import org.geoserver.security.GeoServerRoleService;
import org.geoserver.security.impl.GeoServerRole;
import org.geoserver.security.impl.DataAccessRule;
import org.geoserver.security.web.role.AbstractRuleRolesFormComponent;

public class DataRolesFormComponent extends AbstractRuleRolesFormComponent<DataAccessRule> {

    private static final long serialVersionUID = 1L;

    public DataRolesFormComponent(DataAccessRule rule,Form<?> form) {
        this(rule, form,null);        
    }
    
    public DataRolesFormComponent(DataAccessRule rule,Form<?> form,IBehavior behavior) {
        super("roles", rule,  false,form,behavior);        
    }

    @Override
    protected List<GeoServerRole> getStoredGrantedAuthorities(DataAccessRule rootObject) {
        
        GeoServerRoleService gaService = getSecurityManager().getActiveRoleService();
        List<GeoServerRole> result = new ArrayList<GeoServerRole>();        
        if (hasStoredAnyRole(rootObject))
            return result; // empty list
        
        try {
            for (String roleString : rootObject.getRoles()) 
                result.add(gaService.getRoleByName(roleString));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }                                
        return result;
    }

    @Override
    public boolean hasStoredAnyRole(DataAccessRule rootObject) {
        return rootObject.getRoles().contains(GeoServerRole.HASANY_ROLE.getAuthority());        
    }    
}
