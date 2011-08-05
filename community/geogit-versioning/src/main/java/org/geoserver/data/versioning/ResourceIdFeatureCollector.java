package org.geoserver.data.versioning;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.geogit.api.DiffEntry;
import org.geogit.api.DiffOp;
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
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
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
        final String featureId = id.getID();
        final String featureVersion = id.getFeatureVersion();

        final Ref requestedVersionRef = extractRequestedVersion(ggit, featureId, featureVersion);
        {
            if (requestedVersionRef != null && id.getEndTime() == null && id.getStartTime() == null
                    && id.getVersion() == null) {
                // easy, no extra constraints specified
                return Iterators.singletonIterator(requestedVersionRef);
            }
        }

        List<Ref> result = new ArrayList<Ref>(5);

        // previousRid is for reporting, not for querying, so not needed here
        // String previousRid = rid.getPreviousRid();
        final Version version = id.getVersion();

        // filter commits that affect the requested feature
        final List<String> path = path(featureId);
        LogOp logOp = ggit.log().addPath(path);

        // limit commits by time range, if speficied
        if (id.getStartTime() != null || id.getEndTime() != null) {
            Date startTime = id.getStartTime() == null ? new Date(0L) : id.getStartTime();
            Date endTime = id.getEndTime() == null ? new Date(Long.MAX_VALUE) : id.getEndTime();
            boolean isMinIncluded = true;
            boolean isMaxIncluded = true;
            Range<Date> timeRange = new Range<Date>(Date.class, startTime, isMinIncluded, endTime,
                    isMaxIncluded);
            logOp.setTimeRange(timeRange);
        }

        Iterator<RevCommit> commits = logOp.call();

        if (version == null) {
            List<Ref> allInAscendingOrder = getAllInAscendingOrder(ggit, commits, featureId);
            result.addAll(allInAscendingOrder);
        } else {
            if (version.getDateTime() != null) {
                final Date validAsOf = version.getDateTime();
                // use second precision, as that's what xml uses, right?
                final long lowerThan = toSecondsPrecision(validAsOf.getTime());
                // filter by commits previous to specified time
                commits = Iterators.filter(commits, new Predicate<RevCommit>() {
                    @Override
                    public boolean apply(RevCommit input) {
                        long timestamp = toSecondsPrecision(input.getTimestamp());
                        return timestamp <= lowerThan;
                    }
                });
                // and get only the first one, that's the greatest and closest to the requested
                // valid time, as commints come in descending temporal order from LogOp
                commits = Iterators.limit(commits, 1);
                result.addAll(getAllInAscendingOrder(ggit, commits, featureId));

            } else if (version.getIndex() != null) {
                final int requestIndex = version.getIndex().intValue();
                final int listIndex = requestIndex - 1;// version indexing starts at 1
                List<Ref> allVersions = getAllInAscendingOrder(ggit, commits, featureId);
                if (allVersions.size() > 0) {
                    if (allVersions.size() >= requestIndex) {
                        result.add(allVersions.get(listIndex));
                    } else {
                        result.add(allVersions.get(allVersions.size() - 1));
                    }
                }
            } else if (version.getVersionAction() != null) {
                final VersionAction versionAction = version.getVersionAction();
                List<Ref> allInAscendingOrder = getAllInAscendingOrder(ggit, commits, featureId);
                switch (versionAction) {
                case ALL:
                    result.addAll(allInAscendingOrder);
                    break;
                case FIRST:
                    if (allInAscendingOrder.size() > 0) {
                        result.add(allInAscendingOrder.get(0));
                    }
                    break;
                case LAST:
                    if (allInAscendingOrder.size() > 0) {
                        result.add(allInAscendingOrder.get(allInAscendingOrder.size() - 1));
                    }
                    break;
                case NEXT:
                    Ref next = next(requestedVersionRef, allInAscendingOrder);
                    if (next != null) {
                        result.add(next);
                    }
                    break;
                case PREVIOUS:
                    Ref previous = previous(requestedVersionRef, allInAscendingOrder);
                    if (previous != null) {
                        result.add(previous);
                    }
                    break;
                default:
                    break;
                }
            }
        }

        return result.iterator();
    }

    private long toSecondsPrecision(final long timeStampMillis) {
        return timeStampMillis / 1000;
    }

    private Ref previous(Ref requestedVersionRef, List<Ref> allVersions) {
        int idx = locate(requestedVersionRef, allVersions);
        if (idx > 0) {
            return allVersions.get(idx - 1);
        }
        return null;
    }

    private Ref next(Ref requestedVersionRef, List<Ref> allVersions) {
        int idx = locate(requestedVersionRef, allVersions);
        if (idx > -1 && idx < allVersions.size() - 1) {
            return allVersions.get(idx + 1);
        }
        return null;
    }

    private int locate(final Ref requestedVersionRef, List<Ref> allVersions) {
        if (requestedVersionRef == null) {
            return -1;
        }
        for (int i = 0; i < allVersions.size(); i++) {
            Ref ref = allVersions.get(i);
            if (requestedVersionRef.equals(ref)) {
                return i;
            }
        }
        return -1;
    }

    private List<Ref> getAllInAscendingOrder(final GeoGIT ggit, final Iterator<RevCommit> commits,
            final String featureId) throws Exception {

        LinkedList<Ref> featureRefs = new LinkedList<Ref>();

        final List<String> path = path(featureId);
        // find all commits where this feature is touched
        while (commits.hasNext()) {
            RevCommit commit = commits.next();
            ObjectId commitId = commit.getId();
            ObjectId parentCommitId = commit.getParentIds().get(0);
            DiffOp diffOp = ggit.diff().setOldVersion(parentCommitId).setNewVersion(commitId)
                    .setFilter(path);
            Iterator<DiffEntry> diffs = diffOp.call();
            Preconditions.checkState(diffs.hasNext());
            DiffEntry diff = diffs.next();
            Preconditions.checkState(!diffs.hasNext());
            switch (diff.getType()) {
            case ADD:
            case MODIFY:
                featureRefs.addFirst(diff.getNewObject());
                break;
            case DELETE:
                break;
            }
        }
        return featureRefs;
    }

    /**
     * Extracts the feature version from the given {@code rid} if supplied, or finds out the current
     * feature version from the feature id otherwise.
     * 
     * @return the version identifier of the feature given by {@code version}, or at the current
     *         geogit HEAD if {@code version == null}, or {@code null} if such a feature does not
     *         exist.
     */
    private Ref extractRequestedVersion(final GeoGIT ggit, final String featureId,
            final String version) {
        if (version != null) {
            ObjectId versionedId = ObjectId.valueOf(version);
            // verify the object exists
            boolean exists = repository.getObjectDatabase().exists(versionedId);
            // Ref rootTreeChild = repository.getRootTreeChild(path(featureId));
            if (exists) {
                return new Ref(featureId, versionedId, TYPE.BLOB);
            }
            return null;
        }
        // no version specified, find out the latest
        List<String> path = path(featureId);
        Ref currFeatureRef = repository.getRootTreeChild(path);
        if (currFeatureRef == null) {
            // feature does not exist at the current repository state
            return null;
        }
        return currFeatureRef;
    }

    private List<String> path(final String featureId) {
        Name typeName = featureType.getName();
        List<String> path = new ArrayList<String>(3);

        if (null != typeName.getNamespaceURI()) {
            path.add(typeName.getNamespaceURI());
        }
        path.add(typeName.getLocalPart());
        path.add(featureId);

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
