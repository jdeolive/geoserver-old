/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.geoserver.security.password;

import java.io.IOException;

import org.geoserver.security.GeoserverUserGroupService;
import org.geoserver.security.impl.GeoserverUser;

/**
 * Password Encoder using symmetric encryption for
 * {@link GeoserverUser} objects stored in
 * {@link GeoserverUserGroupService} objects
 * 
 * 
 * @author christian
 *
 */
public  class GeoserverUserPBEPasswordEncoder extends GeoserverPBEPasswordEncoder implements GeoserverUserPasswordEncoder {

    protected String keyAliasInKeyStore;
    public final static String PrototypeName = "pbePasswordEncoder";
    public final static String StrongPrototypeName = "strongPbePasswordEncoder";  

    
    public  String getKeyAliasInKeyStore() {
        return keyAliasInKeyStore;
    }

    @Override
    public void setBeanName(String name) {
        // do nothing, not needed
    }

    @Override
    public void initializeFor(GeoserverUserGroupService service) throws IOException {
        if (KeyStoreProvider.get().hasUserGRoupKey(service.getName())==false) {
            throw new IOException("No key alias: " +
                    KeyStoreProvider.get().aliasForGroupService(service.getName())+
                    "\nin key store: " + KeyStoreProvider.get().getKeyStoreProvderFile().getAbsolutePath());
        }
        
        keyAliasInKeyStore=
                KeyStoreProvider.get().aliasForGroupService(service.getName());

    }

    @Override
    public String getNameReference() {
        return PrototypeName;
    }

}
