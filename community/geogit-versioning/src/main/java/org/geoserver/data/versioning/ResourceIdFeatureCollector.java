package org.geoserver.data.versioning;

import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import org.geogit.api.GeoGIT;
import org.geogit.api.LogOp;
import org.geogit.api.ObjectId;
import org.geogit.api.Ref;
import org.geogit.repository.Repository;
import org.geotools.util.Range;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.identity.ResourceId;
import org.opengis.filter.identity.Version;

import com.google.common.base.Function;
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

        Iterator<Ref> featureRefs = Iterators.emptyIterator();

        GeoGIT ggit = new GeoGIT(repository);
        for (ResourceId rid : resourceIds) {
            Iterator<Ref> ridIterator = query(ggit, rid);
            featureRefs = Iterators.concat(featureRefs, ridIterator);
        }

        Iterator<Feature> features = Iterators.transform(featureRefs, new RefToFeature(repository,
                featureType));

        return features;
    }

    private Iterator<Ref> query(final GeoGIT ggit, final ResourceId id) {
        final String rid = id.getRid();
        final String featureId = featureId(rid);
        final String ridVersion = extractRequestedVersion(ggit, rid);
        // previousRid is for reporting, not for querying, so not needed here
        // String previousRid = rid.getPreviousRid();
        final Version version = id.getVersion();

        LogOp logOp = ggit.log();

        String[] path = path(featureId);
        logOp.addPath(path);

        if (id.getStartTime() != null || id.getEndTime() != null) {
            Date startTime = id.getStartTime() == null ? new Date(0L) : id.getStartTime();
            Date endTime = id.getEndTime() == null ? new Date(Long.MAX_VALUE) : id.getEndTime();
            boolean isMinIncluded = true;
            boolean isMaxIncluded = true;

            Range<Date> timeRange = new Range<Date>(Date.class, startTime, isMinIncluded, endTime,
                    isMaxIncluded);

            logOp.setTimeRange(timeRange);
        }
        return null;
    }

    /**
     * Extracts the feature version from the given {@code rid} if supplied, or finds out the current
     * feature version from the feature id otherwise.
     * 
     * @param ggit
     * @param rid
     *            {@code <featureId>[@<featureVersion>]}
     * @return
     */
    private String extractRequestedVersion(final GeoGIT ggit, final String rid) {
        final int idx = rid.indexOf('@');
        if (idx > 0) {
            return rid.substring(idx + 1);
        }
        // no version specified, find out the latest
        final String featureId = rid;
        String[] path = path(featureId);
        Ref currFeatureObjectId = repository.getRootTreeChild(path);
        if (currFeatureObjectId == null) {
            // feature does not exist at the current repository state
            return null;
        }
        return currFeatureObjectId.toString();
    }

    private String featureId(final String rid) {
        final int idx = rid.indexOf('@');
        return idx == -1 ? rid : rid.substring(0, idx);
    }

    private String[] path(final String featureId) {
        Name typeName = featureType.getName();
        String[] path;

        if (null != typeName.getNamespaceURI()) {
            path = new String[] { typeName.getNamespaceURI(), typeName.getLocalPart(), featureId };
        } else {
            path = new String[] { typeName.getLocalPart(), featureId };
        }

        return path;
    }

    private static class RefToFeature implements Function<Ref, Feature> {

        private final Repository repo;

        private final FeatureType type;

        public RefToFeature(final Repository repo, final FeatureType type) {
            this.repo = repo;
            this.type = type;
        }

        @Override
        public Feature apply(final Ref featureRef) {
            String featureId = featureRef.getName();
            ObjectId contentId = featureRef.getObjectId();
            Feature feature = repo.getFeature(type, featureId, contentId);
            return feature;
        }

    }

}
