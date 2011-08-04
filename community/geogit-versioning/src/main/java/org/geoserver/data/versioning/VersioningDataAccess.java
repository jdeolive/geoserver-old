package org.geoserver.data.versioning;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.geogit.api.GeoGIT;
import org.geogit.api.ObjectId;
import org.geogit.api.Ref;
import org.geogit.api.RevCommit;
import org.geogit.api.RevTree;
import org.geogit.repository.Repository;
import org.geotools.data.DataAccess;
import org.geotools.data.FeatureLocking;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.Query;
import org.geotools.data.ServiceInfo;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Id;
import org.opengis.filter.identity.Identifier;
import org.opengis.filter.identity.ResourceId;
import org.springframework.util.Assert;

import com.google.common.base.Throwables;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class VersioningDataAccess<T extends FeatureType, F extends Feature> implements
        DataAccess<T, F> {

    protected DataAccess<T, F> unversioned;

    protected Repository repository;

    public VersioningDataAccess(DataAccess unversioned, Repository versioningRepo) {
        Assert.notNull(unversioned);
        Assert.notNull(versioningRepo);
        Assert.isTrue(!(unversioned instanceof VersioningDataAccess));
        this.unversioned = unversioned;
        this.repository = versioningRepo;
    }

    public boolean isVersioned(Name name) {
        boolean isVersioned = repository.getWorkingTree().hasRoot(name);
        return isVersioned;
    }

    /**
     * @see org.geotools.data.DataAccess#dispose()
     */
    @Override
    public void dispose() {
        if (unversioned != null) {
            unversioned.dispose();
            unversioned = null;
        }
    }

    /**
     * @see org.geotools.data.DataAccess#getFeatureSource(org.opengis.feature.type.Name)
     */
    @Override
    public FeatureSource<T, F> getFeatureSource(Name typeName) throws IOException {
        FeatureSource source = unversioned.getFeatureSource(typeName);
        if (source instanceof FeatureLocking) {
            return createFeatureLocking((FeatureLocking) source);
        } else if (source instanceof FeatureStore) {
            return createFeatureStore((FeatureStore) source);
        }
        return createFeatureSource(source);
    }

    /**
     * @see org.geotools.data.DataAccess#getInfo()
     */
    @Override
    public ServiceInfo getInfo() {
        return unversioned.getInfo();
    }

    /**
     * @see org.geotools.data.DataAccess#createSchema(org.opengis.feature.type.FeatureType)
     */
    @Override
    public void createSchema(T featureType) throws IOException {
        unversioned.createSchema(featureType);
        try {
            repository.getWorkingTree().init(featureType);
            GeoGIT ggit = new GeoGIT(repository);
            ggit.add().call();
            ggit.commit().call();
        } catch (Exception e) {
            Throwables.propagate(e);
        }
    }

    /**
     * @see org.geotools.data.DataAccess#updateSchema(org.opengis.feature.type.Name,
     *      org.opengis.feature.type.FeatureType)
     */
    @Override
    public void updateSchema(Name typeName, T featureType) throws IOException {
        unversioned.updateSchema(typeName, featureType);
    }

    /**
     * @see org.geotools.data.DataAccess#getNames()
     */
    @Override
    public List<Name> getNames() throws IOException {
        return unversioned.getNames();
    }

    /**
     * @see org.geotools.data.DataAccess#getSchema(org.opengis.feature.type.Name)
     */
    @Override
    public T getSchema(Name name) throws IOException {
        return unversioned.getSchema(name);
    }

    /**
     * @precondition {@code typeName != null && versioningFilter != null}
     * @precondition {@code versioningFilter.getIdentifiers().size() > 0}
     * @postcondition {@code $return != null}
     * 
     * @param typeName
     * @param versioningFilter
     * @param extraQuery
     * @return
     * @throws IOException
     */
    public FeatureCollection getFeatures(final Name typeName, final Id versioningFilter,
            final Query extraQuery) throws IOException {
        Assert.notNull(typeName);
        Assert.notNull(versioningFilter);
        Assert.isTrue(versioningFilter.getIdentifiers().size() > 0);

        final Set<Identifier> identifiers = versioningFilter.getIdentifiers();
        final Set<ResourceId> resourceIds = new HashSet<ResourceId>();
        for (Identifier id : identifiers) {
            if (id instanceof ResourceId) {
                resourceIds.add((ResourceId) id);
            }
        }
        if (resourceIds.size() == 0) {
            throw new IllegalArgumentException("At least one " + ResourceId.class.getName()
                    + " should be provided: " + identifiers);
        }

        final FeatureType featureType = this.getSchema(typeName);
        ResourceIdFeatureCollector versionQuery;
        versionQuery = new ResourceIdFeatureCollector(repository, featureType, resourceIds);

        DefaultFeatureCollection features = new DefaultFeatureCollection(null,
                (SimpleFeatureType) featureType);
        for (Feature f : versionQuery) {
            features.add((SimpleFeature) f);
        }
        return features;
    }

    /**
     * @return the object id of the current HEAD's commit
     */
    public ObjectId getCurrentVersion() {
        // assume HEAD is at MASTER
        try {
            Iterator<RevCommit> lastCommit = new GeoGIT(repository).log().setLimit(1).call();
            if (lastCommit.hasNext()) {
                return lastCommit.next().getId();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    /**
     * Finds out the version (Feature hash) of the Feature addressed by {@code typeName/featureId}
     * at the commit {@code commitId}
     * 
     * @param typeName
     * @param featureId
     * @param commitId
     * @return
     */
    public String getFeatureVersion(final Name typeName, final String featureId,
            final ObjectId commitId) {

        final RevCommit commit = repository.getCommit(commitId);
        if (commit.getTreeId().isNull()) {
            return null;
        }
        final RevTree tree = repository.getTree(commit.getTreeId());
        final List<String> path = path(typeName, featureId);
        Ref featureObjectRef = repository.getObjectDatabase().getTreeChild(tree, path);
        return featureObjectRef == null ? null : featureObjectRef.getObjectId().toString();
    }

    private List<String> path(final Name typeName, final String featureId) {

        List<String> path = new ArrayList<String>(3);

        if (null != typeName.getNamespaceURI()) {
            path.add(typeName.getNamespaceURI());
        }
        path.add(typeName.getLocalPart());
        path.add(featureId);

        return path;
    }

    public VersioningTransactionState newTransactionState() {
        return new VersioningTransactionState(new GeoGIT(repository));
    }

    protected FeatureSource<T, F> createFeatureSource(FeatureSource<T, F> source) {
        return new VersioningFeatureSource(source, repository);
    }

    protected FeatureStore<T, F> createFeatureStore(FeatureStore<T, F> store) {
        return new VersioningFeatureStore(store, repository);
    }

    protected FeatureLocking<T, F> createFeatureLocking(FeatureLocking<T, F> locking) {
        return new VersioningFeatureLocking(locking, repository);
    }
}
