/* Copyright (c) 2011 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.geogit.wfsbridge;

import static org.geoserver.wfs.TransactionEventType.POST_UPDATE;
import static org.geoserver.wfs.TransactionEventType.PRE_DELETE;
import static org.geoserver.wfs.TransactionEventType.PRE_INSERT;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import net.opengis.wfs.DeleteElementType;
import net.opengis.wfs.InsertElementType;
import net.opengis.wfs.InsertResultsType;
import net.opengis.wfs.InsertedFeatureType;
import net.opengis.wfs.PropertyType;
import net.opengis.wfs.TransactionResponseType;
import net.opengis.wfs.TransactionType;
import net.opengis.wfs.UpdateElementType;

import org.apache.commons.collections.KeyValue;
import org.apache.commons.collections.keyvalue.DefaultMapEntry;
import org.geoserver.geogit.GEOGIT;
import org.geoserver.wfs.TransactionEvent;
import org.geoserver.wfs.TransactionEventType;
import org.geoserver.wfs.TransactionPlugin;
import org.geoserver.wfs.WFSException;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.NameImpl;
import org.geotools.util.logging.Logging;
import org.geotools.xml.EMFUtils;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.identity.FeatureId;
import org.springframework.util.Assert;

/**
 * @author Gabriel Roldan
 * 
 */
public class GeoGITTransactionListener implements TransactionPlugin {

    static final String GEOGIT_TRANSACTION_UUID = "GEOGIT.TRANSACTION_UUID";

    static final String GEOGIT_INSERTS_PLACEHOLDER = "GEOGIT_INSERTS_PLACEHOLDER";

    private static final Logger LOGGER = Logging.getLogger(GeoGITTransactionListener.class);

    private static FilterFactory2 FILTER_FACTORY = CommonFactoryFinder.getFilterFactory2(null);

    private final GEOGIT geogitFacade;

    public GeoGITTransactionListener(final GEOGIT geogitFacade) {
        this.geogitFacade = geogitFacade;
    }

    /**
     * Does nothing but assigning a specific transaction id to the request's
     * {@link TransactionType#getExtendedProperties() extended properties map} for the other
     * transaction plugin methods to identify it , we're interested in
     * {@link #beforeCommit(TransactionType)} and
     * {@link #afterTransaction(TransactionType, boolean)}.
     * 
     * @return {@code request} untouched.
     * @see org.geoserver.wfs.TransactionPlugin#beforeTransaction(net.opengis.wfs.TransactionType)
     */
    @SuppressWarnings("unchecked")
    public TransactionType beforeTransaction(TransactionType request) throws WFSException {

        final String transactionUUID = UUID.randomUUID().toString();
        Map<Object, Object> extendedProperties = request.getExtendedProperties();
        Assert.notNull(extendedProperties, "TransactionType.extendedProperties is null");
        extendedProperties.put(GEOGIT_TRANSACTION_UUID, transactionUUID);
        return request;
    }

