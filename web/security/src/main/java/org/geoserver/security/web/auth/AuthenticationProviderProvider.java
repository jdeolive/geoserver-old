package org.geoserver.security.web.auth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.geoserver.security.config.SecurityAuthProviderConfig;
import org.geoserver.security.web.SecurityNamedServiceProvider;

public class AuthenticationProviderProvider 
    extends SecurityNamedServiceProvider<SecurityAuthProviderConfig> {

    @Override
    protected List<SecurityAuthProviderConfig> getItems() {
        List <SecurityAuthProviderConfig> result = new ArrayList<SecurityAuthProviderConfig>();
        try {
            for (String name : getSecurityManager().listAuthenticationProviders()) {
                result.add(getSecurityManager().loadAuthenticationProviderConfig(name));
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return result;
    }

}
