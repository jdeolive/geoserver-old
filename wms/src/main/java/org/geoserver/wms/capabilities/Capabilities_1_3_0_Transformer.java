/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.wms.capabilities;

import static org.geoserver.ows.util.ResponseUtils.appendQueryString;
import static org.geoserver.ows.util.ResponseUtils.buildSchemaURL;
import static org.geoserver.ows.util.ResponseUtils.buildURL;
import static org.geoserver.ows.util.ResponseUtils.params;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.xalan.transformer.TransformerIdentityImpl;
import org.geoserver.catalog.AttributionInfo;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.CoverageInfo;
import org.geoserver.catalog.CoverageStoreInfo;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.catalog.LayerGroupInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.LayerInfo.Type;
import org.geoserver.catalog.LegendInfo;
import org.geoserver.catalog.MetadataLinkInfo;
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.catalog.StyleInfo;
import org.geoserver.catalog.WMSLayerInfo;
import org.geoserver.config.ContactInfo;
import org.geoserver.config.GeoServer;
import org.geoserver.ows.URLMangler.URLType;
import org.geoserver.platform.ServiceException;
import org.geoserver.wms.GetCapabilities;
import org.geoserver.wms.GetCapabilitiesRequest;
import org.geoserver.wms.GetLegendGraphicRequest;
import org.geoserver.wms.WMS;
import org.geoserver.wms.WMSInfo;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.data.ows.Layer;
import org.geotools.data.ows.WMSCapabilities;
import org.geotools.factory.GeoTools;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.styling.Style;
import org.geotools.util.logging.Logging;
import org.geotools.xml.transform.TransformerBase;
import org.geotools.xml.transform.Translator;
import org.opengis.feature.type.Name;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.springframework.util.Assert;
import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.AttributesImpl;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Geotools xml framework based encoder for a Capabilities WMS 1.3.0 document.
 * 
 * @author Gabriel Roldan
 * @version $Id
 * @see GetCapabilities#run(GetCapabilitiesRequest)
 * @see GetCapabilitiesResponse#write(Object, java.io.OutputStream,
 *      org.geoserver.platform.Operation)
 */
public class Capabilities_1_3_0_Transformer extends TransformerBase {

    private static final String NAMESPACE = "http://www.opengis.net/wms";

    /** fixed MIME type for the returned capabilities document */
    public static final String WMS_CAPS_MIME = "text/xml";

    /** the WMS supported exception formats */
    static final String[] EXCEPTION_FORMATS = { "XML", "INIMAGE", "BLANK" };

    /**
     * The geoserver base URL to append it the schemas/wms/1.3.0/exceptions_1_3_0.xsd schema
     * location
     */
    private String schemaBaseURL;

    /** The list of output formats to state as supported for the GetMap request */
    private Set<String> getMapFormats;

    private WMS wmsConfig;

    /**
     * Creates a new WMSCapsTransformer object.
     * 
     * @param wms
     * 
     * @param schemaBaseUrl
     *            the base URL of the current request (usually "http://host:port/geoserver")
     * @param getMapFormats
     *            the list of supported output formats to state for the GetMap request
     */
    public Capabilities_1_3_0_Transformer(WMS wms, String schemaBaseUrl, Set<String> getMapFormats) {
        super();
        Assert.notNull(wms);
        Assert.notNull(schemaBaseUrl, "baseURL");
        Assert.notNull(getMapFormats, "getMapFormats");

        this.wmsConfig = wms;
        this.getMapFormats = getMapFormats;
        this.schemaBaseURL = schemaBaseUrl;
        this.setNamespaceDeclarationEnabled(false);
        setIndentation(2);
        final Charset encoding = wms.getCharSet();
        setEncoding(encoding);
    }

    @Override
    public Translator createTranslator(ContentHandler handler) {
        String schemaLocation = buildSchemaURL(schemaBaseURL, "wms/1.3.0/capabilities_1_3_0.xsd");
        return new Capabilities_1_3_0_Translator(handler, wmsConfig, getMapFormats, schemaLocation);
    }

    /**
     * @author Gabriel Roldan
     * @version $Id
     */
    private static class Capabilities_1_3_0_Translator extends TranslatorSupport {

        private static final String XML_SCHEMA_INSTANCE = "http://www.w3.org/2001/XMLSchema-instance";

        private static final Logger LOGGER = Logging.getLogger(Capabilities_1_3_0_Translator.class);

        private static final String EPSG = "EPSG:";

        private static final String XLINK_NS = "http://www.w3.org/1999/xlink";

