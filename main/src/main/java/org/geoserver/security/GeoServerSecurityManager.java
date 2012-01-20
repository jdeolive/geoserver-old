/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.security;

import static org.geoserver.data.util.IOUtils.xStreamPersist;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.server.UID;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.io.FileUtils;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.StoreInfo;
import org.geoserver.config.GeoServerDataDirectory;
import org.geoserver.config.util.XStreamPersister;
import org.geoserver.config.util.XStreamPersisterFactory;
import org.geoserver.platform.ContextLoadedEvent;
import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.security.FilterChainEntry.Position;
import org.geoserver.security.auth.GeoServerRootAuthenticationProvider;
import org.geoserver.security.concurrent.LockingRoleService;
import org.geoserver.security.concurrent.LockingUserGroupService;
import org.geoserver.security.config.FileBasedSecurityServiceConfig;
import org.geoserver.security.config.PasswordPolicyConfig;
import org.geoserver.security.config.SecurityAuthProviderConfig;
import org.geoserver.security.config.SecurityConfig;
import org.geoserver.security.config.SecurityManagerConfig;
import org.geoserver.security.config.SecurityNamedServiceConfig;
import org.geoserver.security.config.SecurityRoleServiceConfig;
import org.geoserver.security.config.SecurityUserGroupServiceConfig;
import org.geoserver.security.config.UsernamePasswordAuthenticationProviderConfig;
import org.geoserver.security.file.FileWatcher;
import org.geoserver.security.file.RoleFileWatcher;
import org.geoserver.security.file.UserGroupFileWatcher;
import org.geoserver.security.impl.GeoServerRole;
import org.geoserver.security.impl.GeoServerUser;
import org.geoserver.security.impl.Util;
import org.geoserver.security.password.ConfigurationPasswordEncryptionHelper;
import org.geoserver.security.password.GeoServerPBEPasswordEncoder;
import org.geoserver.security.password.GeoServerPasswordEncoder;
import org.geoserver.security.password.KeyStoreProvider;
import org.geoserver.security.password.PasswordValidator;
import org.geoserver.security.password.RandomPasswordProvider;
import org.geoserver.security.rememberme.GeoServerTokenBasedRememberMeServices;
import org.geoserver.security.rememberme.RememberMeServicesConfig;
import org.geoserver.security.validation.PasswordValidatorImpl;
import org.geoserver.security.validation.SecurityConfigException;
import org.geoserver.security.validation.SecurityConfigValidator;
import org.geoserver.security.xml.XMLConstants;
import org.geoserver.security.xml.XMLRoleService;
import org.geoserver.security.xml.XMLRoleServiceConfig;
import org.geoserver.security.xml.XMLUserGroupService;
import org.geoserver.security.xml.XMLUserGroupServiceConfig;
import org.geotools.util.logging.Logging;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.AnonymousAuthenticationProvider;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.RememberMeAuthenticationProvider;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.memory.UserAttribute;
import org.springframework.security.core.userdetails.memory.UserAttributeEditor;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.AbstractCollectionConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

/**
 * Top level singleton/facade/dao for the security authentication/authorization subsystem.  
 * 
 * Christian: implementing UserDetailsservice is temporary.
 * 
 * Reason: applicationSecurityContext.xml
 * 
   <bean id="rememberMeServices"
    class="org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices">
    <!--  TODO, temporary, use GeoserverSecurityManager as UserDetailService -->
    <property name="userDetailsService" ref="authenticationManager" />
    <property name="key" value="geoserver" />
  </bean>
 * 
 * The rememberMeServices Bean needs a UserDetailsService Object
 * 
 * @author Justin Deoliveira, OpenGeo
 *
 */
