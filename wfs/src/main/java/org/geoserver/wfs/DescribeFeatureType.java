/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.wfs;

import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import net.opengis.wfs.DescribeFeatureTypeType;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.catalog.NamespaceInfo;


/**
 * Web Feature Service DescribeFeatureType operation.
 * <p>
 * This operation returns an array of  {@link org.geoserver.data.feature.FeatureTypeInfo} metadata
 * objects corresponding to the feature type names specified in the request.
 * </p>
 *
 * @author Rob Hranac, TOPP
 * @author Chris Holmes, TOPP
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 * @version $Id$
 */
public class DescribeFeatureType {
    /**
    * Catalog reference
    */
    private Catalog catalog;

    /**
     * WFS service
     */
    private WFSInfo wfs;
    
    /**
     * Request object handler
     */
    private RequestObjectHandler handler;
    
    /**
         * Creates a new wfs 1.0/1.1 DescribeFeatureType operation.
         *
         * @param wfs The wfs configuration
         * @param catalog The geoserver catalog.
         */
    public DescribeFeatureType(WFSInfo wfs, Catalog catalog) {
        this(wfs, catalog, new RequestObjectHandler.WFS_11());
    }
    
    /**
     * Creates a new wfs DescribeFeatureType operation specifying the request object handler.
     */
    public DescribeFeatureType(WFSInfo wfs, Catalog catalog, RequestObjectHandler handler) {
        this.catalog = catalog;
        this.wfs = wfs;
        this.handler = handler;
    }

    public WFSInfo getWFS() {
        return wfs;
    }

    public void setWFS(WFSInfo wfs) {
        this.wfs = wfs;
    }

    public Catalog getCatalog() {
        return catalog;
    }

    public void setCatalog(Catalog catalog) {
        this.catalog = catalog;
    }

    public FeatureTypeInfo[] run(Object request)
        throws WFSException {
        List<QName> names = new ArrayList<QName>(handler.getTypeNames(request));

        final boolean citeConformance = getWFS().isCiteCompliant();
        if (!citeConformance) {
            // HACK: as per GEOS-1816, if strict cite compliance is not set, and
            // the user specified a typeName with no namespace prefix, we want
            // it to be interpreted as being in the GeoServer's "default
            // namespace". Yet, the xml parser did its job and since TypeName is
            // of QName type, not having a ns prefix means it got parsed as a
            // QName in the default namespace. That is, in the wfs namespace.
            List<QName> hackedNames = new ArrayList<QName>(names.size());
            final NamespaceInfo defaultNameSpace = catalog.getDefaultNamespace();
            if (defaultNameSpace == null) {
                throw new IllegalStateException("No default namespace configured in GeoServer");
            }
            final String defaultNsUri = defaultNameSpace.getURI();
            for (QName name : names) {
                String nsUri = name.getNamespaceURI();
                if (XMLConstants.NULL_NS_URI.equals(nsUri)
                        || org.geoserver.wfs.xml.v1_1_0.WFS.NAMESPACE.equals(nsUri)
                        || org.geotools.wfs.v2_0.WFS.NAMESPACE.equals(nsUri)) {
                    // for this one we need to assign the default geoserver
                    // namespace
                    name = new QName(defaultNsUri, name.getLocalPart());
                }
                hackedNames.add(name);
            }
            names = hackedNames;
        }

        //list of catalog handles
        List<FeatureTypeInfo> requested = new ArrayList<FeatureTypeInfo>(names.size());

        if (names.isEmpty()) {
            // if there are no specific requested types then get all the ones that
            // are enabled
            for (FeatureTypeInfo ftInfo : new ArrayList<FeatureTypeInfo>(catalog.getFeatureTypes())) {
                if (ftInfo.enabled()) {
                    requested.add(ftInfo);
                }
            }
        } else {
            for (QName name : names) {

                String namespaceURI = name.getNamespaceURI();
                String typeName = name.getLocalPart();
                FeatureTypeInfo typeInfo;
                if (citeConformance && XMLConstants.NULL_NS_URI.equals(namespaceURI)) {
                    // under cite conformance, the typeName shall be completely resolved. If there's
                    // no namespace URI and we ask the catalog with only the localName, the catalog
                    // will try to match against the default namespace
                    typeInfo = null;
                } else {
                    typeInfo = catalog.getFeatureTypeByName(namespaceURI, typeName);
                }
               

                if (typeInfo != null && typeInfo.enabled()) {
                    requested.add(typeInfo);
                } else {
                    // not found
                    String msg = "Could not find type: " + name;
                    if (citeConformance) {
                        msg += ". \nStrict WFS protocol conformance is being applied.\n"
                                + "Make sure the type name is correctly qualified";
                    }
                    throw new WFSException(msg);
                }
            }
        }

        return (FeatureTypeInfo[]) requested.toArray(new FeatureTypeInfo[requested.size()]);
    }
}
