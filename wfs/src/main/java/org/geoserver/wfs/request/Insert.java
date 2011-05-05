package org.geoserver.wfs.request;

import java.util.List;

import org.eclipse.emf.ecore.EObject;

/**
 * Insert element in a Transaction request.
 *  
 * @author Justin Deoliveira, OpenGeo
 */
public abstract class Insert extends TransactionElement {

    protected Insert(EObject adaptee) {
        super(adaptee);
    }
    
    public abstract List getFeatures();
    
    public static class WFS11 extends Insert {

        public WFS11(EObject adaptee) {
            super(adaptee);
        }
        
        @Override
        public List getFeatures() {
            return eGet(adaptee, "feature", List.class);
        }

    }
    
    public static class WFS20 extends Insert {

        public WFS20(EObject adaptee) {
            super(adaptee);
        }
        
        @Override
        public List getFeatures() {
            return eGet(adaptee, "any", List.class);
        }

    }

}
