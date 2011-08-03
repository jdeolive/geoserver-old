package org.geoserver.data.versioning;

import static org.geotools.data.Transaction.AUTO_COMMIT;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureStore;
import org.geotools.data.Transaction;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Feature;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.Id;
import org.opengis.filter.identity.FeatureId;
import org.opengis.filter.identity.Identifier;

import com.google.common.base.Throwables;

@SuppressWarnings("rawtypes")
public class VersioningFeatureStore<T extends FeatureType, F extends Feature> extends VersioningFeatureSource<T,F> 
    implements FeatureStore<T, F> {

    public VersioningFeatureStore(final FeatureStore unversioned, final VersioningDataAccess store) {
        super(unversioned, store);
    }

    @Override
    public Transaction getTransaction() {
        return ((FeatureStore) unversioned).getTransaction();
    }

    @Override
    public void setTransaction(final Transaction transaction) {
        ((FeatureStore) unversioned).setTransaction(transaction);

        if (isVersioned()) {
            checkTransaction();
        }

    }

    private FeatureStore<T, F> getStore() {
        return ((FeatureStore<T, F>) unversioned);
    }

    @Override
    public List<FeatureId> addFeatures(FeatureCollection<T, F> collection) throws IOException {
        final FeatureStore<T, F> unversioned = getStore();
        List<FeatureId> featureIds = unversioned.addFeatures(collection);

        if (isVersioned()) {
            checkTransaction();
            try {
                Name typeName = getSchema().getName();
                getVersioningState().stageInsert(typeName, collection);
            } catch (Exception e) {
                Throwables.propagate(e);
            }
        }
        return featureIds;
    }

    @Override
    public void removeFeatures(Filter filter) throws IOException {
        final FeatureStore<T, F> unversioned = getStore();
        if (isVersioned()) {
            checkTransaction();

            FeatureCollection<T, F> removed = unversioned.getFeatures(filter);
            try {
                Name typeName = getSchema().getName();
                getVersioningState().stageDelete(typeName, filter, removed);
            } catch (Exception e) {
                Throwables.propagate(e);
            }
        }
        unversioned.removeFeatures(filter);
    }

    @Override
    public void modifyFeatures(final Name[] attributeNames, final Object[] attributeValues, final Filter filter)
            throws IOException {
        final FeatureStore<T, F> unversioned = getStore();
        final boolean versioned = isVersioned();
        Id affectedFeaturesFitler = null;
        if (versioned) {
            checkTransaction();
            final FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);
            FeatureCollection<T, F> affectedFeatures = getStore().getFeatures(filter);
            FeatureIterator<F> iterator = affectedFeatures.features();
            Set<Identifier> affectedIds = new HashSet<Identifier>();
            try {
                while (iterator.hasNext()) {
                    affectedIds.add(iterator.next().getIdentifier());
                }
            } finally {
                iterator.close();
            }
            affectedFeaturesFitler = ff.id(affectedIds);
        }

        unversioned.modifyFeatures(attributeNames, attributeValues, filter);

        if (versioned && affectedFeaturesFitler != null
                && affectedFeaturesFitler.getIdentifiers().size() > 0) {
            try {
                FeatureCollection newValues = unversioned.getFeatures(affectedFeaturesFitler);
                getVersioningState().stageUpdate(newValues);
            } catch (Exception e) {
                Throwables.propagate(e);
            }
        }
    }

    /**
     * @see #modifyFeatures(Name[], Object[], Filter)
     */
    @Override
    public void modifyFeatures(AttributeDescriptor[] type, Object[] value, Filter filter)
            throws IOException {

        Name[] attributeNames = new Name[type.length];
        for (int i = 0; i < type.length; i++) {
            attributeNames[i] = type[i].getName();
        }
        modifyFeatures(attributeNames, value, filter);
    }

    /**
     * @see #modifyFeatures(Name[], Object[], Filter)
     */
    @Override
    public void modifyFeatures(Name attributeName, Object attributeValue, Filter filter)
            throws IOException {

        modifyFeatures(new Name[] { attributeName }, new Object[] { attributeValue }, filter);
    }

    /**
     * @see #modifyFeatures(Name[], Object[], Filter)
     */
    @Override
    public void modifyFeatures(AttributeDescriptor type, Object value, Filter filter)
            throws IOException {

        modifyFeatures(new Name[] { type.getName() }, new Object[] { value }, filter);

    }

    /**
     * @see org.geotools.data.FeatureStore#setFeatures(org.geotools.data.FeatureReader)
     */
    @Override
    public void setFeatures(FeatureReader<T, F> reader) throws IOException {
        final FeatureStore<T, F> unversioned = getStore();
        unversioned.setFeatures(reader);
        if (isVersioned()) {
            checkTransaction();
            throw new UnsupportedOperationException("do versioning!");
        }
    }

    private void checkTransaction() {
        if (Transaction.AUTO_COMMIT.equals(getTransaction())) {
            throw new UnsupportedOperationException(
                    "AUTO_COMMIT is not supported for versioned Feature Types");
        }
    }

    protected VersioningTransactionState getVersioningState() {
        Transaction transaction = getTransaction();
        if (AUTO_COMMIT.equals(transaction)) {
            return VersioningTransactionState.VOID;
        }

        Object key = "WHAT_WOULD_BE_A_GOOD_KEY?";
        VersioningTransactionState state = (VersioningTransactionState) transaction.getState(key);
        if (state == null) {
            state = store.newTransactionState();
            transaction.putState(key, state);
        }
        return state;
    }

}
