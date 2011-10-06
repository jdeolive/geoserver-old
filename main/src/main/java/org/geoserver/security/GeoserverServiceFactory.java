/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.geoserver.security;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import org.geoserver.security.concurrent.LockingGrantedAuthorityService;
import org.geoserver.security.concurrent.LockingUserGroupService;
import org.geoserver.security.config.FileBasedSecurityServiceConfig;
import org.geoserver.security.config.SecurityNamedServiceConfig;
import org.geoserver.security.file.GrantedAuthorityFileWatcher;
import org.geoserver.security.file.UserGroupFileWatcher;
import org.geoserver.security.impl.Util;

/**
 * Factory for creating services
 * 
 * 
 * @author christian
 *
 */
public class GeoserverServiceFactory {
    public final static GeoserverServiceFactory Singleton = new GeoserverServiceFactory();
    
    protected Map<String,GeoserverUserGroupService> userGroupServiceMap; 
    protected Map<String,GeoserverGrantedAuthorityService> grantedAuthorityServiceMap;
    
    protected GeoserverServiceFactory() {        
        userGroupServiceMap=new HashMap<String, GeoserverUserGroupService>();
        grantedAuthorityServiceMap=new HashMap<String, GeoserverGrantedAuthorityService>();
    }

    /**
     * Get a {@link GeoserverUserGroupService}
     * If the service is statefull, add a locking wrapper
     * If the service if file based and a reload interval
     * is given, add a file watcher
     * 
     * @param name
     * @return
     * @throws IOException
     */
    synchronized public GeoserverUserGroupService getUserGroupService(String name) throws IOException {
        
        GeoserverUserGroupService service = userGroupServiceMap.get(name);
        if (service !=null) return service;
        
        SecurityNamedServiceConfig config = Util.loadUserGroupServiceConfig(name);
        service = (GeoserverUserGroupService) createObject(config);
        
        if (config.isStateless()==false) { // wrap with locking mechanism
            service = new LockingUserGroupService(service);
        }
        service.initializeFromConfig(config);

        if (config instanceof FileBasedSecurityServiceConfig) {
            FileBasedSecurityServiceConfig fileConfig = 
                (FileBasedSecurityServiceConfig) config;
            if (fileConfig.getCheckInterval()>0) {
                File file = new File(fileConfig.getFileName());
                if (file.isAbsolute()==false) 
                    file = new File(Util.getUserGroupNamedRoot(name),file.getPath());
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
        userGroupServiceMap.put(name,service);
        return service;
    }
    
    /*
     * 
     * Get a {@link GeoserverUserGroupService}
     * If the service is not stateless , add a locking wrapper
     * If the service if file based and a reload interval
     * is given, add a file watcher
     * 
     * @param name
     * @return
     * @throws IOException
     */

    synchronized public GeoserverGrantedAuthorityService getGrantedAuthorityService(String name) throws IOException {
        
        GeoserverGrantedAuthorityService service = null;
        service = grantedAuthorityServiceMap.get(name);
        if (service!=null) return service;
        
        SecurityNamedServiceConfig config = Util.loadGrantedAuthorityServiceConfig(name);
        service = (GeoserverGrantedAuthorityService) createObject(config);
        
        if (config.isStateless()==false) { // wrap with locking mechanism
            service = new LockingGrantedAuthorityService(service);
        }
        service.initializeFromConfig(config);

        if (config instanceof FileBasedSecurityServiceConfig) {
            FileBasedSecurityServiceConfig fileConfig = 
                (FileBasedSecurityServiceConfig) config;
            if (fileConfig.getCheckInterval()>0) {
                File file = new File(fileConfig.getFileName());
                if (file.isAbsolute()==false) 
                    file = new File(Util.getGrantedAuthorityNamedRoot(name),file.getPath());
                if (file.canRead()==false) {
                    throw new IOException("Cannot read file: "+file.getCanonicalPath());
                }
                GrantedAuthorityFileWatcher watcher = new 
                    GrantedAuthorityFileWatcher(file.getCanonicalPath(),service,file.lastModified());
                watcher.setDelay(fileConfig.getCheckInterval());
                service.registerGrantedAuthorityLoadedListener(watcher);
                watcher.start();
                
            }
        }
        grantedAuthorityServiceMap.put(name, service);
        return service;
    }

    
    /**
     * Removes the service from the cache
     * 
     * @param name
     * @return the romoved service or null for an 
     * unknown name
     * @throws IOException
     */
    synchronized public GeoserverGrantedAuthorityService releaseGrantedAuthorityService(String name)
        throws IOException {
        return grantedAuthorityServiceMap.remove(name);
    }
    
    /**
     * Removes the service from the cache
     * 
     * @param name
     * @return the romoved service or null for an 
     * unknown name
     * @throws IOException
     */
    synchronized public GeoserverUserGroupService releaseUserGroupService(String name)
        throws IOException {
        return userGroupServiceMap.remove(name);
    }

    
    /**
     * helper method creating an object using
     * information from the  {@link SecurityNamedServiceConfig}
     * object
     * 
     * @param config
     * @return
     * @throws IOException
     */
    protected Object createObject(SecurityNamedServiceConfig config) throws IOException {
        try {
            Class<?> aClass = Class.forName(config.getClassName());
            Constructor<?> cons = aClass.getConstructor(String.class);
            return cons.newInstance(config.getName());
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }
    
    /**
     * Removes all registered services
     * Useful for test setups 
     * 
     */
    public void reset() {
        userGroupServiceMap.clear();
        grantedAuthorityServiceMap.clear();
    }
}
