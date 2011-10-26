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
import org.geoserver.security.GeoserverRoleService;
import org.geoserver.security.GeoserverUserGroupService;
import org.geoserver.security.impl.GeoserverRole;
import org.geoserver.security.impl.Util;
import org.geoserver.security.jdbc.config.impl.JdbcRoleServiceConfigImpl;
import org.geoserver.security.jdbc.config.impl.JdbcUserGroupServiceConfigImpl;



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
        
        JdbcUserGroupServiceConfigImpl config = new JdbcUserGroupServiceConfigImpl();           
        config.setName(serviceName);
        config.setConnectURL("jdbc:h2:target/h2/security");
        config.setDriverClassName("org.h2.Driver");
        config.setUserName("sa");
        config.setPassword("");                            
        config.setClassName(JDBCUserGroupService.class.getName());
        config.setStateless(true);
        config.setPropertyFileNameDDL(JDBCUserGroupService.DEFAULT_DDL_FILE);
        config.setPropertyFileNameDML(JDBCUserGroupService.DEFAULT_DML_FILE);
        config.setPasswordEncoderName("digestPasswordEncoder");
        securityManager.saveUserGroupService(config);

        return securityManager.loadUserGroupService(serviceName);
    }

    protected static GeoserverRoleService createH2RoleService(
        String serviceName, GeoServerSecurityManager securityManager) throws IOException {
        
        JdbcRoleServiceConfigImpl config = new JdbcRoleServiceConfigImpl();
        
        config.setName(serviceName);
        config.setConnectURL("jdbc:h2:target/h2/security");
        config.setDriverClassName("org.h2.Driver");
        config.setUserName("sa");
        config.setPassword("");                    
        config.setClassName(JDBCRoleService.class.getName());
        config.setStateless(true);
        config.setPropertyFileNameDDL(JDBCRoleService.DEFAULT_DDL_FILE);
        config.setPropertyFileNameDML(JDBCRoleService.DEFAULT_DML_FILE);
        config.setAdminRoleName(GeoserverRole.ADMIN_ROLE.getAuthority());
        securityManager.saveRoleService(config);
        return securityManager.loadRoleService(serviceName);
    }

    static  protected GeoserverRoleService createRoleService(
        String fixtureId, LiveDbmsDataSecurity data, GeoServerSecurityManager securityManager) 
            throws IOException {
    
        JdbcRoleServiceConfigImpl config = new
        JdbcRoleServiceConfigImpl();
        
        Properties props=Util.loadUniversal(new FileInputStream(data.getFixture()));
    
        config.setName(fixtureId);        
        config.setConnectURL(props.getProperty("url"));
        config.setDriverClassName(props.getProperty("driver"));
        config.setUserName(props.getProperty("user") == null ? props.getProperty("username") : props.getProperty("user"));
        config.setPassword(props.getProperty("password"));            
        config.setClassName(JDBCRoleService.class.getName());
        config.setAdminRoleName(GeoserverRole.ADMIN_ROLE.getAuthority());
        config.setStateless(true);
        if ("mysql".equals(fixtureId)) {
            config.setPropertyFileNameDDL("rolesddl.mysql.xml");            
        } else {
            config.setPropertyFileNameDDL(JDBCRoleService.DEFAULT_DDL_FILE);
        }
        config.setPropertyFileNameDML(JDBCRoleService.DEFAULT_DML_FILE);

        securityManager.saveRoleService(config);
        return securityManager.loadRoleService(fixtureId);
    }
    
    static protected GeoserverUserGroupService createUserGroupService(String fixtureId,
        LiveDbmsDataSecurity data, GeoServerSecurityManager securityManager) throws IOException {
        
        JdbcUserGroupServiceConfigImpl config = new
        JdbcUserGroupServiceConfigImpl();
        
        Properties props=Util.loadUniversal(new FileInputStream(data.getFixture()));
    
        config.setName(fixtureId);        
        config.setConnectURL(props.getProperty("url"));
        config.setDriverClassName(props.getProperty("driver"));
        config.setUserName(props.getProperty("user")== null ? props.getProperty("username"): props.getProperty("user"));
        config.setPassword(props.getProperty("password"));                       
        config.setClassName(JDBCUserGroupService.class.getName());
        config.setPasswordEncoderName("digestPasswordEncoder");
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
