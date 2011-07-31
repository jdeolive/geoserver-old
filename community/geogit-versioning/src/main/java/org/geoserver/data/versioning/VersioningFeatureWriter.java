package org.geoserver.data.versioning;

import java.io.IOException;

import org.geotools.data.FeatureWriter;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;

public class VersioningFeatureWriter<FT extends FeatureType, F extends Feature> implements
        FeatureWriter<FT, F> {

    private final FeatureWriter<FT, F> unversioned;

    private final VersioningDataStore store;

    public VersioningFeatureWriter(FeatureWriter<FT, F> unversioned,
            VersioningDataStore versioningDataStore) {

        this.unversioned = unversioned;
        this.store = versioningDataStore;
    }

    @Override
    public boolean hasNext() throws IOException {
        return unversioned.hasNext();
    }

    @Override
    public F next() throws IOException {
        throw new UnsupportedOperationException("do versioning!");
    }

    @Override
    public void write() throws IOException {
        throw new UnsupportedOperationException("do versioning!");
    }

    @Override
    public void remove() throws IOException {
        throw new UnsupportedOperationException("do versioning!");
    }

    @Override
    public FT getFeatureType() {
        return unversioned.getFeatureType();
    }

    @Override
    public void close() throws IOException {
        unversioned.close();
    }
}
