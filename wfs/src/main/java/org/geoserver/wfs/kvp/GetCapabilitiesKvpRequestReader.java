/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.wfs.kvp;

import net.opengis.wfs.GetCapabilitiesType;
import net.opengis.wfs.WfsFactory;

import org.geoserver.wfs.RequestObjectHandler;


public class GetCapabilitiesKvpRequestReader extends GetCapabilitiesKvpRequestReaderBase {
    public GetCapabilitiesKvpRequestReader() {
        super(GetCapabilitiesType.class, WfsFactory.eINSTANCE, new RequestObjectHandler.WFS_11());
    }
}
