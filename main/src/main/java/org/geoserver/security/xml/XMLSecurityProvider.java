package org.geoserver.security.xml;

import java.io.IOException;

import org.geoserver.config.util.XStreamPersister;
import org.geoserver.security.GeoServerSecurityProvider;
import org.geoserver.security.GeoserverRoleService;
import org.geoserver.security.GeoserverUserGroupService;
import org.geoserver.security.config.SecurityNamedServiceConfig;
import org.geoserver.security.config.impl.XMLFileBasedRoleServiceConfigImpl;
import org.geoserver.security.config.impl.XMLFileBasedUserGroupServiceConfigImpl;
import org.springframework.security.authentication.AuthenticationProvider;

/**
 * Security provider for default XML-based implementation.
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class XMLSecurityProvider extends GeoServerSecurityProvider {

    @Override
    public void configure(XStreamPersister xp) {
        super.configure(xp);
        xp.getXStream().alias("usergroupservice", XMLFileBasedUserGroupServiceConfigImpl.class);
        xp.getXStream().alias("roleservice", XMLFileBasedRoleServiceConfigImpl.class);
    }
    
    @Override
    public AuthenticationProvider createAuthProvider(SecurityNamedServiceConfig config) {
        return null;
    }

    @Override
    public Class<? extends GeoserverUserGroupService> getUserGroupServiceClass() {
        return XMLUserGroupService.class;
    }

    @Override
    public GeoserverUserGroupService createUserGroupService(SecurityNamedServiceConfig config) 
        throws IOException {
        return new XMLUserGroupService();
    }

    @Override
    public Class<? extends GeoserverRoleService> getRoleServiceClass() {
        return XMLRoleService.class;
    }

    @Override
    public GeoserverRoleService createRoleService(SecurityNamedServiceConfig config)
            throws IOException {
        return new XMLRoleService();
    }

}
