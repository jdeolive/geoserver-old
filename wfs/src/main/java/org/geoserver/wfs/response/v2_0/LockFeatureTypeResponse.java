/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.wfs.response.v2_0;

import static org.geoserver.ows.util.ResponseUtils.buildSchemaURL;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import net.opengis.wfs20.LockFeatureResponseType;
import net.opengis.wfs20.LockFeatureType;

import org.geoserver.config.GeoServer;
import org.geoserver.platform.Operation;
import org.geoserver.platform.ServiceException;
import org.geoserver.wfs.response.WFSResponse;
import org.geotools.wfs.v2_0.WFS;
import org.geotools.wfs.v2_0.WFSConfiguration;
import org.geotools.xml.Encoder;

public class LockFeatureTypeResponse extends WFSResponse {
    
    public LockFeatureTypeResponse(GeoServer gs) {
        super(gs, LockFeatureResponseType.class);
    }

    public String getMimeType(Object value, Operation operation)
        throws ServiceException {
        return "text/xml";
    }

    public void write(Object value, OutputStream output, Operation operation)
        throws IOException, ServiceException {
        Encoder encoder = new Encoder(new WFSConfiguration());
        encoder.setEncoding(Charset.forName( getInfo().getGeoServer().getGlobal().getCharset()) );
        
        LockFeatureType req = (LockFeatureType)operation.getParameters()[0];
        encoder.setSchemaLocation(org.geoserver.wfs.xml.v1_1_0.WFS.NAMESPACE,
            buildSchemaURL(req.getBaseUrl(), "schemas/wfs/2.0/wfs.xsd"));

        encoder.encode(value, WFS.LockFeatureResponse, output);
    }
}
