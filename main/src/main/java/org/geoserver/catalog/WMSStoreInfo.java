/* Copyright (c) 2001 - 2008 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.catalog;

import java.io.IOException;

import org.geotools.data.wms.WebMapServer;
import org.opengis.util.ProgressListener;

/**
 * A store backed by a {@link WebMapServer}, allows for WMS cascading
 * 
 * @author Andrea Aime - OpenGeo
 * 
 */
public interface WMSStoreInfo extends StoreInfo {

    /**
     * Returns the underlying {@link WebMapServer}
     * <p>
     * This method does I/O and is potentially blocking. The <tt>listener</tt> may be used to report
     * the progress of loading the datastore and also to report any errors or warnings that occur.
     * </p>
     * 
     * @param listener
     *            A progress listener, may be <code>null</code>.
     * 
     * @return The datastore.
     * 
     * @throws IOException
     *             Any I/O problems.
     */
    WebMapServer getWebMapServer(ProgressListener listener) throws IOException;

    /**
     * The capabilities url
     */
    String getCapabilitiesURL();

    /**
     * Sets the web map server capabilities url.
     * 
     * @uml.property name="url"
     */
    void setCapabilitiesURL(String url);
    
    String getUsername();
    
    void setUsername(String user);
    
    String getPassword();
    
    void setPassword(String password);
    
    int getMaxConnections();
    
    void setMaxConnections(int maxConcurrentConnections);
    
}
