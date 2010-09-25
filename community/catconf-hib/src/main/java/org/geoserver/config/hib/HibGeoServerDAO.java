package org.geoserver.config.hib;

import java.util.Collection;

import javax.persistence.Query;

import org.geoserver.config.GeoServer;
import org.geoserver.config.GeoServerDAO;
import org.geoserver.config.GeoServerInfo;
import org.geoserver.config.LoggingInfo;
import org.geoserver.config.ServiceInfo;
import org.geoserver.hibernate.AbstractHibDAO;
import org.geoserver.ows.util.OwsUtils;

public class HibGeoServerDAO extends AbstractHibDAO implements GeoServerDAO {

    GeoServer geoServer;
    
    public GeoServer getGeoServer() {
        return geoServer;
    }
    
    public void setGeoServer(GeoServer geoServer) {
        this.geoServer = geoServer;
    }
    
    //
    // global
    //
    public GeoServerInfo getGlobal() {
        return (GeoServerInfo) first( query("from ", GeoServerInfo.class) );
    }
    
    public void setGlobal(GeoServerInfo global) {
        GeoServerInfo existing = getGlobal();
        if (existing != null) {
            OwsUtils.copy(global, existing, GeoServerInfo.class);
            save(existing);
        }
        else {
            persist(global);
        }
    }

    
    public void save(GeoServerInfo geoServer) {
        merge(geoServer);
    }

    //
    // logging
    //
    public void setLogging(LoggingInfo logging) {
        LoggingInfo existing = getLogging();
        if (existing != null) {
            OwsUtils.copy(logging, existing, LoggingInfo.class);
            save(existing);
        }
        else {
            persist(logging);
        }
    }

    public LoggingInfo getLogging() {
        return (LoggingInfo) first( query("from ", LoggingInfo.class) );
    }

    public void save(LoggingInfo logging) {
        merge(logging);
    }

    //
    // services
    //
    public void add(ServiceInfo service) {
        persist(service);
    }
    
    public void save(ServiceInfo service) {
        merge(service);
    }

    public void remove(ServiceInfo service) {
        delete(service);
    }
    
    public <T extends ServiceInfo> T getService(Class<T> clazz) {
        return (T) first( query("from ", clazz) );
    }

    public <T extends ServiceInfo> T getService(String id, Class<T> clazz) {
        return (T) first( query("from ", clazz, " where id = ", param(id)) );
    }

    public <T extends ServiceInfo> T getServiceByName(String name, Class<T> clazz) {
        Query query = query("from ", clazz, " where name = ", param(name));
        return (T) first(query);
    }

    public Collection<? extends ServiceInfo> getServices() {
        return list(ServiceInfo.class);
    }
    
    public void dispose() {
    }

}
