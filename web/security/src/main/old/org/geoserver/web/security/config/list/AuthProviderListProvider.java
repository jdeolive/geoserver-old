/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.web.security.config.list;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.geoserver.security.config.SecurityAuthProviderConfig;
import org.geoserver.security.config.SecurityNamedServiceConfig;
import org.geoserver.security.config.SecurityRoleServiceConfig;
import org.geoserver.web.wicket.GeoServerDataProvider;
import org.geoserver.web.wicket.GeoServerDataProvider.BeanProperty;
import org.geoserver.web.wicket.GeoServerDataProvider.Property;

/**
 * Page list provider for {@link SecurityNamedServiceConfig} objects
 * 
 * @author christian
 *
 */
public class AuthProviderListProvider extends NamedServiceConfigListProvider<SecurityAuthProviderConfig> {

    private static final long serialVersionUID = 1L;
    public static final Property<SecurityAuthProviderConfig> USERGROUPSERVICENAME = 
            new BeanProperty<SecurityAuthProviderConfig>("userGroupServiceName", "userGroupServiceName");

    
    @Override
    protected List<SecurityAuthProviderConfig> getItems() {
        
        List <SecurityAuthProviderConfig> result = new
                ArrayList<SecurityAuthProviderConfig>();
        try {
            for (String name : getSecurityManager().listAuthenticationProviders()) {
                result.add(
                        getSecurityManager().loadAuthenticationProviderConfig(name));
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return result;
    }

    @Override
    protected List<Property<SecurityAuthProviderConfig>> getProperties() {
        List<Property<SecurityAuthProviderConfig>> result = new ArrayList<GeoServerDataProvider.Property<SecurityAuthProviderConfig>>();
        result = super.getProperties();
        result.add(USERGROUPSERVICENAME);        
        return result;
    }

}
