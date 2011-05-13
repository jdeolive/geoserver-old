package org.geoserver.wfs.v2_0;

import org.custommonkey.xmlunit.XMLAssert;
import org.geoserver.data.test.MockData;
import org.geotools.wfs.v2_0.WFS;
import org.w3c.dom.Document;

public class StoredQueryTest extends WFS20TestSupport {

    public void testCreateStoredQuery() throws Exception {
        String xml = 
            "<wfs:ListStoredQueries service='WFS' version='2.0.0' " +
            " xmlns:wfs='" + WFS.NAMESPACE + "'/>";
        Document dom = postAsDOM("wfs", xml);
        assertEquals("wfs:ListStoredQueriesResponse", dom.getDocumentElement().getNodeName());
        XMLAssert.assertXpathEvaluatesTo("0", "count(//wfs:StoredQuery)", dom);
        
        xml = 
        "<wfs:CreateStoredQuery service='WFS' version='2.0.0' " +
        "   xmlns:wfs='http://www.opengis.net/wfs/2.0' " + 
        "   xmlns:fes='http://www.opengis.org/fes/2.0' " + 
        "   xmlns:gml='http://www.opengis.net/gml/3.2' " + 
        "   xmlns:myns='http://www.someserver.com/myns' " + 
        "   xmlns:sf='" + MockData.SF_URI + "'>" + 
        "   <wfs:StoredQueryDefinition id='myStoredQuery'> " + 
        "      <wfs:Parameter name='AreaOfInterest' type='gml:Polygon'/> " + 
        "      <wfs:QueryExpressionText " + 
        "           returnFeatureTypes='sf:PrimitiveGeoFeature' " + 
        "           language='urn:ogc:def:queryLanguage:OGC-WFS::WFS_QueryExpression' " + 
        "           isPrivate='false'> " + 
        "         <wfs:Query typeNames='myns:Parks'> " + 
        "            <fes:Filter> " + 
        "               <fes:Within> " + 
        "                  <fes:ValueReference>pointProperty</fes:ValueReference> " + 
        "                  ${AreaOfInterest} " + 
        "               </fes:Within> " + 
        "            </fes:Filter> " + 
        "         </wfs:Query> " + 
        "      </wfs:QueryExpressionText> " + 
        "   </wfs:StoredQueryDefinition> " + 
        "</wfs:CreateStoredQuery>"; 
        
        dom = postAsDOM("wfs", xml);
        assertEquals("wfs:CreateStoredQueryResponse", dom.getDocumentElement().getNodeName());
        assertEquals("OK", dom.getDocumentElement().getAttribute("status"));
        
        dom = getAsDOM("wfs?request=ListStoredQueries");
        XMLAssert.assertXpathEvaluatesTo("1", "count(//wfs:StoredQuery)", dom);
        XMLAssert.assertXpathExists("//wfs:StoredQuery[@id = 'myStoredQuery']", dom);
        XMLAssert.assertXpathExists("//wfs:ReturnFeatureType[text() = 'sf:PrimitiveGeoFeature']", dom);
    }
    
    public void testDescribeStoredQueries() throws Exception {
        Document dom = getAsDOM("wfs?request=DescribeStoredQueries&storedQueryId=myStoredQuery");
        assertEquals("ows:ExceptionReport", dom.getDocumentElement().getNodeName());
        
        testCreateStoredQuery();
        
        String xml = 
            "<wfs:DescribeStoredQueries xmlns:wfs='" + WFS.NAMESPACE + "' service='WFS'>" + 
              "<wfs:StoredQueryId>myStoredQuery</wfs:StoredQueryId>" + 
            "</wfs:DescribeStoredQueries>";
        
        dom = postAsDOM("wfs", xml);
        assertEquals("wfs:DescribeStoredQueriesResponse", dom.getDocumentElement().getNodeName());
        XMLAssert.assertXpathExists("//wfs:StoredQueryDescription[@id='myStoredQuery']", dom);
    }
    
    public void testDropStoredQuery() throws Exception {
        Document dom = getAsDOM("wfs?request=DropStoredQuery&id=myStoredQuery");
        assertEquals("ows:ExceptionReport", dom.getDocumentElement().getNodeName());
        
        testCreateStoredQuery();
        
        String xml = 
            "<wfs:DropStoredQuery xmlns:wfs='" + WFS.NAMESPACE + "' service='WFS' id='myStoredQuery'/>"; 
        dom = postAsDOM("wfs", xml);
        
        assertEquals("wfs:DropStoredQueryResponse", dom.getDocumentElement().getNodeName());
        assertEquals("OK", dom.getDocumentElement().getAttribute("status"));
        
        dom = getAsDOM("wfs?request=DropStoredQuery&id=myStoredQuery");
        assertEquals("ows:ExceptionReport", dom.getDocumentElement().getNodeName());   
    }
}
