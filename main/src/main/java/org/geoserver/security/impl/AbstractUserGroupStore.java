/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.security.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.geoserver.security.GeoserverUserGroupService;
import org.geoserver.security.GeoserverUserGroupStore;
import org.geoserver.security.config.SecurityNamedServiceConfig;

/**
 * A base implementation for {@link GeoserverUserGroupStore}
 * 
 * @author christian
 *
 */
public abstract class AbstractUserGroupStore extends AbstractUserGroupService implements GeoserverUserGroupStore{

    /** logger */
    static Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.geoserver.security");
    
    private boolean modified=false;
    protected AbstractUserGroupService service;

    public AbstractUserGroupStore(String name) {
        super(name);
        
    }

    
    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverUserGroupStore#isModified()
     */
    public boolean isModified() {
        return modified;
    }

    /**
     * Setter for modified flag
     * @param value
     */
    public void setModified(Boolean value) {
        modified=value;
    }
    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverUserGroupStore#addUser(org.geoserver.security.impl.GeoserverUser)
     */
    public void addUser(GeoserverUser user) throws IOException{
        
        if(userMap.containsKey(user.getUsername()))
            throw new IllegalArgumentException("The user " + user.getUsername() + " already exists");
        else {
            userMap.put(user.getUsername(), user);
            setModified(true);
        }
    }
    
    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverUserGroupStore#addGroup(org.geoserver.security.impl.GeoserverUserGroup)
     */
    public void addGroup(GeoserverUserGroup group) throws IOException{
                
        if(groupMap.containsKey(group.getGroupname()))
            throw new IllegalArgumentException("The group " + group.getGroupname() + " already exists");
        else {
            groupMap.put(group.getGroupname(), group);
            setModified(true);
        }
    }

    
    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverUserGroupStore#updateUser(org.geoserver.security.impl.GeoserverUser)
     */
    public void updateUser(GeoserverUser user) throws IOException{
        
        if(userMap.containsKey(user.getUsername())) {
            userMap.put(user.getUsername(), user);
            setModified(true);
        }
        else
            throw new IllegalArgumentException("The user " + user.getUsername() + " does not exist");
    }
    
    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverUserGroupStore#updateGroup(org.geoserver.security.impl.GeoserverUserGroup)
     */
    public void updateGroup(GeoserverUserGroup group) throws IOException{
        
        if(groupMap.containsKey(group.getGroupname())) {
            groupMap.put(group.getGroupname(), group);
            setModified(true);
        }
        else
            throw new IllegalArgumentException("The group " + group.getGroupname() + " does not exist");
    }

    
    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverUserGroupStore#removeUser(org.geoserver.security.impl.GeoserverUser)
     */
    public boolean removeUser(GeoserverUser user) throws IOException{
        
        Collection<GeoserverUserGroup> groups = user_groupMap.get(user);        
        if (groups!=null) {
            Collection<GeoserverUserGroup> toBeRemoved = new ArrayList<GeoserverUserGroup>();
            toBeRemoved.addAll(groups);
            for (GeoserverUserGroup group : toBeRemoved) {
                disAssociateUserFromGroup(user, group);
            }
        }
        
        boolean retValue = userMap.remove(user.getUsername()) != null;
        if (retValue)
            setModified(true);
        return retValue;
    }
    
    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverUserGroupStore#removeGroup(org.geoserver.security.impl.GeoserverUserGroup)
     */
    public boolean removeGroup(GeoserverUserGroup group) throws IOException{
        Collection<GeoserverUser> users = group_userMap.get(group);;
        if (users !=null) {
            Collection<GeoserverUser> toBeRemoved = new ArrayList<GeoserverUser>();
            toBeRemoved.addAll(users);
            for (GeoserverUser user : toBeRemoved) {
                disAssociateUserFromGroup(user, group);
            }
        }
        
        boolean retval = groupMap.remove(group.getGroupname()) != null;
        if (retval) { 
            setModified(true);
        }    
        return retval;
    }


    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverUserGroupStore#store()
     *  
     */
    public void store() throws IOException {
        if (isModified()) {
            LOGGER.info("Start storing user/grous for service named "+getName());
            // prevent concurrent write from store and
            // read from service
            synchronized (service) { 
                serialize();
            }
            setModified(false);
            LOGGER.info("Storing user/grous successful for service named "+getName());
            service.load(); // service must reload
        }  else {
            LOGGER.info("Storing unnecessary, no change for user and groups");
        }
    }

    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverUserGroupStore#associateUserToGroup(org.geoserver.security.impl.GeoserverUser, org.geoserver.security.impl.GeoserverUserGroup)
     */
    public void associateUserToGroup(GeoserverUser user, GeoserverUserGroup group) throws IOException{
        checkUser(user);
        checkGroup(group);
        
        boolean changed = false;
        
        
        SortedSet<GeoserverUser> users = group_userMap.get(group);
        if (users == null) {
            users = new TreeSet<GeoserverUser>();
            group_userMap.put(group,users);
        }
        if (users.contains(user)==false) {
            users.add(user);
            changed=true;
        }
        
        SortedSet<GeoserverUserGroup> groups = user_groupMap.get(user);
        if (groups == null) {
            groups = new TreeSet<GeoserverUserGroup>();
            user_groupMap.put(user,groups);
        }
        if (groups.contains(group)==false) {            
            groups.add(group);
            changed=true;            
        }
        if (changed) {
            setModified(true);
        }
    }
    
    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverUserDetailsService#disAssociateUserFromGroup(org.geoserver.security.impl.GeoserverUser, org.geoserver.security.UserGroup)
     */
    public void disAssociateUserFromGroup(GeoserverUser user, GeoserverUserGroup group) throws IOException{
        checkUser(user);
        checkGroup(group);
        boolean changed = false;
        
        SortedSet<GeoserverUser> users = group_userMap.get(group);        
        if (users!=null) {
            changed |=users.remove(user);
            if (users.isEmpty()) {
                group_userMap.remove(group);
            }                
        }
        SortedSet<GeoserverUserGroup> groups = user_groupMap.get(user);
        if (groups!=null) {
            changed |= groups.remove(group);
            if (groups.isEmpty())
                user_groupMap.remove(user);
        }
        if (changed) {
            setModified(true);
        }
    }
    
