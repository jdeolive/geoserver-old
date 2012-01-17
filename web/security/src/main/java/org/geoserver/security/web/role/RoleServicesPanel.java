package org.geoserver.security.web.role;

import org.geoserver.security.GeoServerRoleService;
import org.geoserver.security.config.SecurityRoleServiceConfig;
import org.geoserver.security.validation.SecurityConfigException;
import org.geoserver.security.validation.SecurityConfigValidator;
import org.geoserver.security.web.SecurityNamedServicesPanel;

public class RoleServicesPanel extends SecurityNamedServicesPanel<SecurityRoleServiceConfig> {

    public RoleServicesPanel(String id) {
        super(id, new RoleServiceProvider());
    }

    @Override
    protected Class getServiceClass() {
        return GeoServerRoleService.class;
    }

    @Override
    protected void validateRemoveConfig(SecurityRoleServiceConfig config)
            throws SecurityConfigException {
        SecurityConfigValidator.getConfigurationValiator(GeoServerRoleService.class, 
            config.getClassName()).validateRemoveRoleService(config);
    }

    @Override
    protected void removeConfig(SecurityRoleServiceConfig config) throws Exception {
        getSecurityManager().removeRoleService(config);
    }

}
