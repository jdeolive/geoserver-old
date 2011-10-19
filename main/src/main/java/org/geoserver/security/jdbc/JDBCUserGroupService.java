package org.geoserver.security.jdbc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.geoserver.security.GeoserverUserGroupService;
import org.geoserver.security.GeoserverUserGroupStore;
import org.geoserver.security.config.JdbcBaseSecurityServiceConfig;
import org.geoserver.security.config.SecurityNamedServiceConfig;
import org.geoserver.security.event.UserGroupLoadedEvent;
import org.geoserver.security.event.UserGroupLoadedListener;
import org.geoserver.security.impl.GeoserverUser;
import org.geoserver.security.impl.GeoserverUserGroup;
import org.geoserver.security.impl.Util;

/**
 * JDBC implementation of {@link GeoserverUserGroupService}
 * 
 * @author christian
 *
 */
public  class JDBCUserGroupService extends AbstractJDBCService implements GeoserverUserGroupService {
    
    public final static String DEFAULT_DML_FILE="usersdml.xml";
    public final static String DEFAULT_DDL_FILE="usersddl.xml";

    /** logger */
    static Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.geoserver.security.jdbc");
    protected Set<UserGroupLoadedListener> listeners = 
        Collections.synchronizedSet(new HashSet<UserGroupLoadedListener>());
    
    
    public JDBCUserGroupService() throws IOException{
    }

    public JDBCUserGroupService(String name) throws IOException{
        super(name);
    }

    @Override
    public boolean canCreateStore() {
        return true;
    }

    @Override
    public GeoserverUserGroupStore createStore() throws IOException {
        JDBCUserGroupStore store = new JDBCUserGroupStore(getName());
        store.initializeFromService(this);
        return store;
    }

    /** Uses {@link #initializeDSFromConfig(SecurityNamedServiceConfig)} and
     * {@link #checkORCreateJDBCPropertyFile(String, File, String)} for initializing
     * @see org.geoserver.security.GeoserverUserGroupService#initializeFromConfig(org.geoserver.security.config.SecurityNamedServiceConfig)
     */
    @Override
    public void initializeFromConfig(SecurityNamedServiceConfig config) throws IOException {
        
        initializeDSFromConfig(config);

        if (config instanceof JdbcBaseSecurityServiceConfig) {
            JdbcBaseSecurityServiceConfig jdbcConfig =
                (JdbcBaseSecurityServiceConfig) config;
            
            String fileNameDML =jdbcConfig.getPropertyFileNameDML();
            File file = checkORCreateJDBCPropertyFile(fileNameDML, getConfigRoot(), DEFAULT_DML_FILE);
            dmlProps = Util.loadUniversal(new FileInputStream(file));
            
            String fileNameDDL =jdbcConfig.getPropertyFileNameDDL();
            file = checkORCreateJDBCPropertyFile(fileNameDDL, getConfigRoot(), DEFAULT_DDL_FILE);

            ddlProps = Util.loadUniversal(new FileInputStream(file));                        
        }        
    }

    
    
    /**
     * @see org.geoserver.security.jdbc.AbstractJDBCService#getOrderedNamesForCreate()
     */
    protected  String[] getOrderedNamesForCreate() {
        return new String[] {
            "users.create","userprops.create","groups.create","groupmembers.create",
            "groupmembers.indexcreate"                
        };        
    }
    /**
     * @see org.geoserver.security.jdbc.AbstractJDBCService#getOrderedNamesForDrop()
     */
    protected  String[] getOrderedNamesForDrop() {
        return new String[] {
            "groupmembers.drop","groups.drop","userprops.drop","users.drop"
        };        
    }

    
    
    

    /**
     * @see org.geoserver.security.GeoserverUserGroupService#getUserByUsername(java.lang.String)
     */
    public GeoserverUser getUserByUsername(String username) throws IOException {
            

        Connection con=null;
        PreparedStatement ps = null,ps2=null;
        ResultSet rs = null,rs2=null;
        GeoserverUser u  = null;
        try {
            con = getConnection();
            ps = getDMLStatement("users.keyed",con);            
            ps.setString(1, username);
            rs = ps.executeQuery();
            if (rs.next()) {                
                String password = rs.getString(1);
                String enabledString = rs.getString(2);
                boolean isEnabled = convertFromString(enabledString);
                u = createUserObject(username,password, isEnabled);
                ps2 = getDMLStatement("userprops.selectForUser",con);            
                ps2.setString(1, username);
                rs2 = ps2.executeQuery();
                while (rs2.next()) {
                    String propName = rs2.getString(1);
                    Object propValue = rs2.getObject(2);
                    u.getProperties().put(propName, propValue==null ? "" : propValue);
                }
                
            }            
        } catch (SQLException ex) {
            throw new IOException(ex);
        } finally {
            closeFinally(null, ps2, rs2);
            closeFinally(con, ps, rs);
        }        
                                    
        return u;
    }

    
    
