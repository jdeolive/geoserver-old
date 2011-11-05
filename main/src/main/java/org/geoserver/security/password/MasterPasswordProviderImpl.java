/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.geoserver.security.password;

import java.util.logging.Logger;

import org.geoserver.platform.GeoServerExtensions;
import org.geotools.util.logging.Logging;

/**
 * Class for obtaining a geoserver master password
 * This class is critical for geoserver security
 * 
 * The standard implementation looks for
 * 
 * 1) A java system property called {@link #DEFAULT_PROPERTY_NAME}
 * NOT RECOMMENDED, a simple linux command like "ps -ef" shows
 * the password.
 * 
 * 2) An environment variable called {@link #DEFAULT_PROPERTY_NAME}
 * NOT RECOMMENDED, a simple linux command like "env" shows
 * the password.
 *
 * 3) A parameter named {@link #DEFAULT_PROPERTY_NAME} in the web.xml
 * It is necessary to protect your web.xml
 * 
 * BEST SOLUTION:
 * Inject a specific implementation of {@link MasterPasswordProvider} 
 * using Spring 
 * 
 * @author christian
 *
 */
public class MasterPasswordProviderImpl implements MasterPasswordProvider{
    
    public final static String DEFAULT_PROPERTY_NAME="MASTERPASSWORD";
    public final static String DEFAULT_MASTER_PASSWORD="geoserver";
    static protected Logger LOGGER = Logging.getLogger("org.geoserver.security");
    protected String beanName;    
    protected static MasterPasswordProvider Provider = null;
    
    /**
     * get the singleton
     * 
     * @return
     */
    public static MasterPasswordProvider get() {
        if (Provider!=null) return Provider;
        Provider = GeoServerExtensions.bean(MasterPasswordProvider.class);
        if (Provider==null) {
            Provider = new MasterPasswordProviderImpl();
        }
        LOGGER.info("Using "+Provider.getClass().getName()+ " for obtaining the geoserver master password" );
        return Provider;
    }

    public String getMasterPassword() {
        String pw =GeoServerExtensions.getProperty(DEFAULT_PROPERTY_NAME);
        if (pw==null)
            pw=DEFAULT_MASTER_PASSWORD;
        return pw;
    }
    
}
