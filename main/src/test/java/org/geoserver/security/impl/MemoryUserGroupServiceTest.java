/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.geoserver.security.impl;

import java.io.IOException;

import junit.framework.Assert;

import org.geoserver.security.GeoserverUserGroupService;

public class MemoryUserGroupServiceTest extends AbstractUserGroupServiceTest {


    @Override
    public GeoserverUserGroupService createUserGroupService(String name) throws IOException {
        MemoryUserGroupService service = new MemoryUserGroupService(name, getSecurityManager()); 
        return service;
    }

    @Override
    protected void tearDownInternal() throws Exception {
        super.tearDownInternal();
        store.clear();
    }



    public void testInsert() {
        super.testInsert();
        try {
            for (GeoserverUser user : store.getUsers()) {
                assertTrue(user.getClass()==MemoryGeoserverUser.class);
            }
            for (GeoserverUserGroup group : store.getUserGroups()) {
                assertTrue(group.getClass()==MemoryGeoserverUserGroup.class);
            }

        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }
}
