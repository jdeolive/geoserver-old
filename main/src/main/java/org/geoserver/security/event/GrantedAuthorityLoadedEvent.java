/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.geoserver.security.event;

import java.util.EventObject;

import org.geoserver.security.GeoserverGrantedAuthorityService;

/**
 * 
 * Event fired after loading roles from  
 * the backend store into memory 
 * 
 * This event is intended for stateful services of
 * type {@link GeoserverGrantedAuthorityService}. If the
 * backend is changed externally and a reload occurs, listeners
 * should be notified. 
 * 
 * @author christian
 *
 */
public class GrantedAuthorityLoadedEvent extends EventObject {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public GrantedAuthorityLoadedEvent(GeoserverGrantedAuthorityService source) {
        super(source);
        
    }
    
    public GeoserverGrantedAuthorityService getService() {
        return (GeoserverGrantedAuthorityService) getSource();
    }

}
