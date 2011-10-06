/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.geoserver.security.file;

import java.io.IOException;

import org.geoserver.security.GeoserverGrantedAuthorityService;
import org.geoserver.security.event.GrantedAuthorityLoadedEvent;
import org.geoserver.security.event.GrantedAuthorityLoadedListener;

/**
 * Watches a file storing role information
 * and triggers a load on external file change
 * 
 * @author christian
 *
 */
public class GrantedAuthorityFileWatcher extends FileWatcher implements GrantedAuthorityLoadedListener{
    
    
    public GrantedAuthorityFileWatcher(String fileName,GeoserverGrantedAuthorityService service) {
        super(fileName);
        this.service=service;
        checkAndConfigure();
    }

    public GrantedAuthorityFileWatcher(String fileName,GeoserverGrantedAuthorityService service,long lastModified) {
        super(fileName);
        this.service=service;
        this.lastModified=lastModified;
        checkAndConfigure();
    }

    
    protected GeoserverGrantedAuthorityService service;
    
    public synchronized GeoserverGrantedAuthorityService getService() {
        return service;
    }

    public synchronized void setService(GeoserverGrantedAuthorityService service) {
        this.service = service;
    }

    /**
     * Triggers a load on {@link #service}
     *  
     *  (non-Javadoc)
     * @see org.geoserver.security.file.FileWatcher#doOnChange()
     */
    @Override
    protected void doOnChange() {        
        GeoserverGrantedAuthorityService theService = getService();
        try {
            if (theService!=null)
                theService.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        StringBuffer buff = new StringBuffer();
        String serviceName = service==null ? "UNKNOWN" : service.getName();
         
        buff.append("FileWatcher for ").append(serviceName);
        buff.append(", ").append(getFileInfo());
        return buff.toString();
    }

    /**
     * Another method to avoid reloads if this object
     * is registered 
     * @see GeoserverGrantedAuthorityService#registerGrantedAuthorityLoadedListener(GrantedAuthorityLoadedListener)
     * 
     */
    @Override
    public void grantedAuthoritiesChanged(GrantedAuthorityLoadedEvent event) {
        // avoid reloads
        setLastModified(file.lastModified());
        LOGGER.info("Adjusted last modified for file: " +filename);        
    }
}
