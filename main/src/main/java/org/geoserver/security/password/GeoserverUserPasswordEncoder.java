/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.geoserver.security.password;

import java.io.IOException;

import org.geoserver.security.GeoserverUserGroupService;
import org.jasypt.spring.security3.PasswordEncoder;
import org.springframework.beans.factory.BeanNameAware;

/**
 * {@link PasswordEncoder} implementations useable for 
 * {@link GeoserverUserGroupService} objects
 * 
 * @author christian
 *
 */
public interface GeoserverUserPasswordEncoder extends GeoserverPasswordEncoder, BeanNameAware {
    
    /**
     * Initialize this encoder for a {@link GeoserverUserGroupService} object.
     * 
     * @param service
     * @throws IOExcpetion
     */
    public void initializeFor(GeoserverUserGroupService service) throws IOException;
    
    /**
     * This method returns the spring bean name of this encoder
     * 
     * For singletons, the actual bean name must be returned
     *  
     * For prototype bean, the name of the prototype must be returned, not
     * the auto generated bean name 
     *
     * @return
     */
    public String getNameReference();
    

}
