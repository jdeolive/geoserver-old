package org.geoserver.security.validation;

import java.io.IOException;
import java.util.logging.Logger;

import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.security.GeoServerSecurityManager;
import org.geoserver.security.GeoserverRoleService;
import org.geoserver.security.GeoserverUserGroupService;
import org.geoserver.security.GeoserverUserGroupStore;
import org.geoserver.security.config.impl.MemoryRoleServiceConfigImpl;
import org.geoserver.security.config.impl.MemoryUserGroupServiceConfigImpl;
import org.geoserver.security.impl.GeoserverRole;
import org.geoserver.security.impl.MemoryRoleService;
import org.geoserver.security.impl.MemoryUserGroupService;
import org.geoserver.security.password.GeoserverUserPBEPasswordEncoder;
import org.geoserver.security.password.PasswordValidator;
import org.geoserver.test.GeoServerTestSupport;
import org.geotools.util.logging.Logging;

public class RoleStoreValidationWrapperTest extends GeoServerTestSupport {

    
    static protected Logger LOGGER = Logging.getLogger("org.geoserver.security");
    
    public RoleStoreValidationWrapper createStore(String name, GeoserverUserGroupService ...services) throws IOException {
        MemoryRoleServiceConfigImpl config = new MemoryRoleServiceConfigImpl();
        config.setName(name);
        config.setAdminRoleName(GeoserverRole.ADMIN_ROLE.getAuthority());
        GeoserverRoleService service = new MemoryRoleService();
        service.initializeFromConfig(config);
        service.setSecurityManager(GeoServerExtensions.bean(GeoServerSecurityManager.class));
        return new RoleStoreValidationWrapper(service.createStore(),services);
    }

    
    
    protected GeoserverUserGroupStore createUGStore(String name) throws IOException {
        MemoryUserGroupServiceConfigImpl config = new MemoryUserGroupServiceConfigImpl();         
        config.setName(name);        
        config.setPasswordEncoderName(GeoserverUserPBEPasswordEncoder.PrototypeName);
        config.setPasswordPolicyName(PasswordValidator.DEFAULT_NAME);
        GeoserverUserGroupService service = new MemoryUserGroupService();
        service.setSecurityManager(GeoServerExtensions.bean(GeoServerSecurityManager.class));
        service.initializeFromConfig(config);        
        return service.createStore();
    }

    protected void assertSecurityException (IOException ex, String id, Object... params) {
        assertTrue (ex.getCause() instanceof AbstractSecurityException);
        AbstractSecurityException secEx = (AbstractSecurityException) ex.getCause(); 
        assertEquals(id,secEx.getErrorId());
        for (int i = 0; i <  params.length ;i++) {
            assertEquals(params[i], secEx.getArgs()[i]);
        }
    }
    
    public void testRoleStoreWrapper() throws Exception {
        boolean failed;
        RoleStoreValidationWrapper store = createStore("test");
                        
        failed=false;
        try { 
            store.addRole(store.createRoleObject(""));            
        } catch (IOException ex) {
            assertSecurityException(ex, RoleServiceValidationErrors.ROLE_ERR_01);
            failed=true;
        }
        assertTrue(failed);
        
        store.addRole(store.createRoleObject("role1"));
        assertEquals(1, store.getRoles().size());
        GeoserverRole role1 = store.getRoleByName("role1");
        
        failed=false;
        try { 
            store.addRole(role1);            
        } catch (IOException ex) {
            assertSecurityException(ex, RoleServiceValidationErrors.ROLE_ERR_03,"role1");
            failed=true;
        }
        assertTrue(failed);
        
        failed=false;
        try { 
            store.updateRole(store.createRoleObject("xxx"));            
        } catch (IOException ex) {
            assertSecurityException(ex, RoleServiceValidationErrors.ROLE_ERR_02,"xxx");
            failed=true;
        }
        assertTrue(failed);
        
        store.addRole(store.createRoleObject("parent1"));
        GeoserverRole parent1 = store.getRoleByName("parent1");
        assertNotNull(parent1);
        failed=false;
        try { 
            store.setParentRole(role1,store.createRoleObject("xxx"));            
        } catch (IOException ex) {
            assertSecurityException(ex, RoleServiceValidationErrors.ROLE_ERR_02,"xxx");
            failed=true;
        }
        assertTrue(failed);

        store.setParentRole(role1,parent1);
        store.setParentRole(role1,null);
        
        failed=false;
        try { 
            store.associateRoleToGroup(role1, "");            
        } catch (IOException ex) {
            assertSecurityException(ex, RoleServiceValidationErrors.ROLE_ERR_05);
            failed=true;
        }
        assertTrue(failed);

        failed=false;
        try { 
            store.disAssociateRoleFromGroup(role1, "");            
        } catch (IOException ex) {
            assertSecurityException(ex, RoleServiceValidationErrors.ROLE_ERR_05);
            failed=true;
        }
        assertTrue(failed);
        
        failed=false;
        try { 
            store.associateRoleToUser(role1, "");            
        } catch (IOException ex) {
            assertSecurityException(ex, RoleServiceValidationErrors.ROLE_ERR_04);
            failed=true;
        }
        assertTrue(failed);

        failed=false;
        try { 
            store.disAssociateRoleFromUser(role1, "");            
        } catch (IOException ex) {
            assertSecurityException(ex, RoleServiceValidationErrors.ROLE_ERR_04);
            failed=true;
        }
        assertTrue(failed);

        store.associateRoleToGroup(role1, "group1");
        store.associateRoleToUser(role1, "user1");

        failed=false;
        try { 
            store.getRolesForUser(null);            
        } catch (IOException ex) {
            assertSecurityException(ex, RoleServiceValidationErrors.ROLE_ERR_04);
            failed=true;
        }
        assertTrue(failed);
        
        failed=false;
        try { 
            store.getRolesForGroup(null);            
        } catch (IOException ex) {
            assertSecurityException(ex, RoleServiceValidationErrors.ROLE_ERR_05);
            failed=true;
        }
        assertTrue(failed);

        assertEquals(1,store.getRolesForGroup("group1").size());
        assertEquals(1,store.getRolesForUser("user1").size());

        store.disAssociateRoleFromGroup(role1, "group1");
        store.disAssociateRoleFromUser(role1, "user1");

        store.removeRole(role1);
        store.removeRole(parent1);
    }
    
