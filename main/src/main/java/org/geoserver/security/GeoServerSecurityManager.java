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
import java.util.ArrayList;
import java.util.Arrays;
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

import org.apache.commons.io.FileUtils;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.StoreInfo;
import org.geoserver.config.GeoServerDataDirectory;
import org.geoserver.config.util.XStreamPersister;
import org.geoserver.config.util.XStreamPersisterFactory;
import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.security.concurrent.LockingRoleService;
import org.geoserver.security.concurrent.LockingUserGroupService;
import org.geoserver.security.config.FileBasedSecurityServiceConfig;
import org.geoserver.security.config.PasswordPolicyConfig;
import org.geoserver.security.config.SecurityConfig;
import org.geoserver.security.config.SecurityManagerConfig;
import org.geoserver.security.config.SecurityNamedServiceConfig;
import org.geoserver.security.config.SecurityRoleServiceConfig;
import org.geoserver.security.config.SecurityUserGoupServiceConfig;
import org.geoserver.security.config.impl.PasswordPolicyConfigImpl;
import org.geoserver.security.config.impl.SecurityManagerConfigImpl;
import org.geoserver.security.config.impl.XMLFileBasedRoleServiceConfigImpl;
import org.geoserver.security.config.impl.XMLFileBasedUserGroupServiceConfigImpl;
import org.geoserver.security.file.RoleFileWatcher;
import org.geoserver.security.file.UserGroupFileWatcher;
import org.geoserver.security.impl.GeoserverRole;
import org.geoserver.security.impl.GeoserverUser;
import org.geoserver.security.impl.Util;
import org.geoserver.security.password.ConfigurationPasswordHelper;
import org.geoserver.security.password.GeoserverConfigPBEPasswordEncoder;
import org.geoserver.security.password.GeoserverUserPBEPasswordEncoder;
import org.geoserver.security.password.KeyStoreProvider;
import org.geoserver.security.password.PasswordValidationException;
import org.geoserver.security.password.PasswordValidator;
import org.geoserver.security.password.PasswordValidatorImpl;
import org.geoserver.security.password.RandomPasswordProvider;
import org.geoserver.security.xml.XMLConstants;
import org.geoserver.security.xml.XMLRoleService;
import org.geoserver.security.xml.XMLSecurityProvider;
import org.geoserver.security.xml.XMLUserGroupService;
import org.geotools.util.logging.Logging;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
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

import static org.geoserver.security.GeoServerSecurityFilterChain.*;

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
public class GeoServerSecurityManager extends ProviderManager implements ApplicationContextAware, UserDetailsService {

    static Logger LOGGER = Logging.getLogger("org.geoserver.security");
    public static final String CONFIG_FILE_NAME = "config.xml";
    

    /** data directory file system access */
    GeoServerDataDirectory dataDir;

    /** app context for loading plugins */
    ApplicationContext appContext;

    /** the active role service */
    GeoserverRoleService activeRoleService;
    
    private boolean encryptingUrlParams;
    private String configPasswordEncrypterName;
    


    /** configured authentication providers */
    List<GeoServerAuthenticationProvider> authProviders;

    /** current security config */
    SecurityManagerConfig securityConfig = new SecurityManagerConfigImpl();

    /** cached user groups */
    ConcurrentHashMap<String, GeoserverUserGroupService> userGroupServices = 
        new ConcurrentHashMap<String, GeoserverUserGroupService>();

    /** cached role services */
    ConcurrentHashMap<String, GeoserverRoleService> roleServices = 
        new ConcurrentHashMap<String, GeoserverRoleService>();
    
    /** cached role services */
    ConcurrentHashMap<String, PasswordValidator> passwordValidators = 
        new ConcurrentHashMap<String, PasswordValidator>();


    /** some helper instances for storing/loading service config */ 
    RoleServiceHelper roleServiceHelper = new RoleServiceHelper();
    UserGroupServiceHelper userGroupServiceHelper = new UserGroupServiceHelper();
    AuthProviderHelper authProviderHelper = new AuthProviderHelper();
    FilterHelper filterHelper = new FilterHelper();

    /**
     * listeners
     */
    List<SecurityManagerListener> listeners = new ArrayList<SecurityManagerListener>();

    PasswordValidatorHelper  passwordValidatorHelper = new PasswordValidatorHelper();

    public GeoServerSecurityManager( 
        GeoServerDataDirectory dataDir) throws Exception {
        
        this.dataDir = dataDir;

        //migrate from old security config
        migrateIfNecessary();
    }

