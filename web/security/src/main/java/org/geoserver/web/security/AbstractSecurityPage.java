/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.web.security;

import java.io.IOException;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.link.Link;
import org.geoserver.security.GeoServerSecurityManager;
import org.geoserver.security.GeoserverRoleService;
import org.geoserver.security.GeoserverRoleStore;
import org.geoserver.security.GeoserverUserGroupService;
import org.geoserver.security.GeoserverUserGroupStore;
import org.geoserver.web.GeoServerApplication;
import org.geoserver.web.GeoServerSecuredPage;

/**
 * Allows creation of a new user in users.properties
 */
public abstract class AbstractSecurityPage extends GeoServerSecuredPage {
    
    protected static String ServiceNameKey="serviceName"; 
    protected Page responsePage;
        
    public Link<Page> getCancelLink(final Page returnPage) {
        return new Link<Page>("cancel") {
            private static final long serialVersionUID = 1L;
            @Override
            public void onClick() {
                setResponsePage(returnPage);
            }            
        }; 
    }

    public GeoServerSecurityManager getSecurityManager() {
        return GeoServerApplication.get().getSecurityManager();
    }

    
    public GeoserverUserGroupService getUserGroupService(String name)  {
        try {
            return getSecurityManager().loadUserGroupService(name);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public GeoserverRoleService getRoleService(String name)  {
        try {
            return getSecurityManager().loadRoleService(name);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean hasRoleStore(String name) {
        return getRoleService(name).canCreateStore();
    }
    
    public GeoserverRoleStore getRoleStore(String name) throws IOException {
        return getRoleService(name).createStore();
    }
    
    public boolean hasUserGroupStore(String name) {
        return getUserGroupService(name).canCreateStore();
    }

    public GeoserverUserGroupStore getUserGroupStore(String name) throws IOException{
        return getUserGroupService(name).createStore();
    }

    

}