    /**
     * Only interested in events {@link TransactionEventType#PRE_INSERT},
     * {@link TransactionEventType#POST_UPDATE}, and {@link TransactionEventType#PRE_DELETE}; other
     * events are ignored.
     * 
     * @see org.geoserver.wfs.TransactionListener#dataStoreChange(org.geoserver.wfs.TransactionEvent)
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void dataStoreChange(final TransactionEvent event) throws WFSException {
        if (isIgnorableEvent(event)) {
            return;
        }
        final TransactionEventType eventType = event.getType();
        FeatureCollection affectedFeatures;
        affectedFeatures = event.getAffectedFeatures();

        LOGGER.warning("dataStoreChange: " + eventType + " " + event.getLayerName());

        final TransactionType request = event.getRequest();
        final Map transactionExtraData = request.getExtendedProperties();
        final String geogitTransactionID = (String) transactionExtraData
                .get(GEOGIT_TRANSACTION_UUID);
        Assert.notNull(geogitTransactionID);

        final Object source = event.getSource();
        if (eventType == TransactionEventType.PRE_INSERT) {

            Assert.isTrue(source instanceof InsertElementType);
            final Name typeName = affectedFeatures.getSchema().getName();
            try {
                List<String> insertedFids = geogitFacade.stageInsert(geogitTransactionID, typeName,
                        affectedFeatures);
                saveFidsInTransactionPlaceHolder(transactionExtraData, typeName, insertedFids);
            } catch (Exception e) {
                throw new WFSException(e);
            }

        } else if (eventType == TransactionEventType.POST_UPDATE) {
            Assert.isTrue(source instanceof UpdateElementType);
            final UpdateElementType updateRequest = (UpdateElementType) source;

            final Name typeName = affectedFeatures.getSchema().getName();
            final Filter filter = updateRequest.getFilter();
            final List<PropertyName> updatedProperties;
            final List<Object> newValues;
            {
                final List<PropertyType> properties = updateRequest.getProperty();
                final int size = properties.size();
                updatedProperties = new ArrayList<PropertyName>(size);
                newValues = new ArrayList<Object>(size);

                for (PropertyType property : properties) {
                    QName name = property.getName();
                    Object value = property.getValue();
                    PropertyName propertyName = FILTER_FACTORY.property(new NameImpl(name));
                    updatedProperties.add(propertyName);
                    newValues.add(value);
                }
            }
            try {
                geogitFacade.stageUpdate(geogitTransactionID, typeName, filter, updatedProperties,
                        newValues, affectedFeatures);
            } catch (Exception e) {
                throw new WFSException(e);
            }

        } else if (eventType == TransactionEventType.PRE_DELETE) {

            Assert.isTrue(source instanceof DeleteElementType);
            final DeleteElementType deleteRequest = (DeleteElementType) source;
            final Name typeName = affectedFeatures.getSchema().getName();
            final Filter filter = deleteRequest.getFilter();
            try {
                geogitFacade.stageDelete(geogitTransactionID, typeName, filter, affectedFeatures);
            } catch (Exception e) {
                throw new WFSException(e);
            }
        }
    }

    /**
     * Saves the list of incoming feature ids taken at a PRE_INSERT event into a list for the given
     * feature type inside the transaction placeholder, so they can be taken back when the
     * transaction is committed in order to update set the datastore generated feature ids to the
     * features inserted to the repository database.
     * 
     * @param transactionExtraData
     * @param typeName
     * @param insertedFids
     * @see #updateFeatureIds(Map, List)
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void saveFidsInTransactionPlaceHolder(final Map transactionExtraData,
            final Name typeName, List<String> insertedFids) {

        List<KeyValue> insertsPlaceHolder;
        insertsPlaceHolder = (List<KeyValue>) transactionExtraData.get(GEOGIT_INSERTS_PLACEHOLDER);

        if (insertsPlaceHolder == null) {
            insertsPlaceHolder = new ArrayList<KeyValue>();
            transactionExtraData.put(GEOGIT_INSERTS_PLACEHOLDER, insertsPlaceHolder);
        }
        for (String uncommittedFid : insertedFids) {
            insertsPlaceHolder.add(new DefaultMapEntry(typeName, uncommittedFid));
        }
    }

    /**
     * @param transactionExtraData
     * @param eList
     * @return
     * @see #saveFidsInTransactionPlaceHolder(Map, Name, List)
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void updateFeatureIds(final Map transactionExtraData,
            final List<InsertedFeatureType> committedInserts) {

        final List<KeyValue> uncommittedFids;
        uncommittedFids = (List<KeyValue>) transactionExtraData.get(GEOGIT_INSERTS_PLACEHOLDER);

        List<FeatureId> committedFids = new ArrayList<FeatureId>();

        for (InsertedFeatureType insert : committedInserts) {
            List<FeatureId> finalIds = insert.getFeatureId();
            committedFids.addAll(finalIds);
        }

        if (uncommittedFids == null && committedInserts.size() > 0) {
            throw new IllegalStateException(
                    "The following feature ids were committed but not staged for versioning: "
                            + committedFids);
        }
        if (uncommittedFids.size() != committedInserts.size()) {
            throw new IllegalStateException("The list of staged(" + uncommittedFids.size()
                    + ") and committed(" + committedFids.size() + ") fids differ." + committedFids);
        }

        final int nInserts = uncommittedFids.size();

        Name typeName;
        String uncommittedFid;
        String committedFid;
        for (int i = 0; i < nInserts; i++) {
            {
                KeyValue uncommittedKvp = uncommittedFids.get(i);
                typeName = (Name) uncommittedKvp.getKey();
                uncommittedFid = (String) uncommittedKvp.getValue();
                FeatureId committed = committedFids.get(i);
                committedFid = committed.getID();
            }

            if (!committedFid.equals(uncommittedFid)) {
                geogitFacade.stageRename(typeName, uncommittedFid, committedFid);
            }
        }
    }

    private boolean isIgnorableEvent(final TransactionEvent event) {
        final TransactionEventType eventType = event.getType();
        if (!(eventType == PRE_INSERT || eventType == POST_UPDATE || eventType == PRE_DELETE)) {
            return true;
        }

        final Name featureTypeName = new NameImpl(event.getLayerName());
        if (!geogitFacade.isReplicated(featureTypeName)) {
            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.finest("Ignoring data store change " + event.getType() + " on type "
                        + featureTypeName + " as it's not replicated");
            }
            return true;
        }

        FeatureCollection<? extends FeatureType, ? extends Feature> affectedFeatures;
        affectedFeatures = event.getAffectedFeatures();
        if (affectedFeatures == null) {
            return true;
        }
        final int affectedFeatureCount = affectedFeatures.size();
        if (affectedFeatureCount <= 0) {
            return true;
        }
        return false;
    }

    /**
     * Stages the changes to any replicated feature type be committed or rolled back at
     * {@link #afterTransaction(TransactionType, boolean)}.
     * <p>
     * </p>
     * 
     * @param request
     *            the request about to be committed
     * @see org.geoserver.wfs.TransactionPlugin#beforeCommit(net.opengis.wfs.TransactionType)
     */
    public void beforeCommit(final TransactionType request) throws WFSException {
        if (!EMFUtils.isSet(request, "extendedProperties")) {
            request.setExtendedProperties(new HashMap<Object, Object>());
        }

        LOGGER.warning("beforeCommit: " + request);
    }

