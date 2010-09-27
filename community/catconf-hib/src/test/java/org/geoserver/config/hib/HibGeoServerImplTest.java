package org.geoserver.config.hib;

import org.geoserver.config.GeoServerDAO;
import org.geoserver.config.GeoServerImplTest;
import org.geoserver.config.ServiceInfo;
import org.geoserver.config.impl.GeoServerImpl;
import org.h2.tools.DeleteDbFiles;
import org.springframework.web.context.support.XmlWebApplicationContext;

public class HibGeoServerImplTest extends GeoServerImplTest {

    @Override
    protected GeoServerImpl createGeoServer() {
        XmlWebApplicationContext ctx = new XmlWebApplicationContext() {
            public String[] getConfigLocations() {
                return new String[]{
                    "file:src/main/resources/applicationContext.xml", 
                    "file:src/test/resources/applicationContext-test.xml"
                };
            }
        };
        ctx.refresh();
        return (GeoServerImpl) ctx.getBean("geoServer");
    }
    
    @Override
    protected void setUp() throws Exception {
        DeleteDbFiles.execute(".", "geoserver", true);
        super.setUp();
        
        GeoServerDAO dao = geoServer.getDAO();
        for (ServiceInfo s : dao.getServices()) { dao.remove(s); }
    }
}
