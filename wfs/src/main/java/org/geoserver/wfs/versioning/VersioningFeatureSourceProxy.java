package org.geoserver.wfs.versioning;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.opengis.filter.Filter;
import org.opengis.filter.Id;
import org.springframework.util.Assert;

@SuppressWarnings("rawtypes")
public class VersioningFeatureSourceProxy implements InvocationHandler {

    private static final Map<Method, VersioningMethodProxy> handlers;
    static {
        Map<Method, VersioningMethodProxy> tmp = new HashMap<Method, VersioningMethodProxy>();
        try {
            Class<FeatureSource> proxiedClass = FeatureSource.class;
            Method getBounds = proxiedClass.getMethod("getBounds", Query.class);
            Method getCount = proxiedClass.getMethod("getCount", Query.class);
            Method getFeaturesFilter = proxiedClass.getMethod("getFeatures", Filter.class);
            Method getFeaturesQuery = proxiedClass.getMethod("getFeatures", Query.class);

            tmp.put(getBounds, null);
            tmp.put(getCount, null);
            tmp.put(getFeaturesFilter, null);
            tmp.put(getFeaturesQuery, null);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        handlers = Collections.unmodifiableMap(tmp);
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args)
            throws Throwable {
        Assert.isTrue(proxy instanceof FeatureSource);

        VersioningMethodProxy versioningMethodProxy = handlers.get(method);
        if (versioningMethodProxy != null) {
            return versioningMethodProxy.invoke((FeatureSource) proxy, method, args);
        }

        return method.invoke(proxy, args);
    }

    private static abstract class VersioningMethodProxy {

        protected final Id getVersioningFilter(Object[] args) {
            if (args == null || args.length != 1) {
                return null;
            }

            Filter filter = null;
            if (args[0] instanceof Query) {
                filter = ((Query) args[0]).getFilter();
            } else if (args[0] instanceof Filter) {
                filter = (Filter) args[0];
            }

            Id versioningFilter = ResourceIdFilterExtractor.getVersioningFilter(filter);
            return versioningFilter;
        }

        public Object invoke(FeatureSource subject, Method method, Object[] args) throws Exception {
            final Id resourceIds = getVersioningFilter(args);
            if (null == resourceIds) {
                return method.invoke(subject, args);
            }
            return invokeInternal(subject, resourceIds, args[0]);
        }

        protected abstract Object invokeInternal(final FeatureSource subject, final Id resourceIds,
                final Object origQuery);

    }
}
