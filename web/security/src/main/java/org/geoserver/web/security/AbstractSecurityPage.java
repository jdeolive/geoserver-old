/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.web.security;

import java.io.IOException;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.link.Link;
import org.geoserver.security.GeoserverGrantedAuthorityService;
import org.geoserver.security.GeoserverGrantedAuthorityStore;
import org.geoserver.security.GeoserverStoreFactory;
import org.geoserver.security.GeoserverUserDetailsService;
import org.geoserver.security.GeoserverUserGroupService;
import org.geoserver.security.GeoserverUserGroupStore;
import org.geoserver.security.impl.GeoserverUserDetailsServiceImpl;
import org.geoserver.web.GeoServerSecuredPage;

/**
 * Allows creation of a new user in users.properties
 */
public abstract class AbstractSecurityPage extends GeoServerSecuredPage {
    
    protected Page responsePage;
    
    public AbstractSecurityPage(Page responsePage) {
        this.responsePage=responsePage;        
    }

    public void setActualResponsePage (Class<? extends Page> aClass) {
        if (responsePage!=null)
            setResponsePage(responsePage);
        else
            setResponsePage(aClass);
    }
    
    public Link<Page> getCancelLink(final Class<? extends Page> aClass) {
        return new Link<Page>("cancel") {
            private static final long serialVersionUID = 1L;
            @Override
            public void onClick() {
                if (responsePage!=null) 
                    setResponsePage(responsePage);
                else    
                    setResponsePage(aClass);
            }            
        }; 
    }
    
    public GeoserverUserDetailsService getUserDetailsService() {
        return GeoserverUserDetailsServiceImpl.get();
    }
    
    public GeoserverUserGroupService getUserGroupService() {
        return getUserDetailsService().getUserGroupService();
    }
    
    public GeoserverGrantedAuthorityService getGrantedAuthorityService() {
        return getUserDetailsService().getGrantedAuthorityService();
    }

    public boolean hasGrantedAuthorityStore() {
        return GeoserverStoreFactory.Singleton.hasStoreFor(getGrantedAuthorityService());
    }
    
    public GeoserverGrantedAuthorityStore getGrantedAuthorityStore() throws IOException {
        return GeoserverStoreFactory.Singleton.getStoreFor(getGrantedAuthorityService());
    }

    public GeoserverUserGroupStore getUserGroupStore() throws IOException{
        return GeoserverStoreFactory.Singleton.getStoreFor(getUserGroupService());
    }
    
    public boolean hasUserGroupStore() {
        return GeoserverStoreFactory.Singleton.hasStoreFor(getUserGroupService());
    }

    

}
