/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.wfs;

import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.wfs.request.Query;
import org.opengis.parameter.Parameter;

public interface StoredQuery {

    /**
     * Uniquely identifying name of the stored query.
     */
    String getName();

    /**
     * Human readable title describing the stored query.
     */
    String getTitle();

    /**
     * The parameters accepted by the stored query.
     */
    List<Parameter<?>> getParameters();

    /**
     * The feature types the stored query returns result for. 
     */
    List<QName> getFeatureTypes();

    /**
     * The workspace this stored query is constrained to.
     */
    WorkspaceInfo getWorkspace();

    List<Query> createQuery(Map<String,Object> parameters);
}