        /**
         * The request from wich all the information needed to produce the capabilities document can
         * be obtained
         */
        private GetCapabilitiesRequest request;

        private Set<String> getMapFormats;

        private WMS wmsConfig;

        private String schemaLocationURI;

        /**
         * Creates a new CapabilitiesTranslator object.
         * 
         * @param handler
         *            content handler to send sax events to.
         * @param schemaLoc
         * 
         */
        public Capabilities_1_3_0_Translator(ContentHandler handler, WMS wmsConfig,
                Set<String> getMapFormats, String schemaLocationURI) {
            super(handler, null, null);
            this.wmsConfig = wmsConfig;
            this.getMapFormats = getMapFormats;
            this.schemaLocationURI = schemaLocationURI;
        }

        private AttributesImpl attributes(String... kvp) {
            String[] atts = kvp;
            AttributesImpl attributes = new AttributesImpl();
            for (int i = 0; i < atts.length; i += 2) {
                String name = atts[i];
                String value = atts[i + 1];
                attributes.addAttribute("", name, name, "", value);
            }
            return attributes;
        }

        /**
         * @param o
         *            the {@link GetCapabilitiesRequest}
         * @throws IllegalArgumentException
         *             if {@code o} is not of the expected type
         */
        public void encode(Object o) throws IllegalArgumentException {
            if (!(o instanceof GetCapabilitiesRequest)) {
                throw new IllegalArgumentException();
            }

            this.request = (GetCapabilitiesRequest) o;

            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(new StringBuffer("producing a capabilities document for ").append(
                        request).toString());
            }

            String updateSequence = String.valueOf(wmsConfig.getUpdateSequence());
            String schemaLoc = NAMESPACE + " " + schemaLocationURI;
            AttributesImpl rootAtts = attributes("version", "1.3.0", "updateSequence",
                    updateSequence, "xmlns", NAMESPACE, "xmlns:xlink", XLINK_NS, "xmlns:xsi",
                    XML_SCHEMA_INSTANCE, "xsi:schemaLocation", schemaLoc);

            start("WMS_Capabilities", rootAtts);
            handleService();
            handleCapability();
            end("WMS_Capabilities");
        }

        /**
         * Encodes the service metadata section of a WMS capabilities document.
         */
        private void handleService() {
            start("Service");

            final WMSInfo serviceInfo = wmsConfig.getServiceInfo();
            element("Name", "WMS");
            element("Title", serviceInfo.getTitle());
            element("Abstract", serviceInfo.getAbstract());

            handleKeywordList(serviceInfo.getKeywords());

            String requestBaseUrl = request.getBaseUrl();
            String onlineResource = buildURL(requestBaseUrl, "ows", null, URLType.SERVICE);
            AttributesImpl attributes = attributes("xlink:type", "simple", "xlink:href",
                    onlineResource);
            element("OnlineResource", null, attributes);

            GeoServer geoServer = wmsConfig.getGeoServer();
            ContactInfo contact = geoServer.getGlobal().getContact();
            handleContactInfo(contact);

            element("Fees", serviceInfo.getFees());
            element("AccessConstraints", serviceInfo.getAccessConstraints());

            // TODO: LayerLimit, MaxWidth and MaxHeight have no equivalence in GeoServer config so
            // far
            end("Service");
        }

        /**
         * Encodes contact information in the WMS capabilities document
         */
        public void handleContactInfo(ContactInfo contact) {
            start("ContactInformation");

            start("ContactPersonPrimary");
            element("ContactPerson", contact.getContactPerson());
            element("ContactOrganization", contact.getContactOrganization());
            end("ContactPersonPrimary");

            element("ContactPosition", contact.getContactPosition());

            start("ContactAddress");
            element("AddressType", contact.getAddressType());
            element("Address", contact.getAddress());
            element("City", contact.getAddressCity());
            element("StateOrProvince", contact.getAddressState());
            element("PostCode", contact.getAddressPostalCode());
            element("Country", contact.getAddressCountry());
            end("ContactAddress");

            element("ContactVoiceTelephone", contact.getContactVoice());
            element("ContactFacsimileTelephone", contact.getContactFacsimile());
            element("ContactElectronicMailAddress", contact.getContactEmail());

            end("ContactInformation");
        }

        /**
         * Turns the keyword list to XML
         * 
         * @param keywords
         */
        private void handleKeywordList(List<String> keywords) {
            start("KeywordList");

            if (keywords != null) {
                for (String kw : keywords) {
                    element("Keyword", kw);
                }
            }

            end("KeywordList");
        }

