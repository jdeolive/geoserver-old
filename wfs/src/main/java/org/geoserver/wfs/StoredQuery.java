/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.wfs;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import net.opengis.wfs20.ParameterType;
import net.opengis.wfs20.QueryExpressionTextType;
import net.opengis.wfs20.QueryType;
import net.opengis.wfs20.StoredQueryDescriptionType;
import net.opengis.wfs20.StoredQueryType;

import org.geoserver.catalog.WorkspaceInfo;
import org.geotools.wfs.v2_0.WFSConfiguration;
import org.geotools.xml.Parser;
import org.opengis.parameter.Parameter;

public class StoredQuery {

    StoredQueryDescriptionType queryDef;
    
    public StoredQuery(StoredQueryDescriptionType query) {
        this.queryDef = query;
    }
    
    /**
     * Uniquely identifying name of the stored query.
     */
    public String getName() {
        return queryDef.getId();
    }

    /**
     * Human readable title describing the stored query.
     */
    public String getTitle() {
        if (!queryDef.getTitle().isEmpty()) {
            return queryDef.getTitle().get(0).getValue();
        }
        return null;
    }

    /**
     * The feature types the stored query returns result for. 
     */
    public List<QName> getFeatureTypes() {
        List<QName> types = new ArrayList();
        for (QueryExpressionTextType qe : queryDef.getQueryExpressionText()) {
            types.addAll(qe.getReturnFeatureTypes());
        }
        return types;
    }

    public StoredQueryDescriptionType getQuery() {
        return queryDef;
    }

    public List<QueryType> compile(StoredQueryType query) {
        QueryExpressionTextType qe = queryDef.getQueryExpressionText().get(0);
        
        //do the parameter substitution
        StringBuffer sb = new StringBuffer(qe.getValue());
        for (ParameterType p : query.getParameter()) {
            String name = p.getName();
            String token = "${" + name + "}";
            int i = sb.indexOf(token);
            while(i > 0) {
                sb.replace(i, i + token.length(), p.getValue());
                i = sb.indexOf(token, i + token.length());
            }
        }
        
        //parse
        Parser p = new Parser(new WFSConfiguration());
        try {
            QueryType compiled = 
                (QueryType) p.parse(new ByteArrayInputStream(sb.toString().getBytes()));
            List l = new ArrayList();
            l.add(compiled);
            return l;
        } 
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

}
