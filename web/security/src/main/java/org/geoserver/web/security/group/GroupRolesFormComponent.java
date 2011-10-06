/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.web.security.group;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.markup.html.form.Form;
import org.geoserver.security.impl.GeoserverGrantedAuthority;
import org.geoserver.security.impl.GeoserverUserDetailsServiceImpl;
import org.geoserver.security.impl.GeoserverUserGroup;
import org.geoserver.web.security.AbstractRolesFormComponent;

/**
 * A form component that can be used to edit user/rule role lists
 */
@SuppressWarnings("serial")
public class GroupRolesFormComponent extends AbstractRolesFormComponent<GeoserverUserGroup> {

    public GroupRolesFormComponent(GeoserverUserGroup group, Form<?> form) {
        this(group,form,null);        
    }
    public GroupRolesFormComponent(GeoserverUserGroup group, Form<?> form, IBehavior behavior) {
        super("roles", group,  false,form,behavior);        
    }

    @Override
    protected List<GeoserverGrantedAuthority> getStoredGrantedAuthorities(GeoserverUserGroup rootObject) {
        List<GeoserverGrantedAuthority> result = new ArrayList<GeoserverGrantedAuthority>();
        try {
            result.addAll(
                GeoserverUserDetailsServiceImpl.get().getGrantedAuthorityService().
                    getRolesForGroup(rootObject.getGroupname())
            );
        } catch (IOException e) {
           throw new RuntimeException(e);
        }
        return result;
    }    
}
