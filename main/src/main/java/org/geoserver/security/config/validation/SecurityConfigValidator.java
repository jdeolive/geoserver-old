/* Copyright (c) 2001 - 2008 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.security.config.validation;

import java.io.IOException;
import java.util.SortedSet;

import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.security.GeoServerAuthenticationProvider;
import org.geoserver.security.GeoServerSecurityManager;
import org.geoserver.security.GeoserverAuthenticationProcessingFilter;
import org.geoserver.security.GeoserverRoleService;
import org.geoserver.security.GeoserverUserGroupService;
import org.geoserver.security.UsernamePasswordAuthenticationProvider;
import org.geoserver.security.config.PasswordPolicyConfig;
import org.geoserver.security.config.SecurityAuthProviderConfig;
import org.geoserver.security.config.SecurityManagerConfig;
import org.geoserver.security.config.SecurityNamedServiceConfig;
import org.geoserver.security.config.SecurityRoleServiceConfig;
import org.geoserver.security.config.SecurityUserGoupServiceConfig;
import org.geoserver.security.config.impl.UsernamePasswordAuthenticationProviderConfig;
import org.geoserver.security.password.GeoserverConfigPasswordEncoder;
import org.geoserver.security.password.PasswordValidator;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import static org.geoserver.security.config.validation.SecurityConfigValidationErrors.*;

public class SecurityConfigValidator {

    GeoServerSecurityManager manager;
    
    public SecurityConfigValidator() {
        manager=GeoServerExtensions.bean(GeoServerSecurityManager.class);
    }
    
    /**
     * Checks the {@link SecurityManagerConfig} object
     * 
     * @param config
     * @throws SecurityConfigException
     */
    public void validateManagerConfig(SecurityManagerConfig config) throws SecurityConfigException{
        
        String encrypterName =config.getConfigPasswordEncrypterName(); 
        if (encrypterName!=null && 
                encrypterName.length()>0) {
            Object o=null;
            try {
                o = GeoServerExtensions.bean(config.getConfigPasswordEncrypterName());
            } catch (NoSuchBeanDefinitionException ex) {
                throw createSecurityException(SEC_ERR_01, encrypterName);
            }
            if (o instanceof GeoserverConfigPasswordEncoder == false)
                throw createSecurityException(SEC_ERR_01, encrypterName);                                        
        }
        
        String roleServiceName = config.getRoleServiceName();
        if (roleServiceName==null) 
            roleServiceName="";
        
        try {
            if (manager.listRoleServices().contains(roleServiceName)==false)
                throw createSecurityException(SEC_ERR_02, roleServiceName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        SortedSet<String> authProviders=null;
        try{
            authProviders =manager.listAuthenticationProviders();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (String authProvName : config.getAuthProviderNames()) {
            if (authProviders.contains(authProvName)==false)
                throw createSecurityException(SEC_ERR_03, authProvName);
        }
    }
    
    protected void checkExtensionPont(Class<?> extensionPoint, String className) throws SecurityConfigException{
        Class<?> aClass = null;
        try {
            Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw createSecurityException(SEC_ERR_20, extensionPoint,className);
        }
        if (extensionPoint.isAssignableFrom(aClass)==false) {
            throw createSecurityException(SEC_ERR_21, 
                    extensionPoint,
                    className);
        }
    }
    
    protected void checkServiceName(Class<?> extensionPoint,String name) throws SecurityConfigException{
        if (name==null || name.isEmpty())
                throw createSecurityException(SEC_ERR_22, extensionPoint, name);
            
            
    }
    
    protected  SortedSet<String> getNamesFor(Class<?> extensionPoint) {
        try {
            if (extensionPoint==GeoserverUserGroupService.class)
                return manager.listUserGroupServices();
            if (extensionPoint==GeoserverRoleService.class)
                return manager.listRoleServices();
            if (extensionPoint==GeoServerAuthenticationProvider.class)
                return manager.listAuthenticationProviders();
            if (extensionPoint==GeoserverAuthenticationProcessingFilter.class)
                return manager.listFilters();
            if (extensionPoint==PasswordValidator.class)
                return manager.listPasswordValidators();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        throw new RuntimeException("Unkwnown extension point: "+extensionPoint.getName());
    }
    
    protected void validateAddNamedService(Class<?> extensionPoint,SecurityNamedServiceConfig config) throws SecurityConfigException{
        checkExtensionPont(extensionPoint, config.getClassName());
        checkServiceName(extensionPoint, config.getName());
        SortedSet<String> names= getNamesFor(extensionPoint);
        if (names.contains(config.getName()))
            throw createSecurityException(SEC_ERR_23, extensionPoint,config.getName());
        
    }
    
    protected void validateModifiedNamedService(Class<?> extensionPoint,SecurityNamedServiceConfig config) throws SecurityConfigException{
        checkExtensionPont(extensionPoint, config.getClassName());
        checkServiceName(extensionPoint, config.getName());
        SortedSet<String> names= getNamesFor(extensionPoint);
        if (names.contains(config.getName())==false)
            throw createSecurityException(SEC_ERR_24, extensionPoint,config.getName());
        
    }

    protected void validateRemoveNamedService(Class<?> extensionPoint,SecurityNamedServiceConfig config) throws SecurityConfigException{
        validateModifiedNamedService(extensionPoint, config);
    }
    
    public void validateAddUserGroupService(SecurityUserGoupServiceConfig config) throws SecurityConfigException{
        validateAddNamedService(GeoserverUserGroupService.class, config);        
    }
    
    public void validateAddRoleService(SecurityRoleServiceConfig config) throws SecurityConfigException{
        validateAddNamedService(GeoserverRoleService.class, config);        
    }

    public void validateAddPasswordPolicyService(PasswordPolicyConfig config) throws SecurityConfigException{
        validateAddNamedService(PasswordValidator.class, config);        
    }
    
    public void validateAddAuthProvider(SecurityAuthProviderConfig config) throws SecurityConfigException{
        validateAddNamedService(GeoServerAuthenticationProvider.class, config);        
    }

    public void validateAddFilter(SecurityNamedServiceConfig config) throws SecurityConfigException{
        validateAddNamedService(GeoserverAuthenticationProcessingFilter.class, config);        
    }
    
    public void validateModifiedUserGroupService(SecurityUserGoupServiceConfig config) throws SecurityConfigException{
        validateModifiedNamedService(GeoserverUserGroupService.class, config);        
    }
    
    public void validateModifiedRoleService(SecurityRoleServiceConfig config) throws SecurityConfigException{
        validateModifiedNamedService(GeoserverRoleService.class, config);        
    }

    public void validateModifiedPasswordPolicyService(PasswordPolicyConfig config) throws SecurityConfigException{
        validateModifiedNamedService(PasswordValidator.class, config);        
    }
    
    public void validateModifiedAuthProvider(SecurityAuthProviderConfig config) throws SecurityConfigException{
        validateModifiedNamedService(GeoServerAuthenticationProvider.class, config);        
    }

    public void validateModifiedFilter(SecurityNamedServiceConfig config) throws SecurityConfigException{
        validateModifiedNamedService(GeoserverAuthenticationProcessingFilter.class, config);        
    }
    
    
    public void validateRemoveUserGroupService(SecurityUserGoupServiceConfig config) throws SecurityConfigException{
        validateRemoveNamedService(GeoserverUserGroupService.class, config);        
//        for (GeoServerAuthenticationProvider prov : manager.getAuthenticationProviders()) {
//            // TODO, need better concept
//            if (prov instanceof UsernamePasswordAuthenticationProvider) {
//                if (((UsernamePasswordAuthenticationProvider)prov).
//            }
//        }
    }
    
    public void validateRemoveRoleService(SecurityRoleServiceConfig config) throws SecurityConfigException{
        validateRemoveNamedService(GeoserverRoleService.class, config); 
        if (manager.getActiveRoleService().getName().equals(config.getName())) {
                    throw createSecurityException(SEC_ERR_30, config.getName());
                }
    }

    public void validateRemovePasswordPolicyService(PasswordPolicyConfig config) throws SecurityConfigException{
        validateRemoveNamedService(PasswordValidator.class, config);
//        for (String name: manager.listUserGroupServices()) {
//            SecurityUserGoupServiceConfig ugConfig = (SecurityUserGoupServiceConfig)
//                    manager.loadUserGroupServiceConfig(name);
//            if (ugConfig.getPasswordPolicyName().eq)
//        }
    }
    
    public void validateRemoveAuthProvider(SecurityAuthProviderConfig config) throws SecurityConfigException{
        validateRemoveNamedService(GeoServerAuthenticationProvider.class, config);        
    }

    public void validateRemoveFilter(SecurityNamedServiceConfig config) throws SecurityConfigException{
        validateRemoveNamedService(GeoserverAuthenticationProcessingFilter.class, config);        
    }



        
    /**
     * Helper method for creating a proper
     * {@link SecurityConfigException} object
     * 
     * @param errorid
     * @param args
     * @return
     */
    protected SecurityConfigException createSecurityException (String errorid, Object ...args) {
        String message = SecurityConfigValidationErrors.formatErrorMsg(errorid, args);
        return new SecurityConfigException(errorid, message,args);
    }
}
