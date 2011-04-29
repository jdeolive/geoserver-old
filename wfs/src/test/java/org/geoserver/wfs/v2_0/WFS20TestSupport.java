/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.wfs.v2_0;

import java.util.Map;

import org.geoserver.wfs.WFSTestSupport;

public class WFS20TestSupport extends WFSTestSupport {

    @Override
    protected void setUpNamespaces(Map<String, String> namespaces) {
        //override some namespaces
        namespaces.put("wfs", "http://www.opengis.net/wfs/2.0");
        namespaces.put("ows", "http://www.opengis.net/ows/1.1");
        namespaces.put("fes", "http://www.opengis.net/fes/2.0");
    }
}
