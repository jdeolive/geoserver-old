/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.geoserver.security.impl;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.logging.Logger;
import java.util.Properties;
import java.util.SortedSet;

import javax.management.relation.RoleNotFoundException;

import org.apache.commons.io.FileUtils;
import org.geoserver.config.GeoServerDataDirectory;
import org.geoserver.config.util.XStreamPersister;
import org.geoserver.config.util.XStreamPersisterFactory;
import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.platform.GeoServerResourceLoader;
import org.geoserver.security.GeoserverGrantedAuthorityService;
import org.geoserver.security.GeoserverGrantedAuthorityStore;
import org.geoserver.security.GeoserverStoreFactory;
import org.geoserver.security.GeoserverUserGroupService;
import org.geoserver.security.GeoserverUserGroupStore;
import org.geoserver.security.config.SecurityConfig;
import org.geoserver.security.config.SecurityNamedServiceConfig;
import org.geoserver.security.config.UserDetailsServiceConfig;
import org.geoserver.security.config.impl.UserDetailsServiceConfigImpl;
import org.geoserver.security.config.impl.XMLFileBasedSecurityServiceConfigImpl;
import org.geoserver.security.xml.XMLConstants;
import org.geoserver.security.xml.XMLGrantedAuthorityService;
import org.geoserver.security.xml.XMLUserGroupService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.memory.UserAttribute;
import org.springframework.security.core.userdetails.memory.UserAttributeEditor;


/**
 * Class for common methods
 * 
 * 
 * @author christian
 *
 */
public class Util {
    
    static Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.geoserver.security");
    /**
     * Convert from string to boolean, use defaultValue
     * in case of null or empty string
     * 
     * @param booleanString
     * @param defaultValue
     * @return
     */
    static public boolean convertToBoolean(String booleanString, boolean defaultValue) {        
        if (booleanString == null || booleanString.trim().length()==0)
            return defaultValue;
        return Boolean.valueOf(booleanString.trim());
    }

    static public GeoServerResourceLoader getResourceLoader() {
        return (GeoServerResourceLoader) GeoServerExtensions.bean( "resourceLoader" );
    }
    
    static public GeoServerDataDirectory getDataDirectory() {        
        return new GeoServerDataDirectory(getResourceLoader());
        
    }

    public static File getSecurityRoot() throws IOException {
        return getDataDirectory().findOrCreateSecurityRoot();
    }
    
    public static File getSecurityConfig() throws IOException {
        return new File(getSecurityRoot(),"config.xml");
    }

    
    public static File getUserGroupRoot() throws IOException {
        return new File(getSecurityRoot(),"usergroup");
    }
    
    public static File getUserGroupNamedRoot(String name) throws IOException {        
        return new File(getUserGroupRoot(),name);
    }
    
    public static File getUserGroupConfig(String name) throws IOException {
        File namedRoot = getUserGroupNamedRoot(name);
        return new File(namedRoot,"config.xml");
    }


    
    public static File getGrantedAuthorityRoot() throws IOException {
        return new File(getSecurityRoot(),"role");
    }
    
    public static File getGrantedAuthorityNamedRoot(String name) throws IOException {
        File namedRoot = getGrantedAuthorityRoot();
        return new File(namedRoot,name);
    }
    
    public static File getGrantedAuthorityConfig(String name) throws IOException {
        File namedRoot = getGrantedAuthorityNamedRoot(name);
        return new File(namedRoot,"config.xml");
    }

    /**
     * Deep copy of the whole User/Group database
     * 
     * @param service
     * @param store
     * @throws IOException
     */
    static public void copyFrom(GeoserverUserGroupService service, GeoserverUserGroupStore store) throws IOException {
        store.clear();
        Map<String,GeoserverUser> newUserDict = new HashMap<String,GeoserverUser>();
        Map<String,GeoserverUserGroup> newGroupDict = new HashMap<String,GeoserverUserGroup>();
        
        for (GeoserverUser user : service.getUsers()) {
            GeoserverUser newUser = store.createUserObject(user.getUsername(),user.getPassword(), user.isEnabled());
            for (Object key: user.getProperties().keySet()) {
                newUser.getProperties().put(key, user.getProperties().get(key));
            }
            store.addUser(newUser);
            newUserDict.put(newUser.getUsername(),newUser);
        }
        for (GeoserverUserGroup group : service.getUserGroups()) {
            GeoserverUserGroup newGroup = store.createGroupObject(group.getGroupname(),group.isEnabled());
            store.addGroup(newGroup);
            newGroupDict.put(newGroup.getGroupname(),newGroup);
        }
        for (GeoserverUserGroup group : service.getUserGroups()) {
            GeoserverUserGroup newGroup = newGroupDict.get(group.getGroupname());
            
            for (GeoserverUser member : service.getUsersForGroup(group)) {
                GeoserverUser newUser = newUserDict.get(member.getUsername());
                store.associateUserToGroup(newUser, newGroup);
            }
        }        
    }
    
