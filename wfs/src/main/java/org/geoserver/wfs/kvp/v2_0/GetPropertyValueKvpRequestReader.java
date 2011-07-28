/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.wfs.kvp.v2_0;

import java.util.Map;

import net.opengis.wfs20.GetFeatureType;
import net.opengis.wfs20.GetPropertyValueType;
import net.opengis.wfs20.Wfs20Factory;

import org.geoserver.catalog.Catalog;
import org.geoserver.ows.kvp.EMFKvpRequestReader;
import org.geoserver.wfs.WFSException;
import org.opengis.filter.FilterFactory;

public class GetPropertyValueKvpRequestReader extends EMFKvpRequestReader {

    GetFeatureKvpRequestReader delegate;
    
    public GetPropertyValueKvpRequestReader(Catalog catalog, FilterFactory filterFactory) {
        super(GetPropertyValueType.class, Wfs20Factory.eINSTANCE);
        delegate = new GetFeatureKvpRequestReader(GetFeatureType.class, catalog, filterFactory);
    }
    
    @Override
    public Object read(Object request, Map kvp, Map rawKvp) throws Exception {
        GetPropertyValueType gpv = (GetPropertyValueType) super.read(request, kvp, rawKvp);
        
        //parse a GetFeature and copy the query
        GetFeatureType gf = Wfs20Factory.eINSTANCE.createGetFeatureType();
        delegate.read(gf, kvp, rawKvp);

        if (gf.getAbstractQueryExpression().isEmpty()) {
            throw new WFSException(gpv, "Request did not specify a query");
        }

        gpv.setAbstractQueryExpression(gf.getAbstractQueryExpression().get(0));
        return gpv;
    }

}