public class GeoServerSecurityManager extends ProviderManager implements ApplicationContextAware, 
    ApplicationListener, UserDetailsService {

    static Logger LOGGER = Logging.getLogger("org.geoserver.security");
    public static final String CONFIG_FILE_NAME = "config.xml";

    /** data directory file system access */
    GeoServerDataDirectory dataDir;

    /** app context for loading plugins */
    ApplicationContext appContext;

    /** the active role service */
    GeoServerRoleService activeRoleService;

    /** configured authentication providers */
    List<GeoServerAuthenticationProvider> authProviders;

    /** current security config */
    SecurityManagerConfig securityConfig = new SecurityManagerConfig();

    /** cached user groups */
    ConcurrentHashMap<String, GeoServerUserGroupService> userGroupServices = 
        new ConcurrentHashMap<String, GeoServerUserGroupService>();

    /** cached role services */
    ConcurrentHashMap<String, GeoServerRoleService> roleServices = 
        new ConcurrentHashMap<String, GeoServerRoleService>();
    
    /** cached password validators services */
    ConcurrentHashMap<String, PasswordValidator> passwordValidators = 
        new ConcurrentHashMap<String, PasswordValidator>();


    /** some helper instances for storing/loading service config */ 
    RoleServiceHelper roleServiceHelper = new RoleServiceHelper();
    UserGroupServiceHelper userGroupServiceHelper = new UserGroupServiceHelper();
    AuthProviderHelper authProviderHelper = new AuthProviderHelper();
    FilterHelper filterHelper = new FilterHelper();
    PasswordValidatorHelper  passwordValidatorHelper = new PasswordValidatorHelper();
    
    /** helper for encrypting store configuration parameters */
    ConfigurationPasswordEncryptionHelper configPasswordEncryptionHelper;

    /**
     * listeners
     */
    List<SecurityManagerListener> listeners = new ArrayList<SecurityManagerListener>();
    
    /** cached flag determining is strong cryptography is available */
    Boolean strongEncryptionAvaialble;
    
    /** flag set once the security manager has been fully initialized */
    boolean initialized = false;

    public GeoServerSecurityManager(GeoServerDataDirectory dataDir) throws Exception {
        this.dataDir = dataDir;
        configPasswordEncryptionHelper = new ConfigurationPasswordEncryptionHelper(this);
    }

    public Catalog getCatalog() {
        //have to look this up dynamically on demand on avoid circular dependency on application
        // context startup
        return (Catalog) GeoServerExtensions.bean("catalog");
    }

    public ConfigurationPasswordEncryptionHelper getConfigPasswordEncryptionHelper() {
        return configPasswordEncryptionHelper;
    }

    @Override
    public void setApplicationContext(ApplicationContext appContext) throws BeansException {
        this.appContext = appContext;
    }
    
    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ContextLoadedEvent) {
            // migrate from old security config
            try {
                migrateIfNecessary();
            } catch (Exception e1) {
                throw new RuntimeException(e1);
            }

            // read config and initialize... we do this now since we can be ensured that the spring
            // context has been property initialized, and we can successfully look up security
            // plugins
            try {
                init();
            } catch (Exception e) {
                throw new BeanCreationException("Error occured reading security configuration", e);
            }

            try {
                afterPropertiesSetInternal();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        if (event instanceof ContextClosedEvent) {
            try {
                destroy();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error destroying security manager", e);
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        //this is a bit o a hack but override and do nothing for now, we will call the super 
        // method later, after the app context is loaded, see afterPropertiesSetInternal()
    }

    void afterPropertiesSetInternal() throws Exception {
        super.afterPropertiesSet();
    }

    public void destroy() throws Exception {
        userGroupServices.clear();
        roleServices.clear();

        userGroupServiceHelper.destroy();
        roleServiceHelper.destroy();
    }

    /**
     * Adds a listener to the security manager.
     */
    public void addListener(SecurityManagerListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a listener to the security manager.
     */
    public void removeListener(SecurityManagerListener listener) {
        listeners.remove(listener);
    }

    /**
     * List of active/configured authentication providers
     */
    public List<GeoServerAuthenticationProvider> getAuthenticationProviders() {
        return authProviders;
    }

    /*
     * loads configuration and initializes the security subsystem.
     */
    void init() throws Exception {
        init(loadSecurityConfig());
    }

    void init(SecurityManagerConfig config) throws Exception {
        
        // first, prepare the keystore providing needed key material
        KeyStoreProvider.get().reloadKeyStore();

        //load the role authority and ensure it is properly configured
        String roleServiceName = config.getRoleServiceName();
        GeoServerRoleService roleService = null;
        try {
            roleService = loadRoleService(roleServiceName);
            
            //TODO:
            //if (!roleService.isConfigured()) {
            //    roleService = null;
            //}
        }
        catch(Exception e) {
            LOGGER.log(Level.WARNING, String.format("Error occured loading role service %s, "
                +  "falling back to default role service", roleServiceName), e);
        }

        if (roleService == null) {
            try {
                roleService = loadRoleService("default");
            }
            catch(Exception e) {
                throw new RuntimeException("Fatal error occurred loading default role service", e);
            }
        }

        //configure the user details instance
        setActiveRoleService(roleService);

        //set up authentication providers
        this.authProviders = new ArrayList<GeoServerAuthenticationProvider>();
        // first provider is for the root user
        this.authProviders.add(new GeoServerRootAuthenticationProvider());

        //add the custom/configured ones
        if(!config.getAuthProviderNames().isEmpty()) {
            for (String authProviderName : config.getAuthProviderNames()) {
                //TODO: handle failure here... perhaps simply disabling when auth provider
                // fails to load?
                GeoServerAuthenticationProvider authProvider = 
                    authProviderHelper.load(authProviderName);
                authProviders.add(authProvider);
            }
        }

        List<AuthenticationProvider> allAuthProviders = new ArrayList<AuthenticationProvider>();
        allAuthProviders.addAll(authProviders);

        //anonymous
        if (config.isAnonymousAuth()) {
            AnonymousAuthenticationProvider aap = new AnonymousAuthenticationProvider();
            aap.setKey("geoserver");
            aap.afterPropertiesSet();
            allAuthProviders.add(aap);
        }

        //remember me
        RememberMeAuthenticationProvider rap = new RememberMeAuthenticationProvider();
        rap.setKey(config.getRememberMeService().getKey());
        rap.afterPropertiesSet();
        allAuthProviders.add(rap);

        setProviders(allAuthProviders);

        this.securityConfig = new SecurityManagerConfig(config);
        this.initialized = true;
    }

    /**
     * Determines if the security manager has been initialized yet. 
     * <p>
     * TODO: this is a temporary hack, perhaps we should think about initializing the security 
     * subsystem as the very first thing on startup... but now we have dependencies on the catalog
     * so we cant. 
     * </p>
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Security configuration root directory.
     */
    public File getSecurityRoot() throws IOException {
        return dataDir.findOrCreateSecurityRoot(); 
    }

    /**
     * Role configuration root directory.
     */
    public File getRoleRoot() throws IOException {
        return getRoleRoot(true); 
    }

    public File getRoleRoot(boolean create) throws IOException {
        return create ? 
            dataDir.findOrCreateSecurityDir("role") : dataDir.findSecurityDir("role");
    }

    /**
     * Password policy configuration root directory
     */
    public File getPasswordPolicyRoot() throws IOException {
        return dataDir.findOrCreateSecurityDir("pwpolicy");
    }
    

    /**
     * User/group configuration root directory.
     */
    public File getUserGroupRoot() throws IOException {
        return dataDir.findOrCreateSecurityDir("usergroup");

    }

    /**
     * authentication configuration root directory.
     */
    public File getAuthRoot() throws IOException {
        return dataDir.findOrCreateSecurityDir("auth");
    }

    /**
     * authentication filter root directory.
     */
    public File getFilterRoot() throws IOException {
        return dataDir.findOrCreateSecurityDir("filter");
    }

    /**
     * Lists all available role service configurations.
     */
    public SortedSet<String> listRoleServices() throws IOException {
        return listFiles(getRoleRoot());
    }

    /**
     * Loads a role service from a named configuration.
     * 
     * @param name The name of the role service configuration.
     */
    public GeoServerRoleService loadRoleService(String name)
            throws IOException {
        GeoServerRoleService roleService = roleServices.get(name);
        if (roleService == null) {
            synchronized (this) {
                roleService = roleServices.get(name);
                if (roleService == null) {
                    roleService = roleServiceHelper.load(name);
                    if (roleService != null) {
                        roleServices.put(name, roleService);
                    }
                }
            }
        }
        return roleService;
    }
    
    /**
     * Loads a role {@link SecurityRoleServiceConfig} from a named configuration.
     * <code>null</code> if not found
     * 
     * @param name The name of the role service configuration.
     */
    public SecurityRoleServiceConfig loadRoleServiceConfig(String name)
            throws IOException {
              return  roleServiceHelper.loadConfig(name);
    }

    
    /**
     * Loads a password validator from a named configuration.
     * 
     * @param name The name of the password policy configuration.
     */
    public PasswordValidator loadPasswordValidator(String name)
            throws IOException {
        PasswordValidator validator = passwordValidators.get(name);
        if (validator == null) {
            synchronized (this) {
                validator = passwordValidators.get(name);
                if (validator == null) {
                    validator = passwordValidatorHelper.load(name);
                    if (validator != null) {
                        passwordValidators.put(name, validator);
                    }
                }
            }
        }
        return validator;
    }
    
    /**
     * Loads a password {@link PasswordPolicyConfig} from a named configuration.
     * <code>null</a> if not found
     * 
     * @param name The name of the password policy configuration.
     */
    public PasswordPolicyConfig loadPasswordPolicyConfig(String name) throws IOException {
        return  passwordValidatorHelper.loadConfig(name);
    }

    /**
     * Loads a password encoder with the specified name.
     * 
     * @return The password encoder, or <code>null</code> if non found matching the name.
     */
    public GeoServerPasswordEncoder loadPasswordEncoder(String name) {
        return (GeoServerPasswordEncoder) GeoServerExtensions.bean(name);
    }

    /**
     * Loads the first password encoder that matches the specified class filter.
     * <p>
     * This method is shorthand for:
     * <pre>
     *   loadPasswordEncoder(filter, null, null);
     * </pre>
     * </p> 
     *
     */
    public <T extends GeoServerPasswordEncoder> T loadPasswordEncoder(Class<T> filter) {
        return loadPasswordEncoder(filter, null, null);
    }

    /**
     * Loads the first password encoder that matches the specified criteria.
     * 
     * @param filter Class used to filter password encoders.
     * @param config Flag indicating if a reversible encoder is required, true forces reversible, 
     *  false forces irreversible, null means either.
     * @param strong Flag indicating if an encoder that supports strong encryption is required, true 
     *  forces strong encryption, false forces weak encryption, null means either.
     *  
     * @return The first encoder matching, or null if none was found.
     */
    public <T extends GeoServerPasswordEncoder> T loadPasswordEncoder(Class<T> filter, 
        Boolean reversible, Boolean strong) {
        List<T> pw = loadPasswordEncoders(filter, reversible, strong);
        return pw.isEmpty() ? null : pw.get(0);
    }

    /**
     * Looks up all available password encoders.
     */
    public List<GeoServerPasswordEncoder> loadPasswordEncoders() {
        return loadPasswordEncoders(null);
    }

    /**
     * Looks up all available password encoders filtering out only those that are instances of the
     * specified class.
     * <p>
     * This method is convenience for:
     * <pre>
     * loadPasswordEncoders(filter, null, null)
     * </pre> 
     * </p>
     */
    public <T extends GeoServerPasswordEncoder> List<T> loadPasswordEncoders(Class<T> filter) {
        return loadPasswordEncoders(filter, null, null);
    }

    /**
     * Loads all the password encoders that match the specified criteria.
     * 
     * @param filter Class used to filter password encoders.
     * @param config Flag indicating if a reversible encoder is required, true forces reversible, 
     *  false forces irreversible, null means either.
     * @param strong Flag indicating if an encoder that supports strong encryption is required, true 
     *  forces strong encryption, false forces weak encryption, null means either.
     *  
     * @return All matching encoders, or an empty list.
     */
    public <T extends GeoServerPasswordEncoder> List<T> loadPasswordEncoders(Class<T> filter, 
        Boolean reversible, Boolean strong) {
        
        filter = (Class<T>) (filter != null ? filter : GeoServerPasswordEncoder.class);

        List list = GeoServerExtensions.extensions(filter); 
        for (Iterator it = list.iterator(); it.hasNext(); ) {
            boolean remove = false;
            T pw = (T) it.next();
            if (reversible != null && !reversible.equals(pw.isReversible())) {
                remove = true;
            }
            if (!remove && strong != null && strong.equals(pw.isAvailableWithoutStrongCryptogaphy())) {
                remove = true;
            }
            
            if (remove) {
                it.remove();
            }
        }
        return list;
    }

    /**
     * Determines if strong encryption is available.
     * <p>
     * This method does the determination by trying to encrypt a value with AES 256 Bit encryption.
     * </p>
     * 
     * @return True if strong encryption avaialble, otherwise false.
     */
    public boolean isStrongEncryptionAvailable() {
        if (strongEncryptionAvaialble!=null)
            return strongEncryptionAvaialble;
        
        KeyGenerator kgen;
        try {
            kgen = KeyGenerator.getInstance("AES");
            kgen.init(256);
            SecretKey skey = kgen.generateKey();
            byte[] raw = skey.getEncoded();
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES");

            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            cipher.doFinal("This is just an example".getBytes());            
            strongEncryptionAvaialble = true;
            LOGGER.info("Strong cryptograhpy is available");
        } catch (InvalidKeyException e) {
            strongEncryptionAvaialble = false; 
            LOGGER.warning("Strong cryptograhpy is NOT available"+
            "\nDownload and install of policy files recommended"+
            "\nfrom http://www.oracle.com/technetwork/java/javase/downloads/jce-6-download-429243.html");
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Strong cryptograhpy is NOT available, unexpected error", ex);
            strongEncryptionAvaialble =false; //should not happen
        }
        return strongEncryptionAvaialble;
    }

    /**
     * Saves/persists a role service configuration.
     */
    public void saveRoleService(SecurityRoleServiceConfig config) 
            throws IOException,SecurityConfigException {
        SecurityConfigValidator validator = 
                SecurityConfigValidator.getConfigurationValiator(
                        GeoServerRoleService.class,
                        config.getClassName());

        if (config.getId() == null) {
            config.initBeforeSave();
            validator.validateAddRoleService(config);
        }
        else {
            validator.validateModifiedRoleService(config,
                    roleServiceHelper.loadConfig(config.getName()));
        }

        roleServiceHelper.saveConfig(config);
        // remove from cache
        roleServices.remove(config.getName());

    }
    
    /**
     * Saves/persists a password policy configuration.
     */
    public void savePasswordPolicy(PasswordPolicyConfig config) 
            throws IOException,SecurityConfigException {
        SecurityConfigValidator validator = 
                SecurityConfigValidator.getConfigurationValiator(
                        PasswordValidator.class,
                        config.getClassName());

        if (config.getId() == null) {
            config.initBeforeSave();
            validator.validateAddPasswordPolicy(config);
        }
        else {
            validator.validateModifiedPasswordPolicy(config,
                    passwordValidatorHelper.loadConfig(config.getName()));
        }
        
        passwordValidatorHelper.saveConfig(config);
    }


    /**
     * Removes a role service configuration.
     * 
     * @param name The  role service configuration.
     */
    public void removeRoleService(SecurityRoleServiceConfig config) throws IOException,SecurityConfigException {

        
        SecurityConfigValidator validator = 
                SecurityConfigValidator.getConfigurationValiator(
                        GeoServerRoleService.class,
                        config.getClassName());

        validator.validateRemoveRoleService(config);
        
        roleServices.remove(config.getName());
        roleServiceHelper.removeConfig(config.getName());
    }
    
    /**
     * Removes a password validator configuration.
     * 
     * @param  The  password validator configuration.
     */
    public void removePasswordValidator(PasswordPolicyConfig config) throws IOException,SecurityConfigException {
        SecurityConfigValidator validator = 
                SecurityConfigValidator.getConfigurationValiator(
                        PasswordValidator.class,
                        config.getClassName());

        validator.validateRemovePasswordPolicy(config);
        passwordValidators.remove(config.getName());        
        passwordValidatorHelper.removeConfig(config.getName());
    }


    /**
     * Lists all available user group service configurations.
     */
    public SortedSet<String> listUserGroupServices() throws IOException {
        return listFiles(getUserGroupRoot());
    }
    
    /**
     * Lists all available password Validators.
     */
    public SortedSet<String> listPasswordValidators() throws IOException {
        return listFiles(getPasswordPolicyRoot());
    }


    /**
     * Loads a user group service from a named configuration.
     * 
     * @param name The name of the user group service configuration.
     */
    public GeoServerUserGroupService loadUserGroupService(String name) throws IOException {
        GeoServerUserGroupService ugService = userGroupServices.get(name);
        if (ugService == null) {
            synchronized (this) {
                ugService = userGroupServices.get(name);
                if (ugService == null) {
                    ugService = userGroupServiceHelper.load(name);
                    if (ugService != null) {
                        userGroupServices.put(name, ugService);
                    }
                }
            }
        }
        return ugService;
    }
    
    /**
     * Loads a user {@link SecurityUserGroupServiceConfig} from a named configuration.
     * <code>null</code> if not foun
     * 
     * @param name The name of the user group service configuration.
     */
    public SecurityUserGroupServiceConfig loadUserGroupServiceConfig(String name) throws IOException {
        return userGroupServiceHelper.loadConfig(name);
    }


    /**
     * Saves/persists a user group service configuration.
     */
    public void saveUserGroupService(SecurityUserGroupServiceConfig config) 
            throws IOException,SecurityConfigException {
        SecurityConfigValidator validator = 
                SecurityConfigValidator.getConfigurationValiator(
                        GeoServerUserGroupService.class,
                        config.getClassName());

        if (config.getId() == null) {
            config.initBeforeSave();
            validator.validateAddUserGroupService(config);
        }
        else { 
            validator.validateModifiedUserGroupService(config,
                    userGroupServiceHelper.loadConfig(config.getName()));
        }

        userGroupServiceHelper.saveConfig(config);
        // remove from cache
        userGroupServices.remove(config.getName());

    }

    /**
     * Removes a user group service configuration.
     * 
     * @param name The  user group service configuration.
     */
    public void removeUserGroupService(SecurityUserGroupServiceConfig config) throws IOException,SecurityConfigException {
        
        SecurityConfigValidator validator = 
                SecurityConfigValidator.getConfigurationValiator(
                        GeoServerUserGroupService.class,
                        config.getClassName());

        validator.validateRemoveUserGroupService(config);
                 
        userGroupServices.remove(config.getName());
        userGroupServiceHelper.removeConfig(config.getName());
    }

    /**
     * Lists all available authentication provider configurations.
     */
    public SortedSet<String> listAuthenticationProviders() throws IOException {
        return listFiles(getAuthRoot());
    }

    /**
     * Loads an authentication provider from a named configuration.
     * 
     * @param name The name of the authentication provider service configuration.
     */
    public GeoServerAuthenticationProvider loadAuthenticationProvider(String name) throws IOException {
        return authProviderHelper.load(name);
    }
    
    /**
     * Loads an authentication provider config from a named configuration.
     * <code>null</code> if not found
     * 
     * @param name The name of the authentication provider service configuration.
     */
    public SecurityAuthProviderConfig loadAuthenticationProviderConfig(String name) throws IOException {
        return authProviderHelper.loadConfig(name);
    }

    
    public void saveAuthenticationProvider(SecurityAuthProviderConfig config) 
            throws IOException,SecurityConfigException {
        SecurityConfigValidator validator = 
                SecurityConfigValidator.getConfigurationValiator(GeoServerAuthenticationProvider.class,
                        config.getClassName());

        if (config.getId() == null) {
            config.initBeforeSave();
            validator.validateAddAuthProvider(config);
        }
        else { 
            validator.validateModifiedAuthProvider(config,
                    authProviderHelper.loadConfig(config.getName()));
        }
        authProviderHelper.saveConfig(config);
    }

    /**
     * Lists all available authentication provider configurations.
     */
    public SortedSet<String> listFilters() throws IOException {
        return listFiles(getFilterRoot());
    }

    /**
     * Loads an authentication provider from a named configuration.
     * 
     * @param name The name of the authentication provider service configuration.
     */
    public GeoServerSecurityFilter loadFilter(String name) throws IOException {
        return filterHelper.load(name);
    }
    
    /**
     * Loads an authentication provider config from a named configuration.
     * <code>null</a> if not found
     * 
     * @param name The name of the authentication provider service configuration.
     */
    public SecurityNamedServiceConfig loadFilterConfig(String name) throws IOException {
        return filterHelper.loadConfig(name);
    }

    
    public void saveFilter(SecurityNamedServiceConfig config) 
            throws IOException,SecurityConfigException {
        // TODO
//        SecurityConfigValidator validator = 
//                SecurityConfigValidator.getConfigurationValiator(
//                        GeoserverAuthenticationProcessingFilter.class,
//                        config.getClassName());
//        if (isNew)
//            validator.validateAddFilter(config);
//        else
//            validator.validateModifiedFilter(config,
//                    filterHelper.loadConfig(config.getName()));

        if (config.getId() == null) {
            config.initBeforeSave();
        }
        filterHelper.saveConfig(config);
    }
    
    /**
     * Removes an authentication provider configuration.
     * 
     * @param name The  authentication provider configuration.
     */
    public void removeAuthenticationProvider(SecurityAuthProviderConfig config) throws IOException,SecurityConfigException {        
        SecurityConfigValidator validator = 
                SecurityConfigValidator.getConfigurationValiator(GeoServerAuthenticationProvider.class,
                        config.getClassName());
        validator.validateRemoveAuthProvider(config);        
        authProviderHelper.removeConfig(config.getName());
    }
    

    public void removeAuthenticationFilter(SecurityNamedServiceConfig config) throws IOException,SecurityConfigException {
        // TODO
//        SecurityConfigValidator validator = 
//                SecurityConfigValidator.getConfigurationValiator(
//                        GeoserverAuthenticationProcessingFilter.class,
//                        config.getClassName());
//        validator.validateRemoveFilter(config);        
        filterHelper.removeConfig(config.getName());
    }


    /**
     * Returns the current security configuration.
     * <p>
     * In order to make changes to the security configuration client code may make changes to this 
     * object directly, but must call {@link #saveSecurityConfig(SecurityManagerConfig)} in order
     * to persist changes.
     * </p>
     */
    public SecurityManagerConfig getSecurityConfig() {
        return new SecurityManagerConfig(this.securityConfig);
    }

    /*
     * saves the global security config
     * TODO: use read/write lock rather than full synchronied
     */
    public synchronized void saveSecurityConfig(SecurityManagerConfig config) throws Exception {
        
        SecurityConfigValidator validator = new SecurityConfigValidator(this);
        validator.validateManagerConfig(config);
        //save the current config to fall back to                
        SecurityManagerConfig oldConfig = new SecurityManagerConfig(this.securityConfig);

        try {
            //set the new configuration
            init(config);
            if (config.getConfigPasswordEncrypterName().equals(
                    oldConfig.getConfigPasswordEncrypterName())==false){
                updateConfigurationFilesWithEncryptedFields();
            }

            //save out new configuration
            xStreamPersist(new File(getSecurityRoot(), CONFIG_FILE_NAME), config, globalPersister());
        }
        catch(IOException e) {
            //exception, revert back to known working config
            LOGGER.log(Level.SEVERE, "Error saving security config, reverting back to previous", e);
            init(oldConfig);
            return;
        }

        fireChanged();
    }

    void fireChanged() {
        for (SecurityManagerListener l : listeners) {
            l.handlePostChanged(this);
        }
    }

    /*
     * converts an old security configuration to the new
     */
    void migrateIfNecessary() throws Exception{
        
        if (getRoleRoot(false) != null) {
            File oldUserFile = new File(getSecurityRoot(), "users.properties.old");
            if (oldUserFile.exists()) {
                LOGGER.warning(oldUserFile.getCanonicalPath()+" could be removed manually");
            }
            return; // already migrated
        }
        
        LOGGER.info("Start security migration");
        
        //create required directories
        getRoleRoot();
        getUserGroupRoot();
        getAuthRoot();
        getPasswordPolicyRoot();
        getFilterRoot();

        // check for service.properties, create if necessary
        File serviceFile = new File(getSecurityRoot(), "service.properties");
        if (serviceFile.exists()==false) {
            FileUtils.copyURLToFile(Util.class.getResource("serviceTemplate.properties"),
                    serviceFile);
        }

        long checkInterval = 10000; // 10 secs

        //check for the default user group service, create if necessary
        GeoServerUserGroupService userGroupService = 
            loadUserGroupService(XMLUserGroupService.DEFAULT_NAME);

        KeyStoreProvider.get().reloadKeyStore();
        KeyStoreProvider.get().setUserGroupKey(
                XMLUserGroupService.DEFAULT_NAME, RandomPasswordProvider.get().getRandomPassword(32));
        KeyStoreProvider.get().storeKeyStore();
        
        PasswordValidator validator = 
                loadPasswordValidator(PasswordValidator.DEFAULT_NAME);
        if (validator==null) {
            // Policy allows any password except null, this is the default
            // at before migration
            PasswordPolicyConfig pwpconfig = new PasswordPolicyConfig();
            pwpconfig.setName(PasswordValidator.DEFAULT_NAME);
            pwpconfig.setClassName(PasswordValidatorImpl.class.getName());
            pwpconfig.setMinLength(0);
            savePasswordPolicy(pwpconfig);
            validator = loadPasswordValidator(PasswordValidator.DEFAULT_NAME);    
        }

        validator = loadPasswordValidator(PasswordValidator.MASTERPASSWORD_NAME); 
        if (validator==null) {
            // Policy requires a minimum of 8 chars for the master password            
            PasswordPolicyConfig pwpconfig = new PasswordPolicyConfig();
            pwpconfig.setName(PasswordValidator.MASTERPASSWORD_NAME);
            pwpconfig.setClassName(PasswordValidatorImpl.class.getName());
            pwpconfig.setMinLength(8);
            savePasswordPolicy(pwpconfig);
            validator = loadPasswordValidator(PasswordValidator.MASTERPASSWORD_NAME);    
        }
                
        if (userGroupService == null) {
            XMLUserGroupServiceConfig ugConfig = new XMLUserGroupServiceConfig();            
            ugConfig.setName(XMLUserGroupService.DEFAULT_NAME);
            ugConfig.setClassName(XMLUserGroupService.class.getName());
            ugConfig.setCheckInterval(checkInterval); 
            ugConfig.setFileName(XMLConstants.FILE_UR);            
            ugConfig.setValidating(true);
            // start with weak encryption, plain passwords can be restored
            ugConfig.setPasswordEncoderName(
                loadPasswordEncoder(GeoServerPBEPasswordEncoder.class, null, false).getName());
            ugConfig.setPasswordPolicyName(PasswordValidator.DEFAULT_NAME);
            saveUserGroupService(ugConfig);
            userGroupService = loadUserGroupService(XMLUserGroupService.DEFAULT_NAME);
        }

        //check for the default role service, create if necessary
        GeoServerRoleService roleService = 
            loadRoleService(XMLRoleService.DEFAULT_NAME);

        if (roleService == null) {
            XMLRoleServiceConfig gaConfig = new XMLRoleServiceConfig();                 
            gaConfig.setName(XMLRoleService.DEFAULT_NAME);
            gaConfig.setClassName(XMLRoleService.class.getName());
            gaConfig.setCheckInterval(checkInterval); 
            gaConfig.setFileName(XMLConstants.FILE_RR);
            gaConfig.setValidating(true);
            gaConfig.setAdminRoleName(GeoServerRole.ADMIN_ROLE.getAuthority());
            saveRoleService(gaConfig);
            roleService = loadRoleService(XMLRoleService.DEFAULT_NAME);
        }
        

        //check for the default auth provider, create if necessary
        GeoServerAuthenticationProvider authProvider = (GeoServerAuthenticationProvider) 
            loadAuthenticationProvider(GeoServerAuthenticationProvider.DEFAULT_NAME);
        if (authProvider == null) {
            UsernamePasswordAuthenticationProviderConfig upAuthConfig = 
                    new UsernamePasswordAuthenticationProviderConfig();
            upAuthConfig.setName(GeoServerAuthenticationProvider.DEFAULT_NAME);
            upAuthConfig.setClassName(UsernamePasswordAuthenticationProvider.class.getName());
            upAuthConfig.setUserGroupServiceName(userGroupService.getName());

            saveAuthenticationProvider(upAuthConfig);
            authProvider = loadAuthenticationProvider(GeoServerAuthenticationProvider.DEFAULT_NAME);

        }

        //save the top level config
        SecurityManagerConfig config = new SecurityManagerConfig();
        config.setRoleServiceName(roleService.getName());
        config.getAuthProviderNames().add(authProvider.getName());
        config.setEncryptingUrlParams(false);

        // start with weak encryption
        config.setConfigPasswordEncrypterName(
            loadPasswordEncoder(GeoServerPBEPasswordEncoder.class, true, false).getName());

        // setup the default remember me service
        RememberMeServicesConfig rememberMeConfig = new RememberMeServicesConfig();
        rememberMeConfig.setClassName(GeoServerTokenBasedRememberMeServices.class.getName());
        rememberMeConfig.setUserGroupService(userGroupService.getName());
        config.setRememberMeService(rememberMeConfig);

        saveSecurityConfig(config);

        //TODO: just call initializeFrom
        userGroupService.setSecurityManager(this);
        roleService.setSecurityManager(this);

        //populate the user group and role service
        GeoServerUserGroupStore userGroupStore = userGroupService.createStore();
        GeoServerRoleStore roleStore = roleService.createStore();

        //migradate from users.properties
        File usersFile = new File(getSecurityRoot(), "users.properties");
        if (usersFile.exists()) {
            //load user.properties populate the services 
            Properties props = Util.loadPropertyFile(usersFile);

            UserAttributeEditor configAttribEd = new UserAttributeEditor();

            for (Iterator<Object> iter = props.keySet().iterator(); iter.hasNext();) {
                // the attribute editors parses the list of strings into password, username and enabled
                // flag
                String username = (String) iter.next();
                configAttribEd.setAsText(props.getProperty(username));

                // if the parsing succeeded turn that into a user object
                UserAttribute attr = (UserAttribute) configAttribEd.getValue();
                if (attr != null) {
                    GeoServerUser user = 
                        userGroupStore.createUserObject(username, attr.getPassword(), attr.isEnabled());
                    userGroupStore.addUser(user);

                    for (GrantedAuthority auth : attr.getAuthorities()) {
                        GeoServerRole role = 
                            roleStore.getRoleByName(auth.getAuthority());
                        if (role==null) {
                            role = roleStore.createRoleObject(auth.getAuthority());
                            roleStore.addRole(role);
                        }
                        roleStore.associateRoleToUser(role, username);
                    }
                }
            }
        } else  {
            // no user.properties, populate with default user and roles
            if (userGroupService.getUserByUsername(GeoServerUser.AdminName) == null) {
                userGroupStore.addUser(GeoServerUser.createDefaultAdmin());
                roleStore.addRole(GeoServerRole.ADMIN_ROLE);
                roleStore.associateRoleToUser(GeoServerRole.ADMIN_ROLE,
                        GeoServerUser.AdminName);
            }
        }

        // check for roles in service.properties but not in user.properties 
        serviceFile = new File(getSecurityRoot(), "service.properties");
        if (serviceFile.exists()) {
            Properties props = Util.loadPropertyFile(serviceFile);
            for (Entry<Object,Object> entry: props.entrySet()) {
                StringTokenizer tokenizer = new StringTokenizer((String)entry.getValue(),",");
                while (tokenizer.hasMoreTokens()) {
                    String roleName = tokenizer.nextToken().trim();
                    if (roleName.length()>0) {
                        if (roleStore.getRoleByName(roleName)==null)
                            roleStore.addRole(roleStore.createRoleObject(roleName));
                    }
                }
            }
        }

        // check for  roles in data.properties but not in user.properties
        File dataFile = new File(getSecurityRoot(), "layer.properties");
        if (dataFile.exists()) {
            Properties props = Util.loadPropertyFile(dataFile);
            for (Entry<Object,Object> entry: props.entrySet()) {
                if ("mode".equals(entry.getKey().toString()))
                    continue; // skip mode directive
                StringTokenizer tokenizer = new StringTokenizer((String)entry.getValue(),",");
                while (tokenizer.hasMoreTokens()) {
                    String roleName = tokenizer.nextToken().trim();
                    if (roleName.length()>0 && roleName.equals("*")==false) {
                        if (roleStore.getRoleByName(roleName)==null)
                            roleStore.addRole(roleStore.createRoleObject(roleName));
                    }
                }
            }
        }

        //persist the changes
        roleStore.store();
        userGroupStore.store();
        
        // first part of migration finished, rename old file
        if (usersFile.exists()) {
            File oldUserFile = new File(usersFile.getCanonicalPath()+".old");
            usersFile.renameTo(oldUserFile);
            LOGGER.info("Renamed "+usersFile.getCanonicalPath() + " to " +
                    oldUserFile.getCanonicalPath());
        }
                        
        LOGGER.info("End security migration");
    }

    /*
     * looks up security plugins
     */
    List<GeoServerSecurityProvider> lookupSecurityProviders() {
        List<GeoServerSecurityProvider> list = new ArrayList<GeoServerSecurityProvider>( 
            GeoServerExtensions.extensions(GeoServerSecurityProvider.class, appContext));

        //add the defaults
        // list.add(new XMLSecurityProvider());
        // list.add(new UsernamePasswordAuthenticationProvider.SecurityProvider());
        return list;
    }

    /*
     * list files in a directory.
     */
    SortedSet<String> listFiles(File dir) {
        SortedSet<String> result = new TreeSet<String>();
        File[] dirs = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory() && new File(pathname, CONFIG_FILE_NAME).exists();
            }
        });
        for (File d : dirs) {
            result.add(d.getName());
        }
        return result;
    }

    XStreamPersister globalPersister() throws IOException {
        XStreamPersister xp = persister();
        xp.getXStream().alias("security", SecurityManagerConfig.class);
        xp.getXStream().registerLocalConverter( SecurityManagerConfig.class, "filterChain", 
            new FilterChainConverter(xp.getXStream().getMapper()));
        
        return xp;
    }

    /*
     * creates the persister for security plugin configuration.
     */
    XStreamPersister persister() throws IOException{
        List<GeoServerSecurityProvider> all = lookupSecurityProviders();
        
        //create and configure an xstream persister to load the configuration files
        XStreamPersister xp = new XStreamPersisterFactory().createXMLPersister();
        xp.getXStream().alias("security", SecurityManagerConfig.class);
        
        for (GeoServerSecurityProvider roleService : all) {
            roleService.configure(xp);
        }
        return xp;
    }

    /*
     * loads the global security config
     */
    public SecurityManagerConfig loadSecurityConfig() throws IOException {
        return (SecurityManagerConfig) loadConfigFile(getSecurityRoot(), globalPersister());
    }

    /**
     * reads a file named {@value #CONFIG_FILE_NAME} from the specified directly using the specified xstream 
     * persister
     */
    SecurityConfig loadConfigFile(File directory, XStreamPersister xp) throws IOException {
        FileInputStream fin = new FileInputStream(new File(directory, CONFIG_FILE_NAME));
        try {
            return xp.load(fin, SecurityConfig.class);
        }
        finally {
            fin.close();
        }
    }

    /**
     * saves a file named {@value #CONFIG_FILE_NAME} from the specified directly using the specified xstream 
     * persister
     */
    void saveConfigFile(SecurityConfig config, File directory, XStreamPersister xp) 
            throws IOException {
        
        xStreamPersist(new File(directory, CONFIG_FILE_NAME), config, xp);
    }

    abstract class HelperBase<T, C extends SecurityNamedServiceConfig> {
        /*
         * list of file watchers
         * TODO: we should probably manage these better rather than just throwing them in a 
         * list, repeated loads will cause this list to fill up with threads
         */
        protected List<FileWatcher> fileWatchers = new ArrayList<FileWatcher>();

        public abstract T load(String name) throws IOException;

        /**
         * loads the named entity config from persistence
         */
        public C loadConfig(String name) throws IOException {
            File dir = new File(getRoot(), name);
            if (!dir.exists()) {
                return null;
            }

            XStreamPersister xp = persister();
            return (C) loadConfigFile(dir, xp);
        }

        /**
         * saves the user group service config to persistence
         */
        public void saveConfig(SecurityNamedServiceConfig config) throws IOException {
            File dir = new File(getRoot(), config.getName());
            dir.mkdir();

            boolean isNew = config.getId() == null;
            if (isNew) {
                config.setId(newId());
            }
            try {
                saveConfigFile(config, dir, persister());
            }
            catch(Exception e) {
                //catch exception, if the config was new, clear out the id since it was not added
                if (isNew) {
                    config.setId(null);
                }
                if (e instanceof IOException) {
                    throw (IOException)e;
                }
                throw new IOException(e);
            }
        }

        String newId() {
            return new UID().toString();
        }

        /**
         * removes the user group service config from persistence
         */
        public void removeConfig(String name) throws IOException {
            FileUtils.deleteDirectory(new File(getRoot(), name));
        }

        public void destroy() {
            for (FileWatcher fw : fileWatchers) {
                fw.setTerminate(true);
            }
        }

        /**
         * config root
         */
        protected abstract File getRoot() throws IOException;
    }
    class UserGroupServiceHelper extends HelperBase<GeoServerUserGroupService,SecurityUserGroupServiceConfig> {
        public GeoServerUserGroupService load(String name) throws IOException {
            
            SecurityNamedServiceConfig config = loadConfig(name);
            if (config == null) {
                //no such config
                return null;
            }

            //look up the service for this config
            GeoServerUserGroupService service = null;

            for (GeoServerSecurityProvider p : lookupSecurityProviders()) {
                if (p.getUserGroupServiceClass() == null) {
                    continue;
                }
                if (p.getUserGroupServiceClass().getName().equals(config.getClassName())) {
                    service = p.createUserGroupService(config);
                    break;
                }
            }

            if (service == null) {
                throw new IOException("No user group service matching config: " + config);
            }

            service.setSecurityManager(GeoServerSecurityManager.this);
            if (config instanceof SecurityUserGroupServiceConfig){
                boolean needsLockProtection =
                        GeoServerSecurityProvider.getProvider(GeoServerUserGroupService.class, 
                        config.getClassName()).roleServiceNeedsLockProtection();
                if (needsLockProtection)
                        service = new LockingUserGroupService(service);
            }
            service.setName(name);
            service.initializeFromConfig(config);
            
            if (config instanceof FileBasedSecurityServiceConfig) {
                FileBasedSecurityServiceConfig fileConfig = 
                    (FileBasedSecurityServiceConfig) config;
                if (fileConfig.getCheckInterval()>0) {
                    File file = new File(fileConfig.getFileName());
                    if (file.isAbsolute()==false) 
                        file = new File(new File(getUserGroupRoot(), name), file.getPath());
                    if (file.canRead()==false) {
                        throw new IOException("Cannot read file: "+file.getCanonicalPath());
                    }
                    UserGroupFileWatcher watcher = new 
                        UserGroupFileWatcher(file.getCanonicalPath(),service,file.lastModified());
                    watcher.setDelay(fileConfig.getCheckInterval());
                    service.registerUserGroupLoadedListener(watcher);
                    watcher.start();

                    //register the watcher so we can kill it later on disposale
                    fileWatchers.add(watcher);
                }
            }
            
            return service;
        }
        
        @Override
        protected File getRoot() throws IOException {
            return getUserGroupRoot();
        }
    }

    class RoleServiceHelper extends HelperBase<GeoServerRoleService,SecurityRoleServiceConfig>{

         /**
         * Loads the role service for the named config from persistence.
         */
        public GeoServerRoleService load(String name) throws IOException {
            
            SecurityNamedServiceConfig config = loadConfig(name);
            if (config == null) {
                //no such config
                return null;
            }

            //look up the service for this config
            GeoServerRoleService service = null;

            for (GeoServerSecurityProvider p  : lookupSecurityProviders()) {
                if (p.getRoleServiceClass() == null) {
                    continue;
                }
                if (p.getRoleServiceClass().getName().equals(config.getClassName())) {
                    service = p.createRoleService(config);
                    break;
                }
            }

            if (service == null) {
                throw new IOException("No authority service matching config: " + config);
            }
            service.setSecurityManager(GeoServerSecurityManager.this);

            if (config instanceof SecurityRoleServiceConfig){
                boolean needsLockProtection =
                        GeoServerSecurityProvider.getProvider(GeoServerRoleService.class, 
                        config.getClassName()).roleServiceNeedsLockProtection();
                if (needsLockProtection)
                        service = new LockingRoleService(service);
            }            
            service.setName(name);

            //TODO: do we need this anymore?
            service.initializeFromConfig(config);

            if (config instanceof FileBasedSecurityServiceConfig) {
                FileBasedSecurityServiceConfig fileConfig = 
                    (FileBasedSecurityServiceConfig) config;
                if (fileConfig.getCheckInterval()>0) {
                    File file = new File(fileConfig.getFileName());
                    if (file.isAbsolute()==false) 
                        file = new File(new File(getRoleRoot(), name), file.getPath());
                    if (file.canRead()==false) {
                        throw new IOException("Cannot read file: "+file.getCanonicalPath());
                    }
                    RoleFileWatcher watcher = new 
                        RoleFileWatcher(file.getCanonicalPath(),service,file.lastModified());
                    watcher.setDelay(fileConfig.getCheckInterval());
                    service.registerRoleLoadedListener(watcher);
                    watcher.start();

                    //register the watcher so we can kill it later
                    fileWatchers.add(watcher);
                }
            }

            return service;
        }

        @Override
        protected File getRoot() throws IOException {
            return getRoleRoot();
        }
    }


    class PasswordValidatorHelper extends HelperBase<PasswordValidator,PasswordPolicyConfig> {

        /**
        * Loads the password policy for the named config from persistence.
        */
       public PasswordValidator load(String name) throws IOException {
           
           PasswordPolicyConfig config = loadConfig(name);
           if (config == null) {
               //no such config
               return null;
           }

           //look up the validator for this config
           PasswordValidator validator = null;

           for (GeoServerSecurityProvider p  : lookupSecurityProviders()) {
               if (p.getPasswordValidatorClass() == null) {                   
                   continue;
               }
               if (p.getPasswordValidatorClass().getName().equals(config.getClassName())) {
                   validator = p.createPasswordValidator(config, GeoServerSecurityManager.this);
                   break;
               }    
           }
           if (validator == null) {
               throw new IOException("No password policy matching config: " + config);
           }

           validator.setConfig(config);
           return validator;
       }

       @Override
       protected File getRoot() throws IOException {
           return getPasswordPolicyRoot();
       }
   }
    
    
    /**
     *
     * @return the active {@link GeoServerRoleService}
     */
    public GeoServerRoleService getActiveRoleService() {
        return activeRoleService;
    }

    /**
     * set the active {@link GeoServerRoleService}
     * @param activeRoleService
     */
    public void setActiveRoleService(GeoServerRoleService activeRoleService) {
        this.activeRoleService = activeRoleService;
    }

    /**
     * Temporary, need by rememberMeServices
     *  
     * @see org.springframework.security.core.userdetails.UserDetailsService#loadUserByUsername(java.lang.String)
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException,
            DataAccessException {
        // TODO, get rid of this
        throw new RuntimeException("Should not reach thsi point");
    }

    /**
     * rewrites configuration files with encrypted fields. 
     * Candidates:
     * {@link StoreInfo} from the {@link Catalog}
     * {@link SecurityNamedServiceConfig} objects from the security directory
     * @param catalog
     */
    public void updateConfigurationFilesWithEncryptedFields() throws IOException{
        // rewrite stores in catalog
        LOGGER.info("Start encrypting configuration passwords using " + 
            getSecurityConfig().getConfigPasswordEncrypterName());

        Catalog catalog = getCatalog();
        List<StoreInfo> stores = catalog.getStores(StoreInfo.class);
        for (StoreInfo info : stores) {
            if (!configPasswordEncryptionHelper.getEncryptedFields(info).isEmpty()) {
                catalog.save(info);
            }
        }

        Set<Class<?>> configClasses = new HashSet<Class<?>>();
        
        // filter the interesting classes ones
        for (GeoServerSecurityProvider prov: lookupSecurityProviders()) {
           configClasses.addAll(prov.getFieldsForEncryption().keySet());
        }

        for (String name : listPasswordValidators()) {
            PasswordPolicyConfig config = passwordValidatorHelper.loadConfig(name);
            for (Class<?> classWithEncryption : configClasses) {
                if (config.getClass().isAssignableFrom(classWithEncryption)) {
                    passwordValidatorHelper.saveConfig(config);
                    break;
                }                    
            }
        }
        for (String name : listRoleServices()) {
            SecurityNamedServiceConfig config = roleServiceHelper.loadConfig(name);
            for (Class<?> classWithEncryption : configClasses) {
                if (config.getClass().isAssignableFrom(classWithEncryption)) {
                    roleServiceHelper.saveConfig(config);
                    break;
                }                    
            }
        }
        for (String name : listUserGroupServices()) {
            SecurityNamedServiceConfig config = userGroupServiceHelper.loadConfig(name);
            for (Class<?> classWithEncryption : configClasses) {
                if (config.getClass().isAssignableFrom(classWithEncryption)) {
                    userGroupServiceHelper.saveConfig(config);
                    break;
                }                    
            }
        }
        
        for (String name : listAuthenticationProviders()) {
            SecurityNamedServiceConfig config = authProviderHelper.loadConfig(name);
            for (Class<?> classWithEncryption : configClasses) {
                if (config.getClass().isAssignableFrom(classWithEncryption)) {
                    authProviderHelper.saveConfig(config);
                    break;
                }                    
            }
        }
        
        for (String name : listFilters()) {
            SecurityNamedServiceConfig config = filterHelper.loadConfig(name);
            for (Class<?> classWithEncryption : configClasses) {
                if (config.getClass().isAssignableFrom(classWithEncryption)) {
                    filterHelper.saveConfig(config);
                    break;
                }                    
            }
        }
        LOGGER.info("End encrypting configuration passwords");
    }
 
    class AuthProviderHelper extends HelperBase<GeoServerAuthenticationProvider, SecurityAuthProviderConfig>{

        /**
         * Loads the auth provider for the named config from persistence.
         */
        public GeoServerAuthenticationProvider load(String name) throws IOException {
            
            SecurityNamedServiceConfig config = loadConfig(name);
            if (config == null) {
                //no such config
                return null;
            }

            //look up the service for this config
            GeoServerAuthenticationProvider authProvider = null;

            for (GeoServerSecurityProvider p  : lookupSecurityProviders()) {
                if (p.getAuthenticationProviderClass() == null) {
                    continue;
                }
                if (p.getAuthenticationProviderClass().getName().equals(config.getClassName())) {
                    authProvider = p.createAuthenticationProvider(config);
                    break;
                }
            }

            if (authProvider == null) {
                throw new IOException("No authentication provider matching config: " + config);
            }

            authProvider.setName(name);
            authProvider.setSecurityManager(GeoServerSecurityManager.this);
            authProvider.initializeFromConfig(config);

            return authProvider;
        }

        @Override
        protected File getRoot() throws IOException {
             return getAuthRoot();
        }
    }

    class FilterHelper extends HelperBase<GeoServerSecurityFilter, SecurityNamedServiceConfig>{
        /**
         * Loads the filter for the named config from persistence.
         */
        public GeoServerSecurityFilter load(String name) throws IOException {
            
            SecurityNamedServiceConfig config = loadConfig(name);
            if (config == null) {
                //no such config
                return null;
            }

            //look up the service for this config
            GeoServerSecurityFilter filter = null;

            for (GeoServerSecurityProvider p  : lookupSecurityProviders()) {
                if (p.getFilterClass() == null) {
                    continue;
                }
                if (p.getFilterClass().getName().equals(config.getClassName())) {
                    filter = p.createFilter(config);
                    break;
                }
            }

            if (filter == null) {
                throw new IOException("No authentication provider matching config: " + config);
            }

            filter.setName(name);
            filter.setSecurityManager(GeoServerSecurityManager.this);
            filter.initializeFromConfig(config);

            return filter;
        }

        @Override
        protected File getRoot() throws IOException {
            return getFilterRoot();
        }
    }

    /**
     * custom converter for filter chain
     */
    static class FilterChainConverter extends AbstractCollectionConverter {

        public FilterChainConverter(Mapper mapper) {
            super(mapper);
        }

        @Override
        public boolean canConvert(Class type) {
            return GeoServerSecurityFilterChain.class.isAssignableFrom(type);
        }

        @Override
        public void marshal(Object source, HierarchicalStreamWriter writer,
                MarshallingContext context) {
            GeoServerSecurityFilterChain filterChain = (GeoServerSecurityFilterChain) source;
            for (Map.Entry<String, List<FilterChainEntry>> e : filterChain.entrySet()) {
            
                //<filterChain>
                //  <filters path="...">
                //    <filter>name1</filter>
                //    <filter>name2</filter>
                //    ...
                writer.startNode("filters");
                writer.addAttribute("path", e.getKey());
                
                for (FilterChainEntry filterEntry : e.getValue()) {
                    writer.startNode("filter");

                    Position pos = filterEntry.getPosition();
                    writer.addAttribute("position", pos.name());
                    if (pos == Position.BEFORE || pos == Position.AFTER) {
                        writer.addAttribute("relativeTo", filterEntry.getRelativeTo());
                    }

                    writer.setValue(filterEntry.getFilterName());
                    
                    writer.endNode();
                }

                writer.endNode();
            }
        }

        @Override
        public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
            GeoServerSecurityFilterChain filterChain = new GeoServerSecurityFilterChain();
            while(reader.hasMoreChildren()) {
                
                //<filters path="..."
                reader.moveDown();
                String path = reader.getAttribute("path");

                //<filter
                List<FilterChainEntry> filterEntries = new ArrayList<FilterChainEntry>();
                while(reader.hasMoreChildren()) {
                    reader.moveDown();
                    String name = reader.getValue();
                    Position pos = Position.valueOf(reader.getAttribute("position"));
                    String relativeTo = reader.getAttribute("relativeTo");
                    
                    filterEntries.add(new FilterChainEntry(name, pos, relativeTo));
                    reader.moveUp();
                }

                filterChain.put(path, filterEntries);
                reader.moveUp();
            }
            
            return filterChain;
        }

    }
}
