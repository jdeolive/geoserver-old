package org.geoserver.wfs.versioning;

import java.io.IOException;
import java.util.List;

import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureStore;
import org.geotools.data.Transaction;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.identity.FeatureId;

public class VersioningFeatureStore extends VersioningFeatureSource implements SimpleFeatureStore {

    public VersioningFeatureStore(SimpleFeatureSource unversioned, VersioningDataStore store) {
        super(unversioned, store);
    }

    /**
     * @see org.geotools.data.FeatureStore#addFeatures(org.geotools.feature.FeatureCollection)
     */
    @Override
    public List<FeatureId> addFeatures(
            FeatureCollection<SimpleFeatureType, SimpleFeature> collection) throws IOException {
        throw new UnsupportedOperationException("do versioning!");
    }

    /**
     * @see org.geotools.data.FeatureStore#removeFeatures(org.opengis.filter.Filter)
     */
    @Override
    public void removeFeatures(Filter filter) throws IOException {
        throw new UnsupportedOperationException("do versioning!");
    }

    /**
     * @see org.geotools.data.FeatureStore#modifyFeatures(org.opengis.feature.type.Name[],
     *      java.lang.Object[], org.opengis.filter.Filter)
     */
    @Override
    public void modifyFeatures(Name[] attributeNames, Object[] attributeValues, Filter filter)
            throws IOException {
        throw new UnsupportedOperationException("do versioning!");
    }

    /**
     * @see org.geotools.data.FeatureStore#modifyFeatures(org.opengis.feature.type.AttributeDescriptor[],
     *      java.lang.Object[], org.opengis.filter.Filter)
     */
    @Override
    public void modifyFeatures(AttributeDescriptor[] type, Object[] value, Filter filter)
            throws IOException {
        throw new UnsupportedOperationException("do versioning!");
    }

    /**
     * @see org.geotools.data.FeatureStore#modifyFeatures(org.opengis.feature.type.Name,
     *      java.lang.Object, org.opengis.filter.Filter)
     */
    @Override
    public void modifyFeatures(Name attributeName, Object attributeValue, Filter filter)
            throws IOException {
        throw new UnsupportedOperationException("do versioning!");
    }

    /**
     * @see org.geotools.data.FeatureStore#modifyFeatures(org.opengis.feature.type.AttributeDescriptor,
     *      java.lang.Object, org.opengis.filter.Filter)
     */
    @Override
    public void modifyFeatures(AttributeDescriptor type, Object value, Filter filter)
            throws IOException {
        throw new UnsupportedOperationException("do versioning!");
    }

    /**
     * @see org.geotools.data.FeatureStore#setFeatures(org.geotools.data.FeatureReader)
     */
    @Override
    public void setFeatures(FeatureReader<SimpleFeatureType, SimpleFeature> reader)
            throws IOException {
        throw new UnsupportedOperationException("do versioning!");
    }

    /**
     * @see org.geotools.data.simple.SimpleFeatureStore#modifyFeatures(java.lang.String,
     *      java.lang.Object, org.opengis.filter.Filter)
     */
    @Override
    public void modifyFeatures(String name, Object attributeValue, Filter filter)
            throws IOException {
        throw new UnsupportedOperationException("do versioning!");
    }

    /**
     * @see org.geotools.data.simple.SimpleFeatureStore#modifyFeatures(java.lang.String[],
     *      java.lang.Object[], org.opengis.filter.Filter)
     */
    @Override
    public void modifyFeatures(String[] names, Object[] attributeValues, Filter filter)
            throws IOException {
        throw new UnsupportedOperationException("do versioning!");
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void setTransaction(Transaction transaction) {
        ((FeatureStore) unversioned).setTransaction(transaction);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Transaction getTransaction() {
        return ((FeatureStore) unversioned).getTransaction();
    }
}
