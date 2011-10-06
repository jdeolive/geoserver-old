/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.geoserver.security;

import java.util.Comparator;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

/**
 * Provides some {@link Comparator} implementations for common use
 * 
 * @author christian
 *
 */
public class Comparators {

    public final static  Comparator<GrantedAuthority> GrantedAuthorityComparator = new Comparator<GrantedAuthority>() {
        public int compare(GrantedAuthority a1, GrantedAuthority a2) {
            if (a1==null && a2 == null) return 0;
            if (a1==null) return -1;
            if (a2==null) return 1;
            return a1.getAuthority().compareTo(a2.getAuthority());
        }
    };
 
        
    public final static Comparator<User> UserComparator = new Comparator<User> (){
        public int compare(User u1, User u2) {
            if (u1==null && u2 == null) return 0;
            if (u1==null) return -1;
            if (u2==null) return 1;
            return u1.getUsername().compareTo(u2.getUsername());
        }
    };
    
}
