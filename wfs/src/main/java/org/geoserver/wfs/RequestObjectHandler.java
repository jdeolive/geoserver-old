/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.wfs;

import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import net.opengis.ows10.Ows10Factory;
import net.opengis.ows11.Ows11Factory;
import net.opengis.wfs.ActionType;
import net.opengis.wfs.AllSomeType;
import net.opengis.wfs.GetFeatureType;
import net.opengis.wfs.GetFeatureWithLockType;
import net.opengis.wfs.InsertedFeatureType;
import net.opengis.wfs.LockFeatureResponseType;
import net.opengis.wfs.LockFeatureType;
import net.opengis.wfs.ResultTypeType;
import net.opengis.wfs.TransactionResponseType;
import net.opengis.wfs.TransactionType;
import net.opengis.wfs.WfsFactory;
import net.opengis.wfs.XlinkPropertyNameType;
import net.opengis.wfs20.CreatedOrModifiedFeatureType;
import net.opengis.wfs20.Wfs20Factory;
import net.opengis.wfs20.Wfs20Package;

import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EObject;
import org.geoserver.ows.util.OwsUtils;
import org.geoserver.platform.Operation;
import org.geotools.xml.EMFUtils;
import org.opengis.filter.Filter;
import org.opengis.filter.identity.FeatureId;
import org.opengis.filter.sort.SortBy;

/**
 * Encapsulates interaction with the object model for a particular version of the WFS service.
 * 
 * @author Justin Deoliveira, OpenGeo
 *
 */
public abstract class RequestObjectHandler {

    public static RequestObjectHandler get(Object request) {
        return get(request.getClass());
    }
    
