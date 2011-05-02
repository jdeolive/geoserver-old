/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.wfs;

import net.opengis.wfs.FeatureCollectionType;
import net.opengis.wfs20.DescribeFeatureTypeType;
import net.opengis.wfs20.GetCapabilitiesType;
import net.opengis.wfs20.GetFeatureType;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.config.GeoServer;
import org.geotools.xml.transform.TransformerBase;

public class DefaultWebFeatureService20 implements WebFeatureService20 {

    /**
     * GeoServer configuration
     */
    protected GeoServer geoServer;

    public DefaultWebFeatureService20(GeoServer geoServer) {
        this.geoServer = geoServer;
    }

    public WFSInfo getServiceInfo() {
        return geoServer.getService(WFSInfo.class);
    }
    
    public Catalog getCatalog() {
        return geoServer.getCatalog();
    }

    public TransformerBase getCapabilities(GetCapabilitiesType request) throws WFSException {
        return new GetCapabilities(getServiceInfo(), getCatalog(), handler()).run(request);
    }
    
    public FeatureTypeInfo[] describeFeatureType(DescribeFeatureTypeType request)
            throws WFSException {
        return new DescribeFeatureType(getServiceInfo(), getCatalog(), handler()).run(request);
    }

    public FeatureCollectionType getFeature(GetFeatureType request) throws WFSException {
        return new GetFeature(getServiceInfo(), getCatalog(), handler()).run(request);
    }
    
    RequestObjectHandler handler() {
        return new RequestObjectHandler.WFS_20();
    }

}
