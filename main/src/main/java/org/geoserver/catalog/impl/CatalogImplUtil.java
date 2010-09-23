package org.geoserver.catalog.impl;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.util.Utilities;

/**
 * Some utility methods used by the catalog implementation.
 * 
 * @author Justin Deoliveira, OpenGeo
 *
 */
public class CatalogImplUtil {

    /**
     * Custom equals for envelopes which handles the case of both having NaN values
     */
    static boolean envelopeEquals(ReferencedEnvelope e1, ReferencedEnvelope e2) {
        if (!Utilities.equals(e1, e2)) {
            if(e1 != null && e2 != null && !Utilities.equals(e1.getCoordinateReferenceSystem(), 
                e2.getCoordinateReferenceSystem())) {
                return false;
            }
            
            if (e1 != null && isNaN(e1)) {
                e1 = null;
            }
            if (e2 != null && isNaN(e2)) {
                e2 = null;
            }
            
            return Utilities.equals(e1, e2);
        }
        
        return true;
    }
    
    static boolean isNaN(ReferencedEnvelope e) {
        return Double.isNaN(e.getMinX()) && Double.isNaN(e.getMinY()) && 
            Double.isNaN(e.getMaxX()) && Double.isNaN(e.getMaxY());
    }
}
