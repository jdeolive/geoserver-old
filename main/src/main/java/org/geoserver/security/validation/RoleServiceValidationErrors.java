/* Copyright (c) 2001 - 2008 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */


package org.geoserver.security.validation;

import java.text.MessageFormat;

import org.geoserver.security.GeoserverRoleService;

/**
 *  
 * Validation errors for {@link GeoserverRoleService} 
 * 
 * 
 * @author christian
 *
 */
public class RoleServiceValidationErrors  extends AbstractSecurityValidationErrors{

    public final static String ROLE_ERR_01 = "ROLE_ERR_01";
    public final static String ROLE_ERR_02 = "ROLE_ERR_02";
    public final static String ROLE_ERR_03 = "ROLE_ERR_03";
    public final static String ROLE_ERR_04 = "ROLE_ERR_04";
    public final static String ROLE_ERR_05 = "ROLE_ERR_05";
    public final static String ROLE_ERR_06 = "ROLE_ERR_06";
    public final static String ROLE_ERR_07 = "ROLE_ERR_07";
    
    
        
    @Override
    public  String formatErrorMsg(String id,Object ... args) {
    
    if (ROLE_ERR_01.equals(id))
        return MessageFormat.format("Role name is mandatory",args);
    if (ROLE_ERR_02.equals(id))
        return MessageFormat.format("Role {0} does not exist",args);    
    if (ROLE_ERR_03.equals(id))
        return MessageFormat.format("Role {0} already exists",args);

    if (ROLE_ERR_04.equals(id))
        return MessageFormat.format("User name is mandatory",args);
    if (ROLE_ERR_05.equals(id))
        return MessageFormat.format("Group name is mandatory",args);

    if (ROLE_ERR_06.equals(id))
        return MessageFormat.format("User name {0} is not found",args);
    if (ROLE_ERR_07.equals(id))
        return MessageFormat.format("Group name {0} is not found",args);


    return super.formatErrorMsg(id, args);
    }
}
