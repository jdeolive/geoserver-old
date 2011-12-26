/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.web.security.config.details;

import org.apache.wicket.model.CompoundPropertyModel;
import org.geoserver.security.config.SecurityNamedServiceConfig;
import org.geoserver.security.jdbc.config.JdbcJndiSecurityServiceConfig;
import org.geoserver.security.jdbc.config.JdbcSecurityServiceConfig;
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
        if (configHelper.isNew()) {
            comp = new JDBCConnectFormComponent("jdbcConnectFormComponent",Mode.DYNAMIC);
        } else {
           if (configHelper.getConfig() instanceof JdbcJndiSecurityServiceConfig) {
               JdbcJndiSecurityServiceConfig jndiConfig = 
                       (JdbcJndiSecurityServiceConfig) configHelper.getConfig();
               comp = new JDBCConnectFormComponent("jdbcConnectFormComponent",Mode.DYNAMIC,jndiConfig.getJndiName());
           } else {
               JdbcSecurityServiceConfig jdbcConfig = 
                       (JdbcSecurityServiceConfig) configHelper.getConfig();               
               comp = new JDBCConnectFormComponent("jdbcConnectFormComponent",Mode.DYNAMIC,
                       jdbcConfig.getDriverClassName(),jdbcConfig.getConnectURL(),
                       jdbcConfig.getUserName(),jdbcConfig.getPassword()
                       );
           }
        }        
        addOrReplace(comp);        
    };
        
    
    @Override
    protected SecurityNamedServiceConfig createNewConfigObject() {
        return new JdbcRoleServiceConfigImpl();
    }
          
    @Override
    public void updateModel() {
        super.updateModel();
        comp.updateModel();
        comp.getModelObject();
        configHelper.toString();
    }
}
