package org.geoserver.hibernate;

import java.io.File;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.CatalogDAO;
import org.geoserver.catalog.LayerGroupInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.NamespaceInfo;
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.catalog.StoreInfo;
import org.geoserver.catalog.StyleInfo;
import org.geoserver.catalog.WorkspaceInfo;
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
        //clearCatalog(catalogDAO); -- JD: only enabled when testing
        
        //if this is the first time loading up with hibernate configuration, migrate from old
        // file based structure
        File marker = resourceLoader.find("hibernate.marker");
        if (marker == null) {
            readCatalog(catalog, xp);
        }
        
    }

    void clearCatalog(CatalogDAO dao) {
        for (LayerGroupInfo lg : dao.getLayerGroups())  { dao.remove(lg); }
        for (LayerInfo l : dao.getLayers())  { dao.remove(l); }
        for (ResourceInfo r : dao.getResources(ResourceInfo.class))  { dao.remove(r); }
        for (StoreInfo s : dao.getStores(StoreInfo.class)) { dao.remove(s); }
        for (WorkspaceInfo ws : dao.getWorkspaces()){ dao.remove(ws); }
        for (NamespaceInfo ns : dao.getNamespaces()){ dao.remove(ns); }
        for (StyleInfo s : dao.getStyles()){ dao.remove(s); }

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
    
    @Override
    public void reload() throws Exception {
        //for testing, remove hibernate.marker file
        File f = resourceLoader.find("hibernate.marker");
        if (f != null) {
            f.delete();
        }
        
        super.reload();
    }

}
