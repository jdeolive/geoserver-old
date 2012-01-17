package org.geoserver.security.web.auth;

import org.geoserver.security.UsernamePasswordAuthenticationProvider;
import org.geoserver.security.config.UsernamePasswordAuthenticationProviderConfig;

public class UsernamePasswordAuthProviderPanelInfo extends AuthenticationProviderPanelInfo
    <UsernamePasswordAuthenticationProviderConfig,UsernamePasswordAuthProviderPanel> {

    public UsernamePasswordAuthProviderPanelInfo() {
        setComponentClass(UsernamePasswordAuthProviderPanel.class);
        setServiceClass(UsernamePasswordAuthenticationProvider.class);
        setServiceConfigClass(UsernamePasswordAuthenticationProviderConfig.class);
    }
}
