/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.geoserver.security.jdbc;

import java.io.IOException;
import java.sql.SQLException;

import org.geoserver.data.test.TestData;
import org.geoserver.security.GeoserverGrantedAuthorityService;
import org.geoserver.security.GeoserverGrantedAuthorityStore;
import org.geoserver.security.GeoserverUserGroupService;
import org.geoserver.security.GeoserverUserGroupStore;
import org.geoserver.security.impl.AbstractUserDetailsServiceTest;

public abstract class JDBCUserDetailsServiceTest extends AbstractUserDetailsServiceTest {

    protected abstract String getFixtureId();
        
    @Override
    public GeoserverUserGroupService createUserGroupService(String serviceName) throws IOException {
        
        return JDBCTestSupport.createUserGroupService(getFixtureId(), 
            (LiveDbmsDataSecurity)getTestData(), getSecurityManager());
    }

    @Override
    public GeoserverGrantedAuthorityService createGrantedAuthorityService(String serviceName) throws IOException {    
        return JDBCTestSupport.createGrantedAuthorityService(getFixtureId(),
            (LiveDbmsDataSecurity)getTestData(), getSecurityManager());
    }

    @Override
    public GeoserverGrantedAuthorityStore createStore(GeoserverGrantedAuthorityService service) throws IOException {
        JDBCGrantedAuthorityStore store = 
            (JDBCGrantedAuthorityStore) super.createStore(service);
        try {
            JDBCTestSupport.dropExistingTables(store,store.getConnection());
        } catch (SQLException e) {
            throw new IOException(e);
        }
        store.createTables();
        store.store();
        
        return store;        
    }

    @Override
    public GeoserverUserGroupStore createStore(GeoserverUserGroupService service) throws IOException {
        JDBCUserGroupStore store = 
            (JDBCUserGroupStore) super.createStore(service);
        try {
            JDBCTestSupport.dropExistingTables(store,store.getConnection());
        } catch (SQLException e) {
            throw new IOException(e);
        }
        store.createTables();
        store.store();
        return store;        
    }
    
    
    @Override
    protected void tearDownInternal() throws Exception {
        super.tearDownInternal();
        if (roleStore!=null) {
            JDBCGrantedAuthorityStore jdbcStore1 =(JDBCGrantedAuthorityStore) roleStore;
            JDBCTestSupport.dropExistingTables(jdbcStore1,jdbcStore1.getConnection());
            roleStore.store();
        }
        
        if (usergroupStore!=null) {
            JDBCUserGroupStore jdbcStore2 =(JDBCUserGroupStore) usergroupStore;
            JDBCTestSupport.dropExistingTables(jdbcStore2,jdbcStore2.getConnection());
            usergroupStore.store();
        }
    }

    @Override
    protected void setUpInternal() throws Exception {
        if (getTestData().isTestDataAvailable())
            super.setUpInternal();
    }

    
    @Override
    protected boolean isJDBCTest() {
        return true;
    }

    @Override
    protected TestData buildTestData() throws Exception {
        if ("h2".equalsIgnoreCase(getFixtureId()))
            return super.buildTestData();
        return new LiveDbmsDataSecurity(getFixtureId());
    }

}
