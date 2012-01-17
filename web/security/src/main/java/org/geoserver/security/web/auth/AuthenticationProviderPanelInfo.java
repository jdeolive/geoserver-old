package org.geoserver.security.web.auth;

import org.geoserver.security.config.SecurityAuthProviderConfig;
import org.geoserver.security.web.SecurityNamedServicePanel;
import org.geoserver.security.web.SecurityNamedServicePanelInfo;

public class AuthenticationProviderPanelInfo
    <C extends SecurityAuthProviderConfig, T extends AuthenticationProviderPanel<C>>
    extends SecurityNamedServicePanelInfo<C,T>{
    
}
