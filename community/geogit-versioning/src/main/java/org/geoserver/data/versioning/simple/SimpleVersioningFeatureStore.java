package org.geoserver.data.versioning.simple;

import java.io.IOException;

import org.geogit.api.ObjectId;
import org.geoserver.data.versioning.VersioningDataAccess;
import org.geoserver.data.versioning.VersioningFeatureStore;
import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.NameImpl;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;

public class SimpleVersioningFeatureStore extends
        VersioningFeatureStore<SimpleFeatureType, SimpleFeature> implements SimpleFeatureStore {

    /**
     * @param unversioned
     * @param store
     */
    public SimpleVersioningFeatureStore(SimpleFeatureStore unversioned, VersioningDataStore store) {
        super(unversioned, store);
    }

    /**
     * @see org.geotools.data.simple.SimpleFeatureStore#modifyFeatures(java.lang.String,
     *      java.lang.Object, org.opengis.filter.Filter)
     */
    @Override
    public void modifyFeatures(String name, Object attributeValue, Filter filter)
            throws IOException {

        Name attributeName = new NameImpl(getSchema().getName().getNamespaceURI(), name);
        super.modifyFeatures(attributeName, attributeValue, filter);
    }

    /**
     * @see org.geotools.data.simple.SimpleFeatureStore#modifyFeatures(java.lang.String[],
     *      java.lang.Object[], org.opengis.filter.Filter)
     */
    @Override
    public void modifyFeatures(String[] names, Object[] attributeValues, Filter filter)
            throws IOException {

        Name[] attributeNames = new Name[names.length];
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            attributeNames[i] = new NameImpl(getSchema().getName().getNamespaceURI(), name);
        }
        super.modifyFeatures(attributeNames, attributeValues, filter);
    }

    /**
     * @see SimpleFeatureStore#getFeatures()
     */
    @Override
    public SimpleFeatureCollection getFeatures() throws IOException {
        return (SimpleFeatureCollection) super.getFeatures();
    }

    /**
     * @see org.geoserver.data.versioning.VersioningFeatureSource#getFeatures(org.opengis.filter.Filter)
     */
    @Override
    public SimpleFeatureCollection getFeatures(Filter filter) throws IOException {
        return (SimpleFeatureCollection) super.getFeatures(filter);
    }

    /**
     * @see org.geoserver.data.versioning.VersioningFeatureSource#getFeatures(org.geotools.data.Query)
     */
    @Override
    public SimpleFeatureCollection getFeatures(Query query) throws IOException {
        return (SimpleFeatureCollection) super.getFeatures(query);
    }

    /**
     * @see org.geoserver.data.versioning.VersioningFeatureSource#createFeatureCollection(org.geotools.feature.FeatureCollection,
     *      org.geoserver.data.versioning.VersioningDataAccess, org.geogit.api.ObjectId)
     */
    @Override
    protected FeatureCollection<SimpleFeatureType, SimpleFeature> createFeatureCollection(
            FeatureCollection<SimpleFeatureType, SimpleFeature> delegate,
            VersioningDataAccess store, ObjectId currentCommitId) {
        return new SimpleResourceIdAssigningFeatureCollection((SimpleFeatureCollection) delegate,
                (VersioningDataStore) store, currentCommitId);
    }
}
