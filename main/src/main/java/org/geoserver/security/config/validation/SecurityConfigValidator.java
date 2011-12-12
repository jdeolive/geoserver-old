/* Copyright (c) 2001 - 2008 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.security.config.validation;

import static org.geoserver.security.config.validation.SecurityConfigValidationErrors.SEC_ERR_01;
import static org.geoserver.security.config.validation.SecurityConfigValidationErrors.SEC_ERR_02;
import static org.geoserver.security.config.validation.SecurityConfigValidationErrors.SEC_ERR_03;
import static org.geoserver.security.config.validation.SecurityConfigValidationErrors.SEC_ERR_04;
import static org.geoserver.security.config.validation.SecurityConfigValidationErrors.SEC_ERR_20;
import static org.geoserver.security.config.validation.SecurityConfigValidationErrors.SEC_ERR_21;
import static org.geoserver.security.config.validation.SecurityConfigValidationErrors.SEC_ERR_22;
import static org.geoserver.security.config.validation.SecurityConfigValidationErrors.SEC_ERR_23;
import static org.geoserver.security.config.validation.SecurityConfigValidationErrors.SEC_ERR_24;
import static org.geoserver.security.config.validation.SecurityConfigValidationErrors.SEC_ERR_30;
import static org.geoserver.security.config.validation.SecurityConfigValidationErrors.SEC_ERR_31;
import static org.geoserver.security.config.validation.SecurityConfigValidationErrors.SEC_ERR_32;
import static org.geoserver.security.config.validation.SecurityConfigValidationErrors.SEC_ERR_33;
import static org.geoserver.security.config.validation.SecurityConfigValidationErrors.SEC_ERR_34;
import static org.geoserver.security.config.validation.SecurityConfigValidationErrors.SEC_ERR_35;
import static org.geoserver.security.config.validation.SecurityConfigValidationErrors.SEC_ERR_40;
import static org.geoserver.security.config.validation.SecurityConfigValidationErrors.SEC_ERR_41;
import static org.geoserver.security.config.validation.SecurityConfigValidationErrors.SEC_ERR_42;

import java.io.IOException;
import java.util.SortedSet;

import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.security.GeoServerAuthenticationProvider;
import org.geoserver.security.GeoServerSecurityManager;
import org.geoserver.security.GeoServerSecurityProvider;
import org.geoserver.security.GeoserverAuthenticationProcessingFilter;
import org.geoserver.security.GeoserverRoleService;
import org.geoserver.security.GeoserverUserGroupService;
import org.geoserver.security.config.PasswordPolicyConfig;
import org.geoserver.security.config.SecurityAuthProviderConfig;
import org.geoserver.security.config.SecurityManagerConfig;
import org.geoserver.security.config.SecurityNamedServiceConfig;
import org.geoserver.security.config.SecurityRoleServiceConfig;
import org.geoserver.security.config.SecurityUserGroupServiceConfig;
import org.geoserver.security.password.GeoserverConfigPasswordEncoder;
import org.geoserver.security.password.GeoserverUserPasswordEncoder;
import org.geoserver.security.password.PasswordValidator;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

public class SecurityConfigValidator {

    GeoServerSecurityManager manager;
    GeoServerSecurityProvider provider;
    
    protected SecurityConfigValidator() {
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
            aClass=Class.forName(className);
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
        checkServiceName(extensionPoint, config.getName());
    }
    
    public void validateAddUserGroupService(SecurityUserGroupServiceConfig config) throws SecurityConfigException{
        validateAddNamedService(GeoserverUserGroupService.class, config);        
        validate(config);
    }
    
    public void validateAddRoleService(SecurityRoleServiceConfig config) throws SecurityConfigException{
        validateAddNamedService(GeoserverRoleService.class, config);
        validate(config);
    }

    public void validateAddPasswordPolicyService(PasswordPolicyConfig config) throws SecurityConfigException{
        validateAddNamedService(PasswordValidator.class, config);
        validate(config);
    }
    
    
    public void validateAddAuthProvider(SecurityAuthProviderConfig config) throws SecurityConfigException{
        validateAddNamedService(GeoServerAuthenticationProvider.class, config);
        validate(config);
    }

    public void validateAddFilter(SecurityNamedServiceConfig config) throws SecurityConfigException{
        validateAddNamedService(GeoserverAuthenticationProcessingFilter.class, config);        
    }
    
    public void validateModifiedUserGroupService(SecurityUserGroupServiceConfig config) throws SecurityConfigException{
        validateModifiedNamedService(GeoserverUserGroupService.class, config);
        validate(config);
    }
    
    public void validateModifiedRoleService(SecurityRoleServiceConfig config) throws SecurityConfigException{
        validateModifiedNamedService(GeoserverRoleService.class, config);
        validate(config);
    }

    public void validateModifiedPasswordPolicyService(PasswordPolicyConfig config) throws SecurityConfigException{
        validateModifiedNamedService(PasswordValidator.class, config);
        validate(config);
    }
    
    public void validateModifiedAuthProvider(SecurityAuthProviderConfig config) throws SecurityConfigException{
        validateModifiedNamedService(GeoServerAuthenticationProvider.class, config);
        validate(config);        
    }

    public void validateModifiedFilter(SecurityNamedServiceConfig config) throws SecurityConfigException{
        validateModifiedNamedService(GeoserverAuthenticationProcessingFilter.class, config);        
    }
    
    
    public void validateRemoveUserGroupService(SecurityUserGroupServiceConfig config) throws SecurityConfigException{
        validateRemoveNamedService(GeoserverUserGroupService.class, config);
        try {
            for (String name: manager.listAuthenticationProviders()) {
                SecurityAuthProviderConfig authConfig = 
                        manager.loadAuthenticationProviderConfig(name);
                String userGroupService=authConfig.getUserGroupServiceName();
                if (userGroupService!=null && userGroupService.length()!=0) {
                    if (authConfig.getUserGroupServiceName().equals(config.getName()))
                        throw createSecurityException(SEC_ERR_35, config.getName(),authConfig.getName());
                }    
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        
    }
    
    public void validateRemoveRoleService(SecurityRoleServiceConfig config) throws SecurityConfigException{
        validateRemoveNamedService(GeoserverRoleService.class, config); 
        if (manager.getActiveRoleService().getName().equals(config.getName())) {
                    throw createSecurityException(SEC_ERR_30, config.getName());
                }
    }

    public void validateRemovePasswordPolicyService(PasswordPolicyConfig config) throws SecurityConfigException{
        validateRemoveNamedService(PasswordValidator.class, config);
        
        if (PasswordValidator.MASTERPASSWORD_NAME.equals(config.getName()))
                throw createSecurityException(SEC_ERR_42);
                
        try {
            for (String name: manager.listUserGroupServices()) {
                SecurityUserGroupServiceConfig ugConfig = 
                        manager.loadUserGroupServiceConfig(name);
                if (ugConfig.getPasswordPolicyName().equals(config.getName()))
                    throw createSecurityException(SEC_ERR_34, config.getName(),ugConfig.getName());
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public void validateRemoveAuthProvider(SecurityAuthProviderConfig config) throws SecurityConfigException{
        validateRemoveNamedService(GeoServerAuthenticationProvider.class, config);        
        for (GeoServerAuthenticationProvider prov :manager.getAuthenticationProviders()) {
            if (prov.getName().equals(config.getName()))
                throw createSecurityException(SEC_ERR_31, config.getName());
        }
    }

    public void validateRemoveFilter(SecurityNamedServiceConfig config) throws SecurityConfigException{
        validateRemoveNamedService(GeoserverAuthenticationProcessingFilter.class, config);        
    }


    public void validate(SecurityAuthProviderConfig config) throws SecurityConfigException {
        if (config.getUserGroupServiceName() !=null && 
                config.getUserGroupServiceName().length()>0) {
            if (getNamesFor(GeoserverUserGroupService.class).
                    contains(config.getUserGroupServiceName())==false)
                    throw createSecurityException(SEC_ERR_24,
                            GeoserverUserGroupService.class,config.getUserGroupServiceName() );
        }        
    }
    
    public void validate(SecurityRoleServiceConfig config) throws SecurityConfigException {
    }

    public void validate(SecurityUserGroupServiceConfig config) throws SecurityConfigException {
        String encoderName =config.getPasswordEncoderName();
        if (encoderName!=null && 
                encoderName.length()>0) {
            Object o=null;
            try {
                o = GeoServerExtensions.bean(encoderName);
            } catch (NoSuchBeanDefinitionException ex) {
                throw createSecurityException(SEC_ERR_04, encoderName);
            }
            if (o instanceof GeoserverUserPasswordEncoder == false)
                throw createSecurityException(SEC_ERR_04, encoderName);                                        
        } else {
            throw createSecurityException(SEC_ERR_32, config.getName());
        }
        
        String policyName= config.getPasswordPolicyName();
        if (policyName==null || policyName.length()==0) {
            throw createSecurityException(SEC_ERR_33, config.getName());
        }
        
        if (getNamesFor(PasswordValidator.class).contains(policyName)==false) {
            throw createSecurityException(SEC_ERR_24, PasswordValidator.class,policyName);
        }
    }
    
    public void validate(PasswordPolicyConfig config) throws SecurityConfigException {
        if (config.getMinLength() < 1)
            throw createSecurityException(SEC_ERR_40);
        if (config.getMinLength() !=- 1) {
            if (config.getMinLength()>config.getMaxLength())
                throw createSecurityException(SEC_ERR_41);
        }
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
