/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.geoserver.security.impl;

import java.io.IOException;

import junit.framework.Assert;

import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.security.GeoServerSecurityManager;
import org.geoserver.security.GeoserverRoleService;
import org.geoserver.security.config.impl.MemoryRoleServiceConfigImpl;

public class MemoryRoleServiceTest extends AbstractRoleServiceTest {
  

    @Override
    public GeoserverRoleService createRoleService(String name) throws IOException {
        MemoryRoleServiceConfigImpl config = new MemoryRoleServiceConfigImpl();
        config.setName(name);
        config.setAdminRoleName(GeoserverRole.ADMIN_ROLE.getAuthority());
        GeoserverRoleService service = new MemoryRoleService();
        service.initializeFromConfig(config);
        service.setSecurityManager(GeoServerExtensions.bean(GeoServerSecurityManager.class));
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
            for (GeoserverRole role : store.getRoles()) {
                assertTrue(role.getClass()==MemoryGeoserverRole.class);
            }
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }
}
