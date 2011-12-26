/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.web.security.config.details;

import org.apache.wicket.model.CompoundPropertyModel;
import org.geoserver.security.config.SecurityNamedServiceConfig;
import org.geoserver.security.jdbc.config.JdbcBaseSecurityServiceConfig;
import org.geoserver.security.jdbc.config.impl.JdbcRoleServiceConfigImpl;
import org.geoserver.web.security.JDBCConnectFormComponent;
import org.geoserver.web.security.JDBCConnectFormComponent.JDBCConnectConfig;
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
            JdbcBaseSecurityServiceConfig config = (JdbcBaseSecurityServiceConfig) configHelper.getConfig(); 
           if (config.isJndi()) {
               comp = new JDBCConnectFormComponent("jdbcConnectFormComponent",Mode.DYNAMIC,config.getJndiName());
           } else {
               comp = new JDBCConnectFormComponent("jdbcConnectFormComponent",Mode.DYNAMIC,
                       config.getDriverClassName(),config.getConnectURL(),
                       config.getUserName(),config.getPassword()
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
        JdbcBaseSecurityServiceConfig config = (JdbcBaseSecurityServiceConfig) configHelper.getConfig();
        JDBCConnectConfig c = comp.getModelObject();
        config.setJndiName(null);
        config.setDriverClassName(null);
        config.setConnectURL(null);
        config.setUserName(null);
        config.setPassword(null);
        
        config.setJndi(c.getType().equals(JDBCConnectConfig.TYPEJNDI));
        if (config.isJndi()) {
            config.setJndiName(c.getJndiName());
        } else {
            config.setDriverClassName(c.getDriverName());
            config.setConnectURL(c.getConnectURL());
            config.setUserName(c.getUsername());
            config.setPassword(c.getPassword());
        }
    }
}
