/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.wfs.xml.v1_1_0;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.util.DefaultJAXPConfiguration;
import org.eclipse.xsd.util.XSDResourceImpl;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.config.GeoServer;
import org.geoserver.config.GeoServerInfo;
import org.geoserver.platform.GeoServerResourceLoader;
import org.geoserver.platform.Operation;
import org.geoserver.platform.ServiceException;
import org.geoserver.wfs.WFSDescribeFeatureTypeOutputFormat;
import org.geoserver.wfs.request.DescribeFeatureTypeRequest;
import org.geoserver.wfs.xml.FeatureTypeSchemaBuilder;
import org.geotools.xml.Schemas;
import org.w3c.dom.Element;


public class XmlSchemaEncoder extends WFSDescribeFeatureTypeOutputFormat {
    
    /** the catalog */
    Catalog catalog;

    /** the geoserver resource loader */
    GeoServerResourceLoader resourceLoader;
    
    /** schema builder */
    FeatureTypeSchemaBuilder schemaBuilder;

    public XmlSchemaEncoder(String mimeType, GeoServer gs, FeatureTypeSchemaBuilder schemaBuilder) {
        super(gs, mimeType);
        
       
        this.catalog = gs.getCatalog();
        this.resourceLoader = catalog.getResourceLoader();
        this.schemaBuilder = schemaBuilder;
    }
    
    public XmlSchemaEncoder(Set<String> mimeTypes, GeoServer gs, FeatureTypeSchemaBuilder schemaBuilder) {
        super(gs, mimeTypes);
        
       
        this.catalog = gs.getCatalog();
        this.resourceLoader = catalog.getResourceLoader();
        this.schemaBuilder = schemaBuilder;
    }

    public String getMimeType(Object value, Operation operation)
        throws ServiceException {
        return getOutputFormat();
        //return "text/xml; subtype=gml/3.1.1";
    }

    protected void write(FeatureTypeInfo[] featureTypeInfos, OutputStream output,
        Operation describeFeatureType) throws IOException {
        
        GeoServerInfo global = gs.getGlobal();

        //create the schema
        Object request = describeFeatureType.getParameters()[0];
        DescribeFeatureTypeRequest req = DescribeFeatureTypeRequest.adapt(request);
        
        XSDSchema schema = schemaBuilder.build(featureTypeInfos, req.getBaseURL());

        //serialize
        schema.updateElement();
        final String encoding = global.getCharset();
        
        serialize(output, schema.getElement(), encoding, req);
    }
    
    void serialize(OutputStream output, Element element, String encoding, DescribeFeatureTypeRequest req) throws IOException {
        try {
            Transformer tx = new DefaultJAXPConfiguration().createTransformer(encoding);
            
            if (Boolean.TRUE.equals(req.getExtendedProperties().get("SOAP"))) {
                tx.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            }

            tx.transform(new DOMSource(element), new StreamResult(output));
        } 
        catch(Exception e) {
            throw new IOException("Error serializing schema", e);
        }
    }
    
    public static class V20 extends XmlSchemaEncoder {
        static Set<String> MIME_TYPES = new LinkedHashSet<String>();
        static {
            MIME_TYPES.add("text/xml; subtype=gml/3.2");
            MIME_TYPES.add("application/gml+xml; version=3.2");
        }
        public V20(GeoServer gs) {
            super(MIME_TYPES, gs, new FeatureTypeSchemaBuilder.GML32(gs));
        }
        
    }
    
    public static class V11 extends XmlSchemaEncoder {

        public V11(GeoServer gs) {
            super("text/xml; subtype=gml/3.1.1",gs,new FeatureTypeSchemaBuilder.GML3(gs));
        }
        
    }
    
    public static class V10 extends XmlSchemaEncoder {

        public V10(GeoServer gs) {
            super("XMLSCHEMA", gs, new FeatureTypeSchemaBuilder.GML2(gs));
        }
        
        @Override
        public String getMimeType(Object arg0, Operation arg1) throws ServiceException {
            return "text/xml";
        }
        
    }
}
