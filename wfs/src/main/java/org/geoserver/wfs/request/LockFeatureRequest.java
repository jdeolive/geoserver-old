package org.geoserver.wfs.request;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import net.opengis.wfs.AllSomeType;
import net.opengis.wfs.LockFeatureType;
import net.opengis.wfs.WfsFactory;
import net.opengis.wfs20.Wfs20Factory;

import org.eclipse.emf.ecore.EObject;

/**
 * WFS LockFeature request.
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public abstract class LockFeatureRequest extends RequestObjectAdapter {

    protected LockFeatureRequest(EObject adaptee) {
        super(adaptee);
    }
    
    public BigInteger getExpiry() {
        return eGet(adaptee, "expiry", BigInteger.class);
    }
    public void setExpiry(BigInteger expiry) {
        eSet(adaptee, "expiry", expiry);
    }
    
    public abstract List<Lock> getLocks();
    
    public abstract boolean isLockActionSome();
    
    public abstract boolean isLockActionAll();
    
    public abstract LockFeatureResponse createResponse();
    
    public static class WFS11 extends LockFeatureRequest {

        public WFS11(EObject adaptee) {
            super(adaptee);
        }

        @Override
        public List<Lock> getLocks() {
            List<Lock> locks = new ArrayList();
            for (Object lock : eGet(adaptee, "lock", List.class)) {
                locks.add(new Lock.WFS11((EObject) lock));
            }
            return locks;
        }
        
        @Override
        public boolean isLockActionAll() {
            return ((LockFeatureType)adaptee).getLockAction() == AllSomeType.ALL_LITERAL;
        }
        
        @Override
        public boolean isLockActionSome() {
            return ((LockFeatureType)adaptee).getLockAction() == AllSomeType.SOME_LITERAL;
        }
        
        @Override
        public LockFeatureResponse createResponse() {
            return new LockFeatureResponse.WFS11(
                ((WfsFactory)getFactory()).createLockFeatureResponseType());
        }
    }
    
    public static class WFS20 extends LockFeatureRequest {
        
        public WFS20(EObject adaptee) {
            super(adaptee);
        }
        
        @Override
        public List<Lock> getLocks() {
            List<Lock> locks = new ArrayList();
            for (Object lock : eGet(adaptee, "abstractQueryExpression", List.class)) {
                locks.add(new Lock.WFS20((EObject) lock));
            }
            return locks;
        }
        
        @Override
        public boolean isLockActionAll() {
            return ((net.opengis.wfs20.LockFeatureType)adaptee).getLockAction() 
                == net.opengis.wfs20.AllSomeType.ALL;
        }
        
        @Override
        public boolean isLockActionSome() {
            return ((net.opengis.wfs20.LockFeatureType)adaptee).getLockAction() 
                == net.opengis.wfs20.AllSomeType.SOME;
        }
        
        @Override
        public LockFeatureResponse createResponse() {
            return new LockFeatureResponse.WFS20(
                ((Wfs20Factory)getFactory()).createLockFeatureResponseType());
        } 
        
    }

}
