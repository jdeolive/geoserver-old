package org.geoserver.wfs.request;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.List;

import net.opengis.wfs.FeatureCollectionType;

import org.eclipse.emf.ecore.EObject;
import org.geotools.feature.FeatureCollection;

/**
 * Response object for a feature collection, most notably from a GetFeature request.
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public abstract class FeatureCollectionResponse extends RequestObject {

    public static FeatureCollectionResponse adapt(Object adaptee) {
        if (adaptee instanceof FeatureCollectionType) {
            return new WFS11((EObject) adaptee);
        }
        else if (adaptee instanceof net.opengis.wfs20.FeatureCollectionType) {
            return new WFS20((EObject) adaptee);
        }
        return null;
    }

    protected FeatureCollectionResponse(EObject adaptee) {
        super(adaptee);
    }

    public String getLockId() {
        return eGet(adaptee, "lockId", String.class);
    }
    public void setLockId(String lockId) {
        eSet(adaptee, "lockId", lockId);
    }

    public Calendar getTimeStamp() {
        return eGet(adaptee, "timeStamp", Calendar.class);
    }
    public void setTimeStamp(Calendar timeStamp) {
        eSet(adaptee, "timeStamp", timeStamp);
    }
    
    public abstract BigInteger getNumberOfFeatures();
    public abstract void setNumberOfFeatures(BigInteger n);
    
    public abstract BigInteger getTotalNumberOfFeatures();
    public abstract void setTotalNumberOfFeatures(BigInteger n);
    
    public abstract List<FeatureCollection> getFeatures();
    
    public List<FeatureCollection> getFeature() {
        //alias
        return getFeatures();
    }

    public static class WFS11 extends FeatureCollectionResponse {
        public WFS11(EObject adaptee) {
            super(adaptee);
        }

        @Override
        public BigInteger getNumberOfFeatures() {
            return eGet(adaptee, "numberOfFeatures", BigInteger.class);
        }
        @Override
        public void setNumberOfFeatures(BigInteger n) {
            eSet(adaptee, "numberOfFeatures", n);
        }

        @Override
        public BigInteger getTotalNumberOfFeatures() {
            //noop
            return null;
        }
        @Override
        public void setTotalNumberOfFeatures(BigInteger n) {
            //noop
        }
        
        @Override
        public List<FeatureCollection> getFeatures() {
            return eGet(adaptee, "feature", List.class);
        }
    }

    public static class WFS20 extends FeatureCollectionResponse {
        public WFS20(EObject adaptee) {
            super(adaptee);
        }

        @Override
        public BigInteger getNumberOfFeatures() {
            return eGet(adaptee, "numberReturned", BigInteger.class);
        }

        @Override
        public void setNumberOfFeatures(BigInteger n) {
            eSet(adaptee, "numberReturned", n);
        }

        @Override
        public BigInteger getTotalNumberOfFeatures() {
            return eGet(adaptee, "numberMatched", BigInteger.class);
        }
        @Override
        public void setTotalNumberOfFeatures(BigInteger n) {
            eSet(adaptee, "numberMatched", n);
        }
        
        @Override
        public List<FeatureCollection> getFeatures() {
            return eGet(adaptee, "member", List.class);
        }
    }
}
