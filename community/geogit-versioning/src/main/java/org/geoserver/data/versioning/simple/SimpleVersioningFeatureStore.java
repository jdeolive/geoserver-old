package org.geoserver.data.versioning.simple;

import java.io.IOException;

import org.geogit.api.ObjectId;
import org.geoserver.data.versioning.VersioningDataAccess;
import org.geoserver.data.versioning.VersioningFeatureStore;
import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;

public class SimpleVersioningFeatureStore extends VersioningFeatureStore<SimpleFeatureType,SimpleFeature> 
    implements SimpleFeatureStore {

    public SimpleVersioningFeatureStore(SimpleFeatureStore unversioned, VersioningDataStore store) {
        super(unversioned, store);
    }

    @Override
    public void modifyFeatures(String name, Object attributeValue, Filter filter)
            throws IOException {
        //TODO: implement
        throw new UnsupportedOperationException();
    }

    @Override
    public void modifyFeatures(String[] names, Object[] attributeValues, Filter filter)
            throws IOException {
      //TODO: implement
        throw new UnsupportedOperationException();
    }

    @Override
    public SimpleFeatureCollection getFeatures() throws IOException {
        return (SimpleFeatureCollection) super.getFeatures();
    }

    @Override
    public SimpleFeatureCollection getFeatures(Filter filter)
            throws IOException {
        return (SimpleFeatureCollection) super.getFeatures(filter);
    }

    @Override
    public SimpleFeatureCollection getFeatures(Query query)
            throws IOException {
        return (SimpleFeatureCollection) super.getFeatures(query);
    }

    @Override
    protected FeatureCollection<SimpleFeatureType, SimpleFeature> createFeatureCollection(
            FeatureCollection<SimpleFeatureType, SimpleFeature> delegate,
            VersioningDataAccess store, ObjectId currentCommitId) {
        return new SimpleResourceIdAssigningFeatureCollection((SimpleFeatureCollection)delegate, 
            (VersioningDataStore) store, currentCommitId);
    }
}
