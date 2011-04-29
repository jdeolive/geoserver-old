/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.wfs.xml.v2_0;

import java.io.Reader;
import java.util.Map;

import javax.xml.namespace.QName;

import org.geoserver.config.GeoServer;
import org.geoserver.ows.XmlRequestReader;
import org.geoserver.wfs.WFSInfo;
import org.geoserver.wfs.xml.WFSXmlUtils;
import org.geotools.util.Version;
import org.geotools.wfs.v2_0.WFS;
import org.geotools.wfs.v2_0.WFSConfiguration;
import org.geotools.xml.Configuration;
import org.geotools.xml.Parser;

/**
 * Xml reader for wfs 1.0 xml requests.
 * 
 * @author Justin Deoliveira, OpenGeo
 *
 */
public class WfsXmlReader extends XmlRequestReader {

    GeoServer gs;
    
    public WfsXmlReader(String element, GeoServer gs) {
        super(new QName(WFS.NAMESPACE, element), new Version("2.0.0"), "wfs");
        this.gs = gs;
    }
    
    @Override
    public Object read(Object request, Reader reader, Map kvp) throws Exception {
        Parser parser = new Parser(new WFSConfiguration());
        
        WFSInfo wfs = wfs();
        
        WFSXmlUtils.initRequestParser(parser, wfs, gs.getCatalog(), kvp);
        Object parsed = WFSXmlUtils.parseRequest(parser, reader, wfs);
        WFSXmlUtils.checkValidationErrors(parser, this);
        
        return parsed;
    }

    WFSInfo wfs() {
        return gs.getService(WFSInfo.class);
    }
}
