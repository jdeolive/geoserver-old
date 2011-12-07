/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.web.security.config.details;

import org.apache.wicket.model.CompoundPropertyModel;
import org.geoserver.security.config.SecurityNamedServiceConfig;
import org.geoserver.security.jdbc.config.impl.JdbcRoleServiceConfigImpl;
import org.geoserver.web.security.JDBCConnectFormComponent;
import org.geoserver.web.security.JDBCConnectFormComponent.Mode;
import org.geoserver.web.security.config.SecurityNamedConfigModelHelper;

/**
 * A form component that can be used for xml configurations
 */
public class JDBCRoleConfigDetailsPanel extends AbstractRoleDetailsPanel{
    private static final long serialVersionUID = 1L;
    JDBCConnectFormComponent comp;
    
    public JDBCRoleConfigDetailsPanel(String id, CompoundPropertyModel<SecurityNamedConfigModelHelper> model) {
        super(id,model);
    }

    @Override
    protected void initializeComponents() {
        super.initializeComponents();
        comp = new JDBCConnectFormComponent("jdbcConnectFormComponent",Mode.DYNAMIC);
        addOrReplace(comp);        
    };
        
    
    @Override
    protected SecurityNamedServiceConfig createNewConfigObject() {
        return new JdbcRoleServiceConfigImpl();
    }
                            
}
