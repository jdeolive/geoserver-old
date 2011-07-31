package org.geoserver.data.versioning;

import org.geotools.feature.DecoratingFeature;
import org.geotools.filter.identity.ResourceIdImpl;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.identity.FeatureId;
import org.opengis.filter.identity.ResourceId;

class VersionedFeatureWrapper extends DecoratingFeature {

    private final String versionId;

    public VersionedFeatureWrapper(final SimpleFeature delegate, final String versionId) {
        super(delegate);
        this.versionId = versionId;
    }

    @Override
    public FeatureId getIdentifier() {
        ResourceId rid = new ResourceIdImpl(super.getID() + '@' + versionId);
        return rid;
    }

    @Override
    public String getID() {
        return super.getID() + '@' + versionId;
    }
};