/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.wfs;

import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import net.opengis.ows10.Ows10Factory;
import net.opengis.ows11.Ows11Factory;
import net.opengis.wfs.GetFeatureType;
import net.opengis.wfs.GetFeatureWithLockType;
import net.opengis.wfs.ResultTypeType;
import net.opengis.wfs.WfsFactory;
import net.opengis.wfs.XlinkPropertyNameType;
import net.opengis.wfs20.Wfs20Factory;
import net.opengis.wfs20.Wfs20Package;

import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EObject;
import org.geoserver.ows.util.OwsUtils;
import org.geoserver.platform.Operation;
import org.geotools.xml.EMFUtils;
import org.opengis.filter.Filter;
import org.opengis.filter.sort.SortBy;

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
    
    public static Object findGetFeature(Operation op) {
        Object req = OwsUtils.parameter(op.getParameters(), GetFeatureType.class);
        if (req == null) {
            req = OwsUtils.parameter(op.getParameters(), net.opengis.wfs20.GetFeatureType.class);
        }
        return req;
    }
    
    public static String baseURL(Object request) {
        return get(request).getBaseURL(request);
    }

    //
    //common properties
    //
    public abstract EFactory getWfsFactory();
    
    public String getBaseURL(Object request) {
        return eGet(request, "baseUrl", String.class);
    }
    
    public String getVersion(Object request) {
        return eGet(request, "version", String.class);
    }
    
    public boolean isSetService(Object request) {
        return eIsSet(request, "service");
    }
    
    public Map getMetadata(Object request) {
        return eGet(request, "metadata", Map.class);
    }
    
    public Map getFormatOptions(Object request) {
        return eGet(request, "formatOptions", Map.class);
    }
    
    public String getHandle(Object request) {
        return eGet(request, "handle", String.class);
    }

    //
    //GetCapabilities
    //
    public String getUpdateSequence(Object request) {
        return eGet(request, "updateSequence", String.class);
    }
    
    
    public List<String> getAcceptVersions(Object request) {
        return eGet(request, "acceptVersions.version", List.class);
       
    }
    
    public void setAcceptVersions(Object request, String... versions) {
        Object acceptedVersions = createAcceptedVersions();
        eAdd(acceptedVersions, "version", Arrays.asList(versions));
        eSet(request, "acceptVersions", acceptedVersions);
    }
    
    //
    // DescribeFeatureType
    //
    public List<QName> getTypeNames(Object request) {
        return eGet(request, "typeName", List.class);
    }
    
    public void setTypeNames(Object request, List<QName> typeNames) {
        List l = eGet(request, "typeName", List.class);
        l.clear();
        l.addAll(typeNames);
    }
    
    public boolean isSetOutputFormat(Object request) {
        return eIsSet(request, "outputFormat");
    }
    
    public void setOutputFormat(Object request, String outputFormat) {
        eSet(request, "outputFormat", outputFormat);
    }
    
    //
    // GetFeature
    //
    public abstract List getQueries(Object request);
    
    public abstract BigInteger getMaxFeatures(Object request);
    
    public abstract void setMaxFeatures(Object request, BigInteger maxFeatures); 
    
    public abstract String getTraverseXlinkDepth(Object request);
    
    public abstract boolean isResultTypeResults(Object request);
    
    public abstract boolean isResultTypeHits(Object request);
    
    public abstract boolean isLockRequest(Object request);

    //
    // GetFeatureWithLock
    //
    public BigInteger getExpiry(Object request) {
        return eGet(request, "expiry", BigInteger.class);
    }

    // Query
    public abstract Object createQuery();
    
    public abstract boolean isQueryTypeNamesUnset(List queries);
    
    public abstract List<QName> getQueryTypeNames(Object query);
    
    public abstract List<String> getQueryPropertyNames(Object query);
    
    public abstract Filter getQueryFilter(Object query);
    
    public URI getQuerySrsName(Object query) {
        return eGet(query, "srsName", URI.class);
    }
    
    public abstract List<SortBy> getQuerySortBy(Object query);
    
    public String getQueryFeatureVersion(Object query) {
        return eGet(query, "featureVersion", String.class);
    }
    
    public abstract List<XlinkPropertyNameType> getQueryXlinkPropertyNames(Object query);
    
    //
    // helpers
    //
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
        public WfsFactory getWfsFactory() {
            return WfsFactory.eINSTANCE;
        }
        
        @Override
        public List getQueries(Object request) {
            return eGet(request, "query", List.class);
        }

        @Override
        public BigInteger getMaxFeatures(Object request) {
            return eGet(request, "maxFeatures", BigInteger.class);
        }
        
        @Override
        public void setMaxFeatures(Object request, BigInteger maxFeatures) {
            eSet(request, "maxFeatures", maxFeatures);
        }
        
        @Override
        public String getTraverseXlinkDepth(Object request) {
            return eGet(request, "traverseXlinkDepth", String.class);
        }
        
        @Override
        public boolean isResultTypeResults(Object request) {
            return ((GetFeatureType)request).getResultType() == ResultTypeType.RESULTS_LITERAL;
        }
        
        @Override
        public boolean isResultTypeHits(Object request) {
            return ((GetFeatureType)request).getResultType() == ResultTypeType.HITS_LITERAL;
        }
        
        @Override
        public boolean isLockRequest(Object request) {
            return request instanceof GetFeatureWithLockType;
        }
        
        @Override
        public Object createQuery() {
            return getWfsFactory().createQueryType();
        }
        
        @Override
        public boolean isQueryTypeNamesUnset(List queries) {
            return EMFUtils.isUnset(queries, "typeName");
        }
        
        @Override
        public List<QName> getQueryTypeNames(Object query) {
            return eGet(query, "typeName", List.class);
        }
        
        @Override
        public List<String> getQueryPropertyNames(Object query) {
            return eGet(query, "propertyName", List.class);
        }
        
        @Override
        public Filter getQueryFilter(Object query) {
            return eGet(query, "filter", Filter.class);
        }
        
        @Override
        public List<SortBy> getQuerySortBy(Object query) {
            return eGet(query, "sortBy", List.class);
        }
        
        @Override
        public List<XlinkPropertyNameType> getQueryXlinkPropertyNames(Object query) {
            return eGet(query, "xlinkPropertyName", List.class);
        }

        @Override
        protected Object createAcceptedVersions() {
            return owsFactory.createAcceptVersionsType();
        }
    }
    
    public static class WFS_20 extends RequestObjectHandler {

        Ows11Factory owsFactory = Ows11Factory.eINSTANCE;
        
        @Override
        public Wfs20Factory getWfsFactory() {
            return Wfs20Factory.eINSTANCE;
        }
        
        @Override
        public List getQueries(Object request) {
            return eGet(request, "abstractQueryExpression", List.class);
        }
        
        @Override
        public BigInteger getMaxFeatures(Object request) {
            return eGet(request, "count", BigInteger.class);
        }
        
        @Override
        public void setMaxFeatures(Object request, BigInteger maxFeatures) {
            eSet(request, "count", maxFeatures);
        }
        
        @Override
        public String getTraverseXlinkDepth(Object request) {
            Object obj = eGet(request, "resolveDepth", Object.class);
            return obj != null ? obj.toString() : null;
        }
        
        @Override
        public boolean isResultTypeResults(Object request) {
            return ((net.opengis.wfs20.GetFeatureType)request).getResultType() 
                == net.opengis.wfs20.ResultTypeType.RESULTS;
        }
        
        @Override
        public boolean isResultTypeHits(Object request) {
            return ((net.opengis.wfs20.GetFeatureType)request).getResultType() 
                == net.opengis.wfs20.ResultTypeType.HITS;
        }
        
        @Override
        public boolean isLockRequest(Object request) {
            return request instanceof net.opengis.wfs20.GetFeatureWithLockType;
        }
        
        @Override
        public Object createQuery() {
            return getWfsFactory().createQueryType();
        }
        
        @Override
        public boolean isQueryTypeNamesUnset(List queries) {
            return EMFUtils.isUnset(queries, "typeNames");
        }
        
        @Override
        public List<QName> getQueryTypeNames(Object query) {
            return eGet(query, "typeNames", List.class);
        }
        
        @Override
        public List<String> getQueryPropertyNames(Object query) {
            //WFS 2.0 has this as a list of QNAme, drop the qualified part
            List<QName> propertyNames = eGet(query, "abstractProjectionClause", List.class);
            List<String> l = new ArrayList();
            for (QName name : propertyNames) {
                l.add(name.getLocalPart());
            }
            return l;
        }
        
        @Override
        public Filter getQueryFilter(Object query) {
            return eGet(query, "abstractSelectionClause", Filter.class);
        }
        
        @Override
        public List<SortBy> getQuerySortBy(Object query) {
            return eGet(query, "abstractSortingClause", List.class);
        }
        
        @Override
        public List<XlinkPropertyNameType> getQueryXlinkPropertyNames(Object request) {
            //no equivalent in wfs 2.0
            return Collections.EMPTY_LIST;
        }
        
        
        @Override
        protected Object createAcceptedVersions() {
            return owsFactory.createAcceptVersionsType();
        }
        
    }
}
