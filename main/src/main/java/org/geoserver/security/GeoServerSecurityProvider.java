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
import org.geoserver.security.config.impl.PasswordPolicyConfigImpl;
import org.geoserver.security.password.GeoserverConfigPBEPasswordEncoder;
import org.geoserver.security.password.GeoserverPasswordEncoder;
import org.geoserver.security.password.PasswordValidator;
import org.geoserver.security.password.PasswordValidatorImpl;
import org.springframework.security.authentication.AuthenticationProvider;

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
        xp.getXStream().alias("passwordpolicy", PasswordPolicyConfigImpl.class);
        
        // register converter for fields to be encrypted
        for (Entry<Class<?>, Set<String>> entry: getFieldsForEncryption().entrySet()) {
            for (String fieldName: entry.getValue()) {
                xp.getXStream().registerLocalConverter(entry.getKey(), fieldName, encrypter);
            }                        
        }        
    }

    /**
     * Creates an authentication provider.
     * <p>
     * If the extension does not provide an authentication provider this method should simply return
     * <code>null</code>. 
     */
    public abstract AuthenticationProvider createAuthProvider(SecurityNamedServiceConfig config);

    /**
     * Returns the specific class of the user group service created by 
     * {@link #createUserGroupService(SecurityNamedServiceConfig)}.
     * <p>
     * If the extension does not provide a user group service this method should simply return
     * <code>null</code>. 
     * </p> 
     */
    public abstract Class<? extends GeoserverUserGroupService> getUserGroupServiceClass();
    
    /**
     * Creates a new user group service.
     * <p>
     * If the extension does not provide a user group service this method should simply return
     * <code>null</code>. 
     * </p>
     */
    public abstract GeoserverUserGroupService createUserGroupService(SecurityNamedServiceConfig config)
        throws IOException;

    /**
     * Returns the specific class of the role service created by 
     * {@link #createRoleService(SecurityNamedServiceConfig)}
     * <p>
     * If the extension does not provide a role service this method should simply return
     * <code>null</code>. 
     * </p> 
     */
    public abstract Class<? extends GeoserverRoleService> getRoleServiceClass();
    
    /**
     * Creates a new role group service.
     * <p>
     * If the extension does not provide a role service this method should simply return
     * <code>null</code>. 
     * </p>
     */
    public abstract GeoserverRoleService createRoleService(SecurityNamedServiceConfig config) 
        throws IOException;

    /**
     * Returns the specific class of the password validator created by 
     * {@link #createPasswordValidator(PasswordPolicyConfig))}.
     * <p>
     * If the extension does not provide a user group service this method should simply return
     * <code>null</code>. 
     * </p> 
     */
    public  Class<? extends PasswordValidator> getPasswordValidatorClass() {
        return PasswordValidatorImpl.class;
    }

    
    /**
     * Create the standard password validator
     * 
     * @param config
     * @return
     */
    public PasswordValidator createPasswordValidator(PasswordPolicyConfig config) {
        return new PasswordValidatorImpl();
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
}
