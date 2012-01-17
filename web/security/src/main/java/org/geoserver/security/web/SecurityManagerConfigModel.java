package org.geoserver.security.web;

import org.apache.wicket.model.LoadableDetachableModel;
import org.geoserver.security.config.SecurityManagerConfig;
import org.geoserver.web.GeoServerApplication;

public class SecurityManagerConfigModel extends LoadableDetachableModel<SecurityManagerConfig> {

    @Override
    protected SecurityManagerConfig load() {
        return GeoServerApplication.get().getSecurityManager().getSecurityConfig();
    }

}
