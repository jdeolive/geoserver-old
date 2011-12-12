/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.security;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.geoserver.config.util.XStreamPersister;
import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.security.config.PasswordPolicyConfig;
import org.geoserver.security.config.SecurityNamedServiceConfig;
import org.geoserver.security.password.GeoserverConfigPBEPasswordEncoder;
import org.geoserver.security.password.GeoserverPasswordEncoder;
import org.geoserver.security.password.PasswordValidator;

import com.thoughtworks.xstream.converters.SingleValueConverter;

/**
 * Extension point for backend authentication and authorization services.
 * <p>
 * Subclasses of this class should be registered in the spring context. 
 * </p>
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public abstract class GeoServerSecurityProvider {
    
    /**
     * Find the provider for a service type 
     * and a concrete class name.
     * May return <code>null</code>
     * 
     * @param serviceClass
     * @param className
     * @return
     */
    static public  GeoServerSecurityProvider getProvider (Class<?> serviceClass, String className) {
        for (GeoServerSecurityProvider prov : 
            GeoServerExtensions.extensions(GeoServerSecurityProvider.class)) {
            
            if (GeoServerAuthenticationProvider.class==serviceClass) {
                if (prov.getAuthenticationProviderClass().getName().equals(className))
                    return prov;
            }
            if (GeoserverUserGroupService.class==serviceClass) {
                if (prov.getUserGroupServiceClass().getName().equals(className))
                    return prov;
            }
            if (GeoserverRoleService.class==serviceClass) {
                if (prov.getRoleServiceClass().getName().equals(className))
                    return prov;

            }
            if (PasswordValidator.class==serviceClass) {
                if (prov.getPasswordValidatorClass().getName().equals(className))
                    return prov;
            }
            if (GeoServerSecurityFilter.class==serviceClass) {
                if (prov.getFilterClass().getName().equals(className))
                    return prov;
            }                        
        }
        return null;
    }
    
    /**
     * An implementation of {@link SingleValueConverter} for enryption and
     * decryption of configuation passwords.
     * 
     * Register the fields in {@link #configure(XStreamPersister)}
     * 
     * <code>
     * xp.getXStream().registerLocalConverter(class, fieldName, encrypter);
     * </code>
     * 
     */
    public SingleValueConverter encrypter = new SingleValueConverter() {

        @Override
        public boolean canConvert(Class type) {
            return type.equals(String.class);
        }

        @Override
        public String toString(Object obj) {
            String source = obj == null ? "" : (String) obj;
            GeoServerSecurityManager manager = 
                    GeoServerExtensions.bean(GeoServerSecurityManager.class);
            if (manager.getConfigPasswordEncrypterName()==null ||
                    manager.getConfigPasswordEncrypterName().isEmpty())
                    return source; //end
            
            GeoserverConfigPBEPasswordEncoder enc = (GeoserverConfigPBEPasswordEncoder)
                    GeoServerExtensions.bean(manager.getConfigPasswordEncrypterName());
            
            if (source.startsWith(enc.getPrefix()+GeoserverPasswordEncoder.PREFIX_DELIMTER))
                throw new RuntimeException("Cannot encode a password with prefix: "+
                        enc.getPrefix()+GeoserverPasswordEncoder.PREFIX_DELIMTER);
            
            return enc.encodePassword(source, null);    
        };

        @Override
        public Object fromString(String str) {
            List<GeoserverConfigPBEPasswordEncoder> encoders = 
                    GeoServerExtensions.extensions(GeoserverConfigPBEPasswordEncoder.class);
            for (GeoserverConfigPBEPasswordEncoder enc : encoders) {                    
                if (enc.isResponsibleForEncoding(str))
                    return enc.decode(str);
            }    
            return str;                        
        }        
    };

    /**
     * Flag determining if this provider is available.
     * <p>
     * This default implementation returns <code>true</code>, subclasses should override in cases
     * where a meaningful check can be made... for instance checking for a jdbc driver, etc...
     * </p>
     */
            
    public boolean isAvaialble() {
        return true;
    }

    /**
     * Configures the xstream instance used to serialize/deserialize provider configuration. 
     */
    public void configure(XStreamPersister xp) {
        
        // register converter for fields to be encrypted
        for (Entry<Class<?>, Set<String>> entry: getFieldsForEncryption().entrySet()) {
            for (String fieldName: entry.getValue()) {
                xp.getXStream().registerLocalConverter(entry.getKey(), fieldName, encrypter);
            }                        
        }        
    }

    /**
     * Returns the concrete class of authentication provider created by 
     *  {@link #createAuthenticationProvider(SecurityNamedServiceConfig)}.
     * <p>
     * If the extension does not provide an authentication provider this method should simply return
     * <code>null</code>.
     * </p> 
     */
    public Class<? extends GeoServerAuthenticationProvider> getAuthenticationProviderClass() {
        return null;
    }

    /**
     * Creates an authentication provider.
     * <p>
     * If the extension does not provide an authentication provider this method should simply return
     * <code>null</code>.
     * </p> 
     */
    public GeoServerAuthenticationProvider createAuthenticationProvider(SecurityNamedServiceConfig config) {
        return null;
    }

    /**
     * Returns the concrete class of security filter created by 
     *  {@link #createFilter(SecurityNamedServiceConfig)}.
     * <p>
     * If the extension does not provide an filter this method should simply return <code>null</code>.
     * </p> 
     */
    public Class<? extends GeoServerSecurityFilter> getFilterClass() {
        return null;
    }

    /**
     * Creates a security filter.
     * <p>
     * If the extension does not provide an filter this method should simply return <code>null</code>.
     * </p> 
     */
    public GeoServerSecurityFilter createFilter(SecurityNamedServiceConfig config) {
        return null;
    }

    /**
     * Returns the specific class of the user group service created by 
     * {@link #createUserGroupService(SecurityNamedServiceConfig)}.
     * <p>
     * If the extension does not provide a user group service this method should simply return
     * <code>null</code>. 
     * </p> 
     */
    public Class<? extends GeoserverUserGroupService> getUserGroupServiceClass() {
        return null;
    }
    
    /**
     * Creates a new user group service.
     * <p>
     * If the extension does not provide a user group service this method should simply return
     * <code>null</code>. 
     * </p>
     */
    public GeoserverUserGroupService createUserGroupService(SecurityNamedServiceConfig config)
        throws IOException {
        return null;
    }

    /**
     * Returns the specific class of the role service created by 
     * {@link #createRoleService(SecurityNamedServiceConfig)}
     * <p>
     * If the extension does not provide a role service this method should simply return
     * <code>null</code>. 
     * </p> 
     */
    public Class<? extends GeoserverRoleService> getRoleServiceClass() {
        return null;
    }
    
    /**
     * Creates a new role group service.
     * <p>
     * If the extension does not provide a role service this method should simply return
     * <code>null</code>. 
     * </p>
     */
    public GeoserverRoleService createRoleService(SecurityNamedServiceConfig config) 
        throws IOException {
        return null;
    }

    /**
     * Returns the specific class of the password validator created by 
     * {@link #createPasswordValidator(PasswordPolicyConfig))}.
     * <p>
     * If the extension does not provide a validator this method should simply return
     * <code>null</code>. 
     * </p> 
     */
    public  Class<? extends PasswordValidator> getPasswordValidatorClass() {
        return null;
    }

    
    /**
     * Create the standard password validator or
     * return <code>null</code>
     * 
     * @param config
     * @return
     */
    public PasswordValidator createPasswordValidator(PasswordPolicyConfig config) {
        return null;
    }
    
    /**
     * Returns a map containing the field names
     * which should be encrypted. (backend store 
     * passwords as an example) 
     * 
     * @return
     */
    public Map<Class<?>, Set<String>> getFieldsForEncryption() {
        return Collections.emptyMap();        
    }
    
    /**
     * Return true if the {@link GeoserverRoleService} implementation
     * is not thread safe.
     * 
     * @return 
     */
    public boolean roleServiceNeedsLockProtection() {
        return false;
    }
    
    /**
     * Return true if the {@link GeoserverUserGroupService} implementation
     * is not thread safe.
     * 
     * @return 
     */
    public boolean userGroupServiceNeedsLockProtection() {
        return false;
    }
    
}
