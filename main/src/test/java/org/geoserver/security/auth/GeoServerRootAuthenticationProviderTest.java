/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */


package org.geoserver.security.auth;

import org.geoserver.security.password.MasterPasswordProviderImpl;
import org.geoserver.test.GeoServerTestSupport;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

public class GeoServerRootAuthenticationProviderTest extends GeoServerTestSupport {

    
    
    
    public void testRootProvider() throws Exception {
        
        // Check if the root provider is the first
        AuthenticationProvider first = getSecurityManager().getProviders().get(0);
        assertEquals(GeoServerRootAuthenticationProvider.class, first.getClass());
        
        GeoServerRootAuthenticationProvider provider = new GeoServerRootAuthenticationProvider(); 
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken("abc", null);
        
        assertTrue(provider.supports(token.getClass()));
        assertFalse(provider.supports(RememberMeAuthenticationToken.class));
        
        assertNull(provider.authenticate(token));
        
        token = new UsernamePasswordAuthenticationToken(GeoServerRootAuthenticationProvider.ROOTUSERNAME, null);
        assertNull(provider.authenticate(token));
        
        token = new UsernamePasswordAuthenticationToken(GeoServerRootAuthenticationProvider.ROOTUSERNAME, "abc");
        assertNull(provider.authenticate(token));

        String masterPassword = MasterPasswordProviderImpl.get().getMasterPassword();
        token = new UsernamePasswordAuthenticationToken(GeoServerRootAuthenticationProvider.ROOTUSERNAME, masterPassword);
        token.setDetails("hallo");
        UsernamePasswordAuthenticationToken result = (UsernamePasswordAuthenticationToken)
                provider.authenticate(token);
        
        assertNotNull(result);
        assertNull(result.getCredentials());
        assertEquals(GeoServerRootAuthenticationProvider.ROOTUSERNAME,result.getPrincipal());
        assertEquals("hallo",result.getDetails());        
    }
        
}
