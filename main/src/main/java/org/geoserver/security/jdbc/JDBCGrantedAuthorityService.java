package org.geoserver.security.jdbc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;


import org.geoserver.security.GeoserverGrantedAuthorityService;
import org.geoserver.security.GeoserverStoreFactory;
import org.geoserver.security.config.JdbcBaseSecurityServiceConfig;
import org.geoserver.security.config.SecurityNamedServiceConfig;
import org.geoserver.security.event.GrantedAuthorityLoadedEvent;
import org.geoserver.security.event.GrantedAuthorityLoadedListener;
import org.geoserver.security.impl.GeoserverGrantedAuthority;
import org.geoserver.security.impl.Util;

/**
 * JDBC implementation of {@link GeoserverGrantedAuthorityService}
 * 
 * @author christian
 *
 */
public  class JDBCGrantedAuthorityService extends AbstractJDBCService implements GeoserverGrantedAuthorityService {
    
    public final static String DEFAULT_DML_FILE="rolesdml.xml";
    public final static String DEFAULT_DDL_FILE="rolesddl.xml";
    
    static {
        GeoserverStoreFactory.Singleton.registerGrantedAuthorityMapping(
                JDBCGrantedAuthorityService.class, JDBCGrantedAuthorityStore.class);
    }

    
    /** logger */
    static Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.geoserver.security.jdbc");
    protected Set<GrantedAuthorityLoadedListener> listeners = 
        Collections.synchronizedSet(new HashSet<GrantedAuthorityLoadedListener>());
    
    
    /**
     * @param name
     * @throws IOException
     */
    public JDBCGrantedAuthorityService(String name) throws IOException{
        super(name);
    }

    
    /**
     * Uses {@link #initializeDSFromConfig(SecurityNamedServiceConfig)}
     * and {@link #checkORCreateJDBCPropertyFile(String, File, String)} 
     *  
     * @see org.geoserver.security.GeoserverGrantedAuthorityService#initializeFromConfig(org.geoserver.security.config.SecurityNamedServiceConfig)
     */
    @Override
    public void initializeFromConfig(SecurityNamedServiceConfig config) throws IOException {
        
        initializeDSFromConfig(config);

        if (config instanceof JdbcBaseSecurityServiceConfig) {
            JdbcBaseSecurityServiceConfig jdbcConfig =
                (JdbcBaseSecurityServiceConfig) config;
                        
            String fileNameDML =jdbcConfig.getPropertyFileNameDML();
            File file = checkORCreateJDBCPropertyFile(fileNameDML,
                    Util.getGrantedAuthorityNamedRoot(name),DEFAULT_DML_FILE);                        
            dmlProps = Util.loadUniversal(new FileInputStream(file));
            
            String fileNameDDL =jdbcConfig.getPropertyFileNameDDL();
            file = checkORCreateJDBCPropertyFile(fileNameDDL,
                    Util.getGrantedAuthorityNamedRoot(name),DEFAULT_DDL_FILE);                        

            ddlProps = Util.loadUniversal(new FileInputStream(file));            
        }
        
    }

    
    
    /**
     * @see org.geoserver.security.jdbc.AbstractJDBCService#getOrderedNamesForCreate()
     */
    protected  String[] getOrderedNamesForCreate() {
        return new String[] {
            "roles.create","roleprops.create","userroles.create","userroles.indexcreate",
            "grouproles.create","grouproles.indexcreate"
        };
                
    }
    /** 
     * @see org.geoserver.security.jdbc.AbstractJDBCService#getOrderedNamesForDrop()
     */
    protected  String[] getOrderedNamesForDrop() {
        return new String[] {
            "grouproles.drop","userroles.drop","roleprops.drop","roles.drop"
        };

    }

    
    /**
     * @see org.geoserver.security.GeoserverGrantedAuthorityService#getGrantedAuthorityByName(java.lang.String)
     */
    public GeoserverGrantedAuthority getGrantedAuthorityByName(String role)
            throws IOException {
        
        Connection con=null;
        PreparedStatement ps = null,ps2=null;
        ResultSet rs = null,rs2=null;
        GeoserverGrantedAuthority  roleObject  = null;
        try {
            con = getConnection();
            ps = getDMLStatement("roles.keyed",con);            
            ps.setString(1, role);
            rs = ps.executeQuery();
            if (rs.next()) {                
                roleObject  = createGrantedAuthorityObject(role);
                ps2 = getDMLStatement("roleprops.selectForRole",con);            
                ps2.setString(1, role);
                rs2 = ps2.executeQuery();
                while (rs2.next()) {
                    String propName = rs2.getString(1);
                    Object propValue = rs2.getObject(2);
                    roleObject.getProperties().put(propName, propValue==null ? "" : propValue );
                }                
            }                
        } catch (SQLException ex) {
            throw new IOException(ex);
        } finally {
            closeFinally(null, ps2, rs2);
            closeFinally(con, ps, rs);
        }
        return roleObject;
 
    }
    

