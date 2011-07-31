package org.geoserver.data.versioning;

import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import org.geogit.api.GeoGIT;
import org.geogit.api.LogOp;
import org.geogit.repository.Repository;
import org.geotools.util.Range;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.identity.ResourceId;
import org.opengis.filter.identity.Version;

import com.google.common.collect.Iterators;

public class ResourceIdFeatureCollector implements Iterable<Feature> {

    private final Repository repository;

    private final FeatureType featureType;

    private final Set<ResourceId> resourceIds;

    public ResourceIdFeatureCollector(final Repository repository, final FeatureType featureType,
            final Set<ResourceId> resourceIds) {
        this.repository = repository;
        this.featureType = featureType;
        this.resourceIds = resourceIds;
    }

    @Override
    public Iterator<Feature> iterator() {

        Iterator<Feature> iterator = Iterators.emptyIterator();

        GeoGIT ggit = new GeoGIT(repository);
        for (ResourceId rid : resourceIds) {
            Iterator<Feature> ridIterator = query(ggit, rid);
            iterator = Iterators.concat(iterator, ridIterator);
        }

        return iterator;
    }

    private Iterator<Feature> query(GeoGIT ggit, ResourceId rid) {
        final String featureId = rid.getRid();
        // previousRid is for reporting, not for querying, so not needed here
        // String previousRid = rid.getPreviousRid();
        final Version version = rid.getVersion();

        LogOp logOp = ggit.log();
        if (rid.getStartTime() != null || rid.getEndTime() != null) {
            Date startTime = rid.getStartTime() == null ? new Date(0L) : rid.getStartTime();
            Date endTime = rid.getEndTime() == null ? new Date(Long.MAX_VALUE) : rid.getEndTime();
            boolean isMinIncluded = true;
            boolean isMaxIncluded = true;

            Range<Date> timeRange = new Range<Date>(Date.class, startTime, isMinIncluded, endTime,
                    isMaxIncluded);

            logOp.setTimeRange(timeRange);
        }
        return null;
    }
}