    /**
     * @see org.geoserver.security.GeoserverUserGroupService#getGroupByGroupname(java.lang.String)
     */
    public GeoserverUserGroup getGroupByGroupname(String groupname) throws IOException {
        Connection con=null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        GeoserverUserGroup g =null;
        try {
            con = getConnection();            
            ps = getDMLStatement("groups.keyed",con);
            ps.setString(1, groupname);
            rs = ps.executeQuery();
            if (rs.next()) {                
                String enabledString = rs.getString(1);
                boolean isEnabled= convertFromString(enabledString);
                g = createGroupObject(groupname, isEnabled);                
            }
            
        } catch (SQLException ex) {
            throw new IOException(ex);
        } finally {
            closeFinally(con, ps, rs);
        }
        
        return g;
    }
    
    

    /**
     * @see org.geoserver.security.GeoserverUserGroupService#getUsers()
     */
    public SortedSet<GeoserverUser> getUsers() throws IOException{
        Connection con=null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Map<String,GeoserverUser> map = new HashMap<String,GeoserverUser>();
        try {
            con = getConnection();
            ps = getDMLStatement("users.all",con);            
            rs = ps.executeQuery();
            while (rs.next()) {                
                String username = rs.getString(1);
                String password = rs.getString(2);
                String enabledString = rs.getString(3);
                boolean isEnabled= convertFromString(enabledString);
                GeoserverUser u = createUserObject(username,password, isEnabled);                
                map.put(username, u);
            }
            
            ps.close();
            rs.close();
            
            ps = getDMLStatement("userprops.all",con);
            rs = ps.executeQuery();
            while (rs.next()) {    
                String useName = rs.getString(1);
                String propName = rs.getString(2);
                Object propValue = rs.getString(3);
                GeoserverUser u = map.get(useName);
                if (u!=null) {
                    u.getProperties().put(propName, propValue==null ? "" : propValue);
                }
            }
        } catch (SQLException ex) {
            throw new IOException(ex);
        } finally {
            closeFinally(con, ps, rs);
        }
                                        
        SortedSet<GeoserverUser> users = new TreeSet<GeoserverUser>();
        users.addAll(map.values());
        return Collections.unmodifiableSortedSet(users);
    }
    
    /**
     * @see org.geoserver.security.GeoserverUserGroupService#getUserGroups()
     */
    public SortedSet<GeoserverUserGroup> getUserGroups() throws IOException{
        Connection con=null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Collection<GeoserverUserGroup> tmp = new ArrayList<GeoserverUserGroup>();
        try {
            con = getConnection();
            ps = getDMLStatement("groups.all",con);            
            rs = ps.executeQuery();
            while (rs.next()) {                
                String groupname = rs.getString(1);
                String enabledString = rs.getString(2);
                boolean isEnabled= convertFromString(enabledString);
                GeoserverUserGroup g = createGroupObject(groupname, isEnabled);
                tmp.add(g);
            }
        } catch (SQLException ex) {
            throw new IOException(ex);
        } finally {
            closeFinally(con, ps, rs);
        }        
        
        SortedSet<GeoserverUserGroup> groups = new TreeSet<GeoserverUserGroup>();
        groups.addAll(tmp);
        return Collections.unmodifiableSortedSet(groups);
    }

    
    /**
     * @see org.geoserver.security.GeoserverUserGroupService#createUserObject(java.lang.String, java.lang.String, boolean)
     */
    public GeoserverUser createUserObject(String username,String password, boolean isEnabled) throws IOException{
       GeoserverUser user = new GeoserverUser(username, getUserDetails());
       user.setEnabled(isEnabled);
       user.setPassword(password==null ? "" : password);
       return user;
    }
    
    /**
     * @see org.geoserver.security.GeoserverUserGroupService#createGroupObject(java.lang.String, boolean)
     */
    public GeoserverUserGroup createGroupObject(String groupname, boolean isEnabled) throws IOException{
        GeoserverUserGroup group = new GeoserverUserGroup(groupname);
        group.setEnabled(isEnabled);
        return group;
    }
    
