/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.geoserver.security.xml;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.geoserver.security.GeoserverRoleService;
import org.geoserver.security.GeoserverRoleStore;
import org.geoserver.security.config.impl.XMLFileBasedRoleServiceConfigImpl;
import org.geoserver.security.event.RoleLoadedEvent;
import org.geoserver.security.event.RoleLoadedListener;
import org.geoserver.security.impl.AbstractRoleServiceTest;
import org.geoserver.security.impl.GeoserverRole;
import org.geoserver.security.impl.GeoserverUser;
import org.geoserver.security.impl.Util;

public class XMLRoleServiceTest extends AbstractRoleServiceTest {

    static Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.geoserver.security.xml");
    
    @Override
    public GeoserverRoleService createRoleService(String serviceName) throws IOException {
        return createRoleService(serviceName,XMLConstants.FILE_RR);
    }
    
    @Override
    protected void tearDownInternal() throws Exception {
        super.tearDownInternal();
        if (getSecurityManager().listRoleServices().contains("test")) {
            getSecurityManager().removeRoleService("test");
        }
    }
    
    protected GeoserverRoleService createRoleService(String serviceName, String xmlFileName) throws IOException {
         
        XMLFileBasedRoleServiceConfigImpl gaConfig = new XMLFileBasedRoleServiceConfigImpl();                 
        gaConfig.setName(serviceName);
        gaConfig.setClassName(XMLRoleService.class.getName());
        gaConfig.setCheckInterval(10);  // extreme short for testing 
        gaConfig.setFileName(xmlFileName);
        gaConfig.setLockingNeeded(true);
        gaConfig.setValidating(true);
        gaConfig.setAdminRoleName(GeoserverRole.ADMIN_ROLE.getAuthority());
        getSecurityManager().saveRoleService(gaConfig);
        return getSecurityManager().loadRoleService(serviceName);
    }

        
    
    public void testCopyFrom() {
        try {
            
            GeoserverRoleService service1 = createRoleService("copyFrom");
            GeoserverRoleService service2 = createRoleService("copyTo");
            GeoserverRoleStore store1 = createStore(service1);
            GeoserverRoleStore store2 = createStore(service2);                        
            
            store1.clear();
            checkEmpty(store1);        
            insertValues(store1);
            Util.copyFrom(store1, store2);
            store1.clear();
            checkEmpty(store1);
            checkValuesInserted(store2);
            
            
            
        } catch (IOException ex) {
            Assert.fail(ex.getMessage());
        }                
    }

    public void testDefault() {
        try {
            GeoserverRoleService service = createRoleService("default");
            
            assertEquals(1, service.getRoles().size());
            GeoserverRole admin_role= service.getRoleByName(
                    GeoserverRole.ADMIN_ROLE.getAuthority());
            assertEquals(0,service.getGroupNamesForRole(admin_role).size());
            assertEquals(1,service.getUserNamesForRole(admin_role).size());
            assertEquals(1, 
                    service.getRolesForUser(GeoserverUser.AdminName).size());
            assertTrue(service.getRolesForUser(GeoserverUser.AdminName).contains(admin_role));
            
            
        } catch (IOException ex) {
            Assert.fail(ex.getMessage());
        }                
    }
    
    public void testLocking() throws IOException {
        File xmlFile = File.createTempFile("roles", ".xml");
        FileUtils.copyURLToFile(getClass().getResource("rolesTemplate.xml"),xmlFile);
        GeoserverRoleService service1 =  
            createRoleService("locking1",xmlFile.getCanonicalPath());
        GeoserverRoleService service2 =  
            createRoleService("locking2",xmlFile.getCanonicalPath());
        GeoserverRoleStore store1= createStore(service1);
        GeoserverRoleStore store2= createStore(service2);
        
        
        GeoserverRole role_test1 = store1.createRoleObject("ROLE_TEST");
        GeoserverRole role_test2= store2.createRoleObject("ROLE_TEST");
        
       // obtain a lock
        store1.addRole(role_test1);
        boolean fail;
        String failMessage="Concurrent lock not allowed"; 
        fail=true;
        try {
            store2.clear();
        } catch (IOException ex) {
            fail=false;
        }
        if (fail) 
            Assert.fail(failMessage);
        
        // release lock
        store1.load();
        // get lock
        store2.addRole(role_test1);
        
        fail=true;
        try {
            store1.clear();
        } catch (IOException ex) {
            fail=false;
        }
        if (fail) 
            Assert.fail(failMessage);
        
        // release lock
        store2.store();
        store1.clear();
        store1.store();
        
        //// end of part one, now check all modifying methods

        // obtain a lock
        store1.addRole(role_test1);
        
        fail=true;
        try {
            store2.associateRoleToGroup(role_test2, "agroup");
        } catch (IOException ex) {
            try {
                store2.disAssociateRoleFromGroup(role_test2, "agroup");
            } catch (IOException e) {
                fail=false;
            }
        }
        if (fail) 
            Assert.fail(failMessage);
        
        fail=true;
        try {
            store2.associateRoleToUser(role_test2, "auser");
        } catch (IOException ex) {
            try {
                store2.disAssociateRoleFromUser(role_test2, "auser");
            } catch (IOException e) {
                fail=false;
            }
        }
        if (fail) 
            Assert.fail(failMessage);
        
        fail=true;
        try {
            store2.updateRole(role_test2);
        } catch (IOException ex) {
            try {
                store2.removeRole(role_test2);
            } catch (IOException ex1) {
                try {
                    store2.addRole(role_test2);
                } catch (IOException ex2) {
                    fail=false;
                }
            }
        }
        if (fail) 
            Assert.fail(failMessage);

        fail=true;
        try {
            store2.clear();
        } catch (IOException ex) {
            try {
                store2.store();
            } catch (IOException e) {
                fail=false;
            }
        }
        if (fail) 
            Assert.fail(failMessage);

        fail=true;
        try {
            store2.setParentRole(role_test1, null);
        } catch (IOException ex) {
            fail=false;
        }
        if (fail) 
            Assert.fail(failMessage);
                
    }
    
    public void testDynamicReload() throws Exception {
        File xmlFile = File.createTempFile("roles", ".xml");
        FileUtils.copyURLToFile(getClass().getResource("rolesTemplate.xml"),xmlFile);
        GeoserverRoleService service1 =  
            createRoleService("reload1",xmlFile.getCanonicalPath());
        GeoserverRoleService service2 =  
            createRoleService("reload2",xmlFile.getCanonicalPath());
        
        GeoserverRoleStore store1= createStore(service1);
        
        
        GeoserverRole role_test1 = store1.createRoleObject("ROLE_TEST1");
        
        checkEmpty(service1);
        checkEmpty(service2);
        
        // prepare for syncing
        
        RoleLoadedListener listener = new RoleLoadedListener() {
            
            @Override
            public void rolesChanged(RoleLoadedEvent event) {
                synchronized (this) {
                    this.notifyAll();
                }
                
            }
        }; 
        service2.registerRoleLoadedListener(listener);
        
        // modifiy store1
        store1.addRole(role_test1);
        store1.store();
        assertTrue(service1.getRoles().size()==1);
        
     // increment lastmodified adding a second manually, the test is too fast
        xmlFile.setLastModified(xmlFile.lastModified()+1000);  
        
        // wait for the listener to unlock when 
        // service 2 triggers a load event
        synchronized (listener) {
            listener.wait();            
        }
        
        // here comes the magic !!!
        assertTrue(service2.getRoles().size()==1);

    }
}