    public void testRoleStoreWrapperWithUGServices() throws Exception {
        boolean failed;
        GeoserverUserGroupStore ugStore1=createUGStore("test1");
        ugStore1.addUser(ugStore1.createUserObject("user1", "abc", true));
        ugStore1.addGroup(ugStore1.createGroupObject("group1", true));
        ugStore1.store();
        
        GeoserverUserGroupStore ugStore2=createUGStore("test2");
        ugStore2.addUser(ugStore1.createUserObject("user2", "abc", true));
        ugStore2.addGroup(ugStore1.createGroupObject("group2", true));
        ugStore2.store();
        
        RoleStoreValidationWrapper store = createStore("test",ugStore1,ugStore2);
        GeoserverRole role1 = store.createRoleObject("role1");
        store.addRole(role1);
        store.store();
        
        store.associateRoleToGroup(role1, "group1");
        store.associateRoleToGroup(role1, "group2");
        failed=false;
        try { 
            store.associateRoleToGroup(role1, "group3");            
        } catch (IOException ex) {
            assertSecurityException(ex, RoleServiceValidationErrors.ROLE_ERR_07,"group3");
            failed=true;
        }
        assertTrue(failed);
        
        store.associateRoleToUser(role1, "user1");
        store.associateRoleToUser(role1, "user1");
        failed=false;
        try { 
            store.associateRoleToUser(role1, "user3");            
        } catch (IOException ex) {
            assertSecurityException(ex, RoleServiceValidationErrors.ROLE_ERR_06,"user3");
            failed=true;
        }
        assertTrue(failed);
        
        assertEquals(1, store.getRolesForGroup("group1").size());
        assertEquals(1, store.getRolesForUser("user1").size());
        
        failed=false;
        try { 
            store.getRolesForGroup("group3");            
        } catch (IOException ex) {
            assertSecurityException(ex, RoleServiceValidationErrors.ROLE_ERR_07,"group3");
            failed=true;
        }
        assertTrue(failed);
        
        failed=false;
        try { 
            store.getRolesForUser("user3");            
        } catch (IOException ex) {
            assertSecurityException(ex, RoleServiceValidationErrors.ROLE_ERR_06,"user3");
            failed=true;
        }
        assertTrue(failed);

        store.disAssociateRoleFromGroup(role1, "group1");
        store.disAssociateRoleFromGroup(role1, "group2");
        failed=false;
        try { 
            store.disAssociateRoleFromGroup(role1, "group3");            
        } catch (IOException ex) {
            assertSecurityException(ex, RoleServiceValidationErrors.ROLE_ERR_07,"group3");
            failed=true;
        }
        assertTrue(failed);
        
        store.disAssociateRoleFromUser(role1, "user1");
        store.disAssociateRoleFromUser(role1, "user1");
        failed=false;
        try { 
            store.disAssociateRoleFromUser(role1, "user3");            
        } catch (IOException ex) {
            assertSecurityException(ex, RoleServiceValidationErrors.ROLE_ERR_06,"user3");
            failed=true;
        }
        assertTrue(failed);
    }

}
