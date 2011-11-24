/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.geoserver.security.password;


/**
 * Password Encoder for password in configuration files
 * 
 * @author christian
 *
 */
public  class GeoserverConfigPBEPasswordEncoder extends GeoserverPBEPasswordEncoder  
    implements GeoserverConfigPasswordEncoder {

    public final static String BeanName = "configPasswordEncoder";
    public final static String StrongBeanName = "strongConfigPasswordEncoder";  
    
            
    public String getKeyAliasInKeyStore() {
        return KeyStoreProvider.CONFIGPASSWORDKEY;
    }


}
