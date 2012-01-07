/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.web.security;

import java.io.IOException;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.link.Link;
import org.geoserver.security.GeoServerSecurityManager;
import org.geoserver.security.GeoServerRoleService;
import org.geoserver.security.GeoServerRoleStore;
import org.geoserver.security.GeoServerUserGroupService;
import org.geoserver.security.GeoServerUserGroupStore;
import org.geoserver.web.GeoServerApplication;
import org.geoserver.web.GeoServerSecuredPage;

/**
 * Allows creation of a new user in users.properties
 */
public abstract class AbstractSecurityPage extends GeoServerSecuredPage {
    
    public static String ServiceNameKey="serviceName";
    public static String TabbedPanelId="tabbedPanel";
    /**
     * Indicates if model data has changed
     */
    boolean dirty = false;
    
        
    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public Link<Page> getCancelLink(final AbstractSecurityPage returnPage) {
        return new Link<Page>("cancel") {
            private static final long serialVersionUID = 1L;
            @Override
            public void onClick() {
                returnPage.setDirty(false); 
                setResponsePage(returnPage);
            }            
        }; 
    }

    public GeoServerSecurityManager getSecurityManager() {
        return GeoServerApplication.get().getSecurityManager();
    }

    
    public GeoServerUserGroupService getUserGroupService(String name)  {
        try {
            return getSecurityManager().loadUserGroupService(name);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public GeoServerRoleService getRoleService(String name)  {
        try {
            return getSecurityManager().loadRoleService(name);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean hasRoleStore(String name) {
        return getRoleService(name).canCreateStore();
    }
    
    public GeoServerRoleStore getRoleStore(String name) throws IOException {
        return getRoleService(name).createStore();
    }
    
    public boolean hasUserGroupStore(String name) {
        return getUserGroupService(name).canCreateStore();
    }

    public GeoServerUserGroupStore getUserGroupStore(String name) throws IOException{
        return getUserGroupService(name).createStore();
    }

    

}
