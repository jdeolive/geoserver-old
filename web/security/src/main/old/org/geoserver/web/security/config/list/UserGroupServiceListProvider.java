/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.web.security.config.list;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.geoserver.security.config.SecurityUserGroupServiceConfig;
import org.geoserver.web.wicket.GeoServerDataProvider;

/**
 * Page list provider for {@link SecurityUserGroupServiceConfig} objects
 * 
 * @author christian
 *
 */
public class UserGroupServiceListProvider extends NamedServiceConfigListProvider<SecurityUserGroupServiceConfig> {

    private static final long serialVersionUID = 1L;
    public static final Property<SecurityUserGroupServiceConfig> PASSWORDENCODERNAME = 
            new BeanProperty<SecurityUserGroupServiceConfig>("passwordEncoderName", "passwordEncoderName");
    public static final Property<SecurityUserGroupServiceConfig> PASSWORDPOLICYNAME = 
            new BeanProperty<SecurityUserGroupServiceConfig>("passwordPolicyName", "passwordPolicyName");

    
    @Override
    protected List<SecurityUserGroupServiceConfig> getItems() {
        
        List <SecurityUserGroupServiceConfig> result = new
                ArrayList<SecurityUserGroupServiceConfig>();
        try {
            for (String name : getSecurityManager().listUserGroupServices()) {
                result.add((SecurityUserGroupServiceConfig)
                        getSecurityManager().loadUserGroupServiceConfig(name));
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return result;
    }

    @Override
    protected List<Property<SecurityUserGroupServiceConfig>> getProperties() {
        List<Property<SecurityUserGroupServiceConfig>> result = new ArrayList<GeoServerDataProvider.Property<SecurityUserGroupServiceConfig>>();
        result = super.getProperties();
        result.add(PASSWORDENCODERNAME);
        result.add(PASSWORDPOLICYNAME);
        return result;
    }

}
