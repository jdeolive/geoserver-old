package org.geoserver.wfs.v2_0;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import junit.framework.Test;

import org.custommonkey.xmlunit.XMLAssert;
import org.geoserver.wfs.GetFeatureTest;
import org.geoserver.wfs.WFSExtendedOperatorFactory;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.NameImpl;
import org.geotools.filter.ExtendedOperatorFactory;
import org.opengis.feature.type.Name;
import org.w3c.dom.Document;

public class ExtendedOperatorTest extends WFS20TestSupport {

    /**
     * This is a READ ONLY TEST so we can use one time setup
     */
    public static Test suite() {
        return new OneTimeTestSetup(new GetFeatureTest());
    }

    @Override
    protected void oneTimeSetUp() throws Exception {
        super.oneTimeSetUp();
        new File(getTestData().getDataDirectoryRoot(), "wfs/operators").mkdirs();
        getTestData().copyTo(getClass().getResourceAsStream("ops.xml"), "wfs/operators/ops.xml");
        getTestData().copyTo(getClass().getResourceAsStream("Op.ftl"), "wfs/operators/Op.ftl");

        //needed to reset the WFSExtendedOperator factory which caches the data direcotry, if other
        // tests are running then it would have already been set, and is cached at the geotools level
        CommonFactoryFinder.reset();
    }
    
    public void testExtendedOperatorLookup() throws Exception {
        Set<ExtendedOperatorFactory> ops = CommonFactoryFinder.getExtendedOperatorFactories(null);
        WFSExtendedOperatorFactory wfsOpFactory = null;
        for (ExtendedOperatorFactory eof : ops) {
            if (eof instanceof WFSExtendedOperatorFactory) {
                wfsOpFactory = (WFSExtendedOperatorFactory) eof;
            }
        }

        assertNotNull(wfsOpFactory);
        
        Name opName = new NameImpl("http://foo.org", "Op");
        assertTrue(wfsOpFactory.getFunctionNames().contains(opName));
        assertNotNull(wfsOpFactory.operator(opName, (List)Collections.emptyList(), 
            CommonFactoryFinder.getFilterFactory(null)));
    }

    public void testExtendedOperator() throws Exception {
        
        String xml = 
            "<wfs:GetFeature service='WFS' version='2.0.0' " + 
               "xmlns:wfs='http://www.opengis.net/wfs/2.0' " +
               "xmlns:fes='http://www.opengis.net/fes/2.0' " +
               "xmlns:foo='http://foo.org'> " + 
                "<wfs:Query typeNames='sf:PrimitiveGeoFeature'> " +
                "  <fes:Filter>" +
                "   <foo:Op>" + 
                "     <fes:ValueReference>name</fes:ValueReference>" +
                "   </foo:Op>" + 
                "  </fes:Filter>" + 
                "</wfs:Query> " + 
              "</wfs:GetFeature>";
        Document doc = postAsDOM("wfs", xml);
        XMLAssert.assertXpathEvaluatesTo("1", "count(//sf:PrimitiveGeoFeature)", doc);
        XMLAssert.assertXpathExists("//sf:PrimitiveGeoFeature/gml:name[text()='name-f002']", doc);
    }
}
