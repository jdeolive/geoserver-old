package org.geoserver.wfs.versioning;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import org.geotools.data.FeatureSource;
import org.springframework.util.Assert;

public class VersioningFeatureSourceFactory {

    @SuppressWarnings("rawtypes")
    public static FeatureSource create(final FeatureSource subject) {
        Assert.notNull(subject);

        final ClassLoader loader = subject.getClass().getClassLoader();
        final Class<?>[] interfaces = subject.getClass().getInterfaces();

        InvocationHandler invocationHandler = new VersioningFeatureSourceProxy();
        Object proxy = Proxy.newProxyInstance(loader, interfaces, invocationHandler);
        return (FeatureSource) proxy;
    }
}
