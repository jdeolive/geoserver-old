package org.geoserver.data.versioning.simple;

import java.io.IOException;

import org.geogit.api.ObjectId;
import org.geoserver.data.versioning.VersioningDataAccess;
import org.geoserver.data.versioning.VersioningFeatureSource;
import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;


public class SimpleVersioningFeatureSource extends VersioningFeatureSource<SimpleFeatureType, SimpleFeature>
    implements SimpleFeatureSource{

    
    public SimpleVersioningFeatureSource(SimpleFeatureSource unversioned, VersioningDataStore store) {
        super(unversioned, store);
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
