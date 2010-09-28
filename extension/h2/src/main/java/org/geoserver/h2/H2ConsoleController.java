package org.geoserver.h2;

import java.util.Properties;

import org.geoserver.config.GeoServerDataDirectory;
import org.h2.server.web.WebServlet;
import org.springframework.web.servlet.mvc.ServletWrappingController;

public class H2ConsoleController extends ServletWrappingController {

    public H2ConsoleController(GeoServerDataDirectory data) {
        setServletClass(WebServlet.class);
        setServletName("h2");
        
        Properties props = new Properties();
        //props.put("-baseDir", data.root().getAbsolutePath());
        setInitParameters(props);
    }
}
