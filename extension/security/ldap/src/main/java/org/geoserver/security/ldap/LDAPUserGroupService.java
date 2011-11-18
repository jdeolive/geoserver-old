/* Copyright (c) 2001 - 2012 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.security.ldap;

import java.io.IOException;

import org.geoserver.security.config.SecurityNamedServiceConfig;
import org.geoserver.security.impl.AbstractUserGroupService;
import org.geoserver.security.impl.GeoServerUser;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;

public class LDAPUserGroupService extends AbstractUserGroupService {

    LdapAuthoritiesPopulator authPopulator;
    
    @Override
    public void initializeFromConfig(SecurityNamedServiceConfig config) throws IOException {
    }

    @Override
    public GeoServerUser getUserByUsername(String username) throws IOException {
        return new GeoServerUser(username);
    }

    @Override
    protected void deserialize() throws IOException {
    }
}
