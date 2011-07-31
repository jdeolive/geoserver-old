package org.geoserver.data.versioning;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.logging.Logger;

import org.geogit.repository.Repository;
import org.geoserver.geogit.GEOGIT;
import org.geotools.data.DataAccess;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureSource;
import org.geotools.util.logging.Logging;
import org.springframework.util.Assert;

public class VersioningAdapterFactory {

    private static final Logger LOGGER = Logging.getLogger(VersioningAdapterFactory.class);

    @SuppressWarnings("rawtypes")
    public static FeatureSource create(final FeatureSource subject) {
        Assert.notNull(subject);

        final ClassLoader loader = subject.getClass().getClassLoader();
        final Class<?>[] interfaces = subject.getClass().getInterfaces();

        InvocationHandler invocationHandler = new VersioningFeatureSourceProxy();
        Object proxy = Proxy.newProxyInstance(loader, interfaces, invocationHandler);
        return (FeatureSource) proxy;
    }

    @SuppressWarnings("rawtypes")
    public static DataAccess create(final DataAccess subject) {
        Assert.notNull(subject);

        final Repository versioningRepo = GEOGIT.get().getRepository();

        if (subject instanceof DataStore) {
            return new VersioningDataAccess((DataStore) subject, versioningRepo);
        }

        LOGGER.fine("Versioning not supported for DataAccess yet, only for DataStore. "
                + "Returning unproxied DataAccess");

        return subject;
    }
}
