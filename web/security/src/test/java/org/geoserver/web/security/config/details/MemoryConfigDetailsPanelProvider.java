/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.web.security.config.details;

import org.apache.wicket.model.CompoundPropertyModel;
import org.geoserver.security.impl.MemoryRoleService;
import org.geoserver.security.impl.MemoryUserGroupService;
import org.geoserver.security.impl.ReadOnlyRoleService;
import org.geoserver.security.impl.ReadOnlyUGService;
import org.geoserver.web.security.config.SecurityNamedConfigModelHelper;

/**
 * 
 * Default implementation of {@link NamedConfigDetailsPanelProvider}
 * 
 * @author christian
 *
 */
public class MemoryConfigDetailsPanelProvider implements NamedConfigDetailsPanelProvider {

    @Override
    public AbstractNamedConfigDetailsPanel getDetailsPanel(String className, String id,
            CompoundPropertyModel<SecurityNamedConfigModelHelper> model) {
        
        if (MemoryUserGroupService.class.getName().equals(className))
            return new MemoryUserGroupConfigDetailsPanel(id,model);
        if (ReadOnlyUGService.class.getName().equals(className))
            return new MemoryUserGroupConfigDetailsPanel(id,model);

        
        if (MemoryRoleService.class.getName().equals(className))
            return new MemoryRoleConfigDetailsPanel(id,model);
        if (ReadOnlyRoleService.class.getName().equals(className))
            return new MemoryRoleConfigDetailsPanel(id,model);
        
        return null;
    }

}
