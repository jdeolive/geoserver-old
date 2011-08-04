package org.geoserver.data.versioning;

import org.geotools.feature.DecoratingFeature;
import org.geotools.filter.identity.ResourceIdImpl;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.identity.FeatureId;
import org.opengis.filter.identity.ResourceId;

class VersionedFeatureWrapper {

    public static Feature wrap(final Feature f, final String versionId) {

        if (f instanceof SimpleFeature) {
            return new SimpleFeatureWrapper((SimpleFeature) f, versionId);
        }
        throw new UnsupportedOperationException("Non simple Features are not yet supported: " + f);
    }

    private static final class SimpleFeatureWrapper extends DecoratingFeature {
        private final String versionId;

        public SimpleFeatureWrapper(final SimpleFeature delegate, final String versionId) {
            super(delegate);
            this.versionId = versionId;
        }

        @Override
        public FeatureId getIdentifier() {
            ResourceId rid = new ResourceIdImpl(super.getID(), versionId);
            return rid;
        }

        @Override
        public String getID() {
            return super.getID() + '@' + versionId;
        }
    }
}