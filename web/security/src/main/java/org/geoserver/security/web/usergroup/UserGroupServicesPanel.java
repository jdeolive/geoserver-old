package org.geoserver.security.web.usergroup;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.geoserver.security.GeoServerUserGroupService;
import org.geoserver.security.config.SecurityUserGroupServiceConfig;
import org.geoserver.security.validation.SecurityConfigException;
import org.geoserver.security.validation.SecurityConfigValidator;
import org.geoserver.security.web.SecurityNamedServiceTabbedPanel;
import org.geoserver.security.web.SecurityNamedServicesPanel;

public class UserGroupServicesPanel extends SecurityNamedServicesPanel<SecurityUserGroupServiceConfig> {

    public UserGroupServicesPanel(String id) {
        super(id, new UserGroupServiceProvider());
    }

    @Override
    protected Class getServiceClass() {
        return GeoServerUserGroupService.class;
    }

    @Override
    protected void validateRemoveConfig(SecurityUserGroupServiceConfig config)
            throws SecurityConfigException {
        SecurityConfigValidator.getConfigurationValiator(GeoServerUserGroupService.class, 
            config.getClassName()).validateRemoveUserGroupService(config);
    }

    @Override
    protected void removeConfig(SecurityUserGroupServiceConfig config) throws Exception {
        getSecurityManager().removeUserGroupService(config);
    }
}
