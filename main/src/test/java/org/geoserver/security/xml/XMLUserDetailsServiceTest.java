/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.geoserver.security.xml;

import java.io.File;
import java.io.IOException;

import org.geoserver.data.test.LiveData;
import org.geoserver.data.test.TestData;
import org.geoserver.security.GeoserverGrantedAuthorityService;
import org.geoserver.security.GeoserverUserDetailsService;
import org.geoserver.security.GeoserverServiceFactory;
import org.geoserver.security.GeoserverUserGroupService;
import org.geoserver.security.config.impl.XMLFileBasedSecurityServiceConfigImpl;
import org.geoserver.security.impl.AbstractUserDetailsServiceTest;
import org.geoserver.security.impl.GeoserverGrantedAuthority;
import org.geoserver.security.impl.GeoserverUserDetailsServiceImpl;
import org.geoserver.security.impl.GeoserverUser;
import org.geoserver.security.impl.Util;

public class XMLUserDetailsServiceTest extends AbstractUserDetailsServiceTest {

    @Override
    public GeoserverUserGroupService createUserGroupService(String serviceName) throws IOException {
        XMLFileBasedSecurityServiceConfigImpl ugConfig = new XMLFileBasedSecurityServiceConfigImpl();                 
        ugConfig.setName(serviceName);
        ugConfig.setClassName(XMLUserGroupService.class.getName());
        ugConfig.setCheckInterval(10); 
        ugConfig.setFileName(XMLConstants.FILE_UR);
        ugConfig.setStateless(false);
        ugConfig.setValidating(true);
        Util.storeUserGroupServiceConfig(ugConfig);            

        GeoserverUserGroupService service =
            GeoserverServiceFactory.Singleton.getUserGroupService(serviceName);
        service.initializeFromConfig(ugConfig);
        return service;                
    }

    public GeoserverGrantedAuthorityService createGrantedAuthorityService(String serviceName) throws IOException {
        
        XMLFileBasedSecurityServiceConfigImpl gaConfig = new XMLFileBasedSecurityServiceConfigImpl();                 
        gaConfig.setName(serviceName);
        gaConfig.setClassName(XMLGrantedAuthorityService.class.getName());
        gaConfig.setCheckInterval(10); 
        gaConfig.setFileName(XMLConstants.FILE_RR);
        gaConfig.setStateless(false);
        gaConfig.setValidating(true);
        Util.storeGrantedAuthorityServiceConfig(gaConfig);            

        GeoserverGrantedAuthorityService service =
            GeoserverServiceFactory.Singleton.getGrantedAuthorityService(serviceName);
        service.initializeFromConfig(gaConfig);
        return service;
    }
                    

    public void testMigration() throws IOException {
        GeoserverUserGroupService userService = createUserGroupService(
                XMLUserGroupService.DEFAULT_NAME);
        GeoserverGrantedAuthorityService roleService = createGrantedAuthorityService(
                XMLGrantedAuthorityService.DEFAULT_NAME);
        GeoserverUserDetailsService service = GeoserverUserDetailsServiceImpl.get();
        service.setGrantedAuthorityService(roleService);
        service.setUserGroupService(userService);
        
        assertEquals(3,userService.getUsers().size());
        assertEquals(0,userService.getUserGroups().size());
        
        assertEquals(8,roleService.getRoles().size());
        
        GeoserverUser admin = (GeoserverUser) service.loadUserByUsername("admin");
        assertNotNull(admin);
        assertEquals("gs",admin.getPassword());
        assertTrue(admin.isEnabled());
        
        GeoserverUser wfs = (GeoserverUser) service.loadUserByUsername("wfs");
        assertNotNull(wfs);
        assertEquals("webFeatureService",wfs.getPassword());
        assertTrue(wfs.isEnabled());

        GeoserverUser disabledUser = (GeoserverUser) service.loadUserByUsername("disabledUser");
        assertNotNull(disabledUser);
        assertEquals("nah",disabledUser.getPassword());
        assertFalse(disabledUser.isEnabled());
        
        GeoserverGrantedAuthority role_admin = roleService.getGrantedAuthorityByName("ROLE_ADMINISTRATOR");
        assertNotNull(role_admin);
        GeoserverGrantedAuthority role_wfs_read = roleService.getGrantedAuthorityByName("ROLE_WFS_READ");
        assertNotNull(role_wfs_read);
        GeoserverGrantedAuthority role_wfs_write = roleService.getGrantedAuthorityByName("ROLE_WFS_WRITE");
        assertNotNull(role_wfs_write);
        GeoserverGrantedAuthority role_test = roleService.getGrantedAuthorityByName("ROLE_TEST");
        assertNotNull(role_test);
        assertNotNull(roleService.getGrantedAuthorityByName("NO_ONE"));
        assertNotNull(roleService.getGrantedAuthorityByName("TRUSTED_ROLE"));
        assertNotNull(roleService.getGrantedAuthorityByName("ROLE_SERVICE_1"));
        assertNotNull(roleService.getGrantedAuthorityByName("ROLE_SERVICE_2"));

        

        assertEquals(1,admin.getAuthorities().size());
        assertTrue(admin.getAuthorities().contains(role_admin));
        
        assertEquals(2,wfs.getAuthorities().size());
        assertTrue(wfs.getAuthorities().contains(role_wfs_read));
        assertTrue(wfs.getAuthorities().contains(role_wfs_write));

        assertEquals(1,disabledUser.getAuthorities().size());
        assertTrue(disabledUser.getAuthorities().contains(role_test));
        
        File userfile = new File(Util.getSecurityRoot(),"users.properties");
        assertFalse(userfile.exists());
        File userfileOld = new File(Util.getSecurityRoot(),"users.properties.old");
        assertTrue(userfileOld.exists());
        
        File roleXSD = new File (Util.getGrantedAuthorityNamedRoot(
                XMLGrantedAuthorityService.DEFAULT_NAME),XMLConstants.FILE_RR_SCHEMA);
        assertTrue(roleXSD.exists());

        File userXSD = new File (Util.getUserGroupNamedRoot(
                XMLUserGroupService.DEFAULT_NAME),XMLConstants.FILE_UR_SCHEMA);
        assertTrue(userXSD.exists());


    }

    @Override
    protected TestData buildTestData() throws Exception {
            
        File data = new File("./src/test/resources/migratedatadir");        
        return new LiveData(data);
    }
}