    /**
     * @see org.geoserver.security.GeoserverGrantedAuthorityService#getRoles()
     */
    public  SortedSet<GeoserverGrantedAuthority> getRoles() throws IOException {
        Connection con=null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Map<String,GeoserverGrantedAuthority> map = new HashMap<String,GeoserverGrantedAuthority>();
        try {
            con = getConnection();
            ps = getDMLStatement("roles.all",con);            
            rs = ps.executeQuery();
            while (rs.next()) {                
                String rolename = rs.getString(1);                
                GeoserverGrantedAuthority roleObject = createGrantedAuthorityObject(rolename);            
                map.put(rolename, roleObject);
            }
            
            ps.close();
            rs.close();
            
            ps = getDMLStatement("roleprops.all",con);
            rs = ps.executeQuery();
            while (rs.next()) {    
                String roleName = rs.getString(1);
                String propName = rs.getString(2);
                Object propValue = rs.getString(3);
                GeoserverGrantedAuthority roleObject = map.get(roleName);
                if (roleObject!=null) {
                    roleObject.getProperties().put(propName, propValue==null ? "" : propValue);
                }
            }
        } catch (SQLException ex) {
            throw new IOException(ex);
        } finally {
            closeFinally(con, ps, rs);
        }
                                        
        SortedSet<GeoserverGrantedAuthority> roles = new TreeSet<GeoserverGrantedAuthority>();
        roles.addAll(map.values());               
        return Collections.unmodifiableSortedSet(roles);
                
    }
    
    /**
     * @see org.geoserver.security.GeoserverGrantedAuthorityService#getParentMappings()
     */
    public  Map<String,String> getParentMappings() throws IOException {
        Connection con=null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Map<String,String> map = new HashMap<String,String>();
        try {
            con = getConnection();
            ps = getDMLStatement("roles.all",con);            
            rs = ps.executeQuery();
            while (rs.next()) {                
                String rolename = rs.getString(1);
                String parentname = rs.getString(2);
                map.put(rolename, parentname);
            }
        } catch (SQLException ex) {
            throw new IOException(ex);
        } finally {
            closeFinally(con, ps, rs);
        }                
        return Collections.unmodifiableMap(map);
    }
    

    /**
     * @see org.geoserver.security.GeoserverGrantedAuthorityService#createGrantedAuthorityObject(java.lang.String)
     */
    public GeoserverGrantedAuthority createGrantedAuthorityObject(String role) {
        return new GeoserverGrantedAuthority(role);
    }
    
    
    
