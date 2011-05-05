package org.geoserver.wfs.request;

import org.eclipse.emf.ecore.EObject;

/**
 * Native element in a Transaction request.
 *  
 * @author Justin Deoliveira, OpenGeo
 */
public abstract class Native extends TransactionElement {

    protected Native(EObject adaptee) {
        super(adaptee);
    }
    
    public boolean isSafeToIgnore() {
        return eGet(adaptee, "safeToIgnore", Boolean.class);
    }
    
    public String getVendorId() {
        return eGet(adaptee, "vendorId", String.class);
    }
    
    public static class WFS11 extends Native {
        public WFS11(EObject adaptee) {
            super(adaptee);
        }
    }
    
    public static class WFS20 extends Native {
        public WFS20(EObject adaptee) {
            super(adaptee);
        }
    }

}
