/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.wfs.kvp.v2_0;

import net.opengis.wfs20.Wfs20Factory;

import org.geoserver.catalog.Catalog;
import org.opengis.filter.FilterFactory;


public class GetFeatureKvpRequestReader extends org.geoserver.wfs.kvp.GetFeatureKvpRequestReader {

    public GetFeatureKvpRequestReader(Class requestBean, Catalog catalog, FilterFactory filterFactory) {
        super(requestBean, Wfs20Factory.eINSTANCE, catalog, filterFactory);
    }
}
