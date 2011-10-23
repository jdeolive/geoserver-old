/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.geoserver.security.jdbc;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

import org.geoserver.security.GeoServerSecurityManager;
import org.geoserver.security.GeoserverGrantedAuthorityService;
import org.geoserver.security.GeoserverUserGroupService;
import org.geoserver.security.config.JdbcSecurityServiceConfig;
import org.geoserver.security.config.impl.JdbcSecurityServiceConfigImpl;
import org.geoserver.security.impl.Util;



public class JDBCTestSupport {
    
    static Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.geoserver.security.jdbc");
    
    protected static void dropExistingTables(AbstractJDBCService service) throws IOException {
        Connection con = null;
        try {
            con = service.getDataSource().getConnection();
            dropExistingTables(service,con);
        } catch (SQLException ex) {
            throw new IOException(ex);
        } finally {
            try { if (con != null) con.close();} catch (SQLException ex) {};;
        }        
    }
        
    public static void dropExistingTables(AbstractJDBCService service,Connection con) throws IOException {
        PreparedStatement ps = null;
        try {            
            for (String stmt : service.getOrderedNamesForDrop()) {
                try {
                    ps= service.getDDLStatement(stmt, con);
                    ps.execute();
                    ps.close();
                } catch (SQLException ex) {
                    //ex.printStackTrace();
                }
            }
            con.commit();
        } catch (SQLException ex) {
            throw new IOException(ex);
        } finally {
            try { if (ps!=null) ps.close(); } catch (SQLException ex) {};
        }        
    }

    public static boolean isFixtureDisabled(String fixtureId) {
        final String property = System.getProperty("gs." + fixtureId);
        return property != null && "false".equals(property.toLowerCase());                 
    }
    
    protected static GeoserverUserGroupService createH2UserGroupService(String serviceName, 
        GeoServerSecurityManager securityManager) throws IOException {
        
        JdbcSecurityServiceConfig config = new JdbcSecurityServiceConfigImpl();           
        config.setName(serviceName);
        config.setConnectURL("jdbc:h2:target/h2/security");
        config.setDriverClassName("org.h2.Driver");
        config.setUserName("sa");
        config.setPassword("");                            
        config.setClassName(JDBCUserGroupService.class.getName());
        config.setStateless(true);
        config.setPropertyFileNameDDL(JDBCUserGroupService.DEFAULT_DDL_FILE);
        config.setPropertyFileNameDML(JDBCUserGroupService.DEFAULT_DML_FILE);
        securityManager.saveUserGroupService(config);

        return securityManager.loadUserGroupService(serviceName);
    }

    protected static GeoserverGrantedAuthorityService createH2GrantedAuthorityService(
        String serviceName, GeoServerSecurityManager securityManager) throws IOException {
        
        JdbcSecurityServiceConfig config = new JdbcSecurityServiceConfigImpl();
        
        config.setName(serviceName);
        config.setConnectURL("jdbc:h2:target/h2/security");
        config.setDriverClassName("org.h2.Driver");
        config.setUserName("sa");
        config.setPassword("");                    
        config.setClassName(JDBCGrantedAuthorityService.class.getName());
        config.setStateless(true);
        config.setPropertyFileNameDDL(JDBCGrantedAuthorityService.DEFAULT_DDL_FILE);
        config.setPropertyFileNameDML(JDBCGrantedAuthorityService.DEFAULT_DML_FILE);
        securityManager.saveRoleService(config);
        return securityManager.loadRoleService(serviceName);
    }

    static  protected GeoserverGrantedAuthorityService createGrantedAuthorityService(
        String fixtureId, LiveDbmsDataSecurity data, GeoServerSecurityManager securityManager) 
            throws IOException {
    
        JdbcSecurityServiceConfigImpl config = new
        JdbcSecurityServiceConfigImpl();
        
        Properties props=Util.loadUniversal(new FileInputStream(data.getFixture()));
    
        config.setName(fixtureId);        
        config.setConnectURL(props.getProperty("url"));
        config.setDriverClassName(props.getProperty("driver"));
        config.setUserName(props.getProperty("user") == null ? props.getProperty("username") : props.getProperty("user"));
        config.setPassword(props.getProperty("password"));            
        config.setClassName(JDBCGrantedAuthorityService.class.getName());
        config.setStateless(true);
        if ("mysql".equals(fixtureId)) {
            config.setPropertyFileNameDDL("rolesddl.mysql.xml");            
        } else {
            config.setPropertyFileNameDDL(JDBCGrantedAuthorityService.DEFAULT_DDL_FILE);
        }
        config.setPropertyFileNameDML(JDBCGrantedAuthorityService.DEFAULT_DML_FILE);

        securityManager.saveRoleService(config);
        return securityManager.loadRoleService(fixtureId);
    }
    
    static protected GeoserverUserGroupService createUserGroupService(String fixtureId,
        LiveDbmsDataSecurity data, GeoServerSecurityManager securityManager) throws IOException {
        
        JdbcSecurityServiceConfigImpl config = new
        JdbcSecurityServiceConfigImpl();
        
        Properties props=Util.loadUniversal(new FileInputStream(data.getFixture()));
    
        config.setName(fixtureId);        
        config.setConnectURL(props.getProperty("url"));
        config.setDriverClassName(props.getProperty("driver"));
        config.setUserName(props.getProperty("user")== null ? props.getProperty("username"): props.getProperty("user"));
        config.setPassword(props.getProperty("password"));                       
        config.setClassName(JDBCUserGroupService.class.getName());
        config.setStateless(true);
        if ("mysql".equals(fixtureId)) {
            config.setPropertyFileNameDDL("usersddl.mysql.xml");            
        } else {
            config.setPropertyFileNameDDL(JDBCUserGroupService.DEFAULT_DDL_FILE);
        }
        config.setPropertyFileNameDML(JDBCUserGroupService.DEFAULT_DML_FILE);
        securityManager.saveUserGroupService(config);
        return securityManager.loadUserGroupService(fixtureId);
    }


}
