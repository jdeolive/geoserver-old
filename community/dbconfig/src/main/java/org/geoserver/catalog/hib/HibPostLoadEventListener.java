package org.geoserver.catalog.hib;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.impl.ResourceInfoImpl;
import org.geoserver.catalog.impl.StoreInfoImpl;
import org.geoserver.catalog.impl.StyleInfoImpl;
import org.geoserver.config.GeoServer;
import org.geoserver.config.GeoServerInitializer;
import org.geoserver.platform.GeoServerExtensions;
import org.hibernate.event.PostLoadEvent;
import org.hibernate.event.PostLoadEventListener;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Event listener that sets the transient catalog reference on catalog objects.
 * 
 * @author Justin Deoliveira, OpenGeo
 *
 */
public class HibPostLoadEventListener implements PostLoadEventListener, ApplicationContextAware, 
    GeoServerInitializer {

    ApplicationContext appContext;
    Catalog catalog;
    boolean active = false;
    
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.appContext = applicationContext;
    }
    
    public void initialize(GeoServer geoServer) throws Exception {
        catalog = geoServer.getCatalog();
        active = true;
    }
    
    public void onPostLoad(PostLoadEvent event) {
        if (!active) return;
        
        Object entity = event.getEntity();
        if (entity instanceof StoreInfoImpl) {
            ((StoreInfoImpl)entity).setCatalog(catalog);
        }
        else if (entity instanceof ResourceInfoImpl) {
            ((ResourceInfoImpl)entity).setCatalog(catalog);
        }
        else if (entity instanceof StyleInfoImpl) {
            ((StyleInfoImpl)entity).setCatalog(catalog);
        }
    }

}
