/* Copyright (c) 2001 - 2008 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */


package org.geoserver.security.validation;

import java.text.MessageFormat;

import org.geoserver.security.GeoserverUserGroupService;

/**
 *  
 * Validation errors for {@link GeoserverUserGroupService} 
 * 
 * 
 * @author christian
 *
 */
public class UserGroupServiceValidationErrors  extends AbstractSecurityValidationErrors{

    public final static String UG_ERR_01 = "UG_ERR_01";
    public final static String UG_ERR_02 = "UG_ERR_02";
    public final static String UG_ERR_03 = "UG_ERR_03";
    public final static String UG_ERR_04 = "UG_ERR_04";
    public final static String UG_ERR_05 = "UG_ERR_05";
    public final static String UG_ERR_06 = "UG_ERR_06";
    
    
    
    
    @Override
    public  String formatErrorMsg(String id,Object ... args) {
    
    if (UG_ERR_01.equals(id))
        return MessageFormat.format("User name is mandatory",args);
    if (UG_ERR_02.equals(id))
        return MessageFormat.format("Group name is mandatory",args);

    if (UG_ERR_03.equals(id))
        return MessageFormat.format("User {0} does not exist",args);
    if (UG_ERR_04.equals(id))
        return MessageFormat.format("Group {0} does not exist",args);
    
    if (UG_ERR_05.equals(id))
        return MessageFormat.format("User {0} already exists",args);
    if (UG_ERR_06.equals(id))
        return MessageFormat.format("Group {0} already exists",args);


    return super.formatErrorMsg(id, args);
    }
}