    /**
     * @see org.geoserver.wfs.TransactionPlugin#afterTransaction(net.opengis.wfs.TransactionType,
     *      boolean)
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void afterTransaction(final TransactionType request,
            final TransactionResponseType result, final boolean committed) {
        LOGGER.warning("afterTransaction: " + request);

        final Map transactionExtraData = request.getExtendedProperties();
        final String changeSetID = (String) transactionExtraData.get(GEOGIT_TRANSACTION_UUID);
        if (changeSetID == null) {
            // it was decided at a previous event that this transaction is of no interest
            return;
        }

        if (committed) {
            try {
                final InsertResultsType insertResults = result.getInsertResults();
                if (insertResults != null && insertResults.getFeature().size() > 0) {
                    updateFeatureIds(transactionExtraData, insertResults.getFeature());
                }
                String commitMsg = request.getHandle();
                if (commitMsg == null || commitMsg.trim().length() == 0) {
                    commitMsg = "Commit automatically accepted as it comes from a WFS transaction";
                }
                geogitFacade.commitChangeSet(changeSetID, commitMsg);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Exception committing to GEOGIT change set ID '"
                        + changeSetID + "'", e);
            }
        } else {
            try {
                geogitFacade.rollBackChangeSet(changeSetID);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Exception rolling back GEOGIT change set ID '"
                        + changeSetID + "'", e);
            }
        }
    }

    /**
     * @see org.geoserver.wfs.TransactionPlugin#getPriority()
     */
    public int getPriority() {
        return 0;
    }

}
