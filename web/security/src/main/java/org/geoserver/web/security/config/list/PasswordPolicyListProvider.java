/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.web.security.config.list;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.geoserver.security.config.PasswordPolicyConfig;
import org.geoserver.security.config.PasswordPolicyConfig;
import org.geoserver.web.wicket.GeoServerDataProvider;

/**
 * Page list provider for {@link PasswordPolicyConfig} objects
 * 
 * @author christian
 *
 */
public class PasswordPolicyListProvider extends NamedServiceConfigListProvider<PasswordPolicyConfig> {

    private static final long serialVersionUID = 1L;

    
    @Override
    protected List<PasswordPolicyConfig> getItems() {
        
        List <PasswordPolicyConfig> result = new
                ArrayList<PasswordPolicyConfig>();
        try {
            for (String name : getSecurityManager().listPasswordValidators()) {
                result.add((PasswordPolicyConfig)
                        getSecurityManager().loadPasswordPolicyConfig(name));
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return result;
    }

    @Override
    protected List<Property<PasswordPolicyConfig>> getProperties() {
        List<Property<PasswordPolicyConfig>> result = new ArrayList<GeoServerDataProvider.Property<PasswordPolicyConfig>>();
        result = super.getProperties();
        return result;
    }

}