        /**
         * Turns the metadata URL list to XML
         * 
         * @param keywords
         */
        private void handleMetadataList(List<MetadataLinkInfo> metadataURLs) {
            if (metadataURLs == null) {
                return;
            }

            for (MetadataLinkInfo link : metadataURLs) {

                AttributesImpl lnkAtts = new AttributesImpl();
                lnkAtts.addAttribute("", "type", "type", "", link.getMetadataType());
                start("MetadataURL", lnkAtts);

                element("Format", link.getType());

                AttributesImpl orAtts = attributes("xlink:type", "simple", "xlink:href",
                        link.getContent());
                element("OnlineResource", null, orAtts);

                end("MetadataURL");
            }
        }

        /**
         * Encodes the capabilities metadata section of a WMS capabilities document
         */
        private void handleCapability() {
            start("Capability");
            handleRequest();
            handleException();
            handleLayers();
            end("Capability");
        }

        private void handleRequest() {
            start("Request");

            start("GetCapabilities");
            element("Format", WMS_CAPS_MIME);

            // build the service URL and make sure it ends with &
            String serviceUrl = buildURL(request.getBaseUrl(), "ows", params("SERVICE", "WMS"),
                    URLType.SERVICE);
            serviceUrl = appendQueryString(serviceUrl, "");

            handleDcpType(serviceUrl, serviceUrl);
            end("GetCapabilities");

            start("GetMap");

            List<String> sortedFormats = new ArrayList<String>(getMapFormats);
            Collections.sort(sortedFormats);
            // this is a hack necessary to make cite tests pass: we need an output format
            // that is equal to the mime type as the first one....
            if (sortedFormats.contains("image/png")) {
                sortedFormats.remove("image/png");
                sortedFormats.add(0, "image/png");
            }
            for (String format : sortedFormats) {
                element("Format", format);
            }

            handleDcpType(serviceUrl, null);// only GET method
            end("GetMap");

            start("GetFeatureInfo");

            for (String format : wmsConfig.getAvailableFeatureInfoFormats()) {
                element("Format", format);
            }

            handleDcpType(serviceUrl, null); // only GET method
            end("GetFeatureInfo");

            // no DescribeLayer in 1.3.0
            // start("DescribeLayer");
            // element("Format", DescribeLayerResponse.DESCLAYER_MIME_TYPE);
            // handleDcpType(serviceUrl, null);
            // end("DescribeLayer");

            // same thing, not defined for 1.3.0
            // start("GetLegendGraphic");
            //
            // for (String format : getLegendGraphicFormats) {
            // element("Format", format);
            // }
            //
            // handleDcpType(serviceUrl, null);
            // end("GetLegendGraphic");

            // no way
            // start("GetStyles");
            // element("Format", GetStylesResponse.SLD_MIME_TYPE);
            // handleDcpType(serviceUrl, null);
            // end("GetStyles");

            // but there are _ExtendedOperations in WMS 1.3.0, seems to be calling for an extension
            // point
            // TODO: define extension point for _ExtendedOperation
            end("Request");
        }

        /**
         * Encodes a <code>DCPType</code> fragment for HTTP GET and POST methods.
         * 
         * @param getUrl
         *            the URL of the onlineresource for HTTP GET method requests
         * @param postUrl
         *            the URL of the onlineresource for HTTP POST method requests
         */
        private void handleDcpType(String getUrl, String postUrl) {
            AttributesImpl orAtts = new AttributesImpl();
            orAtts.addAttribute(XLINK_NS, "type", "xlink:type", "", "simple");
            orAtts.addAttribute(XLINK_NS, "href", "xlink:href", "", getUrl);
            start("DCPType");
            start("HTTP");

            if (getUrl != null) {
                start("Get");
                element("OnlineResource", null, orAtts);
                end("Get");
            }

            if (postUrl != null) {
                orAtts.setAttribute(1, "", "href", "xlink:href", "", postUrl);
                start("Post");
                element("OnlineResource", null, orAtts);
                end("Post");
            }

            end("HTTP");
            end("DCPType");
        }

        private void handleException() {
            start("Exception");

            for (String exceptionFormat : EXCEPTION_FORMATS) {
                element("Format", exceptionFormat);
            }

            end("Exception");
        }