    @Override
    public void setApplicationContext(ApplicationContext appContext) throws BeansException {
        this.appContext = appContext;

        //read config and initialize... we do this now since we can be ensured that the spring
        // context has been property initialized, and we can successfully look up security plugins
        try {
            init();
        } catch (Exception e) {
            throw new BeanCreationException("Error occured reading security configuration", e);
        }
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

        setConfigPasswordEncrypterName(config.getConfigPasswordEncrypterName());
        setEncryptingUrlParams(config.isEncryptingUrlParams());


        //load the role authority and ensure it is properly configured
        String roleServiceName = config.getRoleServiceName();
        GeoserverRoleService roleService = null;
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
        rap.setKey("geoserver");
        rap.afterPropertiesSet();
        allAuthProviders.add(rap);

        setProviders(allAuthProviders);

        this.securityConfig = new SecurityManagerConfigImpl(config);
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
    public GeoserverRoleService loadRoleService(String name)
            throws IOException {
        GeoserverRoleService roleService = roleServices.get(name);
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
     * Saves/persists a role service configuration.
     */
    public void saveRoleService(SecurityNamedServiceConfig config) throws IOException {
        roleServiceHelper.saveConfig(config);
    }
    
    /**
     * Saves/persists a password policy configuration.
     */
    public void savePasswordPolicy(PasswordPolicyConfig config) throws IOException {
        passwordValidatorHelper.saveConfig(config);
    }


    /**
     * Removes a role service configuration.
     * 
     * @param name The name of the role service configuration.
     */
    public void removeRoleService(String name) throws IOException {
        //remove the service
        if (getActiveRoleService() != null && getActiveRoleService().getName().equals(name)) {
            throw new IllegalArgumentException("Can't delete active authority service: " + name);
        }

        //remove the cached service
        roleServices.remove(name);
        
        //remove the config dir
        roleServiceHelper.removeConfig(name);
    }
    
    /**
     * Removes a password validator configuration.
     * 
     * @param name The name of the password validator configuration.
     */
    public void removePasswordValidator(String name) throws IOException {
        
        if (PasswordValidator.DEFAULT_NAME.equals(name) || 
                PasswordValidator.MASTERPASSWORD_NAME.equals(name))
            throw new IllegalArgumentException("Can't delete password policy: " + name);
        
        for (String ugName : listFiles(getUserGroupRoot())) {
            GeoserverUserGroupService service = loadUserGroupService(ugName);
            if (name.equals(service.getPasswordValidatorName())) {
                throw new IllegalArgumentException("Can't delete password policy: " + name+
                        " still used in UserGroupService: "+service.getName());
            }
        }
        
        passwordValidators.remove(name);        
        //remove the config dir
        passwordValidatorHelper.removeConfig(name);
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
    public GeoserverUserGroupService loadUserGroupService(String name) throws IOException {
        GeoserverUserGroupService ugService = userGroupServices.get(name);
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
     * Saves/persists a user group service configuration.
     */
    public void saveUserGroupService(SecurityNamedServiceConfig config) throws IOException {
        userGroupServiceHelper.saveConfig(config);
    }

    /**
     * Removes a user group service configuration.
     * 
     * @param name The name of the user group service configuration.
     */
    public void removeUserGroupService(String name) throws IOException {
        //remove the service
        
        
        
         
        // First, I we have only one usergroupservice, we cannot delete it.
        if (userGroupServices.size()==1)
            throw new IllegalArgumentException("Can't delete last user group service: " + name);
        
        // TODO: this check is more complicated.
        // Second, if deletion would also delete the last user with ROLE_ADMINSTRATOR,
        // we have to refuse deletion. To be implemented
        
        //remove the cached service
        userGroupServices.remove(name);
        
        //remove the config dir
        userGroupServiceHelper.removeConfig(name);
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
    
    public void saveAuthenticationProvider(SecurityNamedServiceConfig config) throws IOException {
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
    
    public void saveFilter(SecurityNamedServiceConfig config) throws IOException {
        filterHelper.saveConfig(config);
    }
    
//    /**
//     * Removes an authentication provider configuration.
//     * 
//     * @param name The name of the authentication provider configuration.
//     */
//    public void removeAuthenticationProvider(String name) throws IOException {
//        //TODO:remove from cached list
//        
//        authProviderHelper.removeConfig(name);
//    }

    /**
     * Returns the current security configuration.
     * <p>
     * In order to make changes to the security configuration client code may make changes to this 
     * object directly, but must call {@link #saveSecurityConfig(SecurityManagerConfig)} in order
     * to persist changes.
     * </p>
     */
    public SecurityManagerConfig getSecurityConfig() {
        return new SecurityManagerConfigImpl(this.securityConfig);
    }

    /*
     * saves the global security config
     * TODO: use read/write lock rather than full synchronied
     */
    public synchronized void saveSecurityConfig(SecurityManagerConfig config) throws Exception {
        //save the current config to fall back to
        SecurityManagerConfig oldConfig = new SecurityManagerConfigImpl(this.securityConfig);

        try {
            //set the new configuration
            init(config);

            //save out new configuration
            xStreamPersist(new File(getSecurityRoot(), "config.xml"), config, globalPersister());
        }
        catch(Exception e) {
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
        GeoserverUserGroupService userGroupService = 
            loadUserGroupService(XMLUserGroupService.DEFAULT_NAME);

        KeyStoreProvider.get().reloadKeyStore();
        KeyStoreProvider.get().setUserGroupKey(
                XMLUserGroupService.DEFAULT_NAME, RandomPasswordProvider.get().getRandomPassword(32));
        KeyStoreProvider.get().storeKeyStore();
        
        if (userGroupService == null) {
            XMLFileBasedUserGroupServiceConfigImpl ugConfig = new XMLFileBasedUserGroupServiceConfigImpl();            
            ugConfig.setName(XMLUserGroupService.DEFAULT_NAME);
            ugConfig.setClassName(XMLUserGroupService.class.getName());
            ugConfig.setCheckInterval(checkInterval); 
            ugConfig.setFileName(XMLConstants.FILE_UR);            
            ugConfig.setLockingNeeded(true);
            ugConfig.setValidating(true);
            // start with weak encryption, plain passwords can be restored
            ugConfig.setPasswordEncoderName(GeoserverUserPBEPasswordEncoder.PrototypeName);
            ugConfig.setPasswordPolicyName(PasswordValidator.DEFAULT_NAME);
            saveUserGroupService(ugConfig);
            userGroupService = loadUserGroupService(XMLUserGroupService.DEFAULT_NAME);
        }

        //check for the default role service, create if necessary
        GeoserverRoleService roleService = 
            loadRoleService(XMLRoleService.DEFAULT_NAME);

        if (roleService == null) {
            XMLFileBasedRoleServiceConfigImpl gaConfig = new XMLFileBasedRoleServiceConfigImpl();                 
            gaConfig.setName(XMLRoleService.DEFAULT_NAME);
            gaConfig.setClassName(XMLRoleService.class.getName());
            gaConfig.setCheckInterval(checkInterval); 
            gaConfig.setFileName(XMLConstants.FILE_RR);
            gaConfig.setLockingNeeded(true);
            gaConfig.setValidating(true);
            gaConfig.setAdminRoleName(GeoserverRole.ADMIN_ROLE.getAuthority());
            saveRoleService(gaConfig);
            roleService = loadRoleService(XMLRoleService.DEFAULT_NAME);
        }
        
        PasswordValidator validator = 
                loadPasswordValidator(PasswordValidator.DEFAULT_NAME);
        if (validator==null) {
            // Policy allows any password except null, this is the default
            // at before migration
            PasswordPolicyConfig pwpconfig = new PasswordPolicyConfigImpl();
            pwpconfig.setName(PasswordValidator.DEFAULT_NAME);
            pwpconfig.setClassName(PasswordValidatorImpl.class.getName());
            savePasswordPolicy(pwpconfig);
            validator = loadPasswordValidator(PasswordValidator.DEFAULT_NAME);    
        }

        validator = loadPasswordValidator(PasswordValidator.MASTERPASSWORD_NAME); 
        if (validator==null) {
            // Policy requires a minimum of 8 chars for the master password            
            PasswordPolicyConfig pwpconfig = new PasswordPolicyConfigImpl();
            pwpconfig.setName(PasswordValidator.MASTERPASSWORD_NAME);
            pwpconfig.setClassName(PasswordValidatorImpl.class.getName());
            pwpconfig.setMinLength(8);
            savePasswordPolicy(pwpconfig);
            validator = loadPasswordValidator(PasswordValidator.MASTERPASSWORD_NAME);    
        }

        //check for the default auth provider, create if necessary
        GeoServerAuthenticationProvider authProvider = 
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

        //setup the default filter chain
        GeoServerSecurityFilterChain filterChain = new GeoServerSecurityFilterChain();
        
        filterChain.put("/web/**", Arrays.asList(SECURITY_CONTEXT_ASC_FILTER, LOGOUT_FILTER, 
            FORM_LOGIN_FILTER, SERVLET_API_SUPPORT_FILTER, REMEMBER_ME_FILTER, ANONYMOUS_FILTER, 
            EXCEPTION_TRANSLATION_FILTER, FILTER_SECURITY_INTERCEPTOR));

        filterChain.put("/j_spring_security_check/**", Arrays.asList(SECURITY_CONTEXT_ASC_FILTER, 
            LOGOUT_FILTER, FORM_LOGIN_FILTER, SERVLET_API_SUPPORT_FILTER, REMEMBER_ME_FILTER, 
            ANONYMOUS_FILTER, EXCEPTION_TRANSLATION_FILTER, FILTER_SECURITY_INTERCEPTOR));
        
        filterChain.put("/j_spring_security_logout/**", Arrays.asList(SECURITY_CONTEXT_ASC_FILTER, 
            LOGOUT_FILTER, FORM_LOGIN_FILTER, SERVLET_API_SUPPORT_FILTER, REMEMBER_ME_FILTER, 
            ANONYMOUS_FILTER, EXCEPTION_TRANSLATION_FILTER, FILTER_SECURITY_INTERCEPTOR));
        
        filterChain.put("/rest/**", Arrays.asList(SECURITY_CONTEXT_NO_ASC_FILTER, BASIC_AUTH_FILTER,
            ANONYMOUS_FILTER, EXCEPTION_TRANSLATION_OWS_FILTER, FILTER_SECURITY_REST_INTERCEPTOR));

        filterChain.put("/gwc/rest/web/**", Arrays.asList(ANONYMOUS_FILTER, 
            EXCEPTION_TRANSLATION_FILTER, FILTER_SECURITY_INTERCEPTOR));

        filterChain.put("/gwc/rest/**", Arrays.asList(SECURITY_CONTEXT_NO_ASC_FILTER, 
            BASIC_AUTH_NO_REMEMBER_ME_FILTER, EXCEPTION_TRANSLATION_OWS_FILTER, 
            FILTER_SECURITY_REST_INTERCEPTOR));
        
        filterChain.put("/**", Arrays.asList(SECURITY_CONTEXT_NO_ASC_FILTER, BASIC_AUTH_FILTER, 
            ANONYMOUS_FILTER, EXCEPTION_TRANSLATION_OWS_FILTER, FILTER_SECURITY_INTERCEPTOR));

        //save the top level config
        SecurityManagerConfigImpl config = new SecurityManagerConfigImpl();
        config.setRoleServiceName(roleService.getName());
        config.getAuthProviderNames().add(authProvider.getName());
        config.setEncryptingUrlParams(false);
        // start with weak encryption
        config.setConfigPasswordEncrypterName(GeoserverConfigPBEPasswordEncoder.BeanName);
        config.setFilterChain(filterChain);

        saveSecurityConfig(config);

        //TODO: just call initializeFrom
        userGroupService.setSecurityManager(this);
        roleService.setSecurityManager(this);

        //populate the user group and role service
        GeoserverUserGroupStore userGroupStore = userGroupService.createStore();
        GeoserverRoleStore roleStore = roleService.createStore();

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
                    GeoserverUser user = 
                        userGroupStore.createUserObject(username, attr.getPassword(), attr.isEnabled());
                    try {
                        userGroupStore.addUser(user);
                    } catch (PasswordValidationException e) {
                        throw new IOException(e);
                    }

                    for (GrantedAuthority auth : attr.getAuthorities()) {
                        GeoserverRole role = 
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
            if (userGroupService.getUserByUsername(GeoserverUser.AdminName) == null) {
                try {
                    userGroupStore.addUser(GeoserverUser.createDefaultAdmin());
                } catch (PasswordValidationException e) {
                    throw new IOException(e);
                }
                roleStore.addRole(GeoserverRole.ADMIN_ROLE);
                roleStore.associateRoleToUser(GeoserverRole.ADMIN_ROLE,
                        GeoserverUser.AdminName);
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
        list.add(new XMLSecurityProvider());
        list.add(new UsernamePasswordAuthenticationProvider.SecurityProvider());
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
                return pathname.isDirectory();
            }
        });
        for (File d : dirs) {
            result.add(d.getName());
        }
        return result;
    }

    XStreamPersister globalPersister() throws IOException {
        XStreamPersister xp = persister();
        xp.getXStream().alias("security", SecurityManagerConfigImpl.class);
        xp.getXStream().registerLocalConverter( SecurityManagerConfigImpl.class, "filterChain", 
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
        xp.getXStream().alias("security", SecurityManagerConfigImpl.class);
        
        for (GeoServerSecurityProvider roleService : all) {
            roleService.configure(xp);
        }
        return xp;
    }

    /*
     * loads the global security config
     */
    SecurityManagerConfig loadSecurityConfig() throws IOException {
        return (SecurityManagerConfig) loadConfigFile(getSecurityRoot(), globalPersister());
    }

    /*
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

            saveConfigFile(config, dir, persister());
        }

        /**
         * removes the user group service config from persistence
         */
        public void removeConfig(String name) throws IOException {
            FileUtils.deleteDirectory(new File(getRoot(), name));
        }

        /**
         * config root
         */
        protected abstract File getRoot() throws IOException;
    }
    class UserGroupServiceHelper extends HelperBase<GeoserverUserGroupService,SecurityNamedServiceConfig> {
        public GeoserverUserGroupService load(String name) throws IOException {
            
            SecurityNamedServiceConfig config = loadConfig(name);
            if (config == null) {
                //no such config
                return null;
            }

            //look up the service for this config
            GeoserverUserGroupService service = null;

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
            if (config instanceof SecurityUserGoupServiceConfig){
                if (((SecurityUserGoupServiceConfig) config).isLockingNeeded())
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
                }
            }
            
            return service;
        }
        
        @Override
        protected File getRoot() throws IOException {
            return getUserGroupRoot();
        }
    }

    class RoleServiceHelper extends HelperBase<GeoserverRoleService,SecurityNamedServiceConfig>{

         /**
         * Loads the role service for the named config from persistence.
         */
        public GeoserverRoleService load(String name) throws IOException {
            
            SecurityNamedServiceConfig config = loadConfig(name);
            if (config == null) {
                //no such config
                return null;
            }

            //look up the service for this config
            GeoserverRoleService service = null;

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
                if (((SecurityRoleServiceConfig) config).isLockingNeeded())
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
                   validator = p.createPasswordValidator(config);
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
     * @return the active {@link GeoserverRoleService}
     */
    public GeoserverRoleService getActiveRoleService() {
        return activeRoleService;
    }

    /**
     * set the active {@link GeoserverRoleService}
     * @param activeRoleService
     */
    public void setActiveRoleService(GeoserverRoleService activeRoleService) {
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

    public boolean isEncryptingUrlParams() {
        return encryptingUrlParams;
    }

    public void setEncryptingUrlParams(boolean encryptingUrlParams) {
        this.encryptingUrlParams = encryptingUrlParams;
    }

    public String getConfigPasswordEncrypterName() {
        return configPasswordEncrypterName;
    }
    public void setConfigPasswordEncrypterName(String configPasswordEncrypterName) {
        this.configPasswordEncrypterName = configPasswordEncrypterName;
    }

    /**
     * rewrites configuration files with encrypted fields. 
     * Candidates:
     * {@link StoreInfo} from the {@link Catalog}
     * {@link SecurityNamedServiceConfig} objects from the security directory
     * @param catalog
     */
    public  void updateConfigurationFilesWithEncryptedFields() throws IOException{
        // rewrite stores in catalog
        Catalog catalog = (Catalog)
                GeoServerExtensions.bean("catalog");
        List<StoreInfo> stores = catalog.getStores(StoreInfo.class);
        for (StoreInfo info : stores) {
            if (ConfigurationPasswordHelper.getEncryptionFields(info).isEmpty()==false)
                catalog.save(info);
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
        // TODO, add rewrite for auth configurations
        
    }
 
    class AuthProviderHelper extends HelperBase<GeoServerAuthenticationProvider, SecurityNamedServiceConfig>{
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
            for (Map.Entry<String, List<String>> e : filterChain.entrySet()) {
            
                //<filterChain>
                //  <filters path="...">
                //    <filter>name1</filter>
                //    <filter>name2</filter>
                //    ...
                writer.startNode("filters");
                writer.addAttribute("path", e.getKey());
                
                for (String filterName : e.getValue()) {
                    writer.startNode("filter");
                    writer.setValue(filterName);
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
                List<String> filterNames = new ArrayList<String>();
                while(reader.hasMoreChildren()) {
                    reader.moveDown();
                    filterNames.add(reader.getValue());
                    reader.moveUp();
                }

                filterChain.put(path, filterNames);
                reader.moveUp();
            }
            
            return filterChain;
        }

    }
}
