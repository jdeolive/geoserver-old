package org.geoserver.data.versioning.simple;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.geogit.repository.Repository;
import org.geoserver.data.versioning.VersioningDataAccess;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureLocking;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.FeatureWriter;
import org.geotools.data.LockingManager;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.simple.SimpleFeatureLocking;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.NameImpl;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;

public class VersioningDataStore extends VersioningDataAccess<SimpleFeatureType,SimpleFeature> implements DataStore {

    public VersioningDataStore(DataStore unversioned, Repository versioningRepo) {
        super(unversioned, versioningRepo);
    }

    @Override
    public void updateSchema(String typeName, SimpleFeatureType featureType) throws IOException {
        super.updateSchema(new NameImpl(typeName), featureType);
    }

    @Override
    public String[] getTypeNames() throws IOException {
        List<String> typeNames = new ArrayList();
        for (Name name : getNames()) {
            typeNames.add(name.getLocalPart());
        }
        return typeNames.toArray(new String[typeNames.size()]);
    }

    @Override
    public SimpleFeatureType getSchema(String typeName) throws IOException {
        return ((DataStore)unversioned).getSchema(typeName);
    }

    @Override
    public SimpleFeatureSource getFeatureSource(String typeName) throws IOException {
        return getFeatureSource(new NameImpl(typeName));
    }

    @Override
    public SimpleFeatureSource getFeatureSource(Name typeName)
            throws IOException {
        return (SimpleFeatureSource) super.getFeatureSource(typeName);
    }
    
    @Override
    public FeatureReader<SimpleFeatureType, SimpleFeature> getFeatureReader(Query query,
            Transaction transaction) throws IOException {
        throw new UnsupportedOperationException("TODO: implement");
    }

    @Override
    public FeatureWriter<SimpleFeatureType, SimpleFeature> getFeatureWriter(String typeName,
            Filter filter, Transaction transaction) throws IOException {
        throw new UnsupportedOperationException("TODO: implement");
    }

    @Override
    public FeatureWriter<SimpleFeatureType, SimpleFeature> getFeatureWriter(String typeName,
            Transaction transaction) throws IOException {
        throw new UnsupportedOperationException("TODO: implement");
    }

    @Override
    public FeatureWriter<SimpleFeatureType, SimpleFeature> getFeatureWriterAppend(String typeName,
            Transaction transaction) throws IOException {
        throw new UnsupportedOperationException("TODO: implement");
    }

    @Override
    public LockingManager getLockingManager() {
        throw new UnsupportedOperationException("TODO: implement");
    }

    @Override
    protected FeatureSource<SimpleFeatureType, SimpleFeature> createFeatureSource(
            FeatureSource<SimpleFeatureType, SimpleFeature> source) {
        return new SimpleVersioningFeatureSource((SimpleFeatureSource) source, this);
    }

    @Override
    protected FeatureStore<SimpleFeatureType, SimpleFeature> createFeatureStore(
            FeatureStore<SimpleFeatureType, SimpleFeature> store) {
        return new SimpleVersioningFeatureStore((SimpleFeatureStore) store, this);
    }

    @Override
    protected FeatureLocking<SimpleFeatureType, SimpleFeature> createFeatureLocking(
            FeatureLocking<SimpleFeatureType, SimpleFeature> locking) {
        return new SimpleVersioningFeatureLocking((FeatureLocking<SimpleFeatureType, SimpleFeature>) locking, this);
    }

}
