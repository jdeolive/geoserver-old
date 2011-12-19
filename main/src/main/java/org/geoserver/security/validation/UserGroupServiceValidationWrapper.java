/* Copyright (c) 2001 - 2008 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.security.validation;

import java.io.IOException;
import java.util.SortedSet;

import org.geoserver.security.GeoServerSecurityManager;
import org.geoserver.security.GeoserverUserGroupService;
import org.geoserver.security.GeoserverUserGroupStore;
import org.geoserver.security.config.SecurityNamedServiceConfig;
import org.geoserver.security.event.UserGroupLoadedListener;
import org.geoserver.security.impl.GeoserverUser;
import org.geoserver.security.impl.GeoserverUserGroup;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * 
 * This class is a validation wrapper for {@link GeoserverUserGroupService}
 * 
 * Usage:
 * <code>
 * GeoserverUserGroupService valService = new UserGroupServiceValidationWrapper(service);
 * valService.getUsers()
 * 
 * </code>
 * 
 * Since the {@link GeoserverUserGroupService} interface does not allow to 
 * throw {@link UserGroupServiceException} objects directly, these objects
 * a wrapped into an IOException. Use {@link IOException#getCause()} to
 * get the proper exception.
 * 
 * 
 * @author christian
 *
 */


public class UserGroupServiceValidationWrapper extends AbstractSecurityValidator implements GeoserverUserGroupService{

    protected GeoserverUserGroupService service;

    /**
     * Creates a wrapper object. 
     * 
     * @param service
     */    
    public UserGroupServiceValidationWrapper(GeoserverUserGroupService service) {
        this.service=service;
    }

    public GeoserverUserGroupService getWrappedService() {
        return service;
    }
    
    protected void checkUserName(String userName) throws IOException{
        if (isNotEmpty(userName)==false)
            throw createSecurityException(UserGroupServiceValidationErrors.UG_ERR_01);        
    }
    
    protected void checkGroupName(String groupName) throws IOException{
        if (isNotEmpty(groupName)==false)
            throw createSecurityException(UserGroupServiceValidationErrors.UG_ERR_02);        
    }

        
    protected void checkExistingUserName(String userName) throws IOException{
        checkUserName(userName);
        if (service.getUserByUsername(userName)==null)
            throw createSecurityException(UserGroupServiceValidationErrors.UG_ERR_03,userName);
    }
    
    protected void checkExistingGroupName(String groupName) throws IOException{
        checkGroupName(groupName);
        if (service.getGroupByGroupname(groupName)==null)
            throw createSecurityException(UserGroupServiceValidationErrors.UG_ERR_04,groupName);
    }
    
    protected void checkNotExistingUserName(String userName) throws IOException{
        checkUserName(userName);
        if (service.getUserByUsername(userName)!=null)
            throw createSecurityException(UserGroupServiceValidationErrors.UG_ERR_05,userName);
    }
    
    protected void checkNotExistingGroupName(String groupName) throws IOException{
        checkGroupName(groupName);
        if (service.getGroupByGroupname(groupName)!=null)
            throw createSecurityException(UserGroupServiceValidationErrors.UG_ERR_06,groupName);
    }

    
    
    // start wrapper methods
    
    public void initializeFromConfig(SecurityNamedServiceConfig config) throws IOException {
        service.initializeFromConfig(config);
    }



    public boolean canCreateStore() {
        return service.canCreateStore();
    }






    public String getName() {
        return service.getName();
    }



    public void setName(String name) {
        service.setName(name);
    }



    public void setSecurityManager(GeoServerSecurityManager securityManager) {
        service.setSecurityManager(securityManager);
    }


    public GeoserverUserGroupStore createStore() throws IOException {
        return service.createStore();
    }



    public GeoServerSecurityManager getSecurityManager() {
        return service.getSecurityManager();
    }



    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException,
            DataAccessException {
        return service.loadUserByUsername(username);
    }



    public void registerUserGroupLoadedListener(UserGroupLoadedListener listener) {
        service.registerUserGroupLoadedListener(listener);
    }


    public void unregisterUserGroupLoadedListener(UserGroupLoadedListener listener) {
        service.unregisterUserGroupLoadedListener(listener);
    }



    public GeoserverUserGroup getGroupByGroupname(String groupname) throws IOException {
        return service.getGroupByGroupname(groupname);
    }


    public GeoserverUser getUserByUsername(String username) throws IOException {
        return service.getUserByUsername(username);
    }


    public GeoserverUser createUserObject(String username, String password, boolean isEnabled)
            throws IOException {
        return service.createUserObject(username, password, isEnabled);
    }




    public GeoserverUserGroup createGroupObject(String groupname, boolean isEnabled)
            throws IOException {
        return service.createGroupObject(groupname, isEnabled);
    }



    public SortedSet<GeoserverUser> getUsers() throws IOException {
        return service.getUsers();
    }



    public SortedSet<GeoserverUserGroup> getUserGroups() throws IOException {
        return service.getUserGroups();
    }



    public SortedSet<GeoserverUser> getUsersForGroup(GeoserverUserGroup group) throws IOException {
        checkExistingGroupName(group.getGroupname());
        return service.getUsersForGroup(group);
    }



    public SortedSet<GeoserverUserGroup> getGroupsForUser(GeoserverUser user) throws IOException {
        checkExistingUserName(user.getUsername());
        return service.getGroupsForUser(user);
    }



    public void load() throws IOException {
        service.load();
    }



    public String getPasswordEncoderName() {
        return service.getPasswordEncoderName();
    }



    public String getPasswordValidatorName() {
        return service.getPasswordValidatorName();
    }



    
        
    @Override
    protected AbstractSecurityValidationErrors getSecurityErrors() {
        return new UserGroupServiceValidationErrors();
    }

    
    /**
     * Helper method for creating a proper
     * {@link SecurityConfigException} object
     * 
     * @param errorid
     * @param args
     * @return
     */
    protected IOException createSecurityException (String errorid, Object ...args) {
        String message = getSecurityErrors().formatErrorMsg(errorid, args);
        UserGroupServiceException ex =  new UserGroupServiceException(errorid,message,args);
        return new IOException("Details are in the nested excetpion",ex);
    }
        
}
