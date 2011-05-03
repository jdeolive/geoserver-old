package org.geoserver.wfs.v2_0;

import java.net.URLEncoder;
import java.util.Collections;

import javax.xml.namespace.QName;

import junit.textui.TestRunner;

import org.custommonkey.xmlunit.XMLAssert;
import org.geoserver.data.test.MockData;
import org.geoserver.wfs.GMLInfo;
import org.geoserver.wfs.WFSInfo;
import org.geotools.filter.v2_0.FES;
import org.geotools.gml3.v3_2.GML;

import org.geotools.wfs.v2_0.WFS;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GetFeatureTest extends WFS20TestSupport {
    
    /**
     * This is a READ ONLY TEST so we can use one time setup
     */
    /*public static Test suite() {
        return new OneTimeTestSetup(new GetFeatureTest());
    }*/
    
    @Override
    protected void populateDataDirectory(MockData dataDirectory) throws Exception {
        super.populateDataDirectory(dataDirectory);
        
        // add extra types
        dataDirectory.addPropertiesType( 
                new QName( MockData.SF_URI, "WithGMLProperties", MockData.SF_PREFIX ), 
                org.geoserver.wfs.v1_1.GetFeatureTest.class.getResource("WithGMLProperties.properties"), 
                Collections.EMPTY_MAP);
    }

    public void testGet() throws Exception {
    	testGetFifteenAll("wfs?request=GetFeature&typename=cdf:Fifteen&version=2.0.0&service=wfs");
    }
    
    public void testGetPropertyNameEmpty() throws Exception {
    	testGetFifteenAll("wfs?request=GetFeature&typename=cdf:Fifteen&version=2.0.0&service=wfs&propertyname=");
    }
    
    public void testGetPropertyNameStar() throws Exception {
        testGetFifteenAll("wfs?request=GetFeature&typename=cdf:Fifteen&version=2.0.0&service=wfs&propertyname=*");
    }
    
    private void assertGML32(Document doc) {
        assertEquals(WFS.NAMESPACE, doc.getDocumentElement().getAttribute("xmlns:wfs"));
        
        String schemaLocation = doc.getDocumentElement().getAttribute("xsi:schemaLocation"); 
        assertTrue(schemaLocation.contains(WFS.NAMESPACE));
        
        String[] parts = schemaLocation.split(" ");
        for (int i = 0; i < parts .length; i++) {
            if (parts[i].equals(WFS.NAMESPACE)) {
                assertTrue(parts[i+1].endsWith("2.0/wfs.xsd"));
            }
        }
        assertEquals("wfs:FeatureCollection", doc.getDocumentElement().getNodeName());

    }

    private void testGetFifteenAll(String request) throws Exception{
    	Document doc = getAsDOM(request);
    	assertGML32(doc);

        NodeList features = doc.getElementsByTagName("cdf:Fifteen");
        assertFalse(features.getLength() == 0);

        for (int i = 0; i < features.getLength(); i++) {
            Element feature = (Element) features.item(i);
            assertTrue(feature.hasAttribute("gml:id"));
        }
    }

    // see GEOS-1287
    public void testGetWithFeatureId() throws Exception {

        Document doc = 
            getAsDOM("wfs?request=GetFeature&typeName=cdf:Fifteen&version=2.0.0&service=wfs&featureid=Fifteen.2");
        assertGML32(doc);
        
        XMLAssert.assertXpathEvaluatesTo("1", "count(//wfs:FeatureCollection/wfs:member/cdf:Fifteen)",
                doc);
        XMLAssert.assertXpathEvaluatesTo("Fifteen.2",
                "//wfs:FeatureCollection/wfs:member/cdf:Fifteen/@gml:id", doc);

        doc = getAsDOM("wfs?request=GetFeature&typeName=cite:NamedPlaces&version=2.0.0&service=wfs&featureId=NamedPlaces.1107531895891");

        //super.print(doc);
        assertEquals("wfs:FeatureCollection", doc.getDocumentElement().getNodeName());
        XMLAssert.assertXpathEvaluatesTo("1", "count(//wfs:FeatureCollection/wfs:member/cite:NamedPlaces)",
                doc);
        XMLAssert.assertXpathEvaluatesTo("NamedPlaces.1107531895891",
                "//wfs:FeatureCollection/wfs:member/cite:NamedPlaces/@gml:id", doc);
    }

    public void testPost() throws Exception {

        String xml = "<wfs:GetFeature " + "service='WFS' "
                + "version='2.0.0' "
                + "xmlns:cdf='http://www.opengis.net/cite/data' "
                + "xmlns:wfs='http://www.opengis.net/wfs/2.0' " + "> "
                + "<wfs:Query typeNames='cdf:Other'> "
                + "<wfs:PropertyName>cdf:string2</wfs:PropertyName> "
                + "</wfs:Query> " + "</wfs:GetFeature>";

        Document doc = postAsDOM("wfs", xml);
        assertGML32(doc);

        NodeList features = doc.getElementsByTagName("cdf:Other");
        assertFalse(features.getLength() == 0);

        for (int i = 0; i < features.getLength(); i++) {
            Element feature = (Element) features.item(i);
            assertTrue(feature.hasAttribute("gml:id"));
        }
    }

    public void testPostFormEncoded() throws Exception {
        String request = "wfs?service=WFS&version=2.0.0&request=GetFeature&typename=sf:PrimitiveGeoFeature"
                + "&namespace=xmlns("
                + URLEncoder.encode("sf=http://cite.opengeospatial.org/gmlsf", "UTF-8") + ")";

        Document doc = postAsDOM(request);
        assertGML32(doc);
        assertEquals(5, doc.getElementsByTagName("sf:PrimitiveGeoFeature").getLength());
    }

    public void testPostWithFilter() throws Exception {
        String xml = "<wfs:GetFeature service='WFS' version='2.0.0' "
            + "outputFormat='text/xml; subtype=gml/3.2' "
            + "xmlns:cdf=\"http://www.opengis.net/cite/data\" "
            + "xmlns:wfs='" + WFS.NAMESPACE + "' xmlns:fes='" + FES.NAMESPACE + "'>"
                + "<wfs:Query typeNames=\"cdf:Other\"> " 
                  + "<fes:Filter> "
                    + "<fes:PropertyIsEqualTo> "
                      + "<fes:ValueReference>cdf:integers</fes:ValueReference> "
                      + "<fes:Literal>7</fes:Literal> "
 
                    + "</fes:PropertyIsEqualTo> " 
                  + "</fes:Filter> "
                + "</wfs:Query> " 
              + "</wfs:GetFeature>";

        Document doc = postAsDOM("wfs", xml);
        assertGML32(doc);
        
        NodeList features = doc.getElementsByTagName("cdf:Other");
        assertFalse(features.getLength() == 0);

        for (int i = 0; i < features.getLength(); i++) {
            Element feature = (Element) features.item(i);
            assertTrue(feature.hasAttribute("gml:id"));
        }
    }
    public void testPostWithBboxFilter() throws Exception {
        String xml = "<wfs:GetFeature service='WFS' version='2.0.0' "
            + "outputFormat='text/xml; subtype=gml/3.2' "
            + "xmlns:sf=\"http://cite.opengeospatial.org/gmlsf\" "
            + "xmlns:gml='" + GML.NAMESPACE + "' "
            + "xmlns:wfs='" + WFS.NAMESPACE + "' "
            + "xmlns:fes='" + FES.NAMESPACE + "'>"
                + "<wfs:Query typeNames='sf:PrimitiveGeoFeature'>"
                + "<fes:Filter>"
                + "<fes:BBOX>"
                + "   <fes:ValueReference>pointProperty</fes:ValueReference>"
                + "   <gml:Envelope srsName='EPSG:4326'>"
                + "      <gml:lowerCorner>57.0 -4.5</gml:lowerCorner>"
                + "      <gml:upperCorner>62.0 1.0</gml:upperCorner>"
                + "   </gml:Envelope>"
                + "</fes:BBOX>"
                + "</fes:Filter>"
                + "</wfs:Query>"
            + "</wfs:GetFeature>";
        print(post("wfs", xml));
        Document doc = postAsDOM("wfs", xml);
        assertGML32(doc);

        NodeList features = doc.getElementsByTagName("sf:PrimitiveGeoFeature");
        assertEquals(1, features.getLength());
    }

    public void testPostWithFailingUrnBboxFilter() throws Exception {
        String xml = 
            "<wfs:GetFeature service='WFS' version='2.0.0'  outputFormat='text/xml; subtype=gml/3.2' "
            + "xmlns:sf='http://cite.opengeospatial.org/gmlsf' "
            + "xmlns:wfs='" + WFS.NAMESPACE + "' xmlns:fes='" + FES.NAMESPACE + "' "
            + "xmlns:gml='" + GML.NAMESPACE + "'>"
            + "<wfs:Query typeNames=\"sf:PrimitiveGeoFeature\">"
            + "<fes:Filter>"
            + "<fes:BBOX>"
            + "   <fes:PropertyName>pointProperty</fes:PropertyName>"
            + "   <gml:Envelope srsName='urn:ogc:def:crs:EPSG:6.11.2:4326'>"
            + "      <gml:lowerCorner>57.0 -4.5</gml:lowerCorner>"
            + "      <gml:upperCorner>62.0 1.0</gml:upperCorner>"
            + "   </gml:Envelope>"
            + "</fes:BBOX>"
            + "</fes:Filter>"
            + "</wfs:Query>"
            + "</wfs:GetFeature>";

        Document doc = postAsDOM("wfs", xml);
        assertGML32(doc);
        
        NodeList features = doc.getElementsByTagName("sf:PrimitiveGeoFeature");
        assertEquals(0, features.getLength());
    }
 
    public void testPostWithMatchingUrnBboxFilter() throws Exception {
        String xml = 
            "<wfs:GetFeature service='WFS' version='2.0.0'  outputFormat='text/xml; subtype=gml/3.2' "
            + "xmlns:sf='http://cite.opengeospatial.org/gmlsf' "
            + "xmlns:wfs='" + WFS.NAMESPACE + "' xmlns:fes='" + FES.NAMESPACE + "' "
            + "xmlns:gml='" + GML.NAMESPACE + "'>"
            + "<wfs:Query typeNames=\"sf:PrimitiveGeoFeature\">"
            + "<fes:Filter>"
            + "<fes:BBOX>"
            + "   <fes:PropertyName>pointProperty</fes:PropertyName>"
            + "   <gml:Envelope srsName='urn:ogc:def:crs:EPSG:6.11.2:4326'>"
            + "      <gml:lowerCorner>-4.5 57.0</gml:lowerCorner>"
            + "      <gml:upperCorner>1.0 62.0</gml:upperCorner>"
            + "   </gml:Envelope>"
            + "</fes:BBOX>"
            + "</fes:Filter>"
            + "</wfs:Query>"
            + "</wfs:GetFeature>";
        
        Document doc = postAsDOM("wfs", xml);
        assertGML32(doc);
        
        NodeList features = doc.getElementsByTagName("sf:PrimitiveGeoFeature");
        assertEquals(1, features.getLength());
    }

    public void testResultTypeHitsGet() throws Exception {
        Document doc = getAsDOM("wfs?request=GetFeature&typename=cdf:Fifteen&version=2.0.0&resultType=hits&service=wfs");
        print(doc);
        assertGML32(doc);
        
        NodeList features = doc.getElementsByTagName("cdf:Fifteen");
        assertEquals(0, features.getLength());

        assertEquals("15", doc.getDocumentElement().getAttribute(
                "numberReturned"));
    }

    public void testResultTypeHitsPost() throws Exception {
        String xml = "<wfs:GetFeature service='WFS' version='2.0.0' outputFormat='text/xml; subtype=gml/3.2' "
                + "xmlns:cdf=\"http://www.opengis.net/cite/data\" "
                + "xmlns:wfs='" + WFS.NAMESPACE + "' "
                + "resultType='hits'> "
                + "<wfs:Query typeNames=\"cdf:Seven\"/> " + "</wfs:GetFeature>";

        Document doc = postAsDOM("wfs", xml);
        assertGML32(doc);

        NodeList features = doc.getElementsByTagName("cdf:Seven");
        assertEquals(0, features.getLength());

        assertEquals("7", doc.getDocumentElement().getAttribute(
                "numberReturned"));
    }

    public void testWithSRS() throws Exception {
        String xml = "<wfs:GetFeature version='2.0.0' service='WFS' xmlns:wfs='"+WFS.NAMESPACE+"' >"
                + "<wfs:Query xmlns:cdf='http://www.opengis.net/cite/data' typeNames='cdf:Other' " +
                    "srsName='urn:ogc:def:crs:EPSG:4326'/>"
                + "</wfs:GetFeature>";

        Document dom = postAsDOM("wfs", xml);
        assertGML32(dom);
        assertEquals(1, dom.getElementsByTagName("cdf:Other").getLength());
    }

    public void testWithSillyLiteral() throws Exception {
        String xml = 
            "<wfs:GetFeature version='2.0.0' service='WFS' "
                + "xmlns:cdf='http://www.opengis.net/cite/data' "
                + "xmlns:wfs='" + WFS.NAMESPACE + "' "
                + "xmlns:gml='" + GML.NAMESPACE + "' " 
                + "xmlns:fes='" + FES.NAMESPACE + "'>"
                + "<wfs:Query  typeNames='cdf:Other' srsName='urn:ogc:def:crs:EPSG:4326'>"
                + "<fes:Filter>"
                + "  <fes:PropertyIsEqualTo>"
                + "   <fes:ValueReference>description</fes:ValueReference>"
                + "   <fes:Literal>"
                + "       <wfs:Native vendorId=\"foo\" safeToIgnore=\"true\"/>"
                + "   </fes:Literal>"
                + "   </fes:PropertyIsEqualTo>"
                + " </fes:Filter>" + "</wfs:Query>" + "</wfs:GetFeature>";

        Document dom = postAsDOM("wfs", xml);
        assertGML32(dom);
        assertEquals(0, dom.getElementsByTagName("cdf:Other").getLength());
    }
//
//    public void testWithGmlObjectId() throws Exception {
//        String xml = "<wfs:GetFeature xmlns:cdf=\"http://www.opengis.net/cite/data\" xmlns:wfs=\"http://www.opengis.net/wfs\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:ogc=\"http://www.opengis.net/ogc\" version=\"1.1.0\" service=\"WFS\">"
//                + "<wfs:Query  typeName=\"cdf:Seven\" srsName=\"urn:x-ogc:def:crs:EPSG:6.11.2:4326\">"
//                + "</wfs:Query>" + "</wfs:GetFeature>";
//
//        Document dom = postAsDOM("wfs", xml);
//        assertEquals("wfs:FeatureCollection", dom.getDocumentElement()
//                .getNodeName());
//        assertEquals(7, dom.getElementsByTagName("cdf:Seven")
//                .getLength());
//
//        NodeList others = dom.getElementsByTagName("cdf:Seven");
//        String id = ((Element) others.item(0)).getAttributeNS(GML.NAMESPACE,
//                "id");
//        assertNotNull(id);
//
//        xml = "<wfs:GetFeature xmlns:cdf=\"http://www.opengis.net/cite/data\" xmlns:wfs=\"http://www.opengis.net/wfs\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:ogc=\"http://www.opengis.net/ogc\" version=\"1.1.0\" service=\"WFS\">"
//                + "<wfs:Query  typeName=\"cdf:Seven\" srsName=\"urn:x-ogc:def:crs:EPSG:6.11.2:4326\">"
//                + "<ogc:Filter>"
//                + "<ogc:GmlObjectId gml:id=\""
//                + id
//                + "\"/>"
//                + "</ogc:Filter>" + "</wfs:Query>" + "</wfs:GetFeature>";
//        dom = postAsDOM("wfs", xml);
//
//        assertEquals(1, dom.getElementsByTagName("cdf:Seven")
//                .getLength());
//    }
//    
    public void testPostWithBoundsEnabled() throws Exception {
        // enable feature bounds computation
        WFSInfo wfs = getWFS();
        boolean oldFeatureBounding = wfs.isFeatureBounding();
        wfs.setFeatureBounding(true);
        getGeoServer().save( wfs );
        
        try {
            String xml = "<wfs:GetFeature service='WFS' version='2.0.0' "
                + "xmlns:cdf='http://www.opengis.net/cite/data' "
                + "xmlns:fes='" + FES.NAMESPACE + "' "
                + "xmlns:wfs='" + WFS.NAMESPACE + "'> "
                + "<wfs:Query typeNames='cdf:Other'> "
                + "<wfs:PropertyName>cdf:string2</wfs:PropertyName> "
                + "</wfs:Query> " + "</wfs:GetFeature>";
    
            Document doc = postAsDOM("wfs", xml);
            assertGML32(doc);
            
            NodeList features = doc.getElementsByTagName("cdf:Other");
            assertFalse(features.getLength() == 0);
    
            for (int i = 0; i < features.getLength(); i++) {
                Element feature = (Element) features.item(i);
                assertTrue(feature.hasAttribute("gml:id"));
                NodeList boundList = feature.getElementsByTagName("gml:boundedBy");
                assertEquals(1, boundList.getLength());
                Element boundedBy = (Element) boundList.item(0);
                NodeList boxList = boundedBy.getElementsByTagName("gml:Envelope");
                assertEquals(1, boxList.getLength());
                Element box = (Element) boxList.item(0);
                assertTrue(box.hasAttribute("srsName"));
            }
        } finally {
            wfs.setFeatureBounding(oldFeatureBounding);
            getGeoServer().save( wfs );
        }
    }

    public void testAfterFeatureTypeAdded() throws Exception {
        Document dom = getAsDOM( "wfs?request=getfeature&service=wfs&version=2.0.0&typename=sf:new");
        assertEquals( "ExceptionReport", dom.getDocumentElement().getLocalName() );
        
        getTestData().addPropertiesType( 
                new QName( MockData.SF_URI, "new", MockData.SF_PREFIX ), 
                org.geoserver.wfs.v1_1.GetFeatureTest.class.getResource("new.properties"), 
                Collections.EMPTY_MAP 
            );
        reloadCatalogAndConfiguration();
        
        dom = getAsDOM( "wfs?request=getfeature&service=wfs&version=2.0.0&typename=sf:new");
        print(dom);
        assertGML32(dom);
    }
    
    public void testWithGMLProperties() throws Exception {
        Document dom = getAsDOM( "wfs?request=getfeature&service=wfs&version=2.0.0&typename=sf:WithGMLProperties");
        assertGML32(dom);
        
        NodeList features = dom.getElementsByTagName("sf:WithGMLProperties");
        assertEquals( 1, features.getLength() );
        
        for ( int i = 0; i < features.getLength(); i++ ) {
            Element feature = (Element) features.item( i );
            assertEquals( "one", getFirstElementByTagName( feature, "gml:name").getFirstChild().getNodeValue() );
            assertEquals( "1", getFirstElementByTagName( feature, "sf:foo").getFirstChild().getNodeValue());
            
            Element location = getFirstElementByTagName( feature, "gml:location" );
            assertNotNull( getFirstElementByTagName( location, "gml:Point" ) );
        }
    }

    public void testLayerQualified() throws Exception {
        testGetFifteenAll("cdf/Fifteen/wfs?request=GetFeature&typename=cdf:Fifteen&version=2.0.0&service=wfs");
        
        Document dom = getAsDOM("cdf/Seven/wfs?request=GetFeature&typename=cdf:Fifteen&version=2.0.0&service=wfs");
        XMLAssert.assertXpathEvaluatesTo("1", "count(//ows:ExceptionReport)", dom);
    }

    public void testUserSuppliedNamespacePrefix() throws Exception {
        testGetFifteenAll("wfs?request=GetFeature&typename=myPrefix:Fifteen&version=2.0.0&service=wfs&"
                + "namespace=xmlns(myPrefix%3D" // the '=' sign shall be encoded, hence '%3D'
                + URLEncoder.encode(MockData.FIFTEEN.getNamespaceURI(), "UTF-8") + ")");
    }

    public void testUserSuppliedDefaultNamespace() throws Exception {
        testGetFifteenAll("wfs?request=GetFeature&typename=Fifteen&version=2.0.0&service=wfs&"
                + "namespace=xmlns("
                + URLEncoder.encode(MockData.FIFTEEN.getNamespaceURI(), "UTF-8") + ")");
    }

    public void testGML32OutputFormat() throws Exception {
        testGetFifteenAll(
            "wfs?request=getfeature&typename=cdf:Fifteen&version=2.0.0&service=wfs&outputFormat=gml32");
    }

    public void testGMLAttributeMapping() throws Exception {
        WFSInfo wfs = getWFS();
        GMLInfo gml = wfs.getGML().get(WFSInfo.Version.V_11);
        gml.setOverrideGMLAttributes(false);
        getGeoServer().save(wfs);
        
        Document dom = getAsDOM("ows?service=WFS&version=2.0.0&request=GetFeature" +
                "&typename=" + getLayerId(MockData.PRIMITIVEGEOFEATURE));
        XMLAssert.assertXpathExists("//gml:name", dom);
        XMLAssert.assertXpathExists("//gml:description", dom);
        XMLAssert.assertXpathNotExists("//sf:name", dom);
        XMLAssert.assertXpathNotExists("//sf:description", dom);
        
        gml.setOverrideGMLAttributes(true);
        getGeoServer().save(wfs);
    
        dom = getAsDOM("ows?service=WFS&version=2.0.0&request=GetFeature" +
                "&typename=" + getLayerId(MockData.PRIMITIVEGEOFEATURE));
        XMLAssert.assertXpathNotExists("//gml:name", dom);
        XMLAssert.assertXpathNotExists("//gml:description", dom);
        XMLAssert.assertXpathExists("//sf:name", dom);
        XMLAssert.assertXpathExists("//sf:description", dom);
    }
    
    public static void main(String[] args) {
        TestRunner runner = new TestRunner();
        runner.run(GetFeatureTest.class);
    }
}
