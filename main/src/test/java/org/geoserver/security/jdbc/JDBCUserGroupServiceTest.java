/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.geoserver.security.jdbc;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Logger;

import junit.framework.Assert;

import org.geoserver.data.test.TestData;
import org.geoserver.security.GeoserverUserGroupService;
import org.geoserver.security.GeoserverUserGroupStore;
import org.geoserver.security.impl.AbstractUserGroupServiceTest;


public abstract class JDBCUserGroupServiceTest extends AbstractUserGroupServiceTest {

    static Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.geoserver.security.jdbc");
    
    protected abstract String getFixtureId();
    

    @Override
    protected void tearDownInternal() throws Exception {
        super.tearDownInternal();
        if (store!=null) {
            JDBCUserGroupStore jdbcStore =(JDBCUserGroupStore)store;
            JDBCTestSupport.dropExistingTables(jdbcStore,jdbcStore.getConnection());
            store.store();
        }

    }

    @Override
    protected void setUpInternal() throws Exception {
        if (getTestData().isTestDataAvailable())
            super.setUpInternal();
    }

        
    public GeoserverUserGroupService createUserGroupService(String serviceName) throws IOException {
        
        return JDBCTestSupport.createUserGroupService(getFixtureId(), 
            (LiveDbmsDataSecurity)getTestData(), getSecurityManager());
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
    
    public void testUserGroupDatabaseSetup() {
        try {                        
            JDBCUserGroupStore jdbcStore = 
                (JDBCUserGroupStore) store;            
            jdbcStore.checkDDLStatements();
            jdbcStore.checkDMLStatements();
            jdbcStore.clear();
            jdbcStore.dropTables();
            jdbcStore.store();
        } catch (IOException ex) {
            Assert.fail(ex.getMessage());
        }
    }
        
    @Override
    protected TestData buildTestData() throws Exception {
        if ("h2".equalsIgnoreCase(getFixtureId()))
            return super.buildTestData();
        return new LiveDbmsDataSecurity(getFixtureId());
    }

    
    @Override
    protected boolean isJDBCTest() {
        return true;
    }


}
