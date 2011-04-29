/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.wfs.kvp;

import java.util.Map;

import org.eclipse.emf.ecore.EFactory;
import org.geoserver.wfs.RequestObjectHandler;

public class GetCapabilitiesKvpRequestReaderBase extends WFSKvpRequestReader {

    RequestObjectHandler handler;
    
    public GetCapabilitiesKvpRequestReaderBase(Class requestBean, EFactory factory, RequestObjectHandler handler) {
        super(requestBean, factory);
        this.handler = handler;
    }
    
    @Override
    public Object read(Object request, Map kvp, Map rawKvp) throws Exception {
        request = super.read(request, kvp, rawKvp);

        //set the version attribute on the request
        if (kvp.containsKey("version")) {
            handler.setAcceptVersions(request, (String)kvp.get("version"));
        }

        return request;
    }
}
