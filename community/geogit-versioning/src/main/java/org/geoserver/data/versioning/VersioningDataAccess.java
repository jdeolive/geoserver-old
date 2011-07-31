package org.geoserver.data.versioning;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.geogit.api.GeoGIT;
import org.geogit.api.RevCommit;
import org.geogit.repository.Repository;
import org.geotools.data.DataAccess;
import org.geotools.data.FeatureLocking;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.Query;
import org.geotools.data.ServiceInfo;
import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Id;
import org.opengis.filter.identity.Identifier;
import org.opengis.filter.identity.ResourceId;
import org.springframework.util.Assert;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class VersioningDataAccess implements DataAccess<FeatureType, Feature> {

    private DataAccess unversioned;

    private Repository repository;

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
    public FeatureSource getFeatureSource(Name typeName) throws IOException {
        FeatureSource source = unversioned.getFeatureSource(typeName);
        if (source instanceof FeatureLocking) {
            return new VersioningFeatureLocking((FeatureLocking) source, this);
        } else if (source instanceof FeatureStore) {
            return new VersioningFeatureStore((FeatureStore) source, this);
        }
        return new VersioningFeatureSource(source, this);
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
    public void createSchema(FeatureType featureType) throws IOException {
        unversioned.createSchema(featureType);
    }

    /**
     * @see org.geotools.data.DataAccess#updateSchema(org.opengis.feature.type.Name,
     *      org.opengis.feature.type.FeatureType)
     */
    @Override
    public void updateSchema(Name typeName, FeatureType featureType) throws IOException {
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
    public FeatureType getSchema(Name name) throws IOException {
        return unversioned.getSchema(name);
    }

    public ReferencedEnvelope getBounds(Name name, Id versioningFilter, Query query) {
        // TODO Auto-generated method stub
        return null;
    }

    public int getCount(Name name, Id versioningFilter, Query query) {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * @param typeName
     * @param versioningFilter
     * @param query
     * @return
     * @throws IOException
     */
    public FeatureCollection<FeatureType, Feature> getFeatures(Name typeName, Id versioningFilter,
            Query query) throws IOException {
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
        // return new ResourceIdFeatureCollector(repository, featureType, resourceIds, query);
        return null;
    }

    public String getCurrentVersion() {
        // assume HEAD is at MASTER
        try {
            Iterator<RevCommit> lastCommit = new GeoGIT(repository).log().setLimit(1).call();
            if (lastCommit.hasNext()) {
                return lastCommit.next().getId().toString();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

}
