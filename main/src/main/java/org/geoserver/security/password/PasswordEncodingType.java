/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.geoserver.security.password;

/**
 * Enumeration for password encoding type
 * {@link #PLAIN}       plain text
 * {@link #ENCRYPT}     symmetric encryption 
 * {@link #DIGEST}        password hashing (recommended)
 * 
 * @author christian
 *
 */
public enum PasswordEncodingType {
    PLAIN,ENCRYPT,DIGEST;

}
