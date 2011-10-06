/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.geoserver.security;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.geoserver.security.concurrent.LockingGrantedAuthorityService;
import org.geoserver.security.concurrent.LockingGrantedAuthorityStore;
import org.geoserver.security.concurrent.LockingUserGroupService;
import org.geoserver.security.concurrent.LockingUserGroupStore;

/**
 * Class for creating stores for services
 * 
 * 
 * @author christian
 *
 */
public class GeoserverStoreFactory {
    public final static GeoserverStoreFactory Singleton = new GeoserverStoreFactory();
    
    protected Map<String,String> classNameMappings;
    
    protected GeoserverStoreFactory() {
        
        classNameMappings=Collections.synchronizedMap(new HashMap<String, String>());
    }

    /**
     * register user/group mappings
     * 
     * @param serviceClass
     * @param storeClass
     */
    public void registerUserGroupMapping(Class<? extends GeoserverUserGroupService> serviceClass ,
            Class <? extends GeoserverUserGroupStore> storeClass) {
        classNameMappings.put(serviceClass.getName(), storeClass.getName());
    }
    /**
     * register role mappings
     * 
     * @param serviceClass
     * @param storeClass
     */
    public void registerGrantedAuthorityMapping(Class<? extends GeoserverGrantedAuthorityService> serviceClass ,
            Class <? extends GeoserverGrantedAuthorityStore> storeClass) {
        classNameMappings.put(serviceClass.getName(), storeClass.getName());
    }

    /**
     * Checks if user/group service has a store (is modifiable)
     * @param service
     * @return
     */
    public boolean hasStoreFor(GeoserverUserGroupService service ) {
        
        if (service instanceof LockingUserGroupService)
            service = ((LockingUserGroupService)service).getService(); 
        
        
        return classNameMappings.get(service.getClass().getName()) != null;
    }
    
    /**
     * Checks if user/group service has a store (is modifiable)
     * @param service
     * @return
     */
    public boolean hasStoreFor(GeoserverGrantedAuthorityService service ) {
         
        if (service instanceof LockingGrantedAuthorityService)
            service = ((LockingGrantedAuthorityService)service).getService(); 

        return classNameMappings.get(service.getClass().getName()) != null;
    }

    /**
     * helper method throwing an exception
     * 
     * @param object
     * @throws IOException
     */
    protected void noStoreFound(Object object) throws IOException {
        throw new IOException("No store for "+ object.getClass().getName());
    }
    
    /**
     * Create a user/group store object for a user/group service object
     * The store object is initialized for the service
     * 
     * @param service
     * @return the store object  
     * @throws IOException
     */
    public GeoserverUserGroupStore getStoreFor(GeoserverUserGroupService service) throws IOException {

        boolean isWrapped = service instanceof LockingUserGroupService; 
        if (isWrapped)
            service = ((LockingUserGroupService)service).getService(); 
                                        
        String storeClassName = classNameMappings.get(service.getClass().getName());
        if (storeClassName==null) 
            noStoreFound(service); // throws IOException
        
        GeoserverUserGroupStore store = null;
        try {
            Class<?> storeClass = Class.forName(storeClassName);
            Constructor<?> constructor =storeClass.getConstructor(String.class);
            store = (GeoserverUserGroupStore) constructor.newInstance("Store for "+service.getName());
        } catch (Exception ex) {
            throw new IOException(ex);
        }
        
        store.initializeFromService(service);
        if (isWrapped) 
            store = new LockingUserGroupStore(store);        
        return store;    
    }
    /**
     * 
     * Create a role store object for a role service object
     * The store object is initialized for the service
     * 
     * @param service
     * @return the store object 
     * @throws IOException 
     */
    public GeoserverGrantedAuthorityStore getStoreFor(GeoserverGrantedAuthorityService service) throws IOException {
        boolean isWrapped = service instanceof LockingGrantedAuthorityService; 
        
        if (isWrapped)
            service = ((LockingGrantedAuthorityService)service).getService();
             
        String storeClassName = classNameMappings.get(service.getClass().getName());
        if (storeClassName==null)          
            noStoreFound(service); // throws IOException

        GeoserverGrantedAuthorityStore store = null;
        try {
            Class<?> storeClass = Class.forName(storeClassName);
            Constructor<?> constructor =storeClass.getConstructor(String.class);
            store = (GeoserverGrantedAuthorityStore) constructor.newInstance("Store for "+service.getName());
        } catch (Exception ex) {
            throw new IOException(ex);
        }
        
        store.initializeFromService(service);
        if (isWrapped)
            store=new LockingGrantedAuthorityStore(store);        
        return store;    
    }

}
