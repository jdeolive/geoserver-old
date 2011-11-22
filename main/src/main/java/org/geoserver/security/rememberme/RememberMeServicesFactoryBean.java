/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.security.rememberme;

import org.geoserver.security.GeoServerSecurityManager;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.rememberme.AbstractRememberMeServices;

/**
 * Factory bean that proxies for the global remember me service.
 * <p>
 * The actual underlying rememberme service is determined by 
 *   {@link RememberMeServicesConfig#getClassName()}, obtained from 
 *   {@link GeoServerSecurityManager#getSecurityConfig()}.
 * </p>
 * @author Justin Deoliveira, OpenGeo
 *
 */
public class RememberMeServicesFactoryBean implements FactoryBean<RememberMeServices> {

    GeoServerSecurityManager securityManager;

    public RememberMeServicesFactoryBean(GeoServerSecurityManager securityManager) {
        this.securityManager = securityManager;
    }

    @Override
    public RememberMeServices getObject() throws Exception {
        RememberMeServicesConfig rmsConfig = 
            securityManager.getSecurityConfig().getRememberMeService();
        RememberMeServices rms = 
            (RememberMeServices) Class.forName(rmsConfig.getClassName()).newInstance();
        if (rms instanceof AbstractRememberMeServices) {
            AbstractRememberMeServices arms = (AbstractRememberMeServices) rms; 
            arms.setUserDetailsService(new RememberMeUserDetailsService(securityManager));
            arms.setKey(rmsConfig.getKey());
        }
        if (rms instanceof GeoServerTokenBasedRememberMeServices) {
            ((GeoServerTokenBasedRememberMeServices) rms).setUserGroupServiceName(rmsConfig.getUserGroupService());
        }
        return rms;
    }

    @Override
    public Class<?> getObjectType() {
        return RememberMeServices.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

}
