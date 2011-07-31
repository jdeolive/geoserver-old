package org.geoserver.data.versioning;

import java.io.IOException;
import java.util.List;

import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureStore;
import org.geotools.data.Transaction;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.Feature;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.identity.FeatureId;

@SuppressWarnings("rawtypes")
public class VersioningFeatureStore extends VersioningFeatureSource implements
        FeatureStore<FeatureType, Feature> {

    public VersioningFeatureStore(FeatureStore unversioned, VersioningDataAccess store) {
        super(unversioned, store);
    }

    @Override
    public List<FeatureId> addFeatures(FeatureCollection<FeatureType, Feature> collection)
            throws IOException {
        throw new UnsupportedOperationException("do versioning!");
    }

    @Override
    public void removeFeatures(Filter filter) throws IOException {
        throw new UnsupportedOperationException("do versioning!");
    }

    @Override
    public void modifyFeatures(Name[] attributeNames, Object[] attributeValues, Filter filter)
            throws IOException {
        throw new UnsupportedOperationException("do versioning!");
    }

    @Override
    public void modifyFeatures(AttributeDescriptor[] type, Object[] value, Filter filter)
            throws IOException {
        throw new UnsupportedOperationException("do versioning!");
    }

    @Override
    public void modifyFeatures(Name attributeName, Object attributeValue, Filter filter)
            throws IOException {
        throw new UnsupportedOperationException("do versioning!");
    }

    @Override
    public void modifyFeatures(AttributeDescriptor type, Object value, Filter filter)
            throws IOException {
        throw new UnsupportedOperationException("do versioning!");
    }

    @Override
    public void setFeatures(FeatureReader<FeatureType, Feature> reader) throws IOException {
        throw new UnsupportedOperationException("do versioning!");
    }

    @Override
    public void setTransaction(Transaction transaction) {
        throw new UnsupportedOperationException("do versioning!");
    }

    @Override
    public Transaction getTransaction() {
        return ((FeatureStore) unversioned).getTransaction();
    }

}