    /**
     * Subclasses must implement this method 
     * Save user/groups  to backend
     */
    protected abstract void serialize() throws IOException;

    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverUserGroupStore#clear()
     */
    public void clear() throws IOException {
        clearMaps();
        setModified(true);
    }

    @Override
    public void initializeFromService(GeoserverUserGroupService service)
            throws IOException {
        this.service=(AbstractUserGroupService)service;
        setSecurityManager(service.getSecurityManager());
        deserialize();
    }

    /**
     * Make a deep copy (using serialization) from the
     * service to the store.
    */    
    @SuppressWarnings("unchecked")
    @Override
    protected void deserialize() throws IOException {
        // deepcopy from service, using serialization 
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream oout = new ObjectOutputStream(out);
        oout.writeObject(service.userMap);
        oout.writeObject(service.groupMap);
        oout.writeObject(service.user_groupMap);
        oout.writeObject(service.group_userMap);
        byte[] bytes =out.toByteArray();
        oout.close();            

        clearMaps();
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        ObjectInputStream oin = new ObjectInputStream(in);
        try {
            userMap = (TreeMap<String,GeoserverUser>) oin.readObject();
            groupMap =(TreeMap<String,GeoserverUserGroup>) oin.readObject();
            user_groupMap = (TreeMap<GeoserverUser,SortedSet<GeoserverUserGroup>>)oin.readObject();
            group_userMap = (TreeMap<GeoserverUserGroup,SortedSet<GeoserverUser>>)oin.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
        setModified(false);
    }
    
    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverUserGroupService#initializeFromConfig(org.geoserver.security.config.SecurityNamedServiceConfig)
     */
    @Override
    public void initializeFromConfig(SecurityNamedServiceConfig config) throws IOException {
        // Do nothing
    }

}