    /**
     * Deep copy of the whole role database
     * 
     * @param service
     * @param store
     * @throws IOException
     */
    static public void copyFrom(GeoserverGrantedAuthorityService service, GeoserverGrantedAuthorityStore store) throws IOException {
        store.clear();
        Map<String,GeoserverGrantedAuthority> newRoleDict = new HashMap<String,GeoserverGrantedAuthority>();
        
        for (GeoserverGrantedAuthority role : service.getRoles()) {
            GeoserverGrantedAuthority newRole = store.createGrantedAuthorityObject(role.getAuthority());
            for (Object key: role.getProperties().keySet()) {
                newRole.getProperties().put(key, role.getProperties().get(key));
            }
            store.addGrantedAuthority(newRole);
            newRoleDict.put(newRole.getAuthority(),newRole);
        }
        
        for (GeoserverGrantedAuthority role : service.getRoles()) {
            GeoserverGrantedAuthority parentRole = service.getParentRole(role);
            GeoserverGrantedAuthority newRole = newRoleDict.get(role.getAuthority());
            GeoserverGrantedAuthority newParentRole = parentRole == null ?
                    null : newRoleDict.get(parentRole.getAuthority());
            store.setParentRole(newRole, newParentRole);
        }
        
        for (GeoserverGrantedAuthority role : service.getRoles()) {
            GeoserverGrantedAuthority newRole = newRoleDict.get(role.getAuthority());
            SortedSet<String> usernames = service.getUserNamesForRole(role);
            for (String username : usernames) {
                store.associateRoleToUser(newRole, username);
            }
            SortedSet<String> groupnames = service.getGroupNamesForRole(role);
            for (String groupname : groupnames) {
                store.associateRoleToGroup(newRole, groupname);
            }            
        }                        
    }
    
    static SortedSet<GeoserverUser> usersHavingRole(GeoserverGrantedAuthority role) {
        // TODO
        return null;
    }
    
    /**
     * Migrate old users.properties file
     * 
     * @param props
     * @param userStore
     * @param roleStore
     * @throws IOException
     */
    static public void migrateFrom(Properties props, GeoserverUserGroupStore userStore, 
            GeoserverGrantedAuthorityStore roleStore) throws IOException {
        
        UserAttributeEditor configAttribEd = new UserAttributeEditor();

        for (Iterator<Object> iter = props.keySet().iterator(); iter.hasNext();) {
            // the attribute editors parses the list of strings into password, username and enabled
            // flag
            String username = (String) iter.next();
            configAttribEd.setAsText(props.getProperty(username));

            // if the parsing succeeded turn that into a user object
            UserAttribute attr = (UserAttribute) configAttribEd.getValue();
            if (attr != null) {
                GeoserverUser user = userStore.createUserObject(username, attr.getPassword(), attr.isEnabled());
                userStore.addUser(user);
                for (GrantedAuthority auth : attr.getAuthorities()) {
                    GeoserverGrantedAuthority role = roleStore.getGrantedAuthorityByName(auth.getAuthority());
                    if (role==null) {
                        role = roleStore.createGrantedAuthorityObject(auth.getAuthority());
                        roleStore.addGrantedAuthority(role);
                    }
                    roleStore.associateRoleToUser(role, username);
                }                
            }
        }
    }
    
