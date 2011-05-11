/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.wfs;

import org.geoserver.platform.GeoServerExtensions;
import org.springframework.context.ApplicationContext;

import net.opengis.wfs20.CreateStoredQueryResponseType;
import net.opengis.wfs20.CreateStoredQueryType;
import net.opengis.wfs20.StoredQueryDescriptionType;
import net.opengis.wfs20.Wfs20Factory;

/**
 * Web Feature Service CreateStoredQuery operation.
 *
 * @author Justin Deoliveira, OpenGeo
 *
 * @version $Id$
 */
public class CreateStoredQuery {

    /** service config */
    WFSInfo wfs;
    
    /** app context for looking up stored query providers */
    ApplicationContext appContext;
    
    public CreateStoredQuery(WFSInfo wfs, ApplicationContext appContext) {
        this.wfs = wfs;
        this.appContext = appContext;
    }
    
    public CreateStoredQueryResponseType run(CreateStoredQueryType request) throws WFSException {
        for (StoredQueryDescriptionType sqd : request.getStoredQueryDefinition()) {
            validateStoredQuery(sqd);
            
            StoredQueryProvider provider = lookupStoredQueryProvider(sqd);
            try {
                provider.createStoredQuery(sqd);
            }
            catch(Exception e) {
                throw new WFSException("Error occured creating stored query", e);
            }
        }

        Wfs20Factory factory = Wfs20Factory.eINSTANCE;
        CreateStoredQueryResponseType response = factory.createCreateStoredQueryResponseType();
        response.setStatus("OK");
        return response;
    }

    void validateStoredQuery(StoredQueryDescriptionType sq) throws WFSException {
        if (sq.getQueryExpressionText().isEmpty()) {
            throw new WFSException("Stored query does not specify any queries");
        }
        String language = sq.getQueryExpressionText().get(0).getLanguage();
        for (int i = 1; i < sq.getQueryExpressionText().size(); i++) {
            if (!language.equals(sq.getQueryExpressionText().get(i).getLanguage())) {
                throw new WFSException("Stored query specifies queries with multiple languages. " +
                    "Not supported");
            }
        }
    }

    StoredQueryProvider lookupStoredQueryProvider(StoredQueryDescriptionType sq) throws WFSException {
        String lang = sq.getQueryExpressionText().get(0).getLanguage();
        for (StoredQueryProvider<?> sqp : GeoServerExtensions.extensions(StoredQueryProvider.class)) {
            if (sqp.getLanguage().equals(lang)) {
                return sqp;
            }
        }
        throw new WFSException(String.format("Stored query language %s is not supported", lang));
    }
}
