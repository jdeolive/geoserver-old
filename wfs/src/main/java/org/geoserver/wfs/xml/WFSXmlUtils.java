/* 
 * Copyright (c) 2001 - 2011 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.wfs.xml;

import java.io.Reader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.NamespaceInfo;
import org.geoserver.ows.XmlRequestReader;
import org.geoserver.wfs.WFSException;
import org.geoserver.wfs.WFSInfo;
import org.geotools.xml.Parser;
import org.xml.sax.InputSource;

/**
 * Some utilities shared among WFS xml readers/writers.
 * 
 * @author Justin Deoliveira, OpenGeo
 *
 */
public class WFSXmlUtils {

    public static void initRequestParser(Parser parser, WFSInfo wfs, Catalog catalog, Map kvp) {
      //check the strict flag to determine if we should validate or not
        Boolean strict = (Boolean) kvp.get("strict");
        if ( strict == null ) {
            strict = Boolean.FALSE;
        }
        
        //check for cite compliance, we always validate for cite
        if ( wfs.isCiteCompliant() ) {
            strict = Boolean.TRUE;
        }
        parser.setValidating(strict.booleanValue());
        
        //"inject" namespace mappings
        List<NamespaceInfo> namespaces = catalog.getNamespaces();
        for ( NamespaceInfo ns : namespaces ) {
            if ( ns.equals( catalog.getDefaultNamespace() ) )  
                continue;
            
            parser.getNamespaces().declarePrefix( 
                ns.getPrefix(), ns.getURI());
        }
    }
    
    public static Object parseRequest(Parser parser, Reader reader, WFSInfo wfs) throws Exception {
        //set the input source with the correct encoding
        InputSource source = new InputSource(reader);
        source.setEncoding(wfs.getGeoServer().getGlobal().getCharset());

        return parser.parse(source);
    }

    public static void checkValidationErrors(Parser parser, XmlRequestReader requestReader) {
        //TODO: HACK, disabling validation for transaction
        if (!"Transaction".equalsIgnoreCase(requestReader.getElement().getLocalPart())) {
            if (!parser.getValidationErrors().isEmpty()) {
                WFSException exception = new WFSException("Invalid request", "InvalidParameterValue");

                for (Iterator e = parser.getValidationErrors().iterator(); e.hasNext();) {
                    Exception error = (Exception) e.next();
                    exception.getExceptionText().add(error.getLocalizedMessage());
                }

                throw exception;
            }
        }
        
    }
    
    
}
