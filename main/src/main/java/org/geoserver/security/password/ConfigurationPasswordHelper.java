/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */


package org.geoserver.security.password;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.geoserver.catalog.StoreInfo;
import org.geotools.data.DataAccessFactory.Param;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DataStoreFinder;
import org.geotools.util.logging.Logging;

/**
 * Helper class for encryption of passwords in
 * {@link StoreInfo#getConnectionParameters()}
 * 
 * @author christian
 *
 */
public class ConfigurationPasswordHelper {
    
        
    static protected Logger LOGGER = Logging.getLogger("org.geoserver.security");
    static protected Map<Class<? extends StoreInfo>, Set<String>> Cache = 
            new HashMap<Class<? extends StoreInfo>, Set<String>>();
    
       
    /**
     * Calculates the set of password fields
     * 
     * @param info
     * @return
     */
    static public Set<String> getEncryptionFields(StoreInfo info) {
        Set<String> toEncrypt = Cache.get(info.getClass());
        if (toEncrypt!=null)
            return toEncrypt;
        synchronized (Cache) {
            toEncrypt = Cache.get(info.getClass());
            if (toEncrypt!=null)
                return toEncrypt;
            toEncrypt = Collections.emptySet();
            if (info != null && info.getConnectionParameters() != null) {
                toEncrypt = new HashSet<String>(3);
                Iterator<DataStoreFactorySpi> allDataStores = DataStoreFinder.getAllDataStores();
                while (allDataStores.hasNext()) {
                    DataStoreFactorySpi spi = allDataStores.next();
                    if (spi.canProcess(info.getConnectionParameters())) {
                        Param[] parametersInfo = spi.getParametersInfo();
                        for (int i = 0; i < parametersInfo.length; i++) {
                            if (parametersInfo[i].isPassword()) {
                                toEncrypt.add(parametersInfo[i].getName());
                            }
                        }
                    }
                }
            }
            Cache.put(info.getClass(), toEncrypt);
        }
        return toEncrypt;
    }
           
}