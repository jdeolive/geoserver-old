package org.geoserver.wfs.request;

import org.eclipse.emf.ecore.EObject;

/**
 * Delete element in a Transaction request.
 *  
 * @author Justin Deoliveira, OpenGeo
 */
public abstract class Delete extends TransactionElement {

    protected Delete(EObject adaptee) {
        super(adaptee);
    }

    public static class WFS11 extends Delete {
        public WFS11(EObject adaptee) {
            super(adaptee);
        }
    }
    
    public static class WFS20 extends Delete {
        public WFS20(EObject adaptee) {
            super(adaptee);
        }
    }

}
