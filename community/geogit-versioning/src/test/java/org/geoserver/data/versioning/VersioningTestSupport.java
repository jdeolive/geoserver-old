package org.geoserver.data.versioning;

import org.geogit.test.RepositoryTestCase;
import org.geotools.data.DataAccess;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

public class VersioningTestSupport extends RepositoryTestCase {

    protected DataAccess<SimpleFeatureType, SimpleFeature> unversionedStore;

    protected VersioningDataAccess versioningStore;

    @Override
    protected void setUpInternal() throws Exception {
        unversionedStore = new SimpleMemoryDataAccess();
        versioningStore = new VersioningDataAccess(unversionedStore, super.repo);

    }
}
