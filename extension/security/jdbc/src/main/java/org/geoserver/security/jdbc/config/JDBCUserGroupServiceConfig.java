/* Copyright (c) 2001 - 2008 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.security.jdbc.config;

import org.geoserver.security.config.SecurityUserGroupServiceConfig;
import org.geoserver.security.jdbc.JDBCUserGroupService;

public class JDBCUserGroupServiceConfig extends JDBCSecurityServiceConfig 
    implements SecurityUserGroupServiceConfig {

    private static final long serialVersionUID = 1L;
    protected String passwordEncoderName;
    protected String passwordPolicyName;

    
    public JDBCUserGroupServiceConfig() {
        super();
        setPropertyFileNameDDL(JDBCUserGroupService.DEFAULT_DDL_FILE);
        setPropertyFileNameDML(JDBCUserGroupService.DEFAULT_DML_FILE);
    }
    
    public String getPasswordPolicyName() {
        return passwordPolicyName;
    }

    public void setPasswordPolicyName(String passwordPolicyName) {
        this.passwordPolicyName = passwordPolicyName;
    }

    @Override
    public String getPasswordEncoderName() {
        return passwordEncoderName;
    }

    @Override
    public void setPasswordEncoderName(String name) {
        passwordEncoderName=name;
    }

}
