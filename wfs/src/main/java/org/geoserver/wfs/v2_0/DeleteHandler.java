/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.wfs.v2_0;

import net.opengis.wfs20.DeleteType;

import org.geoserver.config.GeoServer;
import org.geoserver.wfs.DeleteElementHandler;

public class DeleteHandler extends DeleteElementHandler {

    public DeleteHandler(GeoServer gs) {
        super(gs, DeleteType.class);
    }

}
