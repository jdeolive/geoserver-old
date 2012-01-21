/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.web.security.config.details;

import org.apache.wicket.model.CompoundPropertyModel;
import org.geoserver.security.config.BaseSecurityNamedServiceConfig;
import org.geoserver.security.config.SecurityNamedServiceConfig;
import org.geoserver.web.security.config.SecurityNamedConfigModelHelper;

/**
 * A form component without any details
 * 
 * @author christian
 *
 */
public class NamedConfigDetailsEmptyPanel extends AbstractNamedConfigDetailsPanel {
    private static final long serialVersionUID = 1L;
    
    public NamedConfigDetailsEmptyPanel(String id, CompoundPropertyModel<SecurityNamedConfigModelHelper> model) {
        super(id,model);
    }

    @Override
    protected void initializeComponents() {
        return;
    }

    @Override
    protected SecurityNamedServiceConfig createNewConfigObject() {
        return new BaseSecurityNamedServiceConfig();
    }
}
