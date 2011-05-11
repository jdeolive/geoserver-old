/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.wfs;

import java.util.List;

import net.opengis.wfs20.DescribeStoredQueriesResponseType;
import net.opengis.wfs20.DescribeStoredQueriesType;
import net.opengis.wfs20.Wfs20Factory;

import org.geoserver.platform.GeoServerExtensions;
import org.springframework.context.ApplicationContext;

/**
 * Web Feature Service DescribeStoredQueries operation.
 *
 * @author Justin Deoliveira, OpenGeo
 *
 * @version $Id$
 */
public class DescribeStoredQueries {

    /** service config */
    WFSInfo wfs;
    
    /** app context for looking up stored query providers */
    ApplicationContext appContext;
    
    public DescribeStoredQueries(WFSInfo wfs, ApplicationContext appContext) {
        this.wfs = wfs;
        this.appContext = appContext;
    }
    
    public DescribeStoredQueriesResponseType run(DescribeStoredQueriesType request) throws WFSException {
        
        Wfs20Factory factory = Wfs20Factory.eINSTANCE;
        DescribeStoredQueriesResponseType response = 
            factory.createDescribeStoredQueriesResponseType();
        
        List<StoredQueryProvider> providers = 
            GeoServerExtensions.extensions(StoredQueryProvider.class);
        
        if (request.getStoredQueryId().isEmpty()) {
            for (StoredQueryProvider provider : providers) {
                for (StoredQuery query : (List<StoredQuery>) provider.listStoredQueries()) {
                    describeStoredQuery(query, response);
                }
            }
        }
        else {
            for (String id : request.getStoredQueryId()) {
                StoredQuery query = null;
                for (StoredQueryProvider provider : providers) {
                    query = provider.getStoredQuery(id);
                    if (query != null) {
                        break;
                    }
                }

                if (query == null) {
                    throw new WFSException("No such stored query: " + id);
                }

                describeStoredQuery(query, response);
            }
        }
        

        return response;
    }

    void describeStoredQuery(StoredQuery query, DescribeStoredQueriesResponseType response) {
        //shortcut
        if (query instanceof WFSStoredQuery) {
            response.getStoredQueryDescription().add(((WFSStoredQuery) query).getQuery());
        }
        else {
            //TODO
        }
        
    }
}
