/* Copyright (c) 2001 - 2008 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.security.config.impl;

import java.util.ArrayList;
import java.util.List;

import org.geoserver.security.GeoServerSecurityFilterChain;
import org.geoserver.security.config.SecurityManagerConfig;

/**
 * Implementation of {@link SecurityManagerConfig}
 * 
 * @author christian
 *
 */
public class SecurityManagerConfigImpl extends SecurityConfigImpl implements SecurityManagerConfig {
    
    private String roleServiceName;
    private List<String> authProviderNames = new ArrayList<String>();
    private Boolean anonymousAuth = Boolean.TRUE;

    private GeoServerSecurityFilterChain filterChain = new GeoServerSecurityFilterChain();

    public SecurityManagerConfigImpl() {
    }

    public SecurityManagerConfigImpl(SecurityManagerConfig config) {
        this.roleServiceName = config.getRoleServiceName();
        this.authProviderNames = config.getAuthProviderNames() != null ? 
            new ArrayList<String>(config.getAuthProviderNames()) : null;
        this.anonymousAuth = config.isAnonymousAuth();
        this.filterChain = config.getFilterChain() != null ? 
            new GeoServerSecurityFilterChain(config.getFilterChain()) : null;
    }

    private String configPasswordEncrypterName;
    private boolean encryptingUrlParams;
    
    public boolean isEncryptingUrlParams() {
        return encryptingUrlParams;
    }
    public void setEncryptingUrlParams(boolean encryptingUrlParams) {
        this.encryptingUrlParams = encryptingUrlParams;
    }

    public String getRoleServiceName() {
        return roleServiceName;
    }
    public void setRoleServiceName(String roleServiceName) {
        this.roleServiceName = roleServiceName;
    }

    public List<String> getAuthProviderNames() {
        return authProviderNames;
    }

    public void setAnonymousAuth(Boolean anonymousAuth) {
        this.anonymousAuth = anonymousAuth;
    }
    public Boolean isAnonymousAuth() {
        return anonymousAuth;
    }

    @Override
    public GeoServerSecurityFilterChain getFilterChain() {
        return filterChain;
    }

    public void setFilterChain(GeoServerSecurityFilterChain filterChain) {
        this.filterChain = filterChain;
    }

    private Object readResolve() {
        authProviderNames = authProviderNames != null ? authProviderNames : new ArrayList<String>();
        anonymousAuth = anonymousAuth != null ? anonymousAuth : Boolean.TRUE;
        filterChain = filterChain != null ? filterChain : new GeoServerSecurityFilterChain();
        return this;
    }

    public String getConfigPasswordEncrypterName() {
        return configPasswordEncrypterName;
    }
    public void setConfigPasswordEncrypterName(String configPasswordEncrypterName) {
        this.configPasswordEncrypterName = configPasswordEncrypterName;
    }

}
