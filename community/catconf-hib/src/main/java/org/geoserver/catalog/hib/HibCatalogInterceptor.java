package org.geoserver.catalog.hib;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.CatalogInfo;
import org.geoserver.catalog.NamespaceInfo;
import org.geoserver.catalog.StoreInfo;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.platform.GeoServerExtensions;
import org.geotools.util.Utilities;
import org.geotools.util.logging.Logging;
import org.hibernate.EmptyInterceptor;
import org.hibernate.Transaction;
import org.hibernate.type.Type;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Hibernate interceptor which forwards hibernate events to the catalog.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 * 
 */
public class HibCatalogInterceptor extends EmptyInterceptor implements ApplicationContextAware {

    private final static Logger LOGGER = Logging.getLogger(HibCatalogInterceptor.class);

    /**
     * spring app context
     */
    ApplicationContext appContext;
    
    /**
     * catalog
     */
    Catalog catalog;

    public HibCatalogInterceptor() {
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.appContext = applicationContext;
    }
    
    protected Catalog catalog() {
        if (catalog == null) {
            synchronized (this) {
                if (catalog == null) {
                    catalog = GeoServerExtensions.bean(Catalog.class, appContext);
                }
            }
        }
        return catalog;
    }

    public void afterTransactionCompletion(Transaction tx) {
        if (!tx.wasRolledBack()) {
            //TODO: fire post modified... maybe via thread local?
        } 
    }

    @Override
    public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState,
            Object[] previousState, String[] propertyNames, Type[] types) {
        if (!(entity instanceof CatalogInfo)) {
            return false;
        }
        
        List<String> propertyNamesChanged = new ArrayList<String>();
        List<Object> oldValues = new ArrayList<Object>();
        List<Object> newValues = new ArrayList<Object>();

        if (previousState != null) {
            for (int i = 0; i < propertyNames.length; i++) {
                Object oldValue = previousState[i];
                Object newValue = currentState[i];

                if (!Utilities.equals(oldValue, newValue)) {
                    propertyNamesChanged.add(propertyNames[i]);
                    oldValues.add(oldValue);
                    newValues.add(newValue);
                }
            }
        } else {
            for (int i = 0; i < propertyNames.length; i++) {
                propertyNamesChanged.add(propertyNames[i]);
            }
        }
        if (!filterEvent((CatalogInfo)entity, propertyNamesChanged, oldValues, newValues)) {
            catalog().fireModified((CatalogInfo)entity, propertyNamesChanged, oldValues, newValues);    
        }
        
        return false;
    }
    
    /*
     * method to filter out situations in which we don;t want to throw an event
     */
    protected boolean filterEvent(
        CatalogInfo entity, List<String> propertyNamesChanged, List oldValues, List newValues) {
        
        //handle default namespace/workspace changing or default datastore
        if ((entity instanceof WorkspaceInfo || entity instanceof NamespaceInfo || entity instanceof StoreInfo) && 
                propertyNamesChanged.size() == 1 && propertyNamesChanged.contains("default")) {
            return true;
        }
        
        return false;
    }
}
