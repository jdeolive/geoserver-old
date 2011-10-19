/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */


package org.geoserver.security.impl;

import org.geoserver.security.GeoserverUserDetailsService;

/**
 * Needed to test if subclassing works
 * 
 * @author christian
 *
 */
public class MemoryGeoserverUser extends GeoserverUser {

    private static final long serialVersionUID = 1L;

    public MemoryGeoserverUser(String username, GeoserverUserDetailsService userDetailsService) {
        super(username, userDetailsService);
    }
}
