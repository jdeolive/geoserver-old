/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.web.security.config.list;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.geoserver.security.config.SecurityRoleServiceConfig;
import org.geoserver.web.wicket.GeoServerDataProvider;

/**
 * Page list provider for {@link SecurityRoleServiceConfig} objects
 * 
 * @author christian
 *
 */
public class RoleServiceListProvider extends NamedServiceConfigListProvider<SecurityRoleServiceConfig> {

    private static final long serialVersionUID = 1L;
    public static final Property<SecurityRoleServiceConfig> ADMINROLENAME = 
            new BeanProperty<SecurityRoleServiceConfig>("adminRoleName", "adminRoleName");

    
    @Override
    protected List<SecurityRoleServiceConfig> getItems() {
        
        List <SecurityRoleServiceConfig> result = new
                ArrayList<SecurityRoleServiceConfig>();
        try {
            for (String name : getSecurityManager().listRoleServices()) {
                result.add((SecurityRoleServiceConfig)
                        getSecurityManager().loadRoleServiceConfig(name));
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return result;
    }

    @Override
    protected List<Property<SecurityRoleServiceConfig>> getProperties() {
        List<Property<SecurityRoleServiceConfig>> result = new ArrayList<GeoServerDataProvider.Property<SecurityRoleServiceConfig>>();
        result = super.getProperties();
        result.add(ADMINROLENAME);        
        return result;
    }

}
