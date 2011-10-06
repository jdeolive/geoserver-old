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
import org.geoserver.security.GeoserverGrantedAuthorityService;
import org.geoserver.security.GeoserverGrantedAuthorityStore;
import org.geoserver.security.impl.AbstractGrantedAuthorityServiceTest;


public abstract class JDBCGrantedAuthorityServiceTest extends AbstractGrantedAuthorityServiceTest {

    static Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.geoserver.security.jdbc");
    
    protected abstract String getFixtureId();            
    
    

    @Override
    protected void tearDownInternal() throws Exception {
        super.tearDownInternal();
        if (store!=null) {
            JDBCGrantedAuthorityStore jdbcStore =(JDBCGrantedAuthorityStore)store;
            JDBCTestSupport.dropExistingTables(jdbcStore,jdbcStore.getConnection());
            store.store();
        }

    }

    @Override
    protected void setUpInternal() throws Exception {
        if (getTestData().isTestDataAvailable())
            super.setUpInternal();
    }

    
    
    public GeoserverGrantedAuthorityService createGrantedAuthorityService(String serviceName) throws IOException {    
        return JDBCTestSupport.createGrantedAuthorityService(getFixtureId(),(LiveDbmsDataSecurity)getTestData());        
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

    
    public void testGrantedAuthorityDatabaseSetup() {
        try {        
            JDBCGrantedAuthorityStore jdbcStore =  
                (JDBCGrantedAuthorityStore) store;
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
