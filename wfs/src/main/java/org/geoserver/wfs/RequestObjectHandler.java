/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.wfs;

import java.util.Arrays;
import java.util.List;

import net.opengis.ows10.Ows10Factory;
import net.opengis.ows11.Ows11Factory;
import net.opengis.wfs20.Wfs20Package;

import org.eclipse.emf.ecore.EObject;
import org.geotools.xml.EMFUtils;

/**
 * Encapsulates interaction with the object model for a particular version of the WFS service.
 * 
 * @author Justin Deoliveira, OpenGeo
 *
 */
public abstract class RequestObjectHandler {

    public static RequestObjectHandler get(Object request) {
        EObject eobj = (EObject) request;
        if (eobj.eClass().getEPackage() instanceof Wfs20Package) {
            return new WFS_20();
        }
        return new WFS_11();
    }
    
    public String getBaseURL(Object request) {
        return eGet(request, "baseUrl", String.class);
    }
    
    public String getVersion(Object request) {
        return eGet(request, "verison", String.class);
    }
    
    public String getUpdateSequence(Object request) {
        return eGet(request, "updateSequence", String.class);
    }
    
    public boolean isSetService(Object request) {
        return eIsSet(request, "service");
    }
    
    public List<String> getAcceptVersions(Object request) {
        return eGet(request, "acceptVersions.version", List.class);
       
    }
    
    public void setAcceptVersions(Object request, String... versions) {
        Object acceptedVersions = createAcceptedVersions();
        eAdd(acceptedVersions, "version", Arrays.asList(versions));
        eSet(request, "acceptVersions", acceptedVersions);
    }
    
    <T> T eGet(Object obj, String property, Class<T> type) {
        String[] props = property.split("\\.");
        for (String prop : props) {
            if (obj == null) {
                return null;
            }
            obj = EMFUtils.get((EObject) obj, prop); 
        }
        return (T) obj;
    }
    
    void eSet(Object obj, String property, Object value) {
        EMFUtils.set((EObject)obj, property, value); 
    }
    
    void eAdd(Object obj, String property, Object value) {
        EMFUtils.add((EObject) obj, property, value);
    }
    
    boolean eIsSet(Object obj, String property) {
        return EMFUtils.isSet((EObject) obj, property);
    }
    
    protected abstract Object createAcceptedVersions();
    
    public static class WFS_11 extends RequestObjectHandler {

        Ows10Factory owsFactory = Ows10Factory.eINSTANCE;
        
        @Override
        protected Object createAcceptedVersions() {
            return owsFactory.createAcceptVersionsType();
        }
    }
    
    public static class WFS_20 extends RequestObjectHandler {

        Ows11Factory owsFactory = Ows11Factory.eINSTANCE;
        
        @Override
        protected Object createAcceptedVersions() {
            return owsFactory.createAcceptVersionsType();
        }
        
    }
}
