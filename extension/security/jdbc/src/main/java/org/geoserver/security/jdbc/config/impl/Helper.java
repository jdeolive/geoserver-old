/* Copyright (c) 2001 - 2008 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.security.jdbc.config.impl;

public class Helper {

    static public JdbcJndiRoleServiceConfigImpl convertToJNDI(JdbcRoleServiceConfigImpl config, String jndiName) {
        JdbcJndiRoleServiceConfigImpl newConfig = new JdbcJndiRoleServiceConfigImpl();
        newConfig.setName(config.getName());
        newConfig.setClassName(config.getClassName());
        newConfig.setAdminRoleName(config.getAdminRoleName());
        newConfig.setPropertyFileNameDDL(config.getPropertyFileNameDDL());
        newConfig.setPropertyFileNameDML(config.getPropertyFileNameDML());
        newConfig.setJndiName(jndiName);
        return newConfig;
        
    }
    static public JdbcRoleServiceConfigImpl  convertToJdbc( JdbcJndiRoleServiceConfigImpl config,
            String driverClassName,String connectURL, String userName,String password) {
        JdbcRoleServiceConfigImpl newConfig = new JdbcRoleServiceConfigImpl();
        newConfig.setName(config.getName());
        newConfig.setClassName(config.getClassName());
        newConfig.setAdminRoleName(config.getAdminRoleName());
        newConfig.setPropertyFileNameDDL(config.getPropertyFileNameDDL());
        newConfig.setPropertyFileNameDML(config.getPropertyFileNameDML());
        newConfig.setConnectURL(connectURL);
        newConfig.setDriverClassName(driverClassName);
        newConfig.setUserName(userName);
        newConfig.setPassword(password);
        return newConfig;
    }

    static public JdbcJndiUserGroupServiceConfigImpl convertToJNDI(JdbcUserGroupServiceConfigImpl config,String jndiName) {
        JdbcJndiUserGroupServiceConfigImpl newConfig = new JdbcJndiUserGroupServiceConfigImpl();
        newConfig.setName(config.getName());
        newConfig.setClassName(config.getClassName());
        newConfig.setPasswordEncoderName(config.getPasswordEncoderName());
        newConfig.setPasswordPolicyName(config.getPasswordPolicyName());
        newConfig.setPropertyFileNameDDL(config.getPropertyFileNameDDL());
        newConfig.setPropertyFileNameDML(config.getPropertyFileNameDML());
        newConfig.setJndiName(jndiName);
        return newConfig;

    }
    static public JdbcUserGroupServiceConfigImpl  convertToJdbc( JdbcJndiUserGroupServiceConfigImpl config,
            String driverClassName,String connectURL, String userName,String password) {
        JdbcUserGroupServiceConfigImpl newConfig = new JdbcUserGroupServiceConfigImpl();
        newConfig.setName(config.getName());
        newConfig.setClassName(config.getClassName());
        newConfig.setPasswordEncoderName(config.getPasswordEncoderName());
        newConfig.setPasswordPolicyName(config.getPasswordPolicyName());
        newConfig.setPropertyFileNameDDL(config.getPropertyFileNameDDL());
        newConfig.setPropertyFileNameDML(config.getPropertyFileNameDML());
        newConfig.setConnectURL(connectURL);
        newConfig.setDriverClassName(driverClassName);
        newConfig.setUserName(userName);
        newConfig.setPassword(password);
        return newConfig;
    }

    
}
