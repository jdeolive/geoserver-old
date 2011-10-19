/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.geoserver.security.xml;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.geoserver.security.GeoserverUserGroupService;
import org.geoserver.security.GeoserverUserGroupStore;
import org.geoserver.security.config.impl.XMLFileBasedSecurityServiceConfigImpl;
import org.geoserver.security.event.UserGroupLoadedEvent;
import org.geoserver.security.event.UserGroupLoadedListener;
import org.geoserver.security.impl.AbstractUserGroupServiceTest;
import org.geoserver.security.impl.GeoserverUser;
import org.geoserver.security.impl.GeoserverUserGroup;
import org.geoserver.security.impl.Util;

import junit.framework.Assert;

public class XMLUserGroupServiceTest extends AbstractUserGroupServiceTest {

    static Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.geoserver.security.xml");

    @Override
    public GeoserverUserGroupService createUserGroupService(String serviceName) throws IOException {
        return createUserGroupService(serviceName,XMLConstants.FILE_UR); 
    }
    
    @Override
    protected void tearDownInternal() throws Exception {
        super.tearDownInternal();
        if (getSecurityManager().listUserGroupServices().contains("test")) {
            getSecurityManager().removeUserGroupService("test");
        }
    }

    
    protected GeoserverUserGroupService createUserGroupService(String serviceName,String xmlFileName) throws IOException {
        XMLFileBasedSecurityServiceConfigImpl ugConfig = new XMLFileBasedSecurityServiceConfigImpl();                 
        ugConfig.setName(serviceName);
        ugConfig.setClassName(XMLUserGroupService.class.getName());
        ugConfig.setCheckInterval(10); 
        ugConfig.setFileName(xmlFileName);
        ugConfig.setStateless(false);
        ugConfig.setValidating(true);
        getSecurityManager().saveUserGroupService(ugConfig);
        
        GeoserverUserGroupService service = getSecurityManager().loadUserGroupService(serviceName);
        service.initializeFromConfig(ugConfig); // create files
        return service;                
    }
    
        
    public void testCopyFrom() {
        try {
    
            GeoserverUserGroupService service1 = createUserGroupService("copyFrom");
            GeoserverUserGroupService service2 = createUserGroupService("copyTo");
            GeoserverUserGroupStore store1 = createStore(service1);
            GeoserverUserGroupStore store2 = createStore(service2);                        

            
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
            GeoserverUserGroupService service = createUserGroupService(
                    XMLUserGroupService.DEFAULT_NAME);
            
            assertEquals(1, service.getUsers().size());
            assertEquals(0, service.getUserGroups().size());
                        
            GeoserverUser admin= service.getUserByUsername(GeoserverUser.DEFAULT_ADMIN.getUsername());
            assertNotNull(admin);
            assertEquals(GeoserverUser.DEFAULT_ADMIN.isEnabled(),admin.isEnabled());
            assertEquals(GeoserverUser.DEFAULT_ADMIN.getPassword(),admin.getPassword());
            assertEquals(GeoserverUser.DEFAULT_ADMIN.getProperties(),admin.getProperties());
            
            assertEquals(0, service.getGroupsForUser(admin).size());
                        
        } catch (IOException ex) {
            Assert.fail(ex.getMessage());
        }                
    }

    public void testLocking() throws IOException {
        File xmlFile = File.createTempFile("users", ".xml");
        FileUtils.copyURLToFile(getClass().getResource("usersTemplate.xml"),xmlFile);
        GeoserverUserGroupService service1 =  
            createUserGroupService("locking1",xmlFile.getCanonicalPath());
        GeoserverUserGroupService service2 =  
            createUserGroupService("locking2",xmlFile.getCanonicalPath());
        GeoserverUserGroupStore store1= createStore(service1);
        GeoserverUserGroupStore store2= createStore(service2);
        
        
        GeoserverUser user = store1.createUserObject("user", "ps", true);
        GeoserverUserGroup group = store2.createGroupObject("group", true);
        
       // obtain a lock
        store1.addUser(user);
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
        store2.addUser(user);
        
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
        store1.addUser(user);
        
        fail=true;
        try {
            store2.associateUserToGroup(user, group);
        } catch (IOException ex) {
            try {
                store2.disAssociateUserFromGroup(user, group);
            } catch (IOException e) {
                fail=false;
            }
        }
        if (fail) 
            Assert.fail(failMessage);
        
        
        fail=true;
        try {
            store2.updateUser(user);
        } catch (IOException ex) {
            try {
                store2.removeUser(user);
            } catch (IOException ex1) {
                try {
                    store2.addUser(user);
                } catch (IOException ex2) {
                    fail=false;
                }
            }
        }
        if (fail) 
            Assert.fail(failMessage);
        
        fail=true;
        try {
            store2.updateGroup(group);
        } catch (IOException ex) {
            try {
                store2.removeGroup(group);
            } catch (IOException ex1) {
                try {
                    store2.addGroup(group);
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
                        
    }

    public void testDynamicReload() throws Exception {
        File xmlFile = File.createTempFile("users", ".xml");
        FileUtils.copyURLToFile(getClass().getResource("usersTemplate.xml"),xmlFile);
        GeoserverUserGroupService service1 =  
            createUserGroupService("reload1",xmlFile.getCanonicalPath());
        GeoserverUserGroupService service2 =  
            createUserGroupService("reload2",xmlFile.getCanonicalPath());
        
        GeoserverUserGroupStore store1= createStore(service1);
        
        
        GeoserverUserGroup group = store1.createGroupObject("group",true);
        
        checkEmpty(service1);
        checkEmpty(service2);
        
        // prepare for syncing
        
        UserGroupLoadedListener listener = new UserGroupLoadedListener() {
            
            @Override
            public void usersAndGroupsChanged(UserGroupLoadedEvent event) {
                synchronized (this) {
                    this.notifyAll();
                }
            }
        };
            
        service2.registerUserGroupLoadedListener(listener);
        
        // modifiy store1
        store1.addGroup(group);
        store1.store();
        assertTrue(service1.getUserGroups().size()==1);
        
     // increment lastmodified adding a second manually, the test is too fast
        xmlFile.setLastModified(xmlFile.lastModified()+2000);  
        
        // wait for the listener to unlock when 
        // service 2 triggers a load event
        synchronized (listener) {
            listener.wait();            
        }
        
        // here comes the magic !!!
        assertTrue(service2.getUserGroups().size()==1);

    }

}
