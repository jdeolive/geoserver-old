/* Copyright (c) 2001 - 2008 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */


package org.geoserver.security.validation;

import org.geoserver.security.GeoserverUserGroupService;

/**
 * Exception used for validation errors  
 * concerning {@link GeoserverUserGroupService}
 * 
 * @author christian
 *
 */
public class UserGroupServiceException extends AbstractSecurityException {
    private static final long serialVersionUID = 1L;
    
    public UserGroupServiceException(String errorId, String message, Object[] args) {
        super(errorId, message, args);        
    }               
}
