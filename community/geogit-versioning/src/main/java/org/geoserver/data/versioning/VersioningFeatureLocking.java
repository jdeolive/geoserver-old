package org.geoserver.data.versioning;

import java.io.IOException;

import org.geotools.data.FeatureLock;
import org.geotools.data.FeatureLocking;
import org.geotools.data.Query;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;

@SuppressWarnings("rawtypes")
public class VersioningFeatureLocking<T extends FeatureType,F extends Feature> extends VersioningFeatureStore<T,F> 
    implements FeatureLocking<T, F> {

    public VersioningFeatureLocking(FeatureLocking unversioned, VersioningDataAccess store) {
        super(unversioned, store);
    }

    @Override
    public void setFeatureLock(FeatureLock lock) {
        ((FeatureLocking) unversioned).setFeatureLock(lock);
    }

    @Override
    public int lockFeatures(Query query) throws IOException {
        return ((FeatureLocking) unversioned).lockFeatures(query);
    }

    @Override
    public int lockFeatures(Filter filter) throws IOException {
        return ((FeatureLocking) unversioned).lockFeatures(filter);
    }

    @Override
    public int lockFeatures() throws IOException {
        return ((FeatureLocking) unversioned).lockFeatures();
    }

    @Override
    public void unLockFeatures() throws IOException {
        ((FeatureLocking) unversioned).unLockFeatures();
    }

    @Override
    public void unLockFeatures(Filter filter) throws IOException {
        ((FeatureLocking) unversioned).unLockFeatures(filter);
    }

    @Override
    public void unLockFeatures(Query query) throws IOException {
        ((FeatureLocking) unversioned).unLockFeatures(query);
    }
}
