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

import net.opengis.wfs20.StoredQueryDescriptionType;

import org.geoserver.platform.GeoServerResourceLoader;
import org.geotools.util.logging.Logging;
import org.geotools.wfs.v2_0.WFS;
import org.geotools.wfs.v2_0.WFSConfiguration;
import org.geotools.xml.Encoder;
import org.geotools.xml.Parser;

/**
 * Stored query provider that creates/provides a stored query based on a regular WFS query.
 * <p>
 * This is the default stored query provider for WFS and corresponds to the language 
 * <code>urn:ogc:def:queryLanguage:OGC-WFS::WFS_QueryExpression</code> from the WFS 2.0 spec.
 * </p>
 * 
 * @author Justin Deoliveira, OpenGeo
 *
 */
public class WFSStoredQueryProvider implements StoredQueryProvider<WFSStoredQuery> {

    /** logger */
    static Logger LOGGER = Logging.getLogger(WFSStoredQueryProvider.class);
    
    /** file system access */
    GeoServerResourceLoader loader;
    
    public WFSStoredQueryProvider(GeoServerResourceLoader loader) {
        this.loader = loader;
    }
    
    public String getLanguage() {
        return "urn:ogc:def:queryLanguage:OGC-WFS::WFS_QueryExpression";
    }
    
    public WFSStoredQuery createStoredQuery(StoredQueryDescriptionType query) {
        WFSStoredQuery sq = new WFSStoredQuery(query);
        putStoredQuery(sq);
        return sq;
    }
    
    public void removeStoredQuery(WFSStoredQuery query) {
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

    public List<WFSStoredQuery> listStoredQueries() {
        Parser p = new Parser(new WFSConfiguration());
        
        List<WFSStoredQuery> queries = new ArrayList();
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

    public WFSStoredQuery getStoredQuery(String name) {
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
    
    public void putStoredQuery(WFSStoredQuery query) {
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
    
    WFSStoredQuery parseStoredQuery(File file) throws Exception {
        return parseStoredQuery(file, new Parser(new WFSConfiguration()));
    }
    
    WFSStoredQuery parseStoredQuery(File file, Parser p) throws Exception {
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
