/* Copyright (c) 2001 - 2008 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.security.validation;

import static org.geoserver.security.validation.SecurityConfigValidationErrors.*;


import java.io.IOException;
import java.util.SortedSet;

import org.geoserver.security.GeoServerAuthenticationProvider;
import org.geoserver.security.GeoServerSecurityProvider;
import org.geoserver.security.GeoServerAuthenticationProcessingFilter;
import org.geoserver.security.GeoServerRoleService;
import org.geoserver.security.GeoServerUserGroupService;
import org.geoserver.security.config.PasswordPolicyConfig;
import org.geoserver.security.config.SecurityAuthProviderConfig;
import org.geoserver.security.config.SecurityManagerConfig;
import org.geoserver.security.config.SecurityNamedServiceConfig;
import org.geoserver.security.config.SecurityRoleServiceConfig;
import org.geoserver.security.config.SecurityUserGroupServiceConfig;
import org.geoserver.security.password.AbstractGeoserverPasswordEncoder;
import org.geoserver.security.password.GeoServerPasswordEncoder;
import org.geoserver.security.password.PasswordValidator;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;


public class SecurityConfigValidator extends AbstractSecurityValidator{


    
    /**
     * Get the proper {@link SecurityConfigValidator} object
     * 
     * @param serviceClass
     * @param className
     * @return
     */
    static public SecurityConfigValidator getConfigurationValiator(Class <?> serviceClass, String className) {
        GeoServerSecurityProvider prov = GeoServerSecurityProvider.getProvider(serviceClass, className);
        return prov.getConfigurationValidator();
    }
    
    
    /**
     * Checks the {@link SecurityManagerConfig} object
     * 
     * @param config
     * @throws SecurityConfigException
     */
    public void validateManagerConfig(SecurityManagerConfig config) throws SecurityConfigException{
        
        String encrypterName =config.getConfigPasswordEncrypterName();
        if (isNotEmpty(encrypterName)==false) {
            throw createSecurityException(SEC_ERR_07);
        }

        GeoServerPasswordEncoder encoder = null;
        try {
            encoder = manager.loadPasswordEncoder(config.getConfigPasswordEncrypterName());
        } catch (NoSuchBeanDefinitionException ex) {
            throw createSecurityException(SEC_ERR_01, encrypterName);
        }
        if (encoder == null) {
            throw createSecurityException(SEC_ERR_01, encrypterName);
        }

        if (!encoder.isReversible()) {
            throw createSecurityException(SEC_ERR_01, encrypterName);
        }

        if (AbstractGeoserverPasswordEncoder.isStrongCryptographyAvailable()==false) {
            if (encoder!=null && encoder.isAvailableWithoutStrongCryptogaphy()==false)                
                throw createSecurityException(SEC_ERR_05);
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
        if (isNotEmpty(className)==false) {
            throw createSecurityException(SEC_ERR_25);
        }
        Class<?> aClass = null;
        try {
            aClass=Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw createSecurityException(SEC_ERR_20, className);
        } 
        
        if (extensionPoint.isAssignableFrom(aClass)==false) {
            throw createSecurityException(SEC_ERR_21, extensionPoint,
                    className);
        }
    }
    
    protected void checkServiceName(Class<?> extensionPoint,String name) throws SecurityConfigException{
        if (name==null || name.isEmpty())
                throw createSecurityException(SEC_ERR_22);                        
    }
    
    protected  SortedSet<String> getNamesFor(Class<?> extensionPoint) {
        try {
            if (extensionPoint==GeoServerUserGroupService.class)
                return manager.listUserGroupServices();
            if (extensionPoint==GeoServerRoleService.class)
                return manager.listRoleServices();
            if (extensionPoint==GeoServerAuthenticationProvider.class)
                return manager.listAuthenticationProviders();
            if (extensionPoint==GeoServerAuthenticationProcessingFilter.class)
                return manager.listFilters();
            if (extensionPoint==PasswordValidator.class)
                return manager.listPasswordValidators();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        throw new RuntimeException("Unkwnown extension point: "+extensionPoint.getName());
    }
    
    public void validateAddNamedService(Class<?> extensionPoint,SecurityNamedServiceConfig config) throws SecurityConfigException{
        checkExtensionPont(extensionPoint, config.getClassName());
        checkServiceName(extensionPoint, config.getName());
        SortedSet<String> names= getNamesFor(extensionPoint);
        if (names.contains(config.getName()))
            throw createSecurityException(prepareForExtPoint(extensionPoint, "SEC_ERR_23"), config.getName());
        
    }
    
    public void validateModifiedNamedService(Class<?> extensionPoint,SecurityNamedServiceConfig config) throws SecurityConfigException{
        checkExtensionPont(extensionPoint, config.getClassName());
        checkServiceName(extensionPoint, config.getName());
        SortedSet<String> names= getNamesFor(extensionPoint);
        if (names.contains(config.getName())==false)
            throw createSecurityException(prepareForExtPoint(extensionPoint, "SEC_ERR_24"),config.getName());
        
    }

        
    public void validateRemoveNamedService(Class<?> extensionPoint,SecurityNamedServiceConfig config) throws SecurityConfigException{
        checkServiceName(extensionPoint, config.getName());
    }
    
    public void validateAddUserGroupService(SecurityUserGroupServiceConfig config) throws SecurityConfigException{
        validateAddNamedService(GeoServerUserGroupService.class, config);        
        validate(config);
    }
    
    public void validateAddRoleService(SecurityRoleServiceConfig config) throws SecurityConfigException{
        validateAddNamedService(GeoServerRoleService.class, config);
        validate(config);
    }

    public void validateAddPasswordPolicy(PasswordPolicyConfig config) throws SecurityConfigException{
        validateAddNamedService(PasswordValidator.class, config);
        validate(config);
    }
    
    
    public void validateAddAuthProvider(SecurityAuthProviderConfig config) throws SecurityConfigException{
        validateAddNamedService(GeoServerAuthenticationProvider.class, config);
        validate(config);
    }

    public void validateAddFilter(SecurityNamedServiceConfig config) throws SecurityConfigException{
        validateAddNamedService(GeoServerAuthenticationProcessingFilter.class, config);        
    }
    
    public void validateModifiedUserGroupService(SecurityUserGroupServiceConfig config,SecurityUserGroupServiceConfig oldConfig) throws SecurityConfigException{
        validateModifiedNamedService(GeoServerUserGroupService.class, config);
        validate(config);
    }
    
    public void validateModifiedRoleService(SecurityRoleServiceConfig config,SecurityRoleServiceConfig oldConfig) throws SecurityConfigException{
        validateModifiedNamedService(GeoServerRoleService.class, config);
        validate(config);
    }

    public void validateModifiedPasswordPolicy(PasswordPolicyConfig config,PasswordPolicyConfig oldConfig) throws SecurityConfigException{
        validateModifiedNamedService(PasswordValidator.class, config);
        validate(config);
    }
    
    public void validateModifiedAuthProvider(SecurityAuthProviderConfig config,SecurityAuthProviderConfig oldconfig) throws SecurityConfigException{
        validateModifiedNamedService(GeoServerAuthenticationProvider.class, config);
        validate(config);        
    }

    public void validateModifiedFilter(SecurityNamedServiceConfig config,SecurityNamedServiceConfig oldConfig) throws SecurityConfigException{
        validateModifiedNamedService(GeoServerAuthenticationProcessingFilter.class, config);        
    }
    
    
    public void validateRemoveUserGroupService(SecurityUserGroupServiceConfig config) throws SecurityConfigException{
        validateRemoveNamedService(GeoServerUserGroupService.class, config);
        try {
            for (String name: manager.listAuthenticationProviders()) {
                SecurityAuthProviderConfig authConfig = 
                        manager.loadAuthenticationProviderConfig(name);
                String userGroupService=authConfig.getUserGroupServiceName();
                if (isNotEmpty(userGroupService)) {
                    if (authConfig.getUserGroupServiceName().equals(config.getName()))
                        throw createSecurityException(SEC_ERR_35, config.getName(),authConfig.getName());
                }    
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        
    }
    
    public void validateRemoveRoleService(SecurityRoleServiceConfig config) throws SecurityConfigException{
        validateRemoveNamedService(GeoServerRoleService.class, config); 
        if (manager.getActiveRoleService().getName().equals(config.getName())) {
                    throw createSecurityException(SEC_ERR_30, config.getName());
                }
    }

    public void validateRemovePasswordPolicy(PasswordPolicyConfig config) throws SecurityConfigException{
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
        validateRemoveNamedService(GeoServerAuthenticationProcessingFilter.class, config);        
    }


    public void validate(SecurityAuthProviderConfig config) throws SecurityConfigException {
        if (isNotEmpty(config.getUserGroupServiceName())) {
            if (getNamesFor(GeoServerUserGroupService.class).
                    contains(config.getUserGroupServiceName())==false)
                    throw createSecurityException(SEC_ERR_24d,
                            config.getUserGroupServiceName() );
        }        
    }
    
    public void validate(SecurityRoleServiceConfig config) throws SecurityConfigException {
//        if (GeoserverRole.ADMIN_ROLE.getAuthority().equals(config.getAdminRoleName())==false) {
//            throw createSecurityException(SEC_ERR_50, GeoserverRole.ADMIN_ROLE.getAuthority());
//        }
    }

    public void validate(SecurityUserGroupServiceConfig config) throws SecurityConfigException {
        String encoderName =config.getPasswordEncoderName();
        GeoServerPasswordEncoder encoder = null;
        if (isNotEmpty(encoderName)) {
            try {
                encoder = manager.loadPasswordEncoder(encoderName);
            } catch (NoSuchBeanDefinitionException ex) {
                throw createSecurityException(SEC_ERR_04, encoderName);
            }
            if (encoder == null) {
                throw createSecurityException(SEC_ERR_04, encoderName);
            }
        } else {
            throw createSecurityException(SEC_ERR_32, config.getName());
        }
        
        if (AbstractGeoserverPasswordEncoder.isStrongCryptographyAvailable()==false) {
            if (encoder!=null && encoder.isAvailableWithoutStrongCryptogaphy()==false)
                throw createSecurityException(SEC_ERR_06);
        }
        
        String policyName= config.getPasswordPolicyName();
        if (isNotEmpty(policyName)==false) {
            throw createSecurityException(SEC_ERR_33, config.getName());
        }
        
        if (getNamesFor(PasswordValidator.class).contains(policyName)==false) {
            throw createSecurityException(SEC_ERR_24b,policyName);
        }
    }
    
    public void validate(PasswordPolicyConfig config) throws SecurityConfigException {
        if (config.getMinLength() < 0)
            throw createSecurityException(SEC_ERR_40);
        if (config.getMaxLength() !=- 1) {
            if (config.getMinLength()>config.getMaxLength())
                throw createSecurityException(SEC_ERR_41);
        }
    }

    protected String prepareForExtPoint(Class<?> extPoint, String id) {
        if (GeoServerAuthenticationProvider.class==extPoint)
            return id+"a";
        if (PasswordValidator.class==extPoint)
            return id+"b";
        if (GeoServerRoleService.class==extPoint)
            return id+"c";
        if (GeoServerUserGroupService.class==extPoint)
            return id+"d";
        if (GeoServerAuthenticationProcessingFilter.class==extPoint)
            return id+"e";
        throw new RuntimeException("Unkonw extension point: "+extPoint.getName());
    }


    @Override
    protected AbstractSecurityValidationErrors getSecurityErrors() {
        return new SecurityConfigValidationErrors();
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
        String message = getSecurityErrors().formatErrorMsg(errorid, args);
        return new SecurityConfigException(errorid,message,args);
    }
        
}
