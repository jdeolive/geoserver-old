/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.wms.response;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;

import net.opengis.wfs.FeatureCollectionType;

import org.geoserver.platform.ServiceException;
import org.geoserver.wms.request.GetFeatureInfoRequest;
import org.geotools.util.logging.Logging;

/**
 * Base class for GetFeatureInfo delegates responsible of creating GetFeatureInfo responses in
 * different formats.
 * 
 * <p>
 * Subclasses should implement one or more output formats, wich will be returned in a list of mime
 * type strings in <code>getSupportedFormats</code>. For example, a subclass can be created to write
 * one of the following output formats:
 * 
 * <ul>
 * <li>
 * text/plain</li>
 * <li>
 * text/html</li>
 * </ul>
 * </p>
 * 
 * <p>
 * This abstract class takes care of executing the request in the sense of taking the GetFeatureInfo
 * request parameters such as query_layers, bbox, x, y, etc., create the gt2 query objects for each
 * featuretype and executing it. This process leads to a set of FeatureResults objects and its
 * metadata, wich will be given to the <code>execute(FeatureTypeInfo[] ,
 * FeatureResults[])</code> method, that a subclass should implement as a matter of setting up any
 * resource/state it needs to later encoding.
 * </p>
 * 
 * <p>
 * So, it should be enough to a subclass to implement the following methods in order to produce the
 * requested output format:
 * 
 * <ul>
 * <li>
 * execute(FeatureTypeInfo[], FeatureResults[], int, int)</li>
 * <li>
 * canProduce(String mapFormat)</li>
 * <li>
 * getSupportedFormats()</li>
 * <li>
 * writeTo(OutputStream)</li>
 * </ul>
 * </p>
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @author Chris Holmes
 * @version $Id$
 */
public abstract class GetFeatureInfoOutputFormat {

    /** A logger for this class. */
    protected static final Logger LOGGER = Logging.getLogger(GetFeatureInfoOutputFormat.class);

    private final String contentType;

    public GetFeatureInfoOutputFormat(final String contentType) {
        this.contentType = contentType;
    }

    public abstract void write(FeatureCollectionType results, GetFeatureInfoRequest request,
            OutputStream out) throws ServiceException, IOException;

    /**
     * Evaluates if this GetFeatureInfo producer can generate the map format specified by
     * <code>mapFormat</code>, where <code>mapFormat</code> is the MIME type of the requested
     * response.
     * 
     * @param mapFormat
     *            the MIME type of the required output format, might be {@code null}
     * 
     * @return true if class can produce a map in the passed format
     */
    public boolean canProduce(String mapFormat) {
        return this.contentType.equalsIgnoreCase(mapFormat);
    }

    public String getContentType() {
        return contentType;
    }
}
