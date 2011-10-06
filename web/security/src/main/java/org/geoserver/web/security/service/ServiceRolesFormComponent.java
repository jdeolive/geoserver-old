/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.web.security.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.markup.html.form.Form;
import org.geoserver.security.GeoserverGrantedAuthorityService;
import org.geoserver.security.impl.GeoserverGrantedAuthority;
import org.geoserver.security.impl.GeoserverUserDetailsServiceImpl;
import org.geoserver.security.impl.ServiceAccessRule;
import org.geoserver.web.security.AbstractRuleRolesFormComponent;

public class ServiceRolesFormComponent extends AbstractRuleRolesFormComponent<ServiceAccessRule> {

    private static final long serialVersionUID = 1L;

    public ServiceRolesFormComponent(ServiceAccessRule rule,Form<?> form) {
        this(rule ,form,null);        
    }
    
    public ServiceRolesFormComponent(ServiceAccessRule rule,Form<?> form, IBehavior behavior) {
        super("roles", rule,  false,form,behavior);        
    }


    @Override
    protected List<GeoserverGrantedAuthority> getStoredGrantedAuthorities(ServiceAccessRule rootObject) {
        
        GeoserverGrantedAuthorityService gaService = 
                GeoserverUserDetailsServiceImpl.get().getGrantedAuthorityService();
        List<GeoserverGrantedAuthority> result = new ArrayList<GeoserverGrantedAuthority>();        
        if (hasStoredAnyRole(rootObject))
            return result; // empty list
        
        try {
            for (String roleString : rootObject.getRoles()) 
                result.add(gaService.getGrantedAuthorityByName(roleString));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }                                
        return result;
    }

    @Override
    public boolean hasStoredAnyRole(ServiceAccessRule rootObject) {
        return rootObject.getRoles().contains(GeoserverGrantedAuthority.HASANY_ROLE.getAuthority());        
    }    
}