    /** 
     * @see org.geoserver.security.GeoserverGrantedAuthorityService#getRolesForUser(java.lang.String)
     */
    public  SortedSet<GeoserverGrantedAuthority> getRolesForUser(String username) throws IOException {
        Connection con=null;
        PreparedStatement ps = null,ps2 = null;
        ResultSet rs = null,rs2=null;
        Map<String,GeoserverGrantedAuthority> map = new HashMap<String,GeoserverGrantedAuthority>();
        try {
            con = getConnection();
            ps = getDMLStatement("userroles.rolesForUser",con);
                        

            ps.setString(1, username);
            rs = ps.executeQuery();
            while (rs.next()) {                
                String rolename = rs.getString(1);                
                GeoserverGrantedAuthority roleObject = createGrantedAuthorityObject(rolename);                
                map.put(rolename,roleObject);         
                
            }
            rs.close();
            ps.close();
            
            ps = getDMLStatement("roleprops.selectForUser",con);
            ps.setString(1, username);
            rs = ps.executeQuery();
            while (rs.next()) {
                String rolename = rs.getString(1);                
                String propName = rs.getString(2);
                Object propValue = rs.getObject(3);
                GeoserverGrantedAuthority roleObject = map.get(rolename);
                if (roleObject!=null) {
                    roleObject.getProperties().put(propName, propValue==null ? "" : propValue);
                }
            }                                

        } catch (SQLException ex) {
            throw new IOException(ex);
        } finally {
            closeFinally(null, ps2, rs2);
            closeFinally(con, ps, rs);
        }
                                
        TreeSet<GeoserverGrantedAuthority>roles= new TreeSet<GeoserverGrantedAuthority>();
        roles.addAll(map.values());
        return Collections.unmodifiableSortedSet(roles);
    }
    
    /** 
     * @see org.geoserver.security.GeoserverGrantedAuthorityService#getRolesForGroup(java.lang.String)
     */
    public  SortedSet<GeoserverGrantedAuthority> getRolesForGroup(String groupname) throws IOException {
        Connection con=null;
        PreparedStatement ps = null,ps2 = null;
        ResultSet rs = null,rs2=null;
        Map<String,GeoserverGrantedAuthority> map = new HashMap<String,GeoserverGrantedAuthority>();
        try {
            con = getConnection();
            ps = getDMLStatement("grouproles.rolesForGroup",con);
                        

            ps.setString(1, groupname);
            rs = ps.executeQuery();
            while (rs.next()) {                
                String rolename = rs.getString(1);
                GeoserverGrantedAuthority roleObject = createGrantedAuthorityObject(rolename);                
                map.put(rolename,roleObject);         
                
            }
            rs.close();
            ps.close();
            
            ps = getDMLStatement("roleprops.selectForGroup",con);
            ps.setString(1, groupname);
            rs = ps.executeQuery();
            while (rs.next()) {
                String rolename = rs.getString(1);                
                String propName = rs.getString(2);
                Object propValue = rs.getObject(3);
                GeoserverGrantedAuthority roleObject = map.get(rolename);
                if (roleObject!=null) {
                    roleObject.getProperties().put(propName, propValue==null ? "" : propValue);
                }
            }                                

        } catch (SQLException ex) {
            throw new IOException(ex);
        } finally {
            closeFinally(null, ps2, rs2);
            closeFinally(con, ps, rs);
        }
                                
        TreeSet<GeoserverGrantedAuthority>roles= new TreeSet<GeoserverGrantedAuthority>();
        roles.addAll(map.values());
        return Collections.unmodifiableSortedSet(roles);
    }


    /**
     * @see org.geoserver.security.GeoserverGrantedAuthorityService#load()
     */
    public void load() throws IOException {
        // do nothing
    }



    /**
     * @see org.geoserver.security.GeoserverGrantedAuthorityService#getParentRole(org.geoserver.security.impl.GeoserverGrantedAuthority)
     */
    public GeoserverGrantedAuthority getParentRole(GeoserverGrantedAuthority role)
            throws IOException {

        Connection con=null;
        PreparedStatement ps = null,ps2=null;
        ResultSet rs = null,rs2=null;
        GeoserverGrantedAuthority  roleObject  = null;
        try {
            con = getConnection();
            ps = getDMLStatement("roles.keyed",con);            
            ps.setString(1, role.getAuthority());
            rs = ps.executeQuery();
            if (rs.next()) {                
                String parent = rs.getString(1);                
                if (parent!=null) { // do we have a parent ?
                    roleObject  = createGrantedAuthorityObject(parent);
                    ps2 = getDMLStatement("roleprops.selectForRole",con);            
                    ps2.setString(1, parent);
                    rs2 = ps2.executeQuery();
                    while (rs2.next()) {
                        String propName = rs2.getString(1);
                        Object propValue = rs2.getObject(2);
                        roleObject.getProperties().put(propName, propValue==null ? "" : propValue);
                    }
                }    
            }                
        } catch (SQLException ex) {
            throw new IOException(ex);
        } finally {
            closeFinally(null, ps2, rs2);
            closeFinally(con, ps, rs);
        }
        return roleObject;
    }


