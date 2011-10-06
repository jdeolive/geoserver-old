/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.geoserver.security.jdbc;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import org.geoserver.security.GeoserverGrantedAuthorityService;
import org.geoserver.security.GeoserverGrantedAuthorityStore;
import org.geoserver.security.impl.GeoserverGrantedAuthority;
import org.geoserver.security.impl.RoleHierarchyHelper;

/**
 * JDBC Implementation of {@link GeoserverGrantedAuthorityStore}
 * 
 * @author christian
 *
 */
public class JDBCGrantedAuthorityStore extends JDBCGrantedAuthorityService implements GeoserverGrantedAuthorityStore {

    protected boolean modified;
    protected Connection connection;
    
    
    public JDBCGrantedAuthorityStore(String name) throws IOException {
        super(name);
        
    }

    /** 
     * The identical connection is used until {@link #store()} or
     * {@link #load()} is called. Within a transaction it is not possible
     * to use different connections.
     * 
     * @see org.geoserver.security.jdbc.AbstractJDBCService#getConnection()
     */
    @Override
    public Connection getConnection() throws SQLException{
        if (connection == null)
            connection = super.getConnection();
        return connection;
    }
    
    @Override
    protected void closeConnection(Connection con) throws SQLException{
        // do nothing
    }

    /**
     * To be called at the the end of a transaction,
     * frees the current {@link Connection} 
     * 
     * @throws SQLException
     */
    protected void releaseConnection() throws SQLException{
        if (connection!=null) {
            connection.close();
            connection=null;
        }
    }


