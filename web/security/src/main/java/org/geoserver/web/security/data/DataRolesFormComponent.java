/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.web.security.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.markup.html.form.Form;
import org.geoserver.security.GeoserverGrantedAuthorityService;
import org.geoserver.security.impl.GeoserverGrantedAuthority;
import org.geoserver.security.impl.GeoserverUserDetailsServiceImpl;
import org.geoserver.security.impl.DataAccessRule;
import org.geoserver.web.security.AbstractRuleRolesFormComponent;

public class DataRolesFormComponent extends AbstractRuleRolesFormComponent<DataAccessRule> {

    private static final long serialVersionUID = 1L;

    public DataRolesFormComponent(DataAccessRule rule,Form<?> form) {
        this(rule, form,null);        
    }
    
    public DataRolesFormComponent(DataAccessRule rule,Form<?> form,IBehavior behavior) {
        super("roles", rule,  false,form,behavior);        
    }


    @Override
    protected List<GeoserverGrantedAuthority> getStoredGrantedAuthorities(DataAccessRule rootObject) {
        
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
    public boolean hasStoredAnyRole(DataAccessRule rootObject) {
        return rootObject.getRoles().contains(GeoserverGrantedAuthority.HASANY_ROLE.getAuthority());        
    }    
}
