/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.wfs.xml;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;

import net.opengis.wfs.FeatureCollectionType;
import net.opengis.wfs20.Wfs20Factory;

import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.config.GeoServer;
import org.geoserver.platform.Operation;
import org.geoserver.wfs.request.GetFeatureRequest;
import org.geoserver.wfs.xml.v1_1_0.WFSConfiguration;
import org.geotools.wfs.v2_0.WFS;
import org.geotools.xml.Configuration;
import org.geotools.xml.Encoder;
import org.w3c.dom.Document;

public class GML32OutputFormat extends GML3OutputFormat {

    public static final String[] MIME_TYPES = new String[]{
        "text/xml; subtype=gml/3.2", "application/gml+xml; version=3.2"
    };
    
    public static final List<String> FORMATS = new ArrayList<String>();
    static {
        FORMATS.add("gml32");
        FORMATS.addAll(Arrays.asList(MIME_TYPES));
    }
    
    GeoServer geoServer;

    protected static DOMSource xslt;

    static {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setNamespaceAware(true);
        Document xsdDocument = null;
        try {
            xsdDocument = docFactory.newDocumentBuilder().parse(
                    GML3OutputFormat.class.getResourceAsStream("/ChangeNumberOfFeature32.xslt"));
            xslt = new DOMSource(xsdDocument);
        } catch (Exception e) {
            xslt = null;
            LOGGER.log(Level.INFO, e.getMessage(), e);
        }
    }

    public GML32OutputFormat(GeoServer geoServer, WFSConfiguration configuration) {
        super(new HashSet(FORMATS), geoServer, configuration);
        this.geoServer = geoServer;
    }

    @Override
    public String getMimeType(Object value, Operation operation) {
        return MIME_TYPES[0];
    }

    @Override
    protected Encoder createEncoder(Configuration configuration, 
        Map<String, Set<FeatureTypeInfo>> featureTypes, Object request) {
        
        FeatureTypeSchemaBuilder schemaBuilder = new FeatureTypeSchemaBuilder.GML32(geoServer);
        
        ApplicationSchemaXSD2 xsd = new ApplicationSchemaXSD2(schemaBuilder, featureTypes);
        xsd.setBaseURL(GetFeatureRequest.adapt(request).getBaseURL());
        
        ApplicationSchemaConfiguration2 config = new ApplicationSchemaConfiguration2(xsd, 
            new org.geotools.wfs.v2_0.WFSConfiguration());
        
        return new Encoder(config);
    }
    
    @Override
    protected void encode(FeatureCollectionType results, OutputStream output, Encoder encoder)
            throws IOException {
        //convert to wfs 2.0 FeatureCollectionType
        net.opengis.wfs20.FeatureCollectionType fc = Wfs20Factory.eINSTANCE.createFeatureCollectionType();
        fc.setLockId(results.getLockId());
        fc.setTimeStamp(results.getTimeStamp());
        fc.setNumberReturned(results.getNumberOfFeatures());
        fc.getMember().addAll(results.getFeature());
        
        //encoder.getNamespaces().declarePrefix("gml", GML.NAMESPACE);
        encoder.encode(fc, WFS.FeatureCollection, output);
    }
    
    @Override
    protected String getWfsNamespace() {
        return WFS.NAMESPACE;
    }
    
    @Override
    protected String getCanonicalWfsSchemaLocation() {
        return WFS.CANONICAL_SCHEMA_LOCATION;
    }
    
    @Override
    protected String getRelativeWfsSchemaLocation() {
        return "wfs/2.0/wfs.xsd";
    }

    @Override
    protected DOMSource getXSLT() {
        return GML32OutputFormat.xslt;
    }

}