    /** 
     * @see org.geoserver.security.GeoserverGrantedAuthorityService#registerGrantedAuthorityChangedListener(org.geoserver.security.event.GrantedAuthorityChangedListener)
     */
    public void registerGrantedAuthorityLoadedListener(GrantedAuthorityLoadedListener listener) {
        listeners.add(listener);
    }


    /** 
     * @see org.geoserver.security.GeoserverGrantedAuthorityService#unregisterGrantedAuthorityChangedListener(org.geoserver.security.event.GrantedAuthorityChangedListener)
     */
    public void unregisterGrantedAuthorityLoadedListener(GrantedAuthorityLoadedListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Fire {@link GrantedAuthorityLoadedEvent} for all listeners
     */
    protected void fireGrantedAuthorityChangedEvent() {
        GrantedAuthorityLoadedEvent event = new GrantedAuthorityLoadedEvent(this);
        for (GrantedAuthorityLoadedListener listener : listeners) {
            listener.grantedAuthoritiesChanged(event);
        }
    }


    /**
     * @see org.geoserver.security.GeoserverGrantedAuthorityService#getGroupNamesForRole(org.geoserver.security.impl.GeoserverGrantedAuthority)
     */
    public SortedSet<String> getGroupNamesForRole(GeoserverGrantedAuthority role) throws IOException {
        Connection con=null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        SortedSet<String> result = new TreeSet<String>();
        try {
            con = getConnection();
            ps = getDMLStatement("grouproles.groupsForRole",con);
                        
            ps.setString(1, role.getAuthority());
            rs = ps.executeQuery();
            while (rs.next()) {                
                String groupname = rs.getString(1);                
                result.add(groupname);
            }
        } catch (SQLException ex) {
            throw new IOException(ex);
        } finally {
            closeFinally(con, ps, rs);
        }
        return Collections.unmodifiableSortedSet(result);
    }


    /**
     * @see org.geoserver.security.GeoserverGrantedAuthorityService#getUserNamesForRole(org.geoserver.security.impl.GeoserverGrantedAuthority)
     */
    public SortedSet<String> getUserNamesForRole(GeoserverGrantedAuthority role) throws IOException{
        Connection con=null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        SortedSet<String> result = new TreeSet<String>();
        try {
            con = getConnection();
            ps = getDMLStatement("userroles.usersForRole",con);
                        

            ps.setString(1, role.getAuthority());
            rs = ps.executeQuery();
            while (rs.next()) {                
                String username = rs.getString(1);                
                result.add(username);
            }
        } catch (SQLException ex) {
            throw new IOException(ex);
        } finally {
            closeFinally(con, ps, rs);
        }
        return Collections.unmodifiableSortedSet(result);
    }    

    /**
     * @see org.geoserver.security.GeoserverGrantedAuthorityService#personalizeRoleParams(java.lang.String, java.util.Properties, java.lang.String, java.util.Properties)
     * 
     * Default implementation: if a user property name equals a role propertyname, 
     * take the value from to user property and use it for the role property. 
     * 
     */
    public  Properties personalizeRoleParams (String roleName,Properties roleParams, 
            String userName,Properties userProps) throws IOException {
        Properties props = null;
        
        // this is true if the set is modified --> common 
        // property names exist
        
        props = new Properties();
        boolean personalized=false;
        
        for (Object key: roleParams.keySet()) {
            if (userProps.containsKey(key)) {
                props.put(key, userProps.get(key));
                personalized=true;
            }
            else
                props.put(key,roleParams.get(key));
        }
        return personalized ?  props : null;
    }

}
