package org.geoserver.wfs.request;

import javax.xml.namespace.QName;

import org.eclipse.emf.ecore.EObject;

/**
 * Property of an Update element in a Transaction.
 * 
 * @author Justin Deoliveira, OpenGeo
 *
 */
public abstract class Property extends RequestObjectAdapter {

    protected Property(EObject adaptee) {
        super(adaptee);
    }

    public Object getValue() {
        return eGet(adaptee, "value", Object.class);
    }
    
    public abstract QName getName();
    
    public static class WFS11 extends Property {
        public WFS11(EObject adaptee) {
            super(adaptee);
        }

        @Override
        public QName getName() {
            return eGet(adaptee, "name", QName.class);
        }
    }
    
    public static class WFS20 extends Property {
        public WFS20(EObject adaptee) {
            super(adaptee);
        }

        @Override
        public QName getName() {
            return eGet(adaptee, "valueReference.value", QName.class);
        }
    }
}
