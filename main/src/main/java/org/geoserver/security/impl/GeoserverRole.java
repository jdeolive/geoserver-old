/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.geoserver.security.impl;


import java.util.Properties;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;

/**
 * Extends {@link GrantedAuthority} and represents an 
 * anonymous role 
 * 
 *  If a user name is set, the role is personalized
 * 
 * Example: the role ROLE_EMPLOYEE could have a role 
 * parameter EPLOYEE_NUMBER

 * 
 * 
 * @author christian
 *
 */
public class GeoserverRole extends GrantedAuthorityImpl implements Comparable<GeoserverRole>{

    
    /**
     * Predefined Objects 
     */
    public final static GeoserverRole ADMIN_ROLE = new GeoserverRole("ROLE_ADMINISTRATOR");
    public final static GeoserverRole HASANY_ROLE = new GeoserverRole("*");
    
    
    /**
     *  
     */
    private static final long serialVersionUID = 1L;
    
    protected String userName;


    protected Properties properties;


    public GeoserverRole(String role) {
        super(role);
        
    }
    public int compareTo(GeoserverRole o) {
        if (o==null) return 1;
        if (getAuthority().equals(o.getAuthority())) {
            if (getUserName()==null && o.getUserName()==null)
                return 0;
            if (getUserName()==null) 
                return -1;
            if (o.getUserName()==null) 
                return 1;
            return getUserName().compareTo(o.getUserName());
        }
        return getAuthority().compareTo(o.getAuthority());        
    }
    
    /**
     * Generic mechanism to store 
     * additional information (role paramaters)
     * 
     * examples: a user with the role ROLE_EMPLOYEE
     * could have a role parameter EMPLOYEE_NUMBER
     * To be filled by the backend store
     * 
     * @return 
     */
    public Properties getProperties() {
        if (properties==null)
            properties = new Properties();
        return properties;    
    }

    public String getUserName() {
        return userName;
    }


    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public boolean isAnonymous() {
        return getUserName()==null;
    }

    
    public boolean equals(Object obj) {
        
        if (obj instanceof String && getUserName()==null) {
            return super.equals(obj);
        }

        if (obj instanceof GrantedAuthority && getUserName()==null) {
            return super.equals(obj);
        }

        if (obj instanceof GeoserverRole) {
            return compareTo((GeoserverRole) obj)==0;
        }
        return false;
    }


    public int hashCode() {
        int hash = getAuthority().hashCode();
        if (getUserName()!=null)
            hash+=getUserName().hashCode();
        return hash;
            
    }

    public String toString() {
        if (getUserName()!=null) {
            StringBuffer buff = new StringBuffer(super.toString());
            buff.append(" for user ").append(getUserName());
            return buff.toString();
        } else
          return super.toString();
    }

    
    
}
