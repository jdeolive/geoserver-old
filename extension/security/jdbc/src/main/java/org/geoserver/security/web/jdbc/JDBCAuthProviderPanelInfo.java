package org.geoserver.security.web.jdbc;

import org.geoserver.security.jdbc.JDBCConnectAuthProvider;
import org.geoserver.security.jdbc.config.JDBCConnectAuthProviderConfig;
import org.geoserver.security.web.auth.AuthenticationProviderPanelInfo;

public class JDBCAuthProviderPanelInfo 
    extends AuthenticationProviderPanelInfo<JDBCConnectAuthProviderConfig, JDBCAuthProviderPanel>{

    public JDBCAuthProviderPanelInfo() {
        setComponentClass(JDBCAuthProviderPanel.class);
        setServiceConfigClass(JDBCConnectAuthProviderConfig.class);
        setServiceClass(JDBCConnectAuthProvider.class);
    }
}
