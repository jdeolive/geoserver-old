/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.wfs;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geoserver.platform.GeoServerResourceLoader;
import org.geotools.util.logging.Logging;
import org.geotools.wfs.v2_0.WFS;
import org.geotools.wfs.v2_0.WFSConfiguration;
import org.geotools.xml.Encoder;
import org.geotools.xml.Parser;

import net.opengis.wfs20.StoredQueryDescriptionType;

/**
 * Extension point for WFS stored queries.
 * 
 * @author Justin Deoliveira, OpenGeo
 *
 */
public class StoredQueryProvider {
    
    /** logger */
    static Logger LOGGER = Logging.getLogger(StoredQueryProvider.class);
    
    /** file system access */
    GeoServerResourceLoader loader;
    
    public StoredQueryProvider(GeoServerResourceLoader loader) {
        this.loader = loader;
    }

    /**
     * The language/type of stored query the provider handles. 
     */
    public String getLanguage() {
        return "urn:ogc:def:queryLanguage:OGC-WFS::WFS_QueryExpression";
    }

    /**
     * Lists all the stored queries provided.
     */
    public List<StoredQuery> listStoredQueries() {
        Parser p = new Parser(new WFSConfiguration());
        
        List<StoredQuery> queries = new ArrayList();
        try {
            File dir = storedQueryDir();
            for (String f : dir.list()) {
                try {
                    queries.add(parseStoredQuery(new File(dir, f), p));
                } 
                catch(Exception e) {
                    LOGGER.log(Level.WARNING, "Error occured parsing stored query: "+f, e);
                }
            }
        } 
        catch (IOException e) {
            throw new RuntimeException("i/o error listing stored queries", e);
        }
        
        return queries;
    }


    /**
     * Creates a new stored query.
     * 
     * @param def The stored query definition.
     */
    public StoredQuery createStoredQuery(StoredQueryDescriptionType query) {
        StoredQuery sq = new StoredQuery(query);
        putStoredQuery(sq);
        return sq;
    }

    /**
     * Removes an existing stored query.
     * 
     * @param query The stored query
     */
    public void removeStoredQuery(StoredQuery query) {
        try {
            File f = new File(storedQueryDir(), query.getName()+".xml");
            if (f.exists()) {
                f.delete();
            }
        }
        catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    
    /**
     * Retrieves a stored query by name.
     *  
     * @param name Identifying name of the stored query.
     */
    public StoredQuery getStoredQuery(String name) {
        try {
            File dir = storedQueryDir();
            File f = new File(dir, name + ".xml");
            
            if (!f.exists()) {
                return null;
            }
            
            return parseStoredQuery(f);
        } 
        catch(Exception e) {
            throw new RuntimeException("Error accessign stoed query: " + name, e);
        }
    }

    /**
     * Persists a stored query, overwriting it if the query already exists. 
     *  
     * @param query The stored query.
     */
    public void putStoredQuery(StoredQuery query) {
        try {
            File dir = storedQueryDir();
            File f = new File(dir, query.getName()+".xml");
            if (f.exists()) {
                //TODO: back up the old file in case there is an error during encoding
            }
            
            BufferedOutputStream bout = new BufferedOutputStream(new FileOutputStream(f));
            try {
                Encoder e = new Encoder(new WFSConfiguration());
                e.setRootElementType(WFS.StoredQueryDescriptionType);
                e.encode(query.getQuery(), WFS.StoredQueryDescription, new BufferedOutputStream(bout));
                bout.flush();
            }
            finally {
                bout.close();
            }
        }
        catch(IOException e) {
            throw new RuntimeException("i/o error listing stored queries", e);
        }
    }
    
    File storedQueryDir() throws IOException {
        return loader.findOrCreateDirectory("wfs", "query");
    }
    
    StoredQuery parseStoredQuery(File file) throws Exception {
        return parseStoredQuery(file, new Parser(new WFSConfiguration()));
    }
    
    StoredQuery parseStoredQuery(File file, Parser p) throws Exception {
        p.setRootElementType(WFS.StoredQueryDescriptionType);
        FileInputStream fin = new FileInputStream(file);
        try {
            StoredQueryDescriptionType q = 
                (StoredQueryDescriptionType) p.parse(new BufferedInputStream(fin));
            return createStoredQuery(q);
        } 
        finally {
            fin.close();
        }
    }
}
