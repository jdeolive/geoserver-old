package org.geoserver.data.versioning;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.geogit.api.ObjectId;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.collection.DecoratingFeatureCollection;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.identity.ResourceId;

import com.google.common.collect.AbstractIterator;

/**
 * FeatureCollectionDecorator that assigns as {@link ResourceId} as each Feature
 * {@link Feature#getIdentifier() identifier} from the {@link ObjectId} of the current state of the
 * Feature.
 * 
 * @author groldan
 * 
 */
public class ResourceIdAssigningFeatureCollection<T extends FeatureType, F extends Feature> extends
        DecoratingFeatureCollection<T, F> implements FeatureCollection<T, F> {

    private final ObjectId commitId;

    private final VersioningFeatureSource<T, F> store;

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected ResourceIdAssigningFeatureCollection(final FeatureCollection delegate,
            final VersioningFeatureSource<T, F> store, final ObjectId commitId) {

        super(delegate);

        this.store = store;
        this.commitId = commitId;
    }

    private class ResourceIdAssigningIterator extends AbstractIterator<F> {

        private final Iterator<F> iterator;

        public ResourceIdAssigningIterator(final Iterator<F> iterator) {
            this.iterator = iterator;
        }

        @Override
        protected F computeNext() {
            if (!iterator.hasNext()) {
                return endOfData();
            }
            Feature next = iterator.next();
            Name typeName = next.getType().getName();
            String featureId = next.getIdentifier().getID();
            String versionId = store.getFeatureVersion(typeName, featureId, commitId);
            return (F) VersionedFeatureWrapper.wrap(next, versionId);
        }

    }

    @Override
    public Iterator<F> iterator() {
        @SuppressWarnings("deprecation")
        Iterator<F> iterator = delegate.iterator();
        return new ResourceIdAssigningIterator(iterator);
    }

    protected class ResourceIdAssigningFeatureIterator<G extends F> implements FeatureIterator<G> {

        protected FeatureIterator<G> features;

        protected ResourceIdAssigningFeatureIterator(FeatureIterator<G> features) {
            this.features = features;
        }

        @Override
        public boolean hasNext() {
            return features.hasNext();
        }

        @Override
        public G next() throws NoSuchElementException {
            Feature next = features.next();
            Name typeName = next.getType().getName();
            String featureId = next.getIdentifier().getID();
            String versionId = store.getFeatureVersion(typeName, featureId, commitId);
            return (G) VersionedFeatureWrapper.wrap(next, versionId);
        }

        @Override
        public void close() {
            features.close();
        }
    }

    @Override
    public FeatureIterator<F> features() {
        return new ResourceIdAssigningFeatureIterator(delegate.features());
    }

    @SuppressWarnings("deprecation")
    @Override
    public void close(Iterator<F> close) {
        delegate.close(((ResourceIdAssigningIterator) close).iterator);
    }
}
