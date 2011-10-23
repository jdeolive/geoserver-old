/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.geoserver.security.file;

import java.io.IOException;

import org.geoserver.security.GeoserverRoleService;
import org.geoserver.security.event.RoleLoadedEvent;
import org.geoserver.security.event.RoleLoadedListener;

/**
 * Watches a file storing role information
 * and triggers a load on external file change
 * 
 * @author christian
 *
 */
public class RoleFileWatcher extends FileWatcher implements RoleLoadedListener{
    
    
    public RoleFileWatcher(String fileName,GeoserverRoleService service) {
        super(fileName);
        this.service=service;
        checkAndConfigure();
    }

    public RoleFileWatcher(String fileName,GeoserverRoleService service,long lastModified) {
        super(fileName);
        this.service=service;
        this.lastModified=lastModified;
        checkAndConfigure();
    }

    
    protected GeoserverRoleService service;
    
    public synchronized GeoserverRoleService getService() {
        return service;
    }

    public synchronized void setService(GeoserverRoleService service) {
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
        GeoserverRoleService theService = getService();
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
     * @see GeoserverRoleService#registerRoleLoadedListener(RoleLoadedListener)
     * 
     */
    @Override
    public void rolesChanged(RoleLoadedEvent event) {
        // avoid reloads
        setLastModified(file.lastModified());
        LOGGER.info("Adjusted last modified for file: " +filename);        
    }
}