    static public void migrateIfNeccessary() throws IOException{
        
        if (getGrantedAuthorityRoot().exists()) {
            File oldUserFile = new File(getSecurityRoot(), "users.properties.old");
            if (oldUserFile.exists()) {
                LOGGER.warning(oldUserFile.getCanonicalPath()+" could be removed manually");
            }
            return; // already migrated
        }
        
        LOGGER.info("Start security migration");
        
        getGrantedAuthorityRoot().mkdir();
        if (getUserGroupRoot().exists()==false)
            getUserGroupRoot().mkdir();
        
        // check for service.properties, create if necessary
        File serviceFile = new File(getSecurityRoot(), "service.properties");
        if (serviceFile.exists()==false) {
            FileUtils.copyURLToFile(Util.class.getResource("serviceTemplate.properties"),
                    serviceFile);
        }

        
        long checkInterval = 10000; // 10 secs
        
        XMLFileBasedSecurityServiceConfigImpl ugConfig = null;
        if (getUserGroupConfig(XMLUserGroupService.DEFAULT_NAME).exists()==false) {
            ugConfig = new XMLFileBasedSecurityServiceConfigImpl();               
            ugConfig.setName(XMLUserGroupService.DEFAULT_NAME);
            ugConfig.setClassName(XMLUserGroupService.class.getName());
            ugConfig.setCheckInterval(checkInterval); 
            ugConfig.setFileName(XMLConstants.FILE_UR);
            ugConfig.setStateless(false);
            ugConfig.setValidating(true);
            storeUserGroupServiceConfig(ugConfig);            
        } else {
            ugConfig= (XMLFileBasedSecurityServiceConfigImpl)
                loadUserGroupServiceConfig(XMLUserGroupService.DEFAULT_NAME);
        }
        
        XMLFileBasedSecurityServiceConfigImpl gaConfig=null;
        if (getGrantedAuthorityConfig(XMLGrantedAuthorityService.DEFAULT_NAME).exists()==false) {
            gaConfig = new XMLFileBasedSecurityServiceConfigImpl();                 
            gaConfig.setName(XMLGrantedAuthorityService.DEFAULT_NAME);
            gaConfig.setClassName(XMLGrantedAuthorityService.class.getName());
            gaConfig.setCheckInterval(checkInterval); 
            gaConfig.setFileName(XMLConstants.FILE_RR);
            gaConfig.setStateless(false);
            gaConfig.setValidating(true);
            storeGrantedAuthorityServiceConfig(gaConfig);            
        } else {
            gaConfig= (XMLFileBasedSecurityServiceConfigImpl)
                loadGrantedAuthorityServiceConfig(XMLGrantedAuthorityService.DEFAULT_NAME);
        }
        
        UserDetailsServiceConfig config = new UserDetailsServiceConfigImpl();
        config.setGrantedAuthorityServiceName(XMLGrantedAuthorityService.DEFAULT_NAME);
        config.setUserGroupServiceName(XMLUserGroupService.DEFAULT_NAME);
        storeSecurityServiceConfig(config);
        

      GeoserverUserGroupService usergroupService = 
          new XMLUserGroupService(XMLUserGroupService.DEFAULT_NAME);
      usergroupService.initializeFromConfig(ugConfig);        
//        GeoserverUserGroupService usergroupService = 
//            GeoserverServiceFactory.Singleton.getUserGroupService(serviceName);
        GeoserverUserGroupStore userGroupStore = 
            GeoserverStoreFactory.Singleton.getStoreFor(usergroupService);
        
       GeoserverGrantedAuthorityService roleService = 
           new XMLGrantedAuthorityService(XMLGrantedAuthorityService.DEFAULT_NAME);
       roleService.initializeFromConfig(gaConfig);        
//        GeoserverGrantedAuthorityService roleService = 
//            GeoserverServiceFactory.Singleton.getGrantedAuthorityService(serviceName);
        GeoserverGrantedAuthorityStore roleStore = 
            GeoserverStoreFactory.Singleton.getStoreFor(roleService);
        
        
        
        File usersFile = new File(getSecurityRoot(), "users.properties");
        if (usersFile.exists()) {
            Properties props = loadUniversal(new FileInputStream(usersFile));
            migrateFrom(props, userGroupStore, roleStore);
        } else  {
            userGroupStore.addUser(GeoserverUser.DEFAULT_ADMIN);
            roleStore.addGrantedAuthority(GeoserverGrantedAuthority.ADMIN_ROLE);
            roleStore.associateRoleToUser(GeoserverGrantedAuthority.ADMIN_ROLE,
                    GeoserverUser.DEFAULT_ADMIN.getUsername());
        }
        // check for  roles in service.properties but not in user.properties 
        serviceFile = new File(getSecurityRoot(), "service.properties");
        if (serviceFile.exists()) {
            Properties props = loadUniversal(new FileInputStream(serviceFile));
            for (Entry<Object,Object> entry: props.entrySet()) {
                StringTokenizer tokenizer = new StringTokenizer((String)entry.getValue(),",");
                while (tokenizer.hasMoreTokens()) {
                    String roleName = tokenizer.nextToken().trim();
                    if (roleName.length()>0) {
                        if (roleStore.getGrantedAuthorityByName(roleName)==null)
                            roleStore.addGrantedAuthority(roleStore.createGrantedAuthorityObject(roleName));
                    }
                }
            }
        }

        // check for  roles in data.properties but not in user.properties
        File dataFile = new File(getSecurityRoot(), "layer.properties");
        if (dataFile.exists()) {
            Properties props = loadUniversal(new FileInputStream(dataFile));
            for (Entry<Object,Object> entry: props.entrySet()) {
                if ("mode".equals(entry.getKey().toString()))
                    continue; // skip mode directive
                StringTokenizer tokenizer = new StringTokenizer((String)entry.getValue(),",");
                while (tokenizer.hasMoreTokens()) {
                    String roleName = tokenizer.nextToken().trim();
                    if (roleName.length()>0 && roleName.equals("*")==false) {
                        if (roleStore.getGrantedAuthorityByName(roleName)==null)
                            roleStore.addGrantedAuthority(roleStore.createGrantedAuthorityObject(roleName));
                    }
                }
            }
        }

                
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

    
    public static String convertPropsToString(Properties props, String heading) {
        StringBuffer buff = new StringBuffer();
        if (heading !=null) {
            buff.append(heading).append("\n\n");
        }
        for (Entry<Object,Object> entry : props.entrySet()) {
            buff.append(entry.getKey().toString()).append(": ")
                .append(entry.getValue().toString()).append("\n");
        }
        return buff.toString();
    }
    
    public static void storeSecurityServiceConfig(UserDetailsServiceConfig config) throws IOException {
        storeConfigToFile(config,getSecurityConfig());
    }
    
    public static void storeUserGroupServiceConfig(SecurityNamedServiceConfig config) throws IOException {
        File dir = getUserGroupNamedRoot(config.getName());
        if (dir.exists()==false)
            dir.mkdir();
        storeConfigToFile(config,getUserGroupConfig(config.getName()));
    }
    
    public static void storeGrantedAuthorityServiceConfig(SecurityNamedServiceConfig config) throws IOException {
        File dir = getGrantedAuthorityNamedRoot(config.getName());
        if (dir.exists()==false)
            dir.mkdir();
        storeConfigToFile(config,getGrantedAuthorityConfig(config.getName()));
    }

    public static void storeConfigToFile(SecurityConfig config,File configFile) throws IOException{
        XStreamPersister p = new XStreamPersisterFactory().createXMLPersister();
        FileOutputStream out = new FileOutputStream(configFile);
        p.save(config, out);
        out.close();                
    }

    
    public static UserDetailsServiceConfig loadSecurityServiceConfig() throws IOException {
        return (UserDetailsServiceConfig)loadConfigFromFile(getSecurityConfig());
    }
    
    
    public static SecurityNamedServiceConfig loadUserGroupServiceConfig(String name) throws IOException {
        return (SecurityNamedServiceConfig)loadConfigFromFile(getUserGroupConfig(name));
    }
    
    public static void removeGrantedAuthorityServiceConfig(String name) throws IOException{
        File dir = Util.getGrantedAuthorityNamedRoot(name);
        if (dir.exists())
            FileUtils.deleteDirectory(dir);
    }
    
    public static void removeUserGroupServiceConfig(String name) throws IOException{
        File dir = Util.getUserGroupNamedRoot(name);
        if (dir.exists())
            FileUtils.deleteDirectory(dir);
    }

    public static SecurityNamedServiceConfig loadGrantedAuthorityServiceConfig(String name) throws IOException {
        return (SecurityNamedServiceConfig)loadConfigFromFile(getGrantedAuthorityConfig(name));
    }
    
    public static SecurityConfig loadConfigFromFile(File configFile) throws IOException{
        XStreamPersister p = new XStreamPersisterFactory().createXMLPersister();        
        FileInputStream in = new FileInputStream(configFile);
        SecurityConfig result = p.load(in,SecurityConfig.class);
        in.close();                
        return result;
    }
    
    public static SortedSet<String> listUserGroupServices() throws IOException {
        return listSubDirNames(getUserGroupRoot());
    }
    
    public static SortedSet<String> listGrantedAuthorityServices() throws IOException {
        return listSubDirNames(getGrantedAuthorityRoot());
    }


    protected static SortedSet<String> listSubDirNames(File dir) throws IOException {
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
    
    /**
     * Determines if the the input stream is xml
     * if it is, use create properties loaded from xml
     * format, otherwise create properties from default
     * format.
     * 
     * @param in
     * @return
     * @throws IOException
     */
    public static Properties loadUniversal(InputStream in) throws IOException {
        final String xmlDeclarationStart = "<?xml";
        BufferedInputStream bin = new BufferedInputStream(in);
        bin.mark(4096);
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(bin));
        String line = reader.readLine();                
        boolean isXML = line.startsWith(xmlDeclarationStart);
        
        bin.reset();        
        Properties props = new Properties();
        
        if (isXML)
            props.loadFromXML(bin);
        else
            props.load(bin);
                
        return props;
    }
    
}
