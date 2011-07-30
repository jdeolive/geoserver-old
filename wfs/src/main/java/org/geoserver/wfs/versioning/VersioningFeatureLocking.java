package org.geoserver.wfs.versioning;

import java.io.IOException;

import org.geotools.data.FeatureLock;
import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureLocking;
import org.geotools.data.simple.SimpleFeatureSource;
import org.opengis.filter.Filter;

public class VersioningFeatureLocking extends VersioningFeatureStore implements
        SimpleFeatureLocking {

    public VersioningFeatureLocking(SimpleFeatureSource unversioned, VersioningDataStore store) {
        super(unversioned, store);
    }

    @Override
    public int lockFeatures(Query query) throws IOException {
        // REVISIT: can this query contain a versioning predicate?
        //return ((SimpleFeatureLocking) unversioned).lockFeatures();
        throw new UnsupportedOperationException("do versioning!");
    }

    @Override
    public int lockFeatures(Filter filter) throws IOException {
        // REVISIT: can this query contain a versioning predicate?
        //return ((SimpleFeatureLocking) unversioned).lockFeatures(filter);
        throw new UnsupportedOperationException("do versioning!");
    }

    @Override
    public void unLockFeatures(Filter filter) throws IOException {
        // REVISIT: can this query contain a versioning predicate?
        //((SimpleFeatureLocking) unversioned).unLockFeatures(filter);
        throw new UnsupportedOperationException("do versioning!");
    }

    @Override
    public void unLockFeatures(Query query) throws IOException {
        // REVISIT: can this query contain a versioning predicate?
        //((SimpleFeatureLocking) unversioned).unLockFeatures(query);
        throw new UnsupportedOperationException("do versioning!");
    }

    @Override
    public void setFeatureLock(FeatureLock lock) {
        ((SimpleFeatureLocking) unversioned).setFeatureLock(lock);
    }

    @Override
    public int lockFeatures() throws IOException {
        return ((SimpleFeatureLocking) unversioned).lockFeatures();
    }

    @Override
    public void unLockFeatures() throws IOException {
        ((SimpleFeatureLocking) unversioned).unLockFeatures();
    }

}
