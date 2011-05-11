/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.wfs;

import java.util.List;

import net.opengis.wfs20.StoredQueryDescriptionType;

/**
 * Extension point for WFS stored queries.
 * 
 * @author Justin Deoliveira, OpenGeo
 *
 */
public interface StoredQueryProvider<T extends StoredQuery> {

    /**
     * The language/type of stored query the provider handles. 
     */
    String getLanguage();

    /**
     * Lists all the stored queries provided.
     */
    List<T> listStoredQueries();

    /**
     * Creates a new stored query.
     * 
     * @param def The stored query definition.
     */
    T createStoredQuery(StoredQueryDescriptionType def);

    /**
     * Removes an existing stored query.
     * 
     * @param query The stored query
     */
    void removeStoredQuery(T query);
    
    /**
     * Retrieves a stored query by name.
     *  
     * @param name Identifying name of the stored query.
     */
    T getStoredQuery(String name);
    
    /**
     * Persists a stored query, overwriting it if the query already exists. 
     *  
     * @param query The stored query.
     */
    void putStoredQuery(T query);
}
