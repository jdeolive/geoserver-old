package org.geoserver.web;

import org.apache.wicket.util.crypt.ICrypt;
import org.apache.wicket.util.crypt.ICryptFactory;
import org.geoserver.security.password.KeyStoreProvider;
import org.geoserver.test.GeoServerTestSupport;
import org.geoserver.web.GeoserverWicketEncrypterFactory;


public class GeoserverWicketEncrypterFactoryTest extends GeoServerTestSupport {


    public void testEncryption() throws Exception {
        ICryptFactory factory = GeoserverWicketEncrypterFactory.get();
        ICrypt crypt = factory.newCrypt();
        String urlParams = "search?client=ubuntu&channel=fs&q=spring+protected+constructor&ie=utf-8&oe=utf-8";        
        String result  =crypt.encryptUrlSafe(urlParams);
        assertFalse(result.equals(urlParams));
        result = crypt.decryptUrlSafe(result);
        assertEquals(urlParams,result);
        
        // Simulate missing password in keystore, no encrpytion
        GeoserverWicketEncrypterFactory.Factory=null;
        KeyStoreProvider.get().removeKey(KeyStoreProvider.URLPARAMKEY);
        factory = GeoserverWicketEncrypterFactory.get();
        crypt = factory.newCrypt();                
        result  =crypt.encryptUrlSafe(urlParams);
        assertTrue(result.equals(urlParams));
        result = crypt.decryptUrlSafe(result);
        assertEquals(urlParams,result);
        
    }

}
