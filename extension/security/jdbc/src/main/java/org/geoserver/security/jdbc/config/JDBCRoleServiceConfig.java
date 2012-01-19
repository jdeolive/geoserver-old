/* Copyright (c) 2001 - 2008 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.security.jdbc.config;

import org.geoserver.security.config.SecurityRoleServiceConfig;
import org.geoserver.security.jdbc.JDBCRoleService;

public class JDBCRoleServiceConfig extends JDBCSecurityServiceConfig
        implements SecurityRoleServiceConfig {

    public JDBCRoleServiceConfig() {
        super();
        setPropertyFileNameDDL(JDBCRoleService.DEFAULT_DDL_FILE);
        setPropertyFileNameDML(JDBCRoleService.DEFAULT_DML_FILE);        
    }
    
    private static final long serialVersionUID = 1L;

    protected String adminRoleName;

    @Override
    public String getAdminRoleName() {
        return adminRoleName;
    }

    @Override
    public void setAdminRoleName(String name) {
        adminRoleName=name;
    }

}
