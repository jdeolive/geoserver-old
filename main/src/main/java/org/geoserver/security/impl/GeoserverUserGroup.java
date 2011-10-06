/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.geoserver.security.impl;



import java.io.Serializable;

import org.geoserver.security.GeoserverUserDetailsService;

/**
 * Implementation of {@link UserGroup}
 * 
 * @author christian
 *
 */
public class GeoserverUserGroup implements Comparable<GeoserverUserGroup>,Serializable{
    
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final String groupname;
    private boolean enabled;
    protected transient GeoserverUserDetailsService detailsService;
//    protected Properties properties;



    public GeoserverUserGroup(String name) {
        groupname=name;
        enabled=true;
    }


    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }


    
    @Override
    public boolean equals(Object rhs) {
        if (rhs instanceof GeoserverUserGroup) {
            return getGroupname().equals(((GeoserverUserGroup) rhs).getGroupname());
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return getGroupname().hashCode();
    }
    
    public int compareTo(GeoserverUserGroup o) {
        if (o==null) return 1;
        return getGroupname().compareTo(o.getGroupname());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Groupname: ").append(getGroupname());
        sb.append(" Enabled: ").append(this.enabled);
        return sb.toString();
    }

    public String getGroupname() {
        return groupname;
    }
        
    /**
     * Generic mechanism to store 
     * additinall information
     * 
     * To be filled by the backend store
     * 
     * @return 
     */
//    public Properties getProperties() {
//        if (properties==null)
//            properties = new Properties();
//        return properties;    
//    }

}
