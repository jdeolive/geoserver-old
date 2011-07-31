package org.geoserver.data.versioning;

import static org.geoserver.data.versioning.ResourceIdFilterExtractor.getVersioningFilter;

import java.awt.RenderingHints.Key;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import org.geotools.data.DataAccess;
import org.geotools.data.FeatureListener;
import org.geotools.data.Query;
import org.geotools.data.QueryCapabilities;
import org.geotools.data.ResourceInfo;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.Id;

public class VersioningFeatureSource implements SimpleFeatureSource {

    protected final SimpleFeatureSource unversioned;

    protected final VersioningDataStore store;

    public VersioningFeatureSource(SimpleFeatureSource unversioned, VersioningDataStore store) {
        this.unversioned = unversioned;
        this.store = store;
    }

    public boolean isVersioned() {
        return store.isVersioned(getName());
    }

    /**
     * @see org.geotools.data.FeatureSource#getDataStore()
     */
    @Override
    public DataAccess<SimpleFeatureType, SimpleFeature> getDataStore() {
        return store;
    }

    /**
     * @see org.geotools.data.FeatureSource#getBounds(org.geotools.data.Query)
     */
    @Override
    public ReferencedEnvelope getBounds(Query query) throws IOException {

        Id versioningFilter;
        if (!isVersioned() || null == (versioningFilter = getVersioningFilter(query.getFilter()))) {
            return unversioned.getBounds(query);
        }

        return store.getBounds(getName(), versioningFilter, query);
    }

    /**
     * @see org.geotools.data.FeatureSource#getCount(org.geotools.data.Query)
     */
    @Override
    public int getCount(Query query) throws IOException {
        Id versioningFilter;
        if (!isVersioned() || null == (versioningFilter = getVersioningFilter(query.getFilter()))) {
            return unversioned.getCount(query);
        }
        return store.getCount(getName(), versioningFilter, query);
    }

    /**
     * @see org.geotools.data.simple.SimpleFeatureSource#getFeatures(org.opengis.filter.Filter)
     */
    @Override
    public SimpleFeatureCollection getFeatures(Filter filter) throws IOException {
        Id versioningFilter;
        if (!isVersioned() || null == (versioningFilter = getVersioningFilter(filter))) {
            return unversioned.getFeatures(filter);
        }
        return store.getFeatures(getName(), versioningFilter, namedQuery(filter));
    }

    private Query namedQuery(Filter filter) {
        Name name = getName();
        String typeName = name.getLocalPart();
        URI namespace;
        try {
            namespace = new URI(name.getNamespaceURI());
        } catch (URISyntaxException e) {
            namespace = null;
        }
        int maxFeartures = Integer.MAX_VALUE;
        String[] propNames = null;
        String handle = null;
        Query query = new Query(typeName, namespace, filter, maxFeartures, propNames, handle);
        return query;
    }

    /**
     * @see org.geotools.data.simple.SimpleFeatureSource#getFeatures(org.geotools.data.Query)
     */
    @Override
    public SimpleFeatureCollection getFeatures(Query query) throws IOException {
        Id versioningFilter;
        if (!isVersioned() || null == (versioningFilter = getVersioningFilter(query.getFilter()))) {
            return unversioned.getFeatures(query);
        }
        return store.getFeatures(getName(), versioningFilter, query);
    }

    // / directly deferred methods

    @Override
    public Name getName() {
        return unversioned.getName();
    }

    @Override
    public ResourceInfo getInfo() {
        return unversioned.getInfo();
    }

    @Override
    public QueryCapabilities getQueryCapabilities() {
        return unversioned.getQueryCapabilities();
    }

    @Override
    public void addFeatureListener(FeatureListener listener) {
        unversioned.addFeatureListener(listener);
    }

    @Override
    public void removeFeatureListener(FeatureListener listener) {
        unversioned.removeFeatureListener(listener);
    }

    @Override
    public SimpleFeatureType getSchema() {
        return unversioned.getSchema();
    }

    @Override
    public ReferencedEnvelope getBounds() throws IOException {
        return unversioned.getBounds();
    }

    @Override
    public Set<Key> getSupportedHints() {
        return unversioned.getSupportedHints();
    }

    @Override
    public SimpleFeatureCollection getFeatures() throws IOException {
        return unversioned.getFeatures();
    }

}
