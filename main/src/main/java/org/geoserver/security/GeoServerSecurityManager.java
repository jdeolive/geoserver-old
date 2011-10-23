/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.security;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.geoserver.config.GeoServerDataDirectory;
import org.geoserver.config.util.XStreamPersister;
import org.geoserver.config.util.XStreamPersisterFactory;
import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.security.concurrent.LockingRoleService;
import org.geoserver.security.concurrent.LockingUserGroupService;
import org.geoserver.security.config.FileBasedSecurityServiceConfig;
import org.geoserver.security.config.SecurityConfig;
import org.geoserver.security.config.SecurityNamedServiceConfig;
import org.geoserver.security.config.SecurityManagerConfig;
import org.geoserver.security.config.impl.SecurityManagerConfigImpl;
import org.geoserver.security.config.impl.XMLFileBasedSecurityServiceConfigImpl;

import org.geoserver.security.file.RoleFileWatcher;
import org.geoserver.security.file.UserGroupFileWatcher;
import org.geoserver.security.impl.GeoserverRole;
import org.geoserver.security.impl.GeoserverUser;
import org.geoserver.security.impl.Util;
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
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.memory.UserAttribute;
import org.springframework.security.core.userdetails.memory.UserAttributeEditor;

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
    

    /** data directory file system access */
    GeoServerDataDirectory dataDir;

    /** app context for loading plugins */
    ApplicationContext appContext;

    /** the active role service */
    GeoserverRoleService activeRoleService;
    
    /** the active user group servie */
    // TODO, this is needed for current migration !!!! 
    // We have to remove this later. There is no single
    // active usergroup serverGeoServerSecurityManager
    GeoserverUserGroupService activeUserGroupService;

        
    public GeoserverUserGroupService getActiveUserGroupService() {
        return activeUserGroupService;
    }

    public void setActiveUserGroupService(GeoserverUserGroupService activeUserGroupService) {
        this.activeUserGroupService = activeUserGroupService;
    }

    /** cached user groups */
    ConcurrentHashMap<String, GeoserverUserGroupService> userGroupServices = 
        new ConcurrentHashMap<String, GeoserverUserGroupService>();

    /** cached role services */
    ConcurrentHashMap<String, GeoserverRoleService> roleServices = 
        new ConcurrentHashMap<String, GeoserverRoleService>();

    /** some helper instances for storing/loading service config */ 
    RoleServiceHelper roleServiceHelper = new RoleServiceHelper();
    UserGroupServiceHelper userGroupServiceHelper = new UserGroupServiceHelper();
    //AuthProviderHelper authProviderHelper = new AuthProviderHelper();

    public GeoServerSecurityManager( 
        GeoServerDataDirectory dataDir) throws IOException {
        
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

    /*
     * loads configuration and initializes the security subsystem.
     */
    void init() throws Exception {
        SecurityManagerConfig config = loadSecurityConfig();

        //load the user group service and ensure it is properly configured
        String userGroupServiceName = config.getUserGroupServiceName();
        GeoserverUserGroupService userGroupService = null;
        try {
            userGroupService = loadUserGroupService(userGroupServiceName);
            
            //TODO:
            //if (!userGroupService.isConfigured()) {
            //    userGroupService = null;
            //}
        }
        catch(Exception e) {
            LOGGER.log(Level.WARNING, String.format("Error occured loading user group service %s, "
                +  "falling back to default user group service", userGroupServiceName), e);
        }

        if (userGroupService == null) {
            try {
                userGroupService = loadUserGroupService("default");
            }
            catch(Exception e) {
                throw new RuntimeException("Fatal error occurred loading default role service", e);
            }
        }

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
        setActiveUserGroupService(userGroupService);
        setActiveRoleService(roleService);

        //set up authentcation providers
        List<AuthenticationProvider> authProviders = new ArrayList<AuthenticationProvider>();

        //add the custom one
        /*if (config.getAuthProviderName() != null) {
            authProvider = authProviderHelper.load(config.getAuthProviderName());
            authProviders.add(daoAuthProvider);
        }*/

        //dao based authentication that wraps user service
        DaoAuthenticationProvider daoAuthProvider = new DaoAuthenticationProvider();
        daoAuthProvider.setUserDetailsService(getActiveUserGroupService());
        daoAuthProvider.afterPropertiesSet();
        authProviders.add(daoAuthProvider);

        //anonymous
        AnonymousAuthenticationProvider aap = new AnonymousAuthenticationProvider();
        aap.setKey("geoserver");
        aap.afterPropertiesSet();
        authProviders.add(aap);

        //remember me
        RememberMeAuthenticationProvider rap = new RememberMeAuthenticationProvider();
        rap.setKey("geoserver");
        rap.afterPropertiesSet();
        authProviders.add(rap);

        setProviders(authProviders);
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

    File getRoleRoot(boolean create) throws IOException {
        return create ? 
            dataDir.findOrCreateSecurityDir("role") : dataDir.findSecurityDir("role"); 
    }

    /**
     * User/group configuration root directory.
     */
    public File getUserGroupRoot() throws IOException {
        return dataDir.findOrCreateSecurityDir("usergroup"); 
    }

//    /**
//     * authentication configuration root directory.
//     */
//    public File getAuthRoot() throws IOException {
//        return dataDir.findOrCreateSecurityDir("auth");
//    }

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
     * Saves/persists a role service configuration.
     */
    public void saveRoleService(SecurityNamedServiceConfig config) throws IOException {
        roleServiceHelper.saveConfig(config);
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
     * Lists all available user group service configurations.
     */
    public SortedSet<String> listUserGroupServices() throws IOException {
        return listFiles(getUserGroupRoot());
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

    /*
     * converts an old security configuration to the new
     */
    void migrateIfNecessary() throws IOException{
        
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
        //getAuthRoot();
        
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

        if (userGroupService == null) {
            XMLFileBasedSecurityServiceConfigImpl ugConfig = new XMLFileBasedSecurityServiceConfigImpl();
            ugConfig.setName(XMLUserGroupService.DEFAULT_NAME);
            ugConfig.setClassName(XMLUserGroupService.class.getName());
            ugConfig.setCheckInterval(checkInterval); 
            ugConfig.setFileName(XMLConstants.FILE_UR);
            ugConfig.setStateless(false);
            ugConfig.setValidating(true);
            saveUserGroupService(ugConfig);
            userGroupService = loadUserGroupService(XMLUserGroupService.DEFAULT_NAME);
        }

        //check for the default role service, create if necessary
        GeoserverRoleService roleService = 
            loadRoleService(XMLRoleService.DEFAULT_NAME);

        if (roleService == null) {
            XMLFileBasedSecurityServiceConfigImpl gaConfig = new XMLFileBasedSecurityServiceConfigImpl();                 
            gaConfig.setName(XMLRoleService.DEFAULT_NAME);
            gaConfig.setClassName(XMLRoleService.class.getName());
            gaConfig.setCheckInterval(checkInterval); 
            gaConfig.setFileName(XMLConstants.FILE_RR);
            gaConfig.setStateless(false);
            gaConfig.setValidating(true);
            saveRoleService(gaConfig);

            roleService = loadRoleService(XMLRoleService.DEFAULT_NAME);
        }

        //save the top level config
        SecurityManagerConfig config = new SecurityManagerConfigImpl();
        config.setRoleServiceName(XMLRoleService.DEFAULT_NAME);
        config.setUserGroupServiceName(XMLUserGroupService.DEFAULT_NAME);
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
                    userGroupStore.addUser(user);

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
                userGroupStore.addUser(GeoserverUser.createDefaultAdmin());
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
        list.add(new XMLSecurityProvider());
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
        
//      Not needed anymore, property Name has chanted        
//        xp.getXStream().aliasField("roleServiceName", 
//            SecurityManagerConfigImpl.class, "grantedAuthorityServiceName");
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
     * saves the global security config
     */
    void saveSecurityConfig(SecurityManagerConfig config) throws IOException {
        FileOutputStream fout = new FileOutputStream(new File(getSecurityRoot(), "config.xml"));
        try {
            XStreamPersister xp = globalPersister();
            xp.save(config, fout); 
        }
        finally {
            fout.close();
        }
    }

    /*
     * reads a file named 'config.xml' from the specified directly using the specified xstream 
     * persister
     */
    SecurityConfig loadConfigFile(File directory, XStreamPersister xp) throws IOException {
        FileInputStream fin = new FileInputStream(new File(directory, "config.xml"));
        try {
            return xp.load(fin, SecurityConfig.class);
        }
        finally {
            fin.close();
        }
    }

    /*
     * saves a file named 'config.xml' from the specified directly using the specified xstream 
     * persister
     */
    void saveConfigFile(SecurityConfig config, File directory, XStreamPersister xp) 
            throws IOException {
        //TODO: do a safe save, where we write first to a temp file to avoid corrupting the 
        // existing file in case of an error during serialization
        FileOutputStream fout = new FileOutputStream(new File(directory, "config.xml"));
        try {
            xp.save(config, fout);
        }
        finally {
            fout.close();
        }
    }

    class UserGroupServiceHelper {
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
            if (!config.isStateless()) {
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

        /**
         * loads the named user group service config from persistence
         */
        public SecurityNamedServiceConfig loadConfig(String name) throws IOException {
            File dir = new File(getUserGroupRoot(), name);
            if (!dir.exists()) {
                return null;
            }

            XStreamPersister xp = persister();
            return (SecurityNamedServiceConfig) loadConfigFile(dir, xp);
        }

        /**
         * saves the user group service config to persistence
         */
        public void saveConfig(SecurityNamedServiceConfig config) throws IOException {
            File dir = new File(getUserGroupRoot(), config.getName());
            dir.mkdir();

            saveConfigFile(config, dir, persister());
        }

        /**
         * removes the user group service config from persistence
         */
        public void removeConfig(String name) throws IOException {
            FileUtils.deleteDirectory(new File(getUserGroupRoot(), name));
        }
    }

    class RoleServiceHelper {

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

            //TODO: we should probably create a new instance of the service config... or mandate
            // that authority service beans be prototype beans and look them up every time
            if (!config.isStateless()) {
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

        /**
         * loads the named authority service config from persistence
         */
        public SecurityNamedServiceConfig loadConfig(String name) throws IOException {
            File dir = new File(getRoleRoot(), name);
            if (!dir.exists()) {
                return null;
            }

            XStreamPersister xp = persister();
            return (SecurityNamedServiceConfig) loadConfigFile(dir, xp);
        }

        /**
         * saves the authority service config to persistence
         */
        public void saveConfig(SecurityNamedServiceConfig config) throws IOException {
            File dir = new File(getRoleRoot(), config.getName());
            dir.mkdir();
            saveConfigFile(config, dir, persister());
        }

        /**
         * removes the authority service config from persistence
         */
        public void removeConfig(String name) throws IOException {
            FileUtils.deleteDirectory(new File(getRoleRoot(), name));
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
        return getActiveUserGroupService().loadUserByUsername(username);
    }
    
    
//    class AuthProviderHelper {
//        /**
//         * Loads the auth provider for the named config from persistence.
//         */
//        public AuthenticationProvider load(String name) throws IOException {
//            
//            SecurityNamedServiceConfig config = loadConfig(name);
//            if (config == null) {
//                //no such config
//                return null;
//            }
//
//            //look up the service for this config
//            AuthenticationProvider authProvider = null;
//
//            for (GeoServerSecurityProvider p  : lookupSecurityProviders()) {
//                if (p.getAuthenticationProviderClass() == null) {
//                    continue;
//                }
//                if (p.getAuthenticationProviderClass().getName().equals(config.getClassName())) {
//                    authProvider = p.createAuthProvider(config);
//                    break;
//                }
//            }
//
//            if (authProvider == null) {
//                throw new IOException("No authentication provider matching config: " + config);
//            }
//
//            return authProvider;
//        }
//
//        /**
//         * loads the named authority service config from persistence
//         */
//        public SecurityNamedServiceConfig loadConfig(String name) throws IOException {
//            File dir = new File(getAuthRoot(), name);
//            if (!dir.exists()) {
//                return null;
//            }
//
//            XStreamPersister xp = persister();
//            return (SecurityNamedServiceConfig) loadConfigFile(dir, xp);
//        }
//    }
}
