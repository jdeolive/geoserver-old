/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.wfs.kvp.v2_0;

import net.opengis.wfs20.DropStoredQueryType;
import net.opengis.wfs20.Wfs20Factory;

import org.geoserver.wfs.kvp.WFSKvpRequestReader;

public class DropStoredQueryKvpRequestReader extends WFSKvpRequestReader {

    public DropStoredQueryKvpRequestReader() {
        super(DropStoredQueryType.class, Wfs20Factory.eINSTANCE);
    }
    
}