    /**
     * @see org.geoserver.security.GeoserverUserGroupService#getGroupsForUser(org.geoserver.security.impl.GeoserverUser)
     */
    public  SortedSet<GeoserverUserGroup> getGroupsForUser (GeoserverUser user) throws IOException{        
        Connection con=null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Collection<GeoserverUserGroup> tmp = new ArrayList<GeoserverUserGroup>();
        try {
            con = getConnection();
            ps = getDMLStatement("groupmembers.groupsForUser",con);
            ps.setString(1, user.getUsername());
            rs = ps.executeQuery();
            while (rs.next()) {                
                String groupname = rs.getString(1);
                String enabledString = rs.getString(2);
                boolean isEnabled= convertFromString(enabledString);
                GeoserverUserGroup g = createGroupObject(groupname, isEnabled);
                tmp.add(g);
            }
        } catch (SQLException ex) {
            throw new IOException(ex);
        } finally {
            closeFinally(con, ps, rs);
        }        
                        
        TreeSet<GeoserverUserGroup>  groups =  new TreeSet<GeoserverUserGroup>();
        groups.addAll(tmp);
        return Collections.unmodifiableSortedSet(groups);
    }
    
    
    /**
     * @see org.geoserver.security.GeoserverUserGroupService#getUsersForGroup(org.geoserver.security.impl.GeoserverUserGroup)
     */
    public  SortedSet<GeoserverUser> getUsersForGroup (GeoserverUserGroup group) throws IOException{
        Connection con=null;
        PreparedStatement ps = null,ps2 = null;
        ResultSet rs = null,rs2=null;
        Map<String,GeoserverUser> map = new HashMap<String,GeoserverUser>();
        try {
            con = getConnection();
            ps = getDMLStatement("groupmembers.usersForGroup",con);
                        

            ps.setString(1, group.getGroupname());
            rs = ps.executeQuery();
            while (rs.next()) {                
                String username = rs.getString(1);
                String password = rs.getString(2);
                String enabledString = rs.getString(3);
                boolean isEnabled= convertFromString(enabledString);
                GeoserverUser u = createUserObject(username,password, isEnabled);                
                map.put(username,u);         
                
            }
            rs.close();
            ps.close();
            
            ps = getDMLStatement("userprops.userPropsForGroup",con);
            ps.setString(1, group.getGroupname());
            rs = ps.executeQuery();
            while (rs.next()) {
                String userName = rs.getString(1);                
                String propName = rs.getString(2);
                Object propValue = rs.getObject(3);
                GeoserverUser u = map.get(userName);
                if (u!=null) {
                    u.getProperties().put(propName, propValue==null ? "" : propValue);
                }
            }                                

        } catch (SQLException ex) {
            throw new IOException(ex);
        } finally {
            closeFinally(null, ps2, rs2);
            closeFinally(con, ps, rs);
        }
                                
        TreeSet<GeoserverUser>users= new TreeSet<GeoserverUser>();
        users.addAll(map.values());
        return Collections.unmodifiableSortedSet(users);
    }


    /**
     * @see org.geoserver.security.GeoserverUserGroupService#load()
     */
    public void load() throws IOException {
        // do nothing
    }



    /**
     * @see org.geoserver.security.GeoserverUserGroupService#registerUserGroupChangedListener(org.geoserver.security.event.UserGroupChangedListener)
     */
    public void registerUserGroupLoadedListener(UserGroupLoadedListener listener) {
        listeners.add(listener);
    }


    /**
     * @see org.geoserver.security.GeoserverUserGroupService#unregisterUserGroupChangedListener(org.geoserver.security.event.UserGroupChangedListener)
     */
    public void unregisterUserGroupLoadedListener(UserGroupLoadedListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Fire {@link UserGroupLoadedEvent} for all listeners
     */
    protected void fireUserGroupLoadedEvent() {
        UserGroupLoadedEvent event = new UserGroupLoadedEvent(this);
        for (UserGroupLoadedListener listener : listeners) {
            listener.usersAndGroupsChanged(event);
        }
    }

    /**
     * The root configuration for the user group service.
     */
    public File getConfigRoot() throws IOException {
        return new File(getSecurityManager().getUserGroupRoot(), getName());
    }
}
