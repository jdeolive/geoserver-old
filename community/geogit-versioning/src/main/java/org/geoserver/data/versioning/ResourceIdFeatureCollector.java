package org.geoserver.data.versioning;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

import org.geogit.api.GeoGIT;
import org.geogit.api.LogOp;
import org.geogit.api.ObjectId;
import org.geogit.api.Ref;
import org.geogit.api.RevCommit;
import org.geogit.api.RevObject.TYPE;
import org.geogit.repository.Repository;
import org.geotools.util.Range;
import org.geotools.util.logging.Logging;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.identity.ResourceId;
import org.opengis.filter.identity.Version;
import org.opengis.filter.identity.VersionAction;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;

public class ResourceIdFeatureCollector implements Iterable<Feature> {

    private static final Logger LOGGER = Logging.getLogger(ResourceIdFeatureCollector.class);

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
        try {
            for (ResourceId rid : resourceIds) {
                Iterator<Ref> ridIterator;
                ridIterator = query(ggit, rid);
                featureRefs = Iterators.concat(featureRefs, ridIterator);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Iterator<Feature> features = Iterators.transform(featureRefs, new RefToFeature(repository,
                featureType));

        return features;
    }

    /**
     * @param ggit
     * @param id
     * @return an iterator for all the requested versions of a given feature, or the empty iterator
     *         if no such feature is found.
     * @throws Exception
     */
    private Iterator<Ref> query(final GeoGIT ggit, final ResourceId id) throws Exception {
        final String rid = id.getRid();
        final String featureId = featureId(rid);
        final Ref ridVersion = extractRequestedVersion(ggit, rid);
        if (ridVersion == null) {
            LOGGER.finest("Resource id didn't match any versioned Feature: " + rid);
            return Iterators.emptyIterator();
        }

        if (id.getEndTime() == null && id.getStartTime() == null && id.getVersion() == null) {
            // easy, no extra constraints specified
            return Iterators.singletonIterator(ridVersion);
        }

        Set<Ref> result = new HashSet<Ref>();
        // previousRid is for reporting, not for querying, so not needed here
        // String previousRid = rid.getPreviousRid();
        final Version version = id.getVersion();
        final Date validAsOf = version.getDateTime();
        final VersionAction versionAction = version.getVersionAction();

        LogOp logOp = ggit.log();

        String[] path = path(featureId);
        logOp.addPath(path);

        // limit by resource id time range, if speficied
        if (id.getStartTime() != null || id.getEndTime() != null) {
            Date startTime = id.getStartTime() == null ? new Date(0L) : id.getStartTime();
            Date endTime = id.getEndTime() == null ? new Date(Long.MAX_VALUE) : id.getEndTime();
            boolean isMinIncluded = true;
            boolean isMaxIncluded = true;

            Range<Date> timeRange = new Range<Date>(Date.class, startTime, isMinIncluded, endTime,
                    isMaxIncluded);

            logOp.setTimeRange(timeRange);
        }

        Iterator<RevCommit> commitRange = logOp.call();
        // limit as per resource id valid date
        if (validAsOf != null) {

        }

        return result.iterator();
    }

    /**
     * Extracts the feature version from the given {@code rid} if supplied, or finds out the current
     * feature version from the feature id otherwise.
     * 
     * @param ggit
     * @param rid
     *            {@code <featureId>[@<featureVersion>]}
     * @return the version identifier of the feature given by {@code rid}, or at the current geogit
     *         HEAD if {@code rid} doesn't contain the version info, or {@code null} if such a
     *         feature does not exist.
     */
    private Ref extractRequestedVersion(final GeoGIT ggit, final String rid) {
        final int idx = rid.indexOf('@');
        if (idx > 0) {
            String version = rid.substring(idx + 1);
            ObjectId versionedId = ObjectId.valueOf(version);
            final String featureId = rid.substring(0, idx);
            // verify the object exists
            boolean exists = repository.getObjectDatabase().exists(versionedId);
            Ref rootTreeChild = repository.getRootTreeChild(path(featureId));
            //if (exists) {
                return new Ref(featureId, versionedId, TYPE.BLOB);
            //}
            //return null;
        }
        // no version specified, find out the latest
        final String featureId = rid;
        String[] path = path(featureId);
        Ref currFeatureRef = repository.getRootTreeChild(path);
        if (currFeatureRef == null) {
            // feature does not exist at the current repository state
            return null;
        }
        return currFeatureRef;
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
            return VersionedFeatureWrapper.wrap(feature, featureRef.getObjectId().toString());
        }

    }

}
