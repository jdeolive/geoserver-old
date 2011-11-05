package org.geoserver.security.password;

import java.io.File;

import org.geoserver.test.GeoServerTestSupport;

public class KeyStoreProviderTest extends GeoServerTestSupport {

    
    
    
    
    public void testKeyStoreProvider() throws Exception {
        
        //System.setProperty(MasterPasswordProvider.DEFAULT_PROPERTY_NAME, "mymasterpw");
        
        KeyStoreProvider.get().removeKey(KeyStoreProvider.CONFIGPASSWORDKEY);
        KeyStoreProvider.get().removeKey(KeyStoreProvider.URLPARAMKEY);
        KeyStoreProvider.get().removeKey(KeyStoreProvider.get().aliasForGroupService("default"));
        KeyStoreProvider.get().storeKeyStore();
        KeyStoreProvider.get().reloadKeyStore();
        
        assertFalse(KeyStoreProvider.get().hasConfigPasswordKey());
        assertFalse(KeyStoreProvider.get().hasUrlParamKey());
        assertFalse(KeyStoreProvider.get().hasUserGRoupKey("default"));
        
                        
        KeyStoreProvider.get().setSecretKey( KeyStoreProvider.CONFIGPASSWORDKEY, "configKey");
        KeyStoreProvider.get().storeKeyStore();
        
        assertTrue(KeyStoreProvider.get().hasConfigPasswordKey());
        assertEquals("configKey",KeyStoreProvider.get().getConfigPasswordKey());
        assertFalse(KeyStoreProvider.get().hasUrlParamKey());
        assertFalse(KeyStoreProvider.get().hasUserGRoupKey("default"));
        
        String urlKey = RandomPasswordProvider.get().getRandomPassword(32);
        System.out.printf("Random password with length %d : %s\n",urlKey.length(),urlKey);
        String urlKey2 = RandomPasswordProvider.get().getRandomPassword(32);
        System.out.printf("Random password with length %d : %s\n",urlKey2.length(),urlKey2);
        assertFalse(urlKey.equals(urlKey2));

        KeyStoreProvider.get().setSecretKey( KeyStoreProvider.URLPARAMKEY, urlKey);
        KeyStoreProvider.get().setSecretKey( KeyStoreProvider.USERGROUP_PREFIX+"default"+
                    KeyStoreProvider.USERGROUP_POSTFIX, "defaultKey");

        KeyStoreProvider.get().storeKeyStore();
        
        assertTrue(KeyStoreProvider.get().hasConfigPasswordKey());
        assertEquals("configKey",KeyStoreProvider.get().getConfigPasswordKey());
        assertTrue(KeyStoreProvider.get().hasUrlParamKey());
        assertEquals(urlKey,KeyStoreProvider.get().getUrlParamKey());
        assertTrue(KeyStoreProvider.get().hasUserGRoupKey("default"));
        assertEquals("defaultKey",KeyStoreProvider.get().getUserGRoupKey("default"));
        
    }
    
    public void testMasterPasswordChange() throws Exception {
        // keytool -storepasswd -new geoserver1 -storepass geoserver -storetype jceks -keystore geoserver.jks
        
               
        assertTrue(MasterPasswordProviderImpl.DEFAULT_MASTER_PASSWORD.
                equals(MasterPasswordProviderImpl.get().getMasterPassword()));
        
        KeyStoreProvider.get().assertActivatedKeyStore();
        
        File keyStoreFile = KeyStoreProvider.get().keyStoreFile;
        File newKeyStoreFile = new File(KeyStoreProvider.get().keyStoreFile.getParentFile(),
                KeyStoreProvider.PREPARED_FILE_NAME);
        
        String testPassword = KeyStoreProvider.get().getConfigPasswordKey();
        String testPassword2 = KeyStoreProvider.get().getUrlParamKey();
        
        // Phase 1, prepare
        assertTrue(keyStoreFile.exists());
        assertFalse(newKeyStoreFile.exists());
        KeyStoreProvider.get().prepareForMasterPasswordChange("newpasswd");
        assertTrue(keyStoreFile.exists());
        assertTrue(newKeyStoreFile.exists());
        
        KeyStoreProvider.get().reloadKeyStore();
        // the master password is not changed, fallback mechanism
        assertEquals(KeyStoreProvider.get().keyStoreFile, keyStoreFile);
        assertEquals(testPassword,KeyStoreProvider.get().getConfigPasswordKey());
        assertEquals(testPassword2,KeyStoreProvider.get().getUrlParamKey());
        
        // inject the new masterpassword
        System.setProperty(MasterPasswordProviderImpl.DEFAULT_PROPERTY_NAME, "newpasswd");
        assertEquals("newpasswd",MasterPasswordProviderImpl.get().getMasterPassword());

        // reload, everthing should work now
        KeyStoreProvider.get().reloadKeyStore();
        assertTrue(keyStoreFile.exists());
        assertFalse(newKeyStoreFile.exists());        
        assertEquals(testPassword,KeyStoreProvider.get().getConfigPasswordKey());
        assertEquals(testPassword2,KeyStoreProvider.get().getUrlParamKey());

    }
        
}