        /**
         * Handles the encoding of the layers elements.
         * 
         * <p>
         * This method does a search over the SRS of all the layers to see if there are at least a
         * common one, as needed by the spec: "<i>The root Layer element shall include a sequence of
         * zero or more &lt;SRS&gt; elements listing all SRSes that are common to all subsidiary
         * layers. Use a single SRS element with empty content (like so: "&lt;SRS&gt;&lt;/SRS&gt;")
         * if there is no common SRS."</i>
         * </p>
         * 
         * <p>
         * By the other hand, this search is also used to collecto the whole latlon bbox, as stated
         * by the spec: <i>"The bounding box metadata in Capabilities XML specify the minimum
         * enclosing rectangle for the layer as a whole."</i>
         * </p>
         * 
         * @task TODO: manage this differently when we have the layer list of the WMS service
         *       decoupled from the feature types configured for the server instance. (This involves
         *       nested layers, gridcoverages, etc)
         */
        private void handleLayers() {
            start("Layer");

            final List<LayerInfo> layers;

            // filter the layers if a namespace filter has been set
            if (request.getNamespace() != null) {
                final List<LayerInfo> allLayers = wmsConfig.getLayers();
                layers = new ArrayList<LayerInfo>();

                String namespace = wmsConfig.getNamespaceByPrefix(request.getNamespace());
                for (LayerInfo layer : allLayers) {
                    Name name = layer.getResource().getQualifiedName();
                    if (name.getNamespaceURI().equals(namespace)) {
                        layers.add(layer);
                    }
                }
            } else {
                layers = wmsConfig.getLayers();
            }

            WMSInfo serviceInfo = wmsConfig.getServiceInfo();
            element("Title", serviceInfo.getTitle());
            element("Abstract", serviceInfo.getAbstract());

            List<String> srsList = serviceInfo.getSRS();
            Set<String> srs = new HashSet<String>();
            if (srsList != null) {
                srs.addAll(srsList);
            }
            handleRootCrsList(srs);

            handleRootBbox(layers);

            // now encode each layer individually
            LayerTree featuresLayerTree = new LayerTree(layers);
            handleLayerTree(featuresLayerTree);

            try {
                List<LayerGroupInfo> layerGroups = wmsConfig.getLayerGroups();
                handleLayerGroups(new ArrayList<LayerGroupInfo>(layerGroups));
            } catch (FactoryException e) {
                throw new RuntimeException("Can't obtain Envelope of Layer-Groups: "
                        + e.getMessage(), e);
            } catch (TransformException e) {
                throw new RuntimeException("Can't obtain Envelope of Layer-Groups: "
                        + e.getMessage(), e);
            }

            end("Layer");
        }

        /**
         * Called by <code>handleLayers()</code>, writes down list of supported CRS's for the root
         * Layer.
         * <p>
         * If <code>epsgCodes</code> is not empty, the list of supported CRS identifiers written
         * down to the capabilities document is limited to those in the <code>epsgCodes</code> list.
         * Otherwise, all the GeoServer supported CRS identifiers are used.
         * </p>
         * 
         * @param epsgCodes
         *            possibly empty set of CRS identifiers to limit the number of SRS elements to.
         */
        private void handleRootCrsList(final Set<String> epsgCodes) {
            final Set<String> capabilitiesCrsIdentifiers;
            if (epsgCodes.isEmpty()) {
                comment("All supported EPSG projections:");
                capabilitiesCrsIdentifiers = new LinkedHashSet<String>();
                for (String code : CRS.getSupportedCodes("AUTO")) {
                    if ("WGS84(DD)".equals(code))
                        continue;
                    capabilitiesCrsIdentifiers.add("AUTO:" + code);
                }
                capabilitiesCrsIdentifiers.addAll(CRS.getSupportedCodes("EPSG"));
            } else {
                comment("Limited list of EPSG projections:");
                capabilitiesCrsIdentifiers = new TreeSet<String>(epsgCodes);
            }

            try {
                Iterator<String> it = capabilitiesCrsIdentifiers.iterator();
                String currentSRS;

                while (it.hasNext()) {
                    currentSRS = it.next();
                    if (currentSRS.indexOf(':') == -1) {
                        currentSRS = EPSG + currentSRS;
                    }
                    element("CRS", currentSRS);
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
            }
        }

        /**
         * Called by <code>handleLayers()</code>, iterates over the available featuretypes and
         * coverages to summarize their LatLonBBox'es and write the aggregated bounds for the root
         * layer.
         * 
         * @param ftypes
         *            the collection of FeatureTypeInfo and CoverageInfo objects to traverse
         */
        private void handleRootBbox(Collection<LayerInfo> layers) {

            Envelope latlonBbox = new Envelope();
            Envelope layerBbox = null;

            LOGGER.finer("Collecting summarized latlonbbox and common SRS...");

            for (LayerInfo layer : layers) {
                ResourceInfo resource = layer.getResource();
                layerBbox = resource.getLatLonBoundingBox();
                latlonBbox.expandToInclude(layerBbox);
            }

            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Summarized LatLonBBox is " + latlonBbox);
            }

            handleGeographicBoundingBox(latlonBbox);
        }

