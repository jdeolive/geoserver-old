/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.security.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.security.GeoserverUserDetailsService;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;

/**
 * Extending the {@link User} implementation to add
 * support for {@link GrantedAuthority} objects inherited
 * by {@link UserGroup} objects the user belongs to
 * 
 * @author christian
 * 
 */
public class GeoserverUser  implements UserDetails, CredentialsContainer,Comparable<GeoserverUser>{
    


    private static final long serialVersionUID = 1L;
    
    
    /**
     * The default administrator
     */
    public final static GeoserverUser DEFAULT_ADMIN;
    static {
        DEFAULT_ADMIN=new GeoserverUser("admin");
        DEFAULT_ADMIN.setPassword("geoserver");
        DEFAULT_ADMIN.setEnabled(true);
    }
    
    private String password;
    private String username;
    private boolean accountNonExpired;
    private boolean accountNonLocked;
    private boolean credentialsNonExpired;
    private boolean enabled;

    
    protected Properties properties;
    protected transient GeoserverUserDetailsService detailsService;
    
    protected Collection<GrantedAuthority> unmodifiableAuthorities; 



    public GeoserverUser(String username, GeoserverUserDetailsService detailsService) {
        this(username);
        if (detailsService == null) {
            throw new NullPointerException("detailsService must not be null");
        }
        this.detailsService = detailsService;
    }

    private GeoserverUser(String username) {
        this.username=username;
        
        accountNonExpired=accountNonLocked=credentialsNonExpired=enabled=true;
        unmodifiableAuthorities=null;
    }

    
    /* (non-Javadoc)
     * @see org.springframework.security.core.userdetails.UserDetails#getPassword()
     */
    public String getPassword() {
        return password;
    }


    public void setPassword(String password) {
        this.password = password;
    }


    /* (non-Javadoc)
     * @see org.springframework.security.core.userdetails.UserDetails#isAccountNonExpired()
     */
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }


    public void setAccountNonExpired(boolean accountNonExpired) {
        if (this.accountNonExpired!=accountNonExpired) {
            this.accountNonExpired = accountNonExpired;
            //calculateGrantedAuthorities();
        }
    }


    /* (non-Javadoc)
     * @see org.springframework.security.core.userdetails.UserDetails#isAccountNonLocked()
     */
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }


    /**
     * @param accountNonLocked
     */
    public void setAccountNonLocked(boolean accountNonLocked) {
        if (this.accountNonLocked!=accountNonLocked) {
            this.accountNonLocked = accountNonLocked;
            //calculateGrantedAuthorities();
        }
    }


    /* (non-Javadoc)
     * @see org.springframework.security.core.userdetails.UserDetails#isEnabled()
     */
    public boolean isEnabled() {        
        return enabled;
    }


    public void setEnabled(boolean enabled) {
        if (this.enabled!=enabled) {
            this.enabled = enabled;
            //calculateGrantedAuthorities();
        }
    }


    /* (non-Javadoc)
     * @see org.springframework.security.core.userdetails.UserDetails#getUsername()
     */
    public String getUsername() {
        return username;
    }

    /* (non-Javadoc)
     * @see org.springframework.security.core.userdetails.UserDetails#isCredentialsNonExpired()
     */
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }


    public void setCredentialsNonExpired(boolean credentialsNonExpired) {
        if (this.credentialsNonExpired!=credentialsNonExpired) {
            this.credentialsNonExpired = credentialsNonExpired;
            //calculateGrantedAuthorities();
        }
    }

    
    protected GeoserverUserDetailsService getDetailsService() {
        //for the DEFAULT_ADMIN user we have to look up on demand
        return detailsService != null ? 
            detailsService : GeoServerExtensions.bean(GeoserverUserDetailsService.class);
    }
    
    /* (non-Javadoc)
     * @see org.springframework.security.core.userdetails.UserDetails#getAuthorities()
     */
    public Collection<GrantedAuthority> getAuthorities() {
        if (unmodifiableAuthorities==null)
            try {
                List<GrantedAuthority> roles = new ArrayList<GrantedAuthority>();
                roles.addAll(getDetailsService().calculateGrantedAuthorities(this));
                unmodifiableAuthorities= Collections.unmodifiableCollection(roles);                        
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        return unmodifiableAuthorities;
    }

    
    
    /* (non-Javadoc)
     * @see org.springframework.security.core.CredentialsContainer#eraseCredentials()
     */
    public void eraseCredentials() {
        password = null;
    }


    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(GeoserverUser o) {
        if (o==null) return 1;
        return getUsername().compareTo(o.getUsername());
    }
    
    /**
     * Returns {@code true} if the supplied object is a {@code User} instance with the
     * same {@code username} value.
     * <p>
     * In other words, the objects are equal if they have the same username, representing the
     * same principal.
     */
    @Override
    public boolean equals(Object rhs) {
        if (rhs instanceof GeoserverUser) {
            return username.equals(((GeoserverUser) rhs).username);
        }
        return false;
    }

    /**
     * Returns the hashcode of the {@code username}.
     */
    @Override
    public int hashCode() {
        return username.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        //sb.append(super.toString()).append(": ");
        sb.append("Username: ").append(this.username).append("; ");
        sb.append("Password: [PROTECTED]; ");
        sb.append("Enabled: ").append(this.enabled).append("; ");
        sb.append("AccountNonExpired: ").append(this.accountNonExpired).append("; ");
        sb.append("credentialsNonExpired: ").append(this.credentialsNonExpired).append("; ");
        sb.append("AccountNonLocked: ").append(this.accountNonLocked).append("; ");

        sb.append(" [ ");
        if (unmodifiableAuthorities!=null)
            sb.append(StringUtils.collectionToCommaDelimitedString(unmodifiableAuthorities));
        sb.append(" ] ");
        
        return sb.toString();
    }

    /**
     * Generic mechanism to store 
     * additional information (user profile data)
     * 
     * examples: eMail Address, telephone number
     * 
     * To be filled by the backend store
     * 
     * @return 
     */
    public Properties getProperties() {
        if (properties==null)
            properties = new Properties();
        return properties;    
    }
    
    /**
     * force recalculation of authorities
     */
    public void resetGrantedAuthorities() {
        unmodifiableAuthorities=null;
    }

}
