package org.geoserver.data.versioning;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.collection.DecoratingFeatureCollection;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.FeatureType;

import com.google.common.collect.AbstractIterator;

public class ResourceIdAssigningFeatureCollection extends
        DecoratingFeatureCollection<FeatureType, Feature> implements
        FeatureCollection<FeatureType, Feature> {

    private final String versionId;

    protected ResourceIdAssigningFeatureCollection(
            final FeatureCollection<FeatureType, Feature> delegate, final String versionId) {
        super(delegate);
        this.versionId = versionId;
    }

    private static class ResourceIdAssigningIterator extends AbstractIterator<Feature> {

        private final Iterator<Feature> iterator;

        private final String versionId;

        public ResourceIdAssigningIterator(Iterator<Feature> iterator, String versionId) {
            this.iterator = iterator;
            this.versionId = versionId;
        }

        @Override
        protected Feature computeNext() {
            if (!iterator.hasNext()) {
                return endOfData();
            }
            return new VersionedFeatureWrapper((SimpleFeature) iterator.next(), versionId);
        }

    }

    @Override
    public Iterator<Feature> iterator() {
        @SuppressWarnings("deprecation")
        Iterator<Feature> iterator = delegate.iterator();
        return new ResourceIdAssigningIterator(iterator, versionId);
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
                return new VersionedFeatureWrapper((SimpleFeature) next, versionId);
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
