package org.geoserver.data.versioning;

import java.util.HashSet;
import java.util.Set;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.filter.visitor.AbstractFinderFilterVisitor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterVisitor;
import org.opengis.filter.Id;
import org.opengis.filter.identity.Identifier;
import org.opengis.filter.identity.ResourceId;

public class ResourceIdFilterExtractor {

    public static final Id getVersioningFilter(Filter filter) {
        if (filter == null || Filter.INCLUDE.equals(filter) || Filter.EXCLUDE.equals(filter)) {
            return null;
        }

        final FilterVisitor ridFinder = new AbstractFinderFilterVisitor() {
            @Override
            public Object visit(final Id filter, final Object data) {
                Set<ResourceId> resourceIds = null;
                for (Identifier id : filter.getIdentifiers()) {
                    if (id instanceof ResourceId) {
                        if (resourceIds == null) {
                            resourceIds = new HashSet<ResourceId>();
                        }
                        resourceIds.add((ResourceId) id);
                    }
                }
                if (resourceIds != null && resourceIds.size() > 0) {
                    found = true;
                    return CommonFactoryFinder.getFilterFactory2(null).id(resourceIds);
                }
                return null;
            }
        };

        Object found = filter.accept(ridFinder, null);
        if (found instanceof Id) {
            return (Id) found;
        }

        return null;
    }

}
