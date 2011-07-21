package org.geoserver.wfs.v2_0;

import java.io.File;

import org.custommonkey.xmlunit.XMLAssert;
import org.w3c.dom.Document;

public class ExtendedOperatorTest extends WFS20TestSupport {

    @Override
    protected void setUpInternal() throws Exception {
        new File(getTestData().getDataDirectoryRoot(), "wfs/operators").mkdirs();
        getTestData().copyTo(getClass().getResourceAsStream("ops.xml"), "wfs/operators/ops.xml");
        getTestData().copyTo(getClass().getResourceAsStream("Op.ftl"), "wfs/operators/Op.ftl");
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