    /**
     * Executes {@link Connection#rollback() and
     * frees the connection object
     * 
     * @see org.geoserver.security.jdbc.JDBCGrantedAuthorityService#load()
     */
    public void load() throws IOException {
        // Simply roll back the transaction
        try {
            getConnection().rollback();
            releaseConnection();
            
        } catch (SQLException ex) {
            throw new IOException(ex);
        }
        setModified(false);
        //fireGrantedAuthorityChangedEvent();
    }

    
    protected void addRoleProperties(GeoserverGrantedAuthority role, Connection con) throws SQLException,IOException {
        if (role.getProperties().size()==0) return; // nothing to do
        
        PreparedStatement ps = getDMLStatement("roleprops.insert", con);
        try {
            for (Object key : role.getProperties().keySet()) {
                Object propertyVal = role.getProperties().get(key);
                ps.setString(1,role.getAuthority());
                ps.setString(2,key.toString());
                ps.setObject(3,propertyVal);
                ps.execute();
            }
        } finally {        
            closeFinally(null, ps, null);
        }        
    }
    
    
    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverGrantedAuthorityStore#addGrantedAuthority(org.geoserver.security.impl.GeoserverGrantedAuthority)
     */
    public void addGrantedAuthority(GeoserverGrantedAuthority role)  throws IOException{
    
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = getConnection();
            ps = getDMLStatement("roles.insert", con);
            ps.setString(1,role.getAuthority());
            //ps.setNull(2, Types.VARCHAR);
            ps.execute();

            addRoleProperties(role, con);
                                    
        } catch (SQLException ex) {
            throw new IOException(ex);
        } finally {
            closeFinally(con, ps, null);
        }
        setModified(true);
    }

    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverGrantedAuthorityStore#updateGrantedAuthority(org.geoserver.security.impl.GeoserverGrantedAuthority)
     */
    public void updateGrantedAuthority(GeoserverGrantedAuthority role) throws IOException {
 
        
        // No attributes for update   
        Connection con = null;
        PreparedStatement ps = null;
        try {
            
            con = getConnection();
            ps = getDMLStatement("roles.update", con);            
            ps.setString(1,role.getAuthority());
            ps.execute();
            
            ps.close();
            ps = getDMLStatement("roleprops.deleteForRole",con);
            ps.setString(1,role.getAuthority());
            ps.execute();
            
            addRoleProperties(role, con);
            
        } catch (SQLException ex) {
            throw new IOException(ex);
        } finally {
            closeFinally(con, ps, null);
        }
        setModified(true); // we do as if there was an update
        
    }

    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverGrantedAuthorityStore#removeGrantedAuthority(org.geoserver.security.impl.GeoserverGrantedAuthority)
     */
    public boolean removeGrantedAuthority(GeoserverGrantedAuthority role) throws IOException {
        Connection con = null;
        PreparedStatement ps = null;
        boolean retval = false;
        try {
            con = getConnection();
            ps = getDMLStatement("roles.delete", con);            
            ps.setString(1,role.getAuthority());
            ps.execute();
            retval= ps.getUpdateCount()>0;
            
            ps.close();
            ps = getDMLStatement("userroles.deleteRole",con);
            ps.setString(1,role.getAuthority());
            ps.execute();
            
            ps.close();
            ps = getDMLStatement("grouproles.deleteRole",con);
            ps.setString(1,role.getAuthority());
            ps.execute();
            
            ps.close();
            ps = getDMLStatement("roleprops.deleteForRole", con);
            ps.setString(1,role.getAuthority());
            ps.execute();
            
            ps.close();
            ps = getDMLStatement("roles.deleteParent", con);
            ps.setString(1,role.getAuthority());
            ps.execute();
                        
        } catch (SQLException ex) {
            throw new IOException(ex);
        } finally {
            closeFinally(con, ps, null);
        }        
        setModified(true);
        return retval;
    }


    /**
     * Executes {@link Connection#commit()} and frees
     * the connection
     * @see org.geoserver.security.GeoserverGrantedAuthorityStore#store()
     */
    public void store() throws IOException {
        // Simply commit the transaction
        try {
            getConnection().commit();
            releaseConnection();
        } catch (SQLException ex) {
            throw new IOException(ex);
        }
        setModified(false);
        //fireGrantedAuthorityChangedEvent();
    }


    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverGrantedAuthorityStore#associateRoleToUser(org.geoserver.security.impl.GeoserverGrantedAuthority, java.lang.String)
     */
    public void associateRoleToUser(GeoserverGrantedAuthority role, String username) throws IOException{
        
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = getConnection();
            ps = getDMLStatement("userroles.insert", con);
            ps.setString(1,role.getAuthority());            
            ps.setString(2,username);
            ps.execute();
        } catch (SQLException ex) {
            throw new IOException(ex);
        } finally {
            closeFinally(con, ps, null);
        }                
        setModified(true);
    }

    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverGrantedAuthorityStore#disAssociateRoleFromUser(org.geoserver.security.impl.GeoserverGrantedAuthority, java.lang.String)
     */
    public void disAssociateRoleFromUser(GeoserverGrantedAuthority role, String username) throws IOException{
    
       
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = getConnection();
            ps = getDMLStatement("userroles.delete", con);
            ps.setString(1,role.getAuthority());            
            ps.setString(2,username);
            ps.execute();
        } catch (SQLException ex) {
            throw new IOException(ex);
        } finally {
            closeFinally(con, ps, null);
        }                
        setModified(true);
    }

    
    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverGrantedAuthorityStore#associateRoleToGroup(org.geoserver.security.impl.GeoserverGrantedAuthority, java.lang.String)
     */
    public void associateRoleToGroup(GeoserverGrantedAuthority role, String groupname) throws IOException{
        
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = getConnection();
            ps = getDMLStatement("grouproles.insert", con);
            ps.setString(1,role.getAuthority());            
            ps.setString(2,groupname);
            ps.execute();
        } catch (SQLException ex) {
            throw new IOException(ex);
        } finally {
            closeFinally(con, ps, null);
        }                
        setModified(true);
    }

    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverGrantedAuthorityStore#disAssociateRoleFromGroup(org.geoserver.security.impl.GeoserverGrantedAuthority, java.lang.String)
     */
    public void disAssociateRoleFromGroup(GeoserverGrantedAuthority role, String groupname) throws IOException{
    
       
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = getConnection();
            ps = getDMLStatement("grouproles.delete", con);
            ps.setString(1,role.getAuthority());            
            ps.setString(2,groupname);
            ps.execute();
        } catch (SQLException ex) {
            throw new IOException(ex);
        } finally {
            closeFinally(con, ps, null);
        }                
        setModified(true);
    }
    
    
    public boolean isModified() {
        return modified;
    }
    
    public void setModified(boolean modified) {
        this.modified=modified;
    }

    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverGrantedAuthorityStore#setParentRole(org.geoserver.security.impl.GeoserverGrantedAuthority, org.geoserver.security.impl.GeoserverGrantedAuthority)
     */
    public void setParentRole(GeoserverGrantedAuthority role, GeoserverGrantedAuthority parentRole)
            throws IOException {
        
        RoleHierarchyHelper helper = new RoleHierarchyHelper(getParentMappings());
        if (helper.isValidParent(role.getAuthority(), 
                parentRole==null ? null : parentRole.getAuthority())==false)
            throw new IOException(parentRole.getAuthority() +
                    " is not a valid parent for " + role.getAuthority());    

        
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = getConnection();
            ps = getDMLStatement("roles.parentUpdate", con);
            if (parentRole == null)
               ps.setNull(1, Types.VARCHAR);
            else                
                ps.setString(1,parentRole.getAuthority());
            ps.setString(2,role.getAuthority());
            ps.execute();            
            
        } catch (SQLException ex) {
            throw new IOException(ex);
        } finally {
            closeFinally(con, ps, null);
        }
        setModified(true);        
    }



    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverGrantedAuthorityStore#clear()
     */
    public void clear() throws IOException {
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = getConnection();
            ps = getDMLStatement("grouproles.deleteAll", con);
            ps.execute();
            ps.close();
            
            ps = getDMLStatement("userroles.deleteAll", con);
            ps.execute();
            ps.close();
            
            ps = getDMLStatement("roleprops.deleteAll", con);
            ps.execute();
            ps.close();
            
            ps = getDMLStatement("roles.deleteAll", con);
            ps.execute();
            
        } catch (SQLException ex) {
            throw new IOException(ex);
        } finally {
            closeFinally(con, ps, null);
        }
        setModified(true);                                                
    }

    /* (non-Javadoc)
     * @see org.geoserver.security.GeoserverGrantedAuthorityStore#initializeFromServer(org.geoserver.security.GeoserverGrantedAuthorityService)
     */
    public void initializeFromService(GeoserverGrantedAuthorityService service) throws IOException {
        JDBCGrantedAuthorityService jdbcService= (JDBCGrantedAuthorityService) service;
        this.datasource=jdbcService.datasource;
        this.ddlProps=jdbcService.ddlProps;
        this.dmlProps=jdbcService.dmlProps;
        try {
            getConnection().commit();
        } catch (SQLException e) {
            throw new IOException(e);
        }
    }    
}
