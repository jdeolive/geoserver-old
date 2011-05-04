/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.wfs.v2_0;

import net.opengis.wfs20.InsertType;

import org.geoserver.config.GeoServer;
import org.geoserver.wfs.InsertElementHandler;
import org.opengis.filter.FilterFactory;

public class InsertHandler extends InsertElementHandler {

    public InsertHandler(GeoServer gs, FilterFactory filterFactory) {
        super(gs, filterFactory, InsertType.class);
    }

}
