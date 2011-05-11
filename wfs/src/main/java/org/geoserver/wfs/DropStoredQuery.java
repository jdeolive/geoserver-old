/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.wfs;

import net.opengis.wfs20.DropStoredQueryType;
import net.opengis.wfs20.ExecutionStatusType;
import net.opengis.wfs20.Wfs20Factory;

import org.geoserver.platform.GeoServerExtensions;
import org.springframework.context.ApplicationContext;

/**
 * Web Feature Service DropStoredQuery operation.
 *
 * @author Justin Deoliveira, OpenGeo
 *
 * @version $Id$
 */
public class DropStoredQuery {

    /** service config */
    WFSInfo wfs;
    
    /** app context for looking up stored query providers */
    ApplicationContext appContext;
    
    public DropStoredQuery(WFSInfo wfs, ApplicationContext appContext) {
        this.wfs = wfs;
        this.appContext = appContext;
    }
    
    public ExecutionStatusType run(DropStoredQueryType request) throws WFSException {
        
        if (request.getId() == null) {
            throw new WFSException("No stored query id specified");
        }
        
        StoredQuery query = null;
        for (StoredQueryProvider provider : 
            GeoServerExtensions.extensions(StoredQueryProvider.class)) {
            
            query = provider.getStoredQuery(request.getId());
            if (query != null) {
                provider.removeStoredQuery(query);
                break;
            }
        }
        
        if (query == null) {
            throw new WFSException(String.format("Stored query %s does not exist.", request.getId()));
        }
        
        Wfs20Factory factory = Wfs20Factory.eINSTANCE;
        ExecutionStatusType response = factory.createExecutionStatusType();
        response.setStatus("OK");
        return response;
    }
}
