/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.wfs.kvp.v2_0;

import net.opengis.wfs20.Wfs20Factory;

import org.geoserver.catalog.Catalog;
import org.geoserver.wfs.RequestObjectHandler;
import org.geoserver.wfs.kvp.GetFeatureKvpRequestReaderBase;
import org.opengis.filter.FilterFactory;


public class GetFeatureKvpRequestReader extends GetFeatureKvpRequestReaderBase {

    public GetFeatureKvpRequestReader(Class requestBean, Catalog catalog, FilterFactory filterFactory) {
        super(requestBean, Wfs20Factory.eINSTANCE, catalog, filterFactory, new RequestObjectHandler.WFS_20());
    }
}
