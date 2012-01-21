/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.security.web.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.markup.html.form.Form;
import org.geoserver.security.GeoServerRoleService;
import org.geoserver.security.impl.GeoServerRole;
import org.geoserver.security.impl.ServiceAccessRule;
import org.geoserver.security.web.role.AbstractRuleRolesFormComponent;

public class ServiceRolesFormComponent extends AbstractRuleRolesFormComponent<ServiceAccessRule> {

    private static final long serialVersionUID = 1L;

    public ServiceRolesFormComponent(ServiceAccessRule rule,Form<?> form) {
        this(rule ,form,null);        
    }
    
    public ServiceRolesFormComponent(ServiceAccessRule rule,Form<?> form, IBehavior behavior) {
        super("roles", rule,  false,form,behavior);        
    }


    @Override
    protected List<GeoServerRole> getStoredGrantedAuthorities(ServiceAccessRule rootObject) {
        
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
    public boolean hasStoredAnyRole(ServiceAccessRule rootObject) {
        return rootObject.getRoles().contains(GeoServerRole.HASANY_ROLE.getAuthority());        
    }    
}
