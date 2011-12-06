/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.web.security.config;

    
import java.io.IOException;

import org.geoserver.security.GeoServerAuthenticationProvider;
import org.geoserver.security.config.SecurityNamedServiceConfig;
import org.geoserver.security.config.impl.SecurityNamedServiceConfigImpl;
import org.geoserver.web.GeoServerApplication;
import org.geoserver.web.security.AbstractSecurityPage;


public class AuthenticationProviderPage extends AbstractSecurityPage {
    
    protected String providerName;
    protected AbstractSecurityPage responsePage;
    

    public AuthenticationProviderPage(AbstractSecurityPage responsePage) {
        this(null,responsePage);
    }
    

    public AuthenticationProviderPage(final String providerName,AbstractSecurityPage responsePage) {
        this.providerName=providerName;
        this.responsePage=responsePage;
        initializeComponents();        
    }
    
    protected void initializeComponents() {
        SecurityNamedServiceConfig config = null;
        
        if (providerName !=null && providerName.isEmpty()==false)
            try {
                config = GeoServerApplication.get().getSecurityManager().
                    loadAuthenticationProviderConfig(providerName);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        
        SecurityNamedConfigModelHelper helper = null;
        if (config==null)
            helper = new SecurityNamedConfigModelHelper(new SecurityNamedServiceConfigImpl(),true);
        else
            helper = new SecurityNamedConfigModelHelper(config,false);
        
        add(new NamedConfigPanel("authenticationProviderPanel",helper,
                GeoServerAuthenticationProvider.class,responsePage));

    }
                
    @Override
    protected void onBeforeRender() {
        if (isDirty()) {
            initializeComponents();
            setDirty(false);
        }
        super.onBeforeRender();
    }
}
