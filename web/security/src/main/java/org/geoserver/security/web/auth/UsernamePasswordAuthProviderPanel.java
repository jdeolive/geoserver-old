package org.geoserver.security.web.auth;

import org.apache.wicket.model.IModel;
import org.geoserver.security.config.UsernamePasswordAuthenticationProviderConfig;
import org.geoserver.security.web.SecurityNamedServicePanel;
import org.geoserver.security.web.usergroup.UserGroupServiceChoice;

public class UsernamePasswordAuthProviderPanel 
    extends AuthenticationProviderPanel<UsernamePasswordAuthenticationProviderConfig> {

    public UsernamePasswordAuthProviderPanel(String id,
            IModel<UsernamePasswordAuthenticationProviderConfig> model) {
        super(id, model);

        add(new UserGroupServiceChoice("userGroupServiceName"));
    }
}
