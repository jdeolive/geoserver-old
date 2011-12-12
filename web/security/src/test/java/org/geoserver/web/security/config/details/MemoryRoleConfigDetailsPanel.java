/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.web.security.config.details;

import org.apache.wicket.model.CompoundPropertyModel;
import org.geoserver.security.config.SecurityNamedServiceConfig;
import org.geoserver.security.config.impl.MemoryRoleServiceConfigImpl;
import org.geoserver.web.security.config.SecurityNamedConfigModelHelper;

/**
 * A form component that can be used for in memory configurations
 */
public class MemoryRoleConfigDetailsPanel extends AbstractRoleDetailsPanel{
    private static final long serialVersionUID = 1L;
    EncryptedFieldFormComponent enc;
    
    public MemoryRoleConfigDetailsPanel(String id, CompoundPropertyModel<SecurityNamedConfigModelHelper> model) {
        super(id,model);
    }

    @Override
    protected void initializeComponents() {
        super.initializeComponents();
        addOrReplace(enc=new EncryptedFieldFormComponent());
        
    };
        
    
    @Override
    protected SecurityNamedServiceConfig createNewConfigObject() {
        return new MemoryRoleServiceConfigImpl();
    }
 
    @Override
    public void updateModel() {
        super.updateModel();
        enc.updateModel();
    }
}
