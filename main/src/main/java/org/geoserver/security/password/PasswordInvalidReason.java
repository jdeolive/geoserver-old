/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.geoserver.security.password;

/**
 * Enumeration of reasons why a password is invalid
 * 
 * @author christian
 *
 */
public enum PasswordInvalidReason  {
    PW_IS_NULL,
    PW_NO_DIGIT,
    PW_NO_UPPERCASE,
    PW_NO_LOWERCASE,
    PW_MIN_LENGTH,
    PW_MAX_LENGTH,
    PW_RESERVED_PREFIX;
}
