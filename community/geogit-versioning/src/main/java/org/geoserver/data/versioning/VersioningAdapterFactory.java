package org.geoserver.data.versioning;

import java.util.logging.Logger;

import org.geogit.repository.Repository;
import org.geoserver.data.versioning.simple.SimpleVersioningFeatureLocking;
import org.geoserver.data.versioning.simple.SimpleVersioningFeatureSource;
import org.geoserver.data.versioning.simple.SimpleVersioningFeatureStore;
import org.geoserver.geogit.GEOGIT;
import org.geotools.data.DataAccess;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureLocking;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.simple.SimpleFeatureLocking;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.util.logging.Logging;
import org.springframework.util.Assert;

public class VersioningAdapterFactory {

    private static final Logger LOGGER = Logging.getLogger(VersioningAdapterFactory.class);

    @SuppressWarnings({ "rawtypes" })
    public static FeatureSource create(final FeatureSource subject) {
        final Repository versioningRepo = GEOGIT.get().getRepository();

        if (subject instanceof SimpleFeatureLocking) {
            return new SimpleVersioningFeatureLocking((SimpleFeatureLocking) subject,
                    versioningRepo);
        }
        if (subject instanceof SimpleFeatureStore) {
            return new SimpleVersioningFeatureStore((SimpleFeatureStore) subject, versioningRepo);
        }
        if (subject instanceof SimpleFeatureSource) {
            return new SimpleVersioningFeatureSource((SimpleFeatureSource) subject, versioningRepo);
        }

        if (subject instanceof FeatureLocking) {
            return new VersioningFeatureLocking((FeatureLocking) subject, versioningRepo);
        }
        if (subject instanceof FeatureStore) {
            return new VersioningFeatureStore((FeatureStore) subject, versioningRepo);
        }

        return new VersioningFeatureSource(subject, versioningRepo);
    }

    @SuppressWarnings("rawtypes")
    public static DataAccess create(final DataAccess subject) {
        Assert.notNull(subject);

        final Repository versioningRepo = GEOGIT.get().getRepository();

        // if (subject instanceof DataStore) {
        // return new VersioningDataStore((DataStore) subject, versioningRepo);
        // }

        return new VersioningDataAccess((DataStore) subject, versioningRepo);
    }
}
