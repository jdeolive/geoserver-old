/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.wfs.kvp;

import net.opengis.wfs.WfsFactory;

import org.geoserver.catalog.Catalog;
import org.geoserver.wfs.RequestObjectHandler;
import org.opengis.filter.FilterFactory;


public class GetFeatureKvpRequestReader extends GetFeatureKvpRequestReaderBase {

    public GetFeatureKvpRequestReader(Class requestBean, Catalog catalog, FilterFactory filterFactory) {
        super(requestBean, WfsFactory.eINSTANCE, catalog, filterFactory, new RequestObjectHandler.WFS_11());
    }
}
