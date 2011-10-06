/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.geoserver.security.impl;

import java.io.IOException;

import org.geoserver.security.GeoserverGrantedAuthorityService;
import org.geoserver.security.GeoserverUserGroupService;

public class MemoryUserDetailsServiceTest extends AbstractUserDetailsServiceTest {

    @Override
    public GeoserverGrantedAuthorityService createGrantedAuthorityService(String name) throws IOException {
        return new MemoryGrantedAuthorityService(name);
    }
    
    @Override
    public GeoserverUserGroupService createUserGroupService(String name) throws IOException {
        return new MemoryUserGroupService(name);
    }


}
