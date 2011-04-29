/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.wfs.kvp.v2_0;

import net.opengis.wfs20.Wfs20Factory;

import org.geoserver.ows.kvp.EMFKvpRequestReader;


/**
 * Web Feature Service 2.0 Key Value Pair Request reader.
 * <p>
 * This request reader makes use of the Eclipse Modelling Framework
 * reflection api.
 * </p>
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class WFSKvpRequestReader extends EMFKvpRequestReader {

    /**
     * Creates the Wfs Kvp Request reader.
     *
     * @param requestBean The request class, which must be an emf class.
     */
    public WFSKvpRequestReader(Class requestBean) {
        super(requestBean, Wfs20Factory.eINSTANCE);
    }
    
    protected Wfs20Factory getWfsFactory() {
        return (Wfs20Factory) factory;
    }
}
