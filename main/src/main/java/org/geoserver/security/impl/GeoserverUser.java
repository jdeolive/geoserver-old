/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.security.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.security.GeoserverUserGroupService;
import org.geoserver.security.password.GeoserverPasswordEncoder;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;

/**
 * Geoserver implemenation of  {@link UserDetails} 
 * 
 * @author christian
 * 
 */
public class GeoserverUser  implements UserDetails, CredentialsContainer,Comparable<GeoserverUser>{
    


    private static final long serialVersionUID = 1L;
    
    
    public static final String AdminName="admin";
    public static final String AdminPasword="geoserver";
    public static final boolean AdminEnabled=true;
    /**
     * Create the geoserver default administrator
     * 
     * @return
     */
    public static GeoserverUser createDefaultAdmin(GeoserverUserGroupService service) {
        GeoserverUser admin = new GeoserverUser(AdminName);
        admin.setPasswordEncoderName(service.getPasswordEncoderName());
        admin.setPassword(AdminPasword);
        admin.setEnabled(AdminEnabled);
        return admin;
    }
    
    private String password;
    private String username;
    private boolean accountNonExpired;
    private boolean accountNonLocked;
    private boolean credentialsNonExpired;
    private boolean enabled;
    private String passwordEncoderName;
    

    
    public String getPasswordEncoderName() {
        return passwordEncoderName;
    }

    public void setPasswordEncoderName(String passwordEncoderName) {
        this.passwordEncoderName = passwordEncoderName;
    }

    protected Properties properties;
    

    protected Collection<GrantedAuthority> unmodifiableAuthorities; 


    public GeoserverUser(String username) {
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


    public void setPassword(String passwd) {
        // no encoding at all
        
        if (passwd==null) {
            this.password=null;
            return; 
        }
        
        
        if (getPasswordEncoderName()==null) {
            this.password = passwd;
            return;
        }
        GeoserverPasswordEncoder encoder = (GeoserverPasswordEncoder) 
                GeoServerExtensions.bean(getPasswordEncoderName());

        // password already encoded
        if (encoder.isResponsibleForEncoding(passwd)) {
            this.password = passwd;
            return;
        } 
        
        this.password = encoder.encodePassword(passwd, null);
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

    
    
    /* (non-Javadoc)
     * @see org.springframework.security.core.userdetails.UserDetails#getAuthorities()
     */
    public Collection<GrantedAuthority> getAuthorities() {
        if (unmodifiableAuthorities==null)
            unmodifiableAuthorities=Collections.unmodifiableSet(new TreeSet<GrantedAuthority>());
        return unmodifiableAuthorities;
    }
    
    /**
     * Set the roles of the user. 
     * 
     * @param roles
     */
    public void setAuthorities(Set<? extends GrantedAuthority> roles) {
        
        unmodifiableAuthorities=Collections.unmodifiableSet(roles);
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
    

}
