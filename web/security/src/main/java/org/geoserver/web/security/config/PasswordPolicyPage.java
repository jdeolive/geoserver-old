/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.web.security.config;

    
import java.io.IOException;

import org.geoserver.security.config.PasswordPolicyConfig;
import org.geoserver.security.config.impl.SecurityNamedServiceConfigImpl;
import org.geoserver.security.password.PasswordValidator;
import org.geoserver.web.GeoServerApplication;
import org.geoserver.web.security.AbstractSecurityPage;


public class PasswordPolicyPage extends AbstractSecurityPage {
    
    protected String policyName;
    protected AbstractSecurityPage responsePage;
    

    public PasswordPolicyPage(AbstractSecurityPage responsePage) {
        this(null,responsePage);
    }
    

    public PasswordPolicyPage(final String policyName,AbstractSecurityPage responsePage) {
        this.policyName=policyName;
        this.responsePage=responsePage;
        initializeComponents();        
    }
    
    protected void initializeComponents() {
        PasswordPolicyConfig config = null;
        
        if (policyName !=null && policyName.isEmpty()==false)
            try {
                config = GeoServerApplication.get().getSecurityManager().
                    loadPasswordValidatorConfig(policyName);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        
        SecurityNamedConfigModelHelper helper = null;
        if (config==null)
            helper = new SecurityNamedConfigModelHelper(new SecurityNamedServiceConfigImpl(),true);
        else
            helper = new SecurityNamedConfigModelHelper(config,false);
        
        add(new NamedConfigPanel("passwordPolicyPanel",helper,
                PasswordValidator.class,responsePage));

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