        /**
         * @param layerTree
         */
        private void handleLayerTree(final LayerTree layerTree) {
            final List<LayerInfo> data = new ArrayList<LayerInfo>(layerTree.getData());
            final Collection<LayerTree> children = layerTree.getChildrens();

            Collections.sort(data, new Comparator<LayerInfo>() {
                public int compare(LayerInfo o1, LayerInfo o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });

            for (LayerInfo layer : data) {
                // no sense in exposing a geometryless layer through wms...
                boolean wmsExposable = false;
                if (layer.getType() == Type.RASTER || layer.getType() == Type.WMS) {
                    wmsExposable = true;
                } else {
                    try {
                        wmsExposable = layer.getType() == Type.VECTOR
                                && ((FeatureTypeInfo) layer.getResource()).getFeatureType()
                                        .getGeometryDescriptor() != null;
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "An error occurred trying to determine if"
                                + " the layer is geometryless", e);
                    }
                }

                // ask for enabled() instead of isEnabled() to account for disabled resource/store
                if (layer.enabled() && wmsExposable) {
                    try {
                        handleLayer(layer);
                    } catch (Exception e) {
                        // report what layer we failed on to help the admin locate and fix it
                        throw new ServiceException(
                                "Error occurred trying to write out metadata for layer: "
                                        + layer.getName(), e);
                    }
                }
            }

            for (LayerTree childLayerTree : children) {
                start("Layer");
                element("Name", childLayerTree.getName());
                element("Title", childLayerTree.getName());
                handleLayerTree(childLayerTree);
                end("Layer");
            }
        }

        /**
         */
        protected void handleLayer(final LayerInfo layer) {
            AttributesImpl qatts = attributes("queryable", isLayerQueryable(layer) ? "1" : "0");
            int cascadedHopCount = getCascadedHopCount(layer);
            if (cascadedHopCount > 0) {
                qatts.addAttribute("", "cascaded", "cascaded", "", String.valueOf(cascadedHopCount));
            }
            start("Layer", qatts);
            element("Name", layer.getResource().getNamespace().getPrefix() + ":" + layer.getName());
            // REVISIT: this is bad, layer should have title and anbstract by itself
            element("Title", layer.getResource().getTitle());
            element("Abstract", layer.getResource().getAbstract());
            handleKeywordList(layer.getResource().getKeywords());

            final String crs = layer.getResource().getSRS();
            element("CRS", crs);

            ReferencedEnvelope llbbox = layer.getResource().getLatLonBoundingBox();
            handleGeographicBoundingBox(llbbox);

            ReferencedEnvelope bbox;
            try {
                bbox = layer.getResource().boundingBox();
            } catch (Exception e) {
                throw new RuntimeException("Unexpected error obtaining bounding box for layer "
                        + layer.getName(), e);
            }
            // the native bbox might be null
            if (bbox != null) {
                handleBBox(bbox, crs);
            }

            // handle dimensions
            handleDimensions(layer);

            // handle data attribution
            handleAttribution(layer);

            // TODO: AuthorityURL

            // TODO: Identifier

            // handle metadata URLs
            handleMetadataList(layer.getResource().getMetadataLinks());

            // TODO: DataURL
            // TODO: FeatureListURL
            handleStyles(layer);

            end("Layer");
        }

