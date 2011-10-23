/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.geoserver.security.event;

import java.util.EventObject;

import org.geoserver.security.GeoserverRoleService;

/**
 * 
 * Event fired after loading roles from  
 * the backend store into memory 
 * 
 * This event is intended for stateful services of
 * type {@link GeoserverRoleService}. If the
 * backend is changed externally and a reload occurs, listeners
 * should be notified. 
 * 
 * @author christian
 *
 */
public class RoleLoadedEvent extends EventObject {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public RoleLoadedEvent(GeoserverRoleService source) {
        super(source);
        
    }
    
    public GeoserverRoleService getService() {
        return (GeoserverRoleService) getSource();
    }

}
