/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.security.web.user;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.markup.html.form.Form;
import org.geoserver.security.impl.GeoServerRole;
import org.geoserver.security.impl.GeoServerUser;
import org.geoserver.security.web.role.AbstractRolesFormComponent;

/**
 * A form component that can be used to edit user/rule role lists
 */
@SuppressWarnings("serial")
public class UserRolesFormComponent extends AbstractRolesFormComponent<GeoServerUser> {

    public UserRolesFormComponent(GeoServerUser user, Form<?> form) {
        this(user,form,null);        
    }
    
    public UserRolesFormComponent(GeoServerUser user, Form<?> form,IBehavior behavior) {
        super("roles", user, false,form,behavior);        
    }

    @Override
    protected  List<GeoServerRole> getStoredGrantedAuthorities(GeoServerUser rootObject) {
        List<GeoServerRole> result = new ArrayList<GeoServerRole>();
        try {
            result.addAll(
                getSecurityManager().getActiveRoleService().getRolesForUser(rootObject.getUsername())
            );
        } catch (IOException e) {
           throw new RuntimeException(e);
        }
        return result;
    }    
}
