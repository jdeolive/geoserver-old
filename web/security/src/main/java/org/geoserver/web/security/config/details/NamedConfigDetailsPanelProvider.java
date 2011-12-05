/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.web.security.config.details;

import org.apache.wicket.model.CompoundPropertyModel;
import org.geoserver.security.config.SecurityNamedServiceConfig;
import org.geoserver.web.security.config.SecurityNamedConfigModelHelper;

/**
 * Extension point for getting a details panel for a
 * specific security implementation
 * 
 * @author christian
 *
 */
public interface NamedConfigDetailsPanelProvider {

    /**
     * @param className as stored in {@link SecurityNamedServiceConfig#getClassName()}
     * @param wicket id
     * @param wicket model  
     * @return <code>null</code> or a panel object if this provider 
     * can create a panel object for className
     */
    public AbstractNamedConfigDetailsPanel getDetailsPanel(String className, 
            String id, CompoundPropertyModel<SecurityNamedConfigModelHelper> model);
}
