/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.geoserver.security.jdbc;

import org.geoserver.security.config.SecurityRoleServiceConfig;
import org.geoserver.security.config.SecurityUserGroupServiceConfig;
import org.geoserver.security.config.validation.SecurityConfigException;
import org.geoserver.security.config.validation.SecurityConfigValidationErrors;
import org.geoserver.security.config.validation.SecurityConfigValidator;
import org.geoserver.security.jdbc.config.JdbcBaseSecurityServiceConfig;
import org.geoserver.security.jdbc.config.impl.JdbcJndiRoleServiceConfigImpl;
import org.geoserver.security.jdbc.config.impl.JdbcJndiSecurityServiceConfigImpl;
import org.geoserver.security.jdbc.config.impl.JdbcJndiUserGroupServiceConfigImpl;
import org.geoserver.security.jdbc.config.impl.JdbcRoleServiceConfigImpl;
import org.geoserver.security.jdbc.config.impl.JdbcSecurityServiceConfigImpl;
import org.geoserver.security.jdbc.config.impl.JdbcUserGroupServiceConfigImpl;

public class JdbcSecurityConfigValidator extends SecurityConfigValidator {

    @Override
    public void validate(SecurityRoleServiceConfig config) throws SecurityConfigException {
        super.validate(config);
        JdbcBaseSecurityServiceConfig jdbcConfig = (JdbcBaseSecurityServiceConfig) config;
        if (config instanceof JdbcJndiRoleServiceConfigImpl)
            validateJNDI((JdbcJndiRoleServiceConfigImpl)jdbcConfig);
        if (config instanceof JdbcRoleServiceConfigImpl)
            validateJDBC((JdbcRoleServiceConfigImpl)jdbcConfig);
    }
    
    @Override
    public void validate(SecurityUserGroupServiceConfig config)
            throws SecurityConfigException {
        super.validate(config);
        JdbcBaseSecurityServiceConfig jdbcConfig = (JdbcBaseSecurityServiceConfig) config;
        if (config instanceof JdbcJndiUserGroupServiceConfigImpl)
            validateJNDI((JdbcJndiUserGroupServiceConfigImpl)jdbcConfig);
        if (config instanceof JdbcUserGroupServiceConfigImpl)
            validateJDBC((JdbcUserGroupServiceConfigImpl)jdbcConfig);
    }
    
    protected void validateJNDI(JdbcJndiSecurityServiceConfigImpl config) throws SecurityConfigException {
        if (config.getJndiName()==null || config.getJndiName().length()==0)
            throw createSecurityException(JdbcSecurityConfigValidationErrors.SEC_ERR_210);
    }
    
    protected void validateJDBC(JdbcSecurityServiceConfigImpl config) throws SecurityConfigException {
        if (config.getDriverClassName()==null || config.getDriverClassName().length()==0)
            throw createSecurityException(JdbcSecurityConfigValidationErrors.SEC_ERR_200);
        if (config.getUserName()==null || config.getUserName().length()==0)
            throw createSecurityException(JdbcSecurityConfigValidationErrors.SEC_ERR_201);
        if (config.getConnectURL()==null || config.getConnectURL().length()==0)
            throw createSecurityException(JdbcSecurityConfigValidationErrors.SEC_ERR_202);

        try {
            Class.forName(config.getDriverClassName());
        } catch (ClassNotFoundException e) {
            throw createSecurityException(JdbcSecurityConfigValidationErrors.SEC_ERR_203,
                    config.getDriverClassName());
        }

    }
   
    @Override
    protected SecurityConfigValidationErrors getSecurityErrors() {
        return new JdbcSecurityConfigValidationErrors();
    }

}
