/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.wfs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import net.opengis.wfs20.ParameterExpressionType;
import net.opengis.wfs20.QueryExpressionTextType;
import net.opengis.wfs20.StoredQueryDescriptionType;

import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.wfs.request.Query;
import org.opengis.parameter.Parameter;

public class WFSStoredQuery implements StoredQuery {

    StoredQueryDescriptionType query;
    
    public WFSStoredQuery(StoredQueryDescriptionType query) {
        this.query = query;
    }
    
    public String getName() {
        return query.getId();
    }
    
    public StoredQueryDescriptionType getQuery() {
        return query;
    }

    public String getTitle() {
        if (!query.getTitle().isEmpty()) {
            return query.getTitle().get(0).getValue();
        }
        return null;
    }

    public List<Parameter<?>> getParameters() {
        List<Parameter<?>> params = new ArrayList();
        for (ParameterExpressionType p : query.getParameter()) {
            
        }
        return params;
    }

    public List<QName> getFeatureTypes() {
        List<QName> types = new ArrayList();
        for (QueryExpressionTextType qe : query.getQueryExpressionText()) {
            types.addAll(qe.getReturnFeatureTypes());
        }
        return types;
    }
    
    public WorkspaceInfo getWorkspace() {
        return null;
    }

    public List<Query> createQuery(Map<String, Object> parameters) {
        // TODO Auto-generated method stub
        return null;
    }

}
