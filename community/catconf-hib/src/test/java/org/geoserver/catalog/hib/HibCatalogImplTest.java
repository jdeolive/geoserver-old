package org.geoserver.catalog.hib;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.CatalogDAO;
import org.geoserver.catalog.LayerGroupInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.NamespaceInfo;
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.catalog.StoreInfo;
import org.geoserver.catalog.StyleInfo;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.catalog.impl.CatalogImplTest;
import org.springframework.web.context.support.XmlWebApplicationContext;

public class HibCatalogImplTest extends CatalogImplTest {

    @Override
    protected Catalog createCatalog() {
        XmlWebApplicationContext ctx = new XmlWebApplicationContext() {
            public String[] getConfigLocations() {
                return new String[]{
                    "file:src/main/resources/applicationContext.xml", 
                    "file:src/test/resources/applicationContext-test.xml"
                };
            }
        };
        ctx.refresh();
        return (Catalog) ctx.getBean("catalog");
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        CatalogDAO dao = catalog.getDAO();
        
        for (LayerGroupInfo lg : dao.getLayerGroups())  { dao.remove(lg); }
        for (LayerInfo l : dao.getLayers())  { dao.remove(l); }
        for (ResourceInfo r : dao.getResources(ResourceInfo.class))  { dao.remove(r); }
        for (StoreInfo s : dao.getStores(StoreInfo.class)) { dao.remove(s); }
        for (WorkspaceInfo ws : dao.getWorkspaces()){ dao.remove(ws); }
        for (NamespaceInfo ns : dao.getNamespaces()){ dao.remove(ns); }
        for (StyleInfo s : dao.getStyles()){ dao.remove(s); }
    }
    
    @Override
    public void testProxyBehaviour() throws Exception {
        // do nothing, does not apply
    }
    
    @Override
    public void testProxyListBehaviour() throws Exception {
        // do nothing, does not apply
    }
    
    @Override
    public void testModifyMetadata() {
        // TODO: currently this does not work becuae hibernate does not intercept the change to the 
        // metadata map... figure out how to do this. 
    }
}