        private void handleStyles(final LayerInfo layer) {
            if (layer.getResource() instanceof WMSLayerInfo) {
                // do nothing for the moment, we may want to list the set of cascaded named styles
                // in the future (when we add support for that)
            } else {
                // add the layer style
                start("Style");

                StyleInfo defaultStyle = layer.getDefaultStyle();
                if (defaultStyle == null) {
                    throw new NullPointerException("Layer " + layer.getName()
                            + " has no default style");
                }
                Style ftStyle;
                try {
                    ftStyle = defaultStyle.getStyle();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                element("Name", defaultStyle.getName());
                element("Title", ftStyle.getTitle());
                element("Abstract", ftStyle.getAbstract());
                handleLegendURL(layer.getName(), layer.getLegend(), null);
                end("Style");

                Set<StyleInfo> styles = layer.getStyles();

                for (StyleInfo styleInfo : styles) {
                    try {
                        ftStyle = styleInfo.getStyle();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    start("Style");
                    element("Name", styleInfo.getName());
                    element("Title", ftStyle.getTitle());
                    element("Abstract", ftStyle.getAbstract());
                    handleLegendURL(layer.getName(), null, styleInfo);
                    end("Style");
                }
            }
        }

        private void handleDimensions(final LayerInfo layer) {
            String timeMetadataExtent = null;
            String elevationMetadataExtent = null;
            if (layer.getType() == Type.RASTER) {
                CoverageInfo cvinfo = ((CoverageInfo) layer.getResource());

                if (cvinfo == null)
                    throw new RuntimeException("Unable to acquire coverage resource for layer: "
                            + layer.getName());

                Catalog catalog = cvinfo.getCatalog();

                if (catalog == null)
                    throw new RuntimeException("Unable to acquire catalog resource for layer: "
                            + layer.getName());

                CoverageStoreInfo csinfo = cvinfo.getStore();

                if (csinfo == null)
                    throw new RuntimeException(
                            "Unable to acquire coverage store resource for layer: "
                                    + layer.getName());

                AbstractGridCoverage2DReader reader = null;
                try {
                    reader = (AbstractGridCoverage2DReader) catalog.getResourcePool()
                            .getGridCoverageReader(csinfo, GeoTools.getDefaultHints());
                } catch (Throwable t) {
                    LOGGER.severe("Unable to acquire a reader for this coverage with format: "
                            + csinfo.getFormat().getName());
                }

                if (reader == null)
                    throw new RuntimeException(
                            "Unable to acquire a reader for this coverage with format: "
                                    + csinfo.getFormat().getName());

                final String[] metadataNames = reader.getMetadataNames();

                if (metadataNames != null && metadataNames.length > 0) {
                    // TIME DIMENSION
                    timeMetadataExtent = reader.getMetadataValue("TIME_DOMAIN");

                    if (timeMetadataExtent != null) {
                        AttributesImpl timeDim = new AttributesImpl();
                        timeDim.addAttribute("", "name", "name", "", "time");
                        timeDim.addAttribute("", "units", "units", "", "ISO8601");
                        timeDim.addAttribute("", "current", "current", "", "true");
                        timeDim.addAttribute("", "default", "default", "", "current");
                        element("Dimension", timeMetadataExtent, timeDim);
                    }

                    // ELEVATION DIMENSION
                    elevationMetadataExtent = reader.getMetadataValue("ELEVATION_DOMAIN");
                    final String[] elevationLevels = elevationMetadataExtent.split(",");
                    if (elevationMetadataExtent != null) {
                        AttributesImpl elevDim = new AttributesImpl();
                        elevDim.addAttribute("", "name", "name", "", "elevation");
                        elevDim.addAttribute("", "units", "units", "", "EPSG:5030");
                        elevDim.addAttribute("", "default", "default", "", elevationLevels[0]);
                        element("Dimension", elevationMetadataExtent, elevDim);
                    }
                }
            }
        }

        /**
         * Returns the layer hop count if the layer is cascaded
         * <p>
         * TODO: the geotools wms Layer object does not hold information on the cascaded status of
         * layers, so in order to correctly implement this we need to gather that information in the
         * geotools wms module. For now this method just returns {@code 1} is layer is a
         * {@link WMSLayerInfo}
         * </p>
         */
        private int getCascadedHopCount(final LayerInfo layer) {
            if (layer instanceof WMSLayerInfo) {
                return 1;
            }
            return 0;
        }

        /**
         * Returns true if the layer can be queried
         */
        boolean isLayerQueryable(LayerInfo layer) {
            try {
                if (layer.getResource() instanceof WMSLayerInfo) {
                    WMSLayerInfo info = (WMSLayerInfo) layer.getResource();
                    Layer wl = info.getWMSLayer(null);
                    if (!wl.isQueryable()) {
                        return false;
                    }
                    WMSCapabilities caps = info.getStore().getWebMapServer(null).getCapabilities();
                    if (!caps.getRequest().getGetFeatureInfo().getFormats()
                            .contains("application/vnd.ogc.gml")) {
                        return false;
                    }
                }
                // all other layers are queryable
                return true;
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE,
                        "Failed to determin if the layer is queryable, assuming it's not", e);
                return false;
            }
        }

        boolean isLayerQueryable(LayerGroupInfo layerGroup) {
            for (LayerInfo layer : layerGroup.getLayers()) {
                if (!isLayerQueryable(layer)) {
                    return false;
                }
            }
            return true;
        }

        protected void handleLayerGroups(List<LayerGroupInfo> layerGroups) throws FactoryException,
                TransformException {
            if (layerGroups == null || layerGroups.size() == 0) {
                return;
            }

            Collections.sort(layerGroups, new Comparator<LayerGroupInfo>() {
                public int compare(LayerGroupInfo o1, LayerGroupInfo o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });

            for (LayerGroupInfo layerGroup : layerGroups) {
                String layerName = layerGroup.getName();

                AttributesImpl qatts = new AttributesImpl();
                boolean queryable = isLayerQueryable(layerGroup);
                qatts.addAttribute("", "queryable", "queryable", "", queryable ? "1" : "0");
                start("Layer", qatts);
                element("Name", layerName);
                element("Title", layerName);
                element("Abstract", "Layer-Group type layer: " + layerName);

                final ReferencedEnvelope layerGroupBounds = layerGroup.getBounds();
                final ReferencedEnvelope latLonBounds = layerGroupBounds.transform(
                        DefaultGeographicCRS.WGS84, true);

                String authority = layerGroupBounds.getCoordinateReferenceSystem().getIdentifiers()
                        .toArray()[0].toString();

                element("CRS", authority);

                handleGeographicBoundingBox(latLonBounds);
                handleBBox(layerGroupBounds, authority);

                // the layer style is not provided since the group does just have
                // one possibility, the lack of styles that will make it use
                // the default ones for each layer

                end("Layer");
            }
        }

        protected void handleAttribution(LayerInfo layer) {
            AttributionInfo attribution = layer.getAttribution();

            String title = attribution.getTitle();
            String url = attribution.getHref();
            String logoURL = attribution.getLogoURL();
            String logoType = attribution.getLogoType();
            int logoWidth = attribution.getLogoWidth();
            int logoHeight = attribution.getLogoHeight();

            boolean titleGood = (title != null), urlGood = (url != null), logoGood = (logoURL != null
                    && logoType != null && logoWidth > 0 && logoHeight > 0);

            if (titleGood || urlGood || logoGood) {
                start("Attribution");
                if (titleGood)
                    element("Title", title);

                if (urlGood) {
                    AttributesImpl urlAttributes = new AttributesImpl();
                    urlAttributes.addAttribute("", "xmlns:xlink", "xmlns:xlink", "", XLINK_NS);
                    urlAttributes.addAttribute(XLINK_NS, "type", "xlink:type", "", "simple");
                    urlAttributes.addAttribute(XLINK_NS, "href", "xlink:href", "", url);
                    element("OnlineResource", null, urlAttributes);
                }

                if (logoGood) {
                    AttributesImpl logoAttributes = new AttributesImpl();
                    logoAttributes.addAttribute("", "", "height", "", "" + logoHeight);
                    logoAttributes.addAttribute("", "", "width", "", "" + logoWidth);
                    start("LogoURL", logoAttributes);
                    element("Format", logoType);

                    AttributesImpl urlAttributes = new AttributesImpl();
                    urlAttributes.addAttribute("", "xmlns:xlink", "xmlns:xlink", "", XLINK_NS);
                    urlAttributes.addAttribute(XLINK_NS, "type", "xlink:type", "", "simple");
                    urlAttributes.addAttribute(XLINK_NS, "href", "xlink:href", "", logoURL);

                    element("OnlineResource", null, urlAttributes);
                    end("LogoURL");
                }

                end("Attribution");
            }
        }

        /**
         * Writes layer LegendURL pointing to the user supplied icon URL, if any, or to the proper
         * GetLegendGraphic operation if an URL was not supplied by configuration file.
         * 
         * <p>
         * It is common practice to supply a URL to a WMS accesible legend graphic when it is
         * difficult to create a dynamic legend for a layer.
         * </p>
         * 
         * @param ft
         *            The FeatureTypeInfo that holds the legendURL to write out, or<code>null</code>
         *            if dynamically generated.
         * 
         * @task TODO: figure out how to unhack legend parameters such as WIDTH, HEIGHT and FORMAT
         */
        protected void handleLegendURL(String layerName, LegendInfo legend, StyleInfo style) {
            if (legend != null) {
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.fine("using user supplied legend URL");
                }

                AttributesImpl attrs = new AttributesImpl();
                attrs.addAttribute("", "width", "width", "", String.valueOf(legend.getWidth()));
                attrs.addAttribute("", "height", "height", "", String.valueOf(legend.getHeight()));

                start("LegendURL", attrs);

                element("Format", legend.getFormat());
                attrs.clear();
                attrs.addAttribute("", "xmlns:xlink", "xmlns:xlink", "", XLINK_NS);
                attrs.addAttribute(XLINK_NS, "type", "xlink:type", "", "simple");
                attrs.addAttribute(XLINK_NS, "href", "xlink:href", "", legend.getOnlineResource());

                element("OnlineResource", null, attrs);

                end("LegendURL");
            } else {
                String defaultFormat = GetLegendGraphicRequest.DEFAULT_FORMAT;

                if (null == wmsConfig.getLegendGraphicOutputFormat(defaultFormat)) {
                    if (LOGGER.isLoggable(Level.WARNING)) {
                        LOGGER.warning(new StringBuffer("Default legend format (")
                                .append(defaultFormat)
                                .append(")is not supported (jai not available?), can't add LegendURL element")
                                .toString());
                    }

                    return;
                }

                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.fine("Adding GetLegendGraphic call as LegendURL");
                }

                AttributesImpl attrs = new AttributesImpl();
                attrs.addAttribute("", "width", "width", "",
                        String.valueOf(GetLegendGraphicRequest.DEFAULT_WIDTH));

                // DJB: problem here is that we do not know the size of the
                // legend apriori - we need
                // to make one and find its height. Not the best way, but it
                // would work quite well.
                // This was advertising a 20*20 icon, but actually producing
                // ones of a different size.
                // An alternative is to just scale the resulting icon to what
                // the server requested, but this isnt
                // the nicest thing since warped images dont look nice. The
                // client should do the warping.

                // however, to actually estimate the size is a bit difficult.
                // I'm going to do the scaling
                // so it obeys the what the request says. For people with a
                // problem with that should consider
                // changing the default size here so that the request is for the
                // correct size.
                attrs.addAttribute("", "height", "height", "",
                        String.valueOf(GetLegendGraphicRequest.DEFAULT_HEIGHT));

                start("LegendURL", attrs);

                element("Format", defaultFormat);
                attrs.clear();

                Map<String, String> params = params("request", "GetLegendGraphic", "format",
                        defaultFormat, "width",
                        String.valueOf(GetLegendGraphicRequest.DEFAULT_WIDTH), "height",
                        String.valueOf(GetLegendGraphicRequest.DEFAULT_HEIGHT), "layer", layerName);
                if (style != null) {
                    params.put("style", style.getName());
                }
                String legendURL = buildURL(request.getBaseUrl(), "ows", params, URLType.SERVICE);

                attrs.addAttribute("", "xmlns:xlink", "xmlns:xlink", "", XLINK_NS);
                attrs.addAttribute(XLINK_NS, "type", "xlink:type", "", "simple");
                attrs.addAttribute(XLINK_NS, "href", "xlink:href", "", legendURL);
                element("OnlineResource", null, attrs);

                end("LegendURL");
            }
        }

