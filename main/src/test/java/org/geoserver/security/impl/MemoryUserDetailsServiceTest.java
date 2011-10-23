/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.geoserver.security.impl;

import java.io.IOException;

import org.geoserver.security.GeoserverRoleService;
import org.geoserver.security.GeoserverUserGroupService;

public class MemoryUserDetailsServiceTest extends AbstractUserDetailsServiceTest {

    @Override
    public GeoserverRoleService createRoleService(String name) throws IOException {
        return new MemoryRoleService(name);
    }
    
    @Override
    public GeoserverUserGroupService createUserGroupService(String name) throws IOException {
        return new MemoryUserGroupService(name, getSecurityManager());
    }


}
