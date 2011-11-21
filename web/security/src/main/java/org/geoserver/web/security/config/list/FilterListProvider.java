/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.web.security.config.list;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.geoserver.security.config.SecurityNamedServiceConfig;
import org.geoserver.web.wicket.GeoServerDataProvider;

/**
 * Page list provider for {@link SecurityNamedServiceConfig} objects
 * 
 * @author christian
 *
 */
public class FilterListProvider extends NamedServiceConfigListProvider<SecurityNamedServiceConfig> {

    private static final long serialVersionUID = 1L;

    
    @Override
    protected List<SecurityNamedServiceConfig> getItems() {
        
        List <SecurityNamedServiceConfig> result = new
                ArrayList<SecurityNamedServiceConfig>();
        try {
            for (String name : getSecurityManager().listFilters()) {
                result.add((SecurityNamedServiceConfig)
                        getSecurityManager().loadFilterConfig(name));
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return result;
    }

    @Override
    protected List<Property<SecurityNamedServiceConfig>> getProperties() {
        List<Property<SecurityNamedServiceConfig>> result = new ArrayList<GeoServerDataProvider.Property<SecurityNamedServiceConfig>>();
        result = super.getProperties();
        return result;
    }

}
