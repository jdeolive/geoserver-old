package org.geoserver.hibernate;

import java.io.File;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.CatalogDAO;
import org.geoserver.catalog.impl.CatalogImpl;
import org.geoserver.config.GeoServer;
import org.geoserver.config.GeoServerDAO;
import org.geoserver.config.GeoServerLoader;
import org.geoserver.config.impl.GeoServerImpl;
import org.geoserver.config.util.XStreamPersister;
import org.geoserver.platform.GeoServerResourceLoader;

public class HibGeoServerLoader extends GeoServerLoader {

    CatalogDAO catalogDAO;
    GeoServerDAO geoServerDAO;
    
    public HibGeoServerLoader(GeoServerResourceLoader resourceLoader) {
        super(resourceLoader);
    }

    public void setCatalogDAO(CatalogDAO catalogDAO) {
        this.catalogDAO = catalogDAO;
    }
    
    public void setGeoServerDAO(GeoServerDAO geoServerDAO) {
        this.geoServerDAO = geoServerDAO;
    }
    
    @Override
    protected void loadCatalog(Catalog catalog, XStreamPersister xp) throws Exception {
        ((CatalogImpl)catalog).setDAO(catalogDAO);
        
        //if this is the first time loading up with hibernate configuration, migrate from old
        // file based structure
        File marker = resourceLoader.find("hibernate.marker");
        if (marker == null) {
            readCatalog(catalog, xp);
        }
        
    }

    @Override
    protected void loadGeoServer(GeoServer geoServer, XStreamPersister xp) throws Exception {
        ((GeoServerImpl)geoServer).setDAO(geoServerDAO);
        
        //if this is the first time loading up with hibernate configuration, migrate from old
        // file based structure
        File marker = resourceLoader.find("hibernate.marker");
        if (marker == null) {
            readConfiguration(geoServer, xp);
            
            resourceLoader.createFile("hibernate.marker");
        }
        
    }

}
