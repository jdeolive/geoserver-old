/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.web.security.user;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.markup.html.form.Form;
import org.geoserver.security.impl.GeoserverGrantedAuthority;
import org.geoserver.security.impl.GeoserverUserDetailsServiceImpl;
import org.geoserver.security.impl.GeoserverUser;
import org.geoserver.web.security.AbstractRolesFormComponent;

/**
 * A form component that can be used to edit user/rule role lists
 */
@SuppressWarnings("serial")
public class UserRolesFormComponent extends AbstractRolesFormComponent<GeoserverUser> {

    public UserRolesFormComponent(GeoserverUser user, Form<?> form) {
        this(user,form,null);        
    }
    
    public UserRolesFormComponent(GeoserverUser user, Form<?> form,IBehavior behavior) {
        super("roles", user, false,form,behavior);        
    }

    @Override
    protected  List<GeoserverGrantedAuthority> getStoredGrantedAuthorities(GeoserverUser rootObject) {
        List<GeoserverGrantedAuthority> result = new ArrayList<GeoserverGrantedAuthority>();
        try {
            result.addAll(
                GeoserverUserDetailsServiceImpl.get().getGrantedAuthorityService().
                    getRolesForUser(rootObject.getUsername())
            );
        } catch (IOException e) {
           throw new RuntimeException(e);
        }
        return result;
    }    
}
