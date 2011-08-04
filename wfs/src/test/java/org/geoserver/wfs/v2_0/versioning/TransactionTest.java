package org.geoserver.wfs.v2_0.versioning;

import org.custommonkey.xmlunit.XMLAssert;
import org.geoserver.data.test.MockData;
import org.geotools.gml3.v3_2.GML;
import org.geotools.wfs.v2_0.WFS;
import org.w3c.dom.Document;

public class TransactionTest extends WFS20VersioningTestSupport {

    public void testInsert() throws Exception {
        String buildings = getLayerId(MockData.BUILDINGS);
        Document dom = getAsDOM("wfs?request=GetFeature&version=2.0.0&typeName="+buildings);

        XMLAssert.assertXpathEvaluatesTo("2", "count(//"+buildings+")", dom);

        //do an insert
        String xml = "<wfs:Transaction service=\"WFS\" version=\"2.0.0\" "
                + " xmlns:wfs='" + WFS.NAMESPACE + "' "
                + " xmlns:gml='" + GML.NAMESPACE + "' "
                + " xmlns:cite=\"http://www.opengis.net/cite\">"
                + "<wfs:Insert>"
                + " <cite:Buildings>"
                + "  <cite:the_geom>"
                + "<gml:MultiCurve srsName=\"EPSG:4326\">"
                + " <gml:curveMember>"
                + "   <gml:LineString>"
                + "        <gml:posList>4.2582 52.0643 4.2584 52.0648</gml:posList>"
                + "   </gml:LineString>"
                + " </gml:curveMember>"
                + "</gml:MultiCurve>"
                + "  </cite:the_geom>"
                + "  <cite:FID>foo</cite:FID>"
                + "  <cite:NAME>bar</cite:NAME>" 
                + " </cite:Buildings>"
                + "</wfs:Insert>"
                + "</wfs:Transaction>";
    }

    public void testUpdate() throws Exception {
        
    }
}
