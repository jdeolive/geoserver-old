/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.web.security.config.details;

import org.apache.wicket.model.CompoundPropertyModel;
import org.geoserver.security.xml.XMLUserGroupService;
import org.geoserver.web.security.config.SecurityNamedConfigModelHelper;

/**
 * 
 * Default implementation of {@link NamedConfigDetailsPanelProvider}
 * 
 * @author christian
 *
 */
public class NamedConfigDetailsPanelProviderImpl implements NamedConfigDetailsPanelProvider {

    @Override
    public AbstractNamedConfigDetailsPanel getDetailsPanel(String className, String id,
            CompoundPropertyModel<SecurityNamedConfigModelHelper> model) {
        
        if (XMLUserGroupService.class.getName().equals(className))
            return new XMLUserGroupConfigDetailsPanel(id,model);
        
        return null;
    }

}
