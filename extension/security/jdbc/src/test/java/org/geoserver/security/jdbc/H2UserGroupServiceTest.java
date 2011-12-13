/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.geoserver.security.jdbc;

import org.geoserver.security.GeoserverUserGroupService;

public class H2UserGroupServiceTest extends JDBCUserGroupServiceTest {

    @Override
    protected String getFixtureId() {
        return "h2";
    }


    public GeoserverUserGroupService createUserGroupService(String serviceName) throws Exception {        
        return JDBCTestSupport.createH2UserGroupService(getFixtureId(), getSecurityManager());
    }

}