        /**
         * Encodes a LatLonBoundingBox for the given Envelope.
         * 
         * @param bbox
         */
        private void handleGeographicBoundingBox(Envelope bbox) {
            String minx = String.valueOf(bbox.getMinX());
            String miny = String.valueOf(bbox.getMinY());
            String maxx = String.valueOf(bbox.getMaxX());
            String maxy = String.valueOf(bbox.getMaxY());

            start("EX_GeographicBoundingBox");
            element("westBoundLongitude", minx);
            element("eastBoundLongitude", maxx);
            element("southBoundLatitude", miny);
            element("northBoundLatitude", maxy);
            end("EX_GeographicBoundingBox");
        }

        /**
         * adds a comment to the output xml file. THIS IS A BIG HACK. TODO: do this in the correct
         * manner!
         * 
         * @param comment
         */
        public void comment(String comment) {
            if (contentHandler instanceof TransformerIdentityImpl) // HACK HACK
                                                                   // HACK --
                                                                   // not sure
                                                                   // of the
                                                                   // proper
                                                                   // way to do
                                                                   // this.
            {
                try {
                    TransformerIdentityImpl ch = (TransformerIdentityImpl) contentHandler;
                    ch.comment(comment.toCharArray(), 0, comment.length());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * Encodes a BoundingBox for the given Envelope.
         * 
         * @param bbox
         */
        private void handleBBox(Envelope bbox, String CRS) {
            String minx = String.valueOf(bbox.getMinX());
            String miny = String.valueOf(bbox.getMinY());
            String maxx = String.valueOf(bbox.getMaxX());
            String maxy = String.valueOf(bbox.getMaxY());

            AttributesImpl bboxAtts = attributes("CRS", CRS, //
                    "minx", minx, //
                    "miny", miny,//
                    "maxx", maxx,//
                    "maxy", maxy);

            element("BoundingBox", null, bboxAtts);
        }
    }
}
