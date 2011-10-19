/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.geoserver.security.jdbc;

import java.io.IOException;

import org.geoserver.security.GeoserverGrantedAuthorityService;
import org.geoserver.security.GeoserverUserGroupService;

public class H2UserDetailsServiceTest extends JDBCUserDetailsServiceTest {

    
    @Override
    protected String getFixtureId() {
        return "h2";
    }
        
    @Override
    public GeoserverGrantedAuthorityService createGrantedAuthorityService(String serviceName) throws IOException {
        return JDBCTestSupport.createH2GrantedAuthorityService(getFixtureId(), getSecurityManager());
    }

    @Override
    public GeoserverUserGroupService createUserGroupService(String serviceName) throws IOException {
        return JDBCTestSupport.createH2UserGroupService(getFixtureId(), getSecurityManager());
    }

}
