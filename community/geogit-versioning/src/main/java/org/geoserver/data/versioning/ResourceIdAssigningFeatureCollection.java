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
public class ResourceIdAssigningFeatureCollection extends
        DecoratingFeatureCollection<FeatureType, Feature> implements
        FeatureCollection<FeatureType, Feature> {

    private final ObjectId commitId;

    private final VersioningDataAccess store;

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected ResourceIdAssigningFeatureCollection(final FeatureCollection delegate,
            final VersioningDataAccess store, final ObjectId commitId) {

        super(delegate);

        this.store = store;
        this.commitId = commitId;
    }

    private class ResourceIdAssigningIterator extends AbstractIterator<Feature> {

        private final Iterator<Feature> iterator;

        public ResourceIdAssigningIterator(final Iterator<Feature> iterator) {
            this.iterator = iterator;
        }

        @Override
        protected Feature computeNext() {
            if (!iterator.hasNext()) {
                return endOfData();
            }
            Feature next = iterator.next();
            Name typeName = next.getType().getName();
            String featureId = next.getIdentifier().getID();
            String versionId = store.getFeatureVersion(typeName, featureId, commitId);
            return VersionedFeatureWrapper.wrap(next, versionId);
        }

    }

    @Override
    public Iterator<Feature> iterator() {
        @SuppressWarnings("deprecation")
        Iterator<Feature> iterator = delegate.iterator();
        return new ResourceIdAssigningIterator(iterator);
    }

    @Override
    public FeatureIterator<Feature> features() {

        final FeatureIterator<Feature> features = delegate.features();

        return new FeatureIterator<Feature>() {

            @Override
            public boolean hasNext() {
                return features.hasNext();
            }

            @Override
            public Feature next() throws NoSuchElementException {
                Feature next = features.next();
                Name typeName = next.getType().getName();
                String featureId = next.getIdentifier().getID();
                String versionId = store.getFeatureVersion(typeName, featureId, commitId);
                return VersionedFeatureWrapper.wrap(next, versionId);
            }

            @Override
            public void close() {
                features.close();
            }
        };
    }

    @SuppressWarnings("deprecation")
    @Override
    public void close(Iterator<Feature> close) {
        delegate.close(((ResourceIdAssigningIterator) close).iterator);
    }
}
