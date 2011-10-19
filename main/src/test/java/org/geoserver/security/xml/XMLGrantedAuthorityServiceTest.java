/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.geoserver.security.xml;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.geoserver.security.GeoserverGrantedAuthorityService;
import org.geoserver.security.GeoserverGrantedAuthorityStore;
import org.geoserver.security.config.impl.XMLFileBasedSecurityServiceConfigImpl;
import org.geoserver.security.event.GrantedAuthorityLoadedEvent;
import org.geoserver.security.event.GrantedAuthorityLoadedListener;
import org.geoserver.security.impl.AbstractGrantedAuthorityServiceTest;
import org.geoserver.security.impl.GeoserverGrantedAuthority;
import org.geoserver.security.impl.GeoserverUser;
import org.geoserver.security.impl.Util;

import junit.framework.Assert;

public class XMLGrantedAuthorityServiceTest extends AbstractGrantedAuthorityServiceTest {

    static Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.geoserver.security.xml");
    
    @Override
    public GeoserverGrantedAuthorityService createGrantedAuthorityService(String serviceName) throws IOException {
        return createGrantedAuthorityService(serviceName,XMLConstants.FILE_RR);
    }
    
    @Override
    protected void tearDownInternal() throws Exception {
        super.tearDownInternal();
        if (getSecurityManager().listRoleServices().contains("test")) {
            getSecurityManager().removeRoleService("test");
        }
    }
    
    protected GeoserverGrantedAuthorityService createGrantedAuthorityService(String serviceName, String xmlFileName) throws IOException {
         
        XMLFileBasedSecurityServiceConfigImpl gaConfig = new XMLFileBasedSecurityServiceConfigImpl();                 
        gaConfig.setName(serviceName);
        gaConfig.setClassName(XMLGrantedAuthorityService.class.getName());
        gaConfig.setCheckInterval(10);  // extreme short for testing 
        gaConfig.setFileName(xmlFileName);
        gaConfig.setStateless(false);
        gaConfig.setValidating(true);
        getSecurityManager().saveRoleService(gaConfig);
        return getSecurityManager().loadRoleService(serviceName);
    }

        
    
    public void testCopyFrom() {
        try {
            
            GeoserverGrantedAuthorityService service1 = createGrantedAuthorityService("copyFrom");
            GeoserverGrantedAuthorityService service2 = createGrantedAuthorityService("copyTo");
            GeoserverGrantedAuthorityStore store1 = createStore(service1);
            GeoserverGrantedAuthorityStore store2 = createStore(service2);                        
            
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
            GeoserverGrantedAuthorityService service = createGrantedAuthorityService("default");
            
            assertEquals(1, service.getRoles().size());
            GeoserverGrantedAuthority admin_role= service.getGrantedAuthorityByName(
                    GeoserverGrantedAuthority.ADMIN_ROLE.getAuthority());
            assertEquals(0,service.getGroupNamesForRole(admin_role).size());
            assertEquals(1,service.getUserNamesForRole(admin_role).size());
            assertEquals(1, 
                    service.getRolesForUser(GeoserverUser.DEFAULT_ADMIN.getUsername()).size());
            assertTrue(service.getRolesForUser(GeoserverUser.DEFAULT_ADMIN.getUsername()).contains(admin_role));
            
            
        } catch (IOException ex) {
            Assert.fail(ex.getMessage());
        }                
    }
    
    public void testLocking() throws IOException {
        File xmlFile = File.createTempFile("roles", ".xml");
        FileUtils.copyURLToFile(getClass().getResource("rolesTemplate.xml"),xmlFile);
        GeoserverGrantedAuthorityService service1 =  
            createGrantedAuthorityService("locking1",xmlFile.getCanonicalPath());
        GeoserverGrantedAuthorityService service2 =  
            createGrantedAuthorityService("locking2",xmlFile.getCanonicalPath());
        GeoserverGrantedAuthorityStore store1= createStore(service1);
        GeoserverGrantedAuthorityStore store2= createStore(service2);
        
        
        GeoserverGrantedAuthority role_test1 = store1.createGrantedAuthorityObject("ROLE_TEST");
        GeoserverGrantedAuthority role_test2= store2.createGrantedAuthorityObject("ROLE_TEST");
        
       // obtain a lock
        store1.addGrantedAuthority(role_test1);
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
        store2.addGrantedAuthority(role_test1);
        
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
        store1.addGrantedAuthority(role_test1);
        
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
            store2.updateGrantedAuthority(role_test2);
        } catch (IOException ex) {
            try {
                store2.removeGrantedAuthority(role_test2);
            } catch (IOException ex1) {
                try {
                    store2.addGrantedAuthority(role_test2);
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
        GeoserverGrantedAuthorityService service1 =  
            createGrantedAuthorityService("reload1",xmlFile.getCanonicalPath());
        GeoserverGrantedAuthorityService service2 =  
            createGrantedAuthorityService("reload2",xmlFile.getCanonicalPath());
        
        GeoserverGrantedAuthorityStore store1= createStore(service1);
        
        
        GeoserverGrantedAuthority role_test1 = store1.createGrantedAuthorityObject("ROLE_TEST1");
        
        checkEmpty(service1);
        checkEmpty(service2);
        
        // prepare for syncing
        
        GrantedAuthorityLoadedListener listener = new GrantedAuthorityLoadedListener() {
            
            @Override
            public void grantedAuthoritiesChanged(GrantedAuthorityLoadedEvent event) {
                synchronized (this) {
                    this.notifyAll();
                }
                
            }
        }; 
        service2.registerGrantedAuthorityLoadedListener(listener);
        
        // modifiy store1
        store1.addGrantedAuthority(role_test1);
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
