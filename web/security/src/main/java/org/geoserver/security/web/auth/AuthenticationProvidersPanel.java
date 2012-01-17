package org.geoserver.security.web.auth;

import org.geoserver.security.GeoServerAuthenticationProvider;
import org.geoserver.security.config.SecurityAuthProviderConfig;
import org.geoserver.security.validation.SecurityConfigException;
import org.geoserver.security.validation.SecurityConfigValidator;
import org.geoserver.security.web.SecurityNamedServicesPanel;
import org.springframework.security.authentication.AuthenticationProvider;

public class AuthenticationProvidersPanel extends SecurityNamedServicesPanel<SecurityAuthProviderConfig> {

    public AuthenticationProvidersPanel(String id) {
        super(id, new AuthenticationProviderProvider());
    }

    @Override
    protected Class getServiceClass() {
        //return GeoServerAuthenticationProvider.class;
        return AuthenticationProvider.class;
    }

    @Override
    protected void validateRemoveConfig(SecurityAuthProviderConfig config)
            throws SecurityConfigException {
        SecurityConfigValidator.getConfigurationValiator(GeoServerAuthenticationProvider.class, 
            config.getClassName()).validateRemoveAuthProvider(config);
    }

    @Override
    protected void removeConfig(SecurityAuthProviderConfig config) throws Exception {
        getSecurityManager().removeAuthenticationProvider(config);
    }

}
