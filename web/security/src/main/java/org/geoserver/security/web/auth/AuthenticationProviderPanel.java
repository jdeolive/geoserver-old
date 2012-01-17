package org.geoserver.security.web.auth;

import org.apache.wicket.model.IModel;
import org.geoserver.security.config.SecurityAuthProviderConfig;
import org.geoserver.security.web.SecurityNamedServicePanel;

/**
 * Base class for authentication panels.
 * 
 * @author Justin Deoliveira, OpenGeo
 *
 */
public class AuthenticationProviderPanel<T extends SecurityAuthProviderConfig> 
    extends SecurityNamedServicePanel<T> {

    public AuthenticationProviderPanel(String id, IModel<T> model) {
        super(id, model);
    }

    @Override
    public void doSave(T config) throws Exception {
       getSecurityManager().saveAuthenticationProvider(config);
    }

    @Override
    public void doLoad(T config) throws Exception {
        getSecurityManager().loadAuthenticationProvider(config.getName());
    }
}
