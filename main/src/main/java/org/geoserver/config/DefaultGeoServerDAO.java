package org.geoserver.config;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.geoserver.catalog.impl.ModificationProxy;

public class DefaultGeoServerDAO implements GeoServerDAO {

    GeoServerInfo global;
    LoggingInfo logging;
    List<ServiceInfo> services = new ArrayList<ServiceInfo>();
    
    GeoServer geoServer;
    
    public DefaultGeoServerDAO(GeoServer geoServer) {
        this.geoServer = geoServer;
        this.global = geoServer.getFactory().createGlobal();
        this.logging = geoServer.getFactory().createLogging();
    }
    
    public GeoServer getGeoServer() {
        return geoServer;
    }
    
    public void setGeoServer(GeoServer geoServer) {
        this.geoServer = geoServer;
    }
    
    public GeoServerInfo getGlobal() {
        if ( global == null ) {
            return null;
        }
        
        return ModificationProxy.create( global, GeoServerInfo.class );
    }
    
    public void setGlobal(GeoServerInfo global) {
        this.global = global;
    }
    
    public void save(GeoServerInfo global) {
        ModificationProxy proxy = 
            (ModificationProxy) Proxy.getInvocationHandler( global );
        
        List propertyNames = proxy.getPropertyNames();
        List oldValues = proxy.getOldValues();
        List newValues = proxy.getNewValues();
        
        for ( ConfigurationListener l : geoServer.getListeners() ) {
            try {
                l.handleGlobalChange( global, propertyNames, oldValues, newValues);
            }
            catch( Exception e ) {
                //log this
            }
        }
        
        proxy.commit();
    }
    
    public LoggingInfo getLogging() {
        if ( logging == null ) {
            return null;
        }
        
        return ModificationProxy.create( logging, LoggingInfo.class );
    }
    
    public void setLogging(LoggingInfo logging) {
        this.logging = logging;
    }

    public void save(LoggingInfo logging) {
        ModificationProxy proxy = 
            (ModificationProxy) Proxy.getInvocationHandler( logging );
        
        List propertyNames = proxy.getPropertyNames();
        List oldValues = proxy.getOldValues();
        List newValues = proxy.getNewValues();
        
        for ( ConfigurationListener l : geoServer.getListeners() ) {
            try {
                l.handleLoggingChange( logging, propertyNames, oldValues, newValues);
            }
            catch( Exception e ) {
                //log this
            }
        }
        
        proxy.commit();
    }
    
    public void add(ServiceInfo service) {
        //may be adding a proxy, need to unwrap
        service = unwrap(service);
        service.setGeoServer(geoServer);
        services.add( service );
    }
    
    public void save(ServiceInfo service) {
        ModificationProxy proxy = 
            (ModificationProxy) Proxy.getInvocationHandler( service );
        
        List propertyNames = proxy.getPropertyNames();
        List oldValues = proxy.getOldValues();
        List newValues = proxy.getNewValues();
        
        for ( ConfigurationListener l : geoServer.getListeners() ) {
            try {
                l.handleServiceChange( service, propertyNames, oldValues, newValues);
            }
            catch( Exception e ) {
                //log this
            }
        }
        
        proxy.commit();
    }
    
    public void remove(ServiceInfo service) {
        services.remove( service );
    }

    public <T extends ServiceInfo> T getService(Class<T> clazz) {
        for ( ServiceInfo si : services ) {
            if( clazz.isAssignableFrom( si.getClass() ) ) {
                return ModificationProxy.create( (T) si, clazz );
            }
         }
         
         return null;
    }
    
    public <T extends ServiceInfo> T getService(String id, Class<T> clazz) {
        for ( ServiceInfo si : services ) {
            if( id.equals( si.getId() ) ) {
                return ModificationProxy.create( (T) si, clazz );
            }
         }
         
         return null;
    }
    
    public <T extends ServiceInfo> T getServiceByName(String name, Class<T> clazz) {
        for ( ServiceInfo si : services ) {
            if( name.equals( si.getName() ) ) {
                return ModificationProxy.create( (T) si, clazz );
            }
         }
         
         return null;
    }
    
    public Collection<? extends ServiceInfo> getServices() {
        return ModificationProxy.createList( services, ServiceInfo.class );
    }
    
    public void dispose() {
        if ( global != null ) global.dispose();
        if ( services != null ) services.clear();
    }
    
    public static <T> T unwrap(T obj) {
        return ModificationProxy.unwrap(obj);
    }
}