    public static RequestObjectHandler get(Class clazz) {
        if (clazz.getCanonicalName().startsWith("net.opengis.wfs20")) {
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
    
    public void setHandle(Object request, String handle) {
        eSet(request, "handle", handle);
    }
    
    public QName getTypeName(Object request) {
        return eGet(request, "typeName", QName.class);
    }
    
    public void setTypeName(Object request, QName typeName) {
        eSet(request, "typeName", typeName);
    }
    
    public List<QName> getTypeNames(Object request) {
        return eGet(request, "typeName", List.class);
    }
    
    public void setTypeNames(Object request, List<QName> typeNames) {
        List l = eGet(request, "typeName", List.class);
        l.clear();
        l.addAll(typeNames);
    }

    public Filter getFilter(Object request) {
        return eGet(request, "filter", Filter.class);
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

    protected abstract Object createAcceptedVersions();
    
    //
    // DescribeFeatureType
    //
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
    public void setExpiry(Object request, BigInteger expiry) {
        eSet(request, "expiry", expiry);
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
    // LockFeature
    //
    public abstract List getLocks(Object request);
    
    public abstract boolean isLockActionSome(Object request);
    
    public abstract boolean isLockActionAll(Object request);
    
    public abstract QName getLockTypeName(Object lock);
    
    public abstract Object createLockFeatureResponse();
    
    public abstract void addLockedFeature(Object response, FeatureId fid);
    
    public abstract void addNotLockedFeature(Object response, FeatureId fid);
    
    public abstract List<FeatureId> getNotLockedFeatures(Object response);
    
    //
    //Transaction
    //
    public Object getReleaseAction(Object request) {
        return eGet(request, "releaseAction", Object.class);
    }
    
    public abstract boolean isReleaseActionAll(Object request);
    
    public abstract boolean isReleaseActionSome(Object request);
    
    public abstract void setReleaseActionAll(Object request);
    
    public abstract Iterator getTransactionElements(Object transaction);
    
    public String getLockId(Object transaction) {
        return eGet(transaction, "lockId", String.class);
    }
    
    public void setLockId(Object transaction, String lockId) {
        eSet(transaction, "lockId", lockId);
    }
    
    //TransactionResponse
    public abstract Object createTransactionResponse();
    
    public abstract void setTransactionResponseHandle(Object response, String handle);
    

    public BigInteger getTotalInserted(Object response) {
        return eGet(response, "transactionSummary.totalInserted", BigInteger.class);
    }
    
    public void setTotalInserted(Object response, BigInteger inserted) {
        eSet(response, "transactionSummary.totalInserted", inserted);
    }
    
    public List getInsertedFeatures(Object response) {
        return eGet(response, "insertResults.feature", List.class);
    }
    
    public abstract void addInsertedFeature(Object response, String handle, FeatureId id);
    
    public abstract void addAction(Object result, String code, String locator, String message);

    public BigInteger getTotalUpdated(Object response) {
        return eGet(response, "transactionSummary.totalUpdated", BigInteger.class);
    }
    
    public void setTotalUpdated(Object response, BigInteger updated) {
        eSet(response, "transactionSummary.totalUpdated", updated);
    }
    
    public abstract void addUpdatedFeatures(Object response, String handle, Collection<FeatureId> ids);
    
    public BigInteger getTotalDeleted(Object response) {
        return eGet(response, "transactionSummary.totalDeleted", BigInteger.class);
    }
    
    public void setTotalDeleted(Object response, BigInteger deleted) {
        eSet(response, "transactionSummary.totalDeleted", deleted);
    }
    
    //
    // Insert
    //
    public abstract List getInsertFeatures(Object insert);
    
    //
    // Update
    //
    public List getUpdateProperties(Object update) {
        return eGet(update, "property", List.class);
    }
    
    //
    // Native
    //
    public boolean isSafeToIgnore(Object nativ) {
        return eGet(nativ, "safeToIgnore", Boolean.class);
    }
    
    public String getVendorId(Object nativ) {
        return eGet(nativ, "vendorId", String.class);
    }

    //
    //Property
    //
    public Object getPropertyValue(Object property) {
        return eGet(property, "value", Object.class);
    }
    
    public abstract QName getPropertyName(Object property);
    
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
        String[] props = property.split("\\.");
        for (int i = 0; i < props.length-1; i++) {
            obj = eGet(obj, props[i], Object.class);
        }
        
        EMFUtils.set((EObject)obj, props[props.length-1], value); 
    }
    
    void eAdd(Object obj, String property, Object value) {
        EMFUtils.add((EObject) obj, property, value);
    }
    
    boolean eIsSet(Object obj, String property) {
        return EMFUtils.isSet((EObject) obj, property);
    }
    
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
        public List getLocks(Object request) {
            return eGet(request, "lock", List.class);
        }
        
        @Override
        public boolean isLockActionAll(Object request) {
            return ((LockFeatureType)request).getLockAction() == AllSomeType.ALL_LITERAL;
        }
        
        @Override
        public boolean isLockActionSome(Object request) {
            return ((LockFeatureType)request).getLockAction() == AllSomeType.SOME_LITERAL;
        }
        
        @Override
        public QName getLockTypeName(Object lock) {
            return eGet(lock, "typeName", QName.class);
        }
        
        @Override
        public Object createLockFeatureResponse() {
            return getWfsFactory().createLockFeatureResponseType();
        } 
        
        @Override
        public void addLockedFeature(Object response, FeatureId fid) {
            LockFeatureResponseType lfr = (LockFeatureResponseType) response;
            if (lfr.getFeaturesLocked() == null) {
                lfr.setFeaturesLocked(getWfsFactory().createFeaturesLockedType());
            }
            lfr.getFeaturesLocked().getFeatureId().add(fid);
        }
        
        @Override
        public void addNotLockedFeature(Object response, FeatureId fid) {
            LockFeatureResponseType lfr = (LockFeatureResponseType) response;
            if (lfr.getFeaturesNotLocked() == null) {
                lfr.setFeaturesNotLocked(getWfsFactory().createFeaturesNotLockedType());
            }
            lfr.getFeaturesNotLocked().getFeatureId().add(fid);
        }
        
        @Override
        public List<FeatureId> getNotLockedFeatures(Object response) {
            return eGet(response, "featuresNotLocked.featureId", List.class);
        }
        
        @Override
        public boolean isReleaseActionAll(Object request) {
            return ((TransactionType)request).getReleaseAction() == AllSomeType.ALL_LITERAL;
        }
        
        @Override
        public boolean isReleaseActionSome(Object request) {
            return ((TransactionType)request).getReleaseAction() == AllSomeType.SOME_LITERAL;
        }
        
        @Override
        public void setReleaseActionAll(Object request) {
            ((TransactionType)request).setReleaseAction(AllSomeType.ALL_LITERAL);
        }
        
        @Override
        public Iterator getTransactionElements(Object transaction) {
            return ((TransactionType)transaction).getGroup().valueListIterator();
        }
        
        @Override
        public Object createTransactionResponse() {
            WfsFactory factory = getWfsFactory();
            TransactionResponseType tr = factory.createTransactionResponseType();
            tr.setTransactionSummary(factory.createTransactionSummaryType());
            tr.getTransactionSummary().setTotalInserted(BigInteger.valueOf(0));
            tr.getTransactionSummary().setTotalUpdated(BigInteger.valueOf(0));
            tr.getTransactionSummary().setTotalDeleted(BigInteger.valueOf(0));
            tr.setTransactionResults(factory.createTransactionResultsType());
            tr.setInsertResults(factory.createInsertResultsType());
            return tr;
        }
        
        @Override
        public void setTransactionResponseHandle(Object response, String handle) {
            eSet(response, "transactionResults.handle", handle);
        }
        
        public void addInsertedFeature(Object response, String handle, FeatureId featureId) {
            InsertedFeatureType insertedFeature = getWfsFactory().createInsertedFeatureType();
            insertedFeature.setHandle(handle);
            insertedFeature.getFeatureId().add(featureId);

            ((TransactionResponseType)response).getInsertResults().getFeature().add(insertedFeature);
        }
        
        @Override
        public void addUpdatedFeatures(Object response, String handle, Collection<FeatureId> id) {
            // no-op
        }
        
        @Override
        public void addAction(Object result, String code, String locator, String message) {
            // transaction failed, rollback
            ActionType action = getWfsFactory().createActionType();
            action.setCode(code);
            action.setLocator(locator);
            action.setMessage(message);
            
            ((TransactionResponseType)result).getTransactionResults().getAction().add(action);
        }
        
        @Override
        public List getInsertFeatures(Object insert) {
            return eGet(insert, "feature", List.class);
        }
        
        @Override
        public QName getPropertyName(Object property) {
            return eGet(property, "name", QName.class);
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
        public List getLocks(Object request) {
            return eGet(request, "abstractQueryExpression", List.class);
        }
        
        @Override
        public boolean isLockActionAll(Object request) {
            return ((net.opengis.wfs20.LockFeatureType)request).getLockAction() 
                == net.opengis.wfs20.AllSomeType.ALL;
        }
        
        @Override
        public boolean isLockActionSome(Object request) {
            return ((net.opengis.wfs20.LockFeatureType)request).getLockAction() 
                == net.opengis.wfs20.AllSomeType.SOME;
        }
        
        @Override
        public QName getLockTypeName(Object lock) {
            List typeNames = eGet(lock, "typeNames", List.class);
            if (typeNames.size() == 1) {
                return (QName) typeNames.get(0);
            }
            throw new IllegalArgumentException("Multiple type names on single lock not supported");
        }
        
        @Override
        public Object createLockFeatureResponse() {
            return getWfsFactory().createLockFeatureResponseType();
        } 
        
        @Override
        public void addLockedFeature(Object response, FeatureId fid) {
            net.opengis.wfs20.LockFeatureResponseType lfr = 
                (net.opengis.wfs20.LockFeatureResponseType) response;
            if (lfr.getFeaturesLocked() == null) {
                lfr.setFeaturesLocked(getWfsFactory().createFeaturesLockedType());
            }
            lfr.getFeaturesLocked().getResourceId().add(fid);
        }
        
        @Override
        public void addNotLockedFeature(Object response, FeatureId fid) {
            net.opengis.wfs20.LockFeatureResponseType lfr = 
                (net.opengis.wfs20.LockFeatureResponseType) response;
            if (lfr.getFeaturesNotLocked() == null) {
                lfr.setFeaturesNotLocked(getWfsFactory().createFeaturesNotLockedType());
            }
            lfr.getFeaturesNotLocked().getResourceId().add(fid);
        }
        
        @Override
        public List<FeatureId> getNotLockedFeatures(Object response) {
            return eGet(response, "featuresNotLocked.resourceId", List.class);
        }

        @Override
        public boolean isReleaseActionAll(Object request) {
            return ((net.opengis.wfs20.TransactionType)request).getReleaseAction() == 
                net.opengis.wfs20.AllSomeType.ALL;
        }
        
        @Override
        public boolean isReleaseActionSome(Object request) {
            return ((net.opengis.wfs20.TransactionType)request).getReleaseAction() == 
                net.opengis.wfs20.AllSomeType.SOME;
        }
        
        @Override
        public void setReleaseActionAll(Object request) {
            ((net.opengis.wfs20.TransactionType)request).setReleaseAction(net.opengis.wfs20.AllSomeType.ALL);
        }
        
        @Override
        public Iterator getTransactionElements(Object transaction) {
            return ((net.opengis.wfs20.TransactionType)transaction)
                .getAbstractTransactionAction().iterator();
        }
        
        @Override
        public Object createTransactionResponse() {
            Wfs20Factory factory = getWfsFactory();
            net.opengis.wfs20.TransactionResponseType tr = factory.createTransactionResponseType();
            tr.setTransactionSummary(factory.createTransactionSummaryType());
            tr.getTransactionSummary().setTotalDeleted(BigInteger.valueOf(0));
            tr.getTransactionSummary().setTotalInserted(BigInteger.valueOf(0));
            tr.getTransactionSummary().setTotalUpdated(BigInteger.valueOf(0));
            tr.getTransactionSummary().setTotalReplaced(BigInteger.valueOf(0));
            return tr;
        }
        
        @Override
        public void setTransactionResponseHandle(Object response, String handle) {
            //no-op
        }
        
        @Override
        public void addInsertedFeature(Object response, String handle, FeatureId featureId) {
            CreatedOrModifiedFeatureType inserted = getWfsFactory().createCreatedOrModifiedFeatureType();
            inserted.setHandle(handle);
            inserted.getResourceId().add(featureId);
            
            net.opengis.wfs20.TransactionResponseType tr = 
                (net.opengis.wfs20.TransactionResponseType) response;
            if (tr.getInsertResults() == null) {
                tr.setInsertResults(getWfsFactory().createActionResultsType());
            }
            
            tr.getInsertResults().getFeature().add(inserted);
        }
        
        @Override
        public void addUpdatedFeatures(Object response, String handle, Collection<FeatureId>  ids) {
            CreatedOrModifiedFeatureType updated = getWfsFactory().createCreatedOrModifiedFeatureType();
            updated.setHandle(handle);
            updated.getResourceId().addAll(ids);
            
            net.opengis.wfs20.TransactionResponseType tr = 
                (net.opengis.wfs20.TransactionResponseType) response;
            if (tr.getUpdateResults() == null) {
                tr.setUpdateResults(getWfsFactory().createActionResultsType());
            }
            
            tr.getUpdateResults().getFeature().add(updated);
        }

        @Override
        public void addAction(Object result, String code, String locator, String message) {
            //no-op
        }
        
        @Override
        public List getInsertFeatures(Object insert) {
            return eGet(insert, "any", List.class);
        }
        
        @Override
        public QName getPropertyName(Object property) {
            return eGet(property, "valueReference.value", QName.class);
        }

        @Override
        protected Object createAcceptedVersions() {
            return owsFactory.createAcceptVersionsType();
        }
        
    }
}
