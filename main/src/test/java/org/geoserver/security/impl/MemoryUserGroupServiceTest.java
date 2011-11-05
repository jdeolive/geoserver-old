/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.geoserver.security.impl;

import java.io.IOException;

import junit.framework.Assert;

import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.security.GeoServerSecurityManager;
import org.geoserver.security.GeoserverUserGroupService;
import org.geoserver.security.config.impl.MemoryUserGroupServiceConfigImpl;
import org.geoserver.security.password.GeoserverUserPBEPasswordEncoder;
import org.geoserver.security.password.PasswordValidator;

public class MemoryUserGroupServiceTest extends AbstractUserGroupServiceTest {


    @Override
    public GeoserverUserGroupService createUserGroupService(String name) throws IOException {
        MemoryUserGroupServiceConfigImpl config = new MemoryUserGroupServiceConfigImpl();         
        config.setName(name);        
        config.setLockingNeeded(false);
        config.setPasswordEncoderName(GeoserverUserPBEPasswordEncoder.PrototypeName);
        config.setPasswordPolicyName(PasswordValidator.DEFAULT_NAME);
        GeoserverUserGroupService service = new MemoryUserGroupService();
        service.setSecurityManager(GeoServerExtensions.bean(GeoServerSecurityManager.class));
        service.initializeFromConfig(config);        
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
