/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.wfs;

import net.opengis.wfs20.DescribeFeatureTypeType;
import net.opengis.wfs20.GetCapabilitiesType;

import org.geoserver.catalog.FeatureTypeInfo;
import org.geotools.xml.transform.TransformerBase;

/**
 * Web Feature Service implementation version 2.0.
 * <p>
 * Each of the methods on this class corresponds to an operation as defined
 * by the Web Feature Specification. See {@link http://www.opengeospatial.org/standards/wfs}
 * for more details.
 * </p>
 * @author Justin Deoliveira, OpenGeo
  */
public interface WebFeatureService20 {
    /**
     * The configuration of the service.
     */
    WFSInfo getServiceInfo();
    
    /**
     * WFS GetCapabilities operation.
     *
     * @param request The get capabilities request.
     *
     * @return A transformer instance capable of serializing a wfs capabilities
     * document.
     *
     * @throws WFSException Any service exceptions.
     */
    TransformerBase getCapabilities(GetCapabilitiesType request)
        throws WFSException;
    
    /**
     * WFS DescribeFeatureType operation.
     *
     * @param request The describe feature type request.
     *
     * @return A set of feature type metadata objects.
     *
     * @throws WFSException Any service exceptions.
     */
    FeatureTypeInfo[] describeFeatureType(DescribeFeatureTypeType request)
        throws WFSException;
}
