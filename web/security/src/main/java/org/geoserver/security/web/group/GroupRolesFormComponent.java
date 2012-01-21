/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.security.web.group;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.markup.html.form.Form;
import org.geoserver.security.impl.GeoServerRole;
import org.geoserver.security.impl.GeoServerUserGroup;
import org.geoserver.security.web.role.AbstractRolesFormComponent;

/**
 * A form component that can be used to edit user/rule role lists
 */
@SuppressWarnings("serial")
public class GroupRolesFormComponent extends AbstractRolesFormComponent<GeoServerUserGroup> {

    public GroupRolesFormComponent(GeoServerUserGroup group, Form<?> form) {
        this(group,form,null);        
    }
    public GroupRolesFormComponent(GeoServerUserGroup group, Form<?> form, IBehavior behavior) {
        super("roles", group,  false,form,behavior);        
    }

    @Override
    protected List<GeoServerRole> getStoredGrantedAuthorities(GeoServerUserGroup rootObject) {
        List<GeoServerRole> result = new ArrayList<GeoServerRole>();
        try {
            result.addAll(getSecurityManager().getActiveRoleService()
                .getRolesForGroup(rootObject.getGroupname()));
        } catch (IOException e) {
           throw new RuntimeException(e);
        }
        return result;
    }    
}
