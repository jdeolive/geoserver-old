/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.web.security.config.list;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.geoserver.security.config.SecurityUserGoupServiceConfig;
import org.geoserver.web.wicket.GeoServerDataProvider;

/**
 * Page list provider for {@link SecurityUserGoupServiceConfig} objects
 * 
 * @author christian
 *
 */
public class UserGroupServiceListProvider extends NamedServiceConfigListProvider<SecurityUserGoupServiceConfig> {

    private static final long serialVersionUID = 1L;
    public static final Property<SecurityUserGoupServiceConfig> PASSWORDENCODERNAME = 
            new BeanProperty<SecurityUserGoupServiceConfig>("passwordEncoderName", "passwordEncoderName");
    public static final Property<SecurityUserGoupServiceConfig> PASSWORDPOLICYNAME = 
            new BeanProperty<SecurityUserGoupServiceConfig>("passwordPolicyName", "passwordPolicyName");

    
    @Override
    protected List<SecurityUserGoupServiceConfig> getItems() {
        
        List <SecurityUserGoupServiceConfig> result = new
                ArrayList<SecurityUserGoupServiceConfig>();
        try {
            for (String name : getSecurityManager().listUserGroupServices()) {
                result.add((SecurityUserGoupServiceConfig)
                        getSecurityManager().loadUserGroupServiceConfig(name));
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return result;
    }

    @Override
    protected List<Property<SecurityUserGoupServiceConfig>> getProperties() {
        List<Property<SecurityUserGoupServiceConfig>> result = new ArrayList<GeoServerDataProvider.Property<SecurityUserGoupServiceConfig>>();
        result = super.getProperties();
        result.add(PASSWORDENCODERNAME);
        result.add(PASSWORDPOLICYNAME);
        return result;
    }

}
