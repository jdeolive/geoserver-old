package org.geoserver.wfs.versioning;

import java.io.IOException;
import java.util.List;

import org.geogit.repository.Repository;
import org.geotools.data.CollectionFeatureReader;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureLocking;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureStore;
import org.geotools.data.FeatureWriter;
import org.geotools.data.LockingManager;
import org.geotools.data.Query;
import org.geotools.data.ServiceInfo;
import org.geotools.data.Transaction;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.springframework.util.Assert;

public class VersioningDataStore implements DataStore {

    private DataStore unversioned;

    private Repository versioningRepo;

    public VersioningDataStore(DataStore unversioned, Repository versioningRepo) {
        Assert.notNull(unversioned);
        Assert.notNull(versioningRepo);
        Assert.isTrue(!(unversioned instanceof VersioningDataStore));
        this.unversioned = unversioned;
        this.versioningRepo = versioningRepo;
    }

    @Override
    public void dispose() {
        if (unversioned != null) {
            unversioned.dispose();
            unversioned = null;
        }
    }

    /**
     * @see org.geotools.data.DataStore#getFeatureSource(java.lang.String)
     */
    @Override
    public SimpleFeatureSource getFeatureSource(String typeName) throws IOException {
        SimpleFeatureSource source = unversioned.getFeatureSource(typeName);
        if (source instanceof FeatureLocking) {
            return new VersioningFeatureLocking(source, this);
        } else if (source instanceof FeatureStore) {
            return new VersioningFeatureStore(source, this);
        }
        return new VersioningFeatureSource(source, this);
    }

    /**
     * @see org.geotools.data.DataStore#getFeatureSource(org.opengis.feature.type.Name)
     */
    @Override
    public SimpleFeatureSource getFeatureSource(Name typeName) throws IOException {
        SimpleFeatureSource source = unversioned.getFeatureSource(typeName);
        if (source instanceof FeatureLocking) {
            return new VersioningFeatureLocking(source, this);
        } else if (source instanceof FeatureStore) {
            return new VersioningFeatureStore(source, this);
        }
        return new VersioningFeatureSource(source, this);
    }

    /**
     * @see org.geotools.data.DataStore#getFeatureReader(org.geotools.data.Query,
     *      org.geotools.data.Transaction)
     */
    @Override
    public FeatureReader<SimpleFeatureType, SimpleFeature> getFeatureReader(Query query,
            Transaction transaction) throws IOException {

        SimpleFeatureSource source = getFeatureSource(query.getTypeName());
        if (source instanceof FeatureStore) {
            ((FeatureStore) source).setTransaction(transaction);
        }
        SimpleFeatureCollection features = source.getFeatures(query);
        SimpleFeatureType type = features.getSchema();
        return new CollectionFeatureReader(features, type);
    }

    /**
     * @see org.geotools.data.DataStore#getFeatureWriter(java.lang.String,
     *      org.opengis.filter.Filter, org.geotools.data.Transaction)
     */
    @Override
    public FeatureWriter<SimpleFeatureType, SimpleFeature> getFeatureWriter(String typeName,
            Filter filter, Transaction transaction) throws IOException {
        throw new UnsupportedOperationException("do versioning!");
    }

    /**
     * @see org.geotools.data.DataStore#getFeatureWriter(java.lang.String,
     *      org.geotools.data.Transaction)
     */
    @Override
    public FeatureWriter<SimpleFeatureType, SimpleFeature> getFeatureWriter(String typeName,
            Transaction transaction) throws IOException {
        throw new UnsupportedOperationException("do versioning!");
    }

    /**
     * @see org.geotools.data.DataStore#getFeatureWriterAppend(java.lang.String,
     *      org.geotools.data.Transaction)
     */
    @Override
    public FeatureWriter<SimpleFeatureType, SimpleFeature> getFeatureWriterAppend(String typeName,
            Transaction transaction) throws IOException {

        throw new UnsupportedOperationException("do versioning!");
    }

    @Override
    public ServiceInfo getInfo() {
        return unversioned.getInfo();
    }

    @Override
    public void createSchema(SimpleFeatureType featureType) throws IOException {
        unversioned.createSchema(featureType);
    }

    @Override
    public void updateSchema(Name typeName, SimpleFeatureType featureType) throws IOException {
        unversioned.updateSchema(typeName, featureType);
    }

    @Override
    public List<Name> getNames() throws IOException {
        return unversioned.getNames();
    }

    @Override
    public SimpleFeatureType getSchema(Name name) throws IOException {
        return unversioned.getSchema(name);
    }

    @Override
    public void updateSchema(String typeName, SimpleFeatureType featureType) throws IOException {
        unversioned.updateSchema(typeName, featureType);
    }

    @Override
    public String[] getTypeNames() throws IOException {
        return unversioned.getTypeNames();
    }

    @Override
    public SimpleFeatureType getSchema(String typeName) throws IOException {
        return unversioned.getSchema(typeName);
    }

    @Override
    public LockingManager getLockingManager() {

        return unversioned.getLockingManager();
    }

}
