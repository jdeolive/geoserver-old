/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.wfs.kvp.v2_0;

import java.util.Map;

import org.geoserver.wfs.RequestObjectHandler;

import net.opengis.wfs20.GetCapabilitiesType;

public class GetCapabilitiesKvpRequestReader extends WFSKvpRequestReader {
    public GetCapabilitiesKvpRequestReader() {
        super(GetCapabilitiesType.class);
    }

    public Object read(Object request, Map kvp, Map rawKvp) throws Exception {
        request = super.read(request, kvp, rawKvp);
        
        //set the version attribute on the request
        if (kvp.containsKey("version")) {
            new RequestObjectHandler.WFS_20().setAcceptVersions(request, (String)kvp.get("version"));
        }
        
        return request;
    }
}
