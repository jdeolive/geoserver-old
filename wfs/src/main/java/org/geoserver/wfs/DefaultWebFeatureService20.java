/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.wfs;

import net.opengis.wfs20.GetCapabilitiesType;

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

    public TransformerBase getCapabilities(GetCapabilitiesType request) throws WFSException {
        return new GetCapabilities(getServiceInfo(), geoServer.getCatalog()).run(request);
    }

    RequestObjectHandler handler() {
        return new RequestObjectHandler.WFS_20();
    }

}
