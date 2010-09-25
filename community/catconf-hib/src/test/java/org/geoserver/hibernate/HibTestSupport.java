package org.geoserver.hibernate;

import org.h2.tools.DeleteDbFiles;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.springframework.web.context.support.XmlWebApplicationContext;

public class HibTestSupport {

    protected static XmlWebApplicationContext ctx;
    
    @BeforeClass
    public static void initAppContext() throws Exception {
        ctx = new XmlWebApplicationContext() {
            public String[] getConfigLocations() {
                return new String[]{
                    "file:src/main/resources/applicationContext.xml", 
                    "file:src/test/resources/applicationContext-test.xml"};
            }
        };
        ctx.refresh();
    }
    
    @AfterClass
    public static void destroy() throws Exception {
        ctx.close();
        DeleteDbFiles.execute(".", "catalog", false);
    }
}
