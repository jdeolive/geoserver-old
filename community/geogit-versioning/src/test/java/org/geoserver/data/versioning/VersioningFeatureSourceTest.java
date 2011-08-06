package org.geoserver.data.versioning;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.geogit.api.ObjectId;
import org.geogit.api.Ref;
import org.geogit.api.RevObject.TYPE;
import org.geogit.api.TreeVisitor;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.Transaction;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.identity.ResourceId;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class VersioningFeatureSourceTest extends VersioningTestSupport {

    private FeatureCollection type1InitialFeatures;

    private FeatureCollection type2InitialFeatures;

    private FeatureSource source1;

    /**
     * Map of featureId/blob hash code for the initial insert of {@link #feature1_1},
     * {@link #feature1_2}, and {@link #feature1_3}
     */
    private Map<String, String> initialFeatureVersions;

    /**
     * Sets {@link #featureType1} as versioned and performs the following commits:
     * <p>
     * <li>first commit inserts {@link #feature1_1}, {@link #feature1_2}, and {@link #feature1_3}
     * <li>second commit updates {@link #feature1_1} and {@link #feature1_2}
     * <li>third commit deletes {@link #feature1_1}
     * </p>
     * <p>
     * So the current state of the feature type will be the modified version of {@link #feature1_2}
     * and the initial version of {@link #feature1_3}.
     * </p>
     * 
     * @see org.geoserver.data.versioning.VersioningTestSupport#setUpInternal()
     */
    @Override
    protected void setUpInternal() throws Exception {
        super.setUpInternal();

//        // creates the schema and initializes the repo with an empty tree for the feature type
//        versioningStore.createSchema(pointsType);
//
//        FeatureSource source = versioningStore.getFeatureSource(pointsType.getName());
//        assertTrue(source instanceof VersioningFeatureStore);
//        FeatureStore store = (FeatureStore) source;
//
//        type1InitialFeatures = DataUtilities.collection(Arrays.asList((SimpleFeature) points1,
//                (SimpleFeature) points2, (SimpleFeature) points3));
//
//        type2InitialFeatures = DataUtilities.collection(Arrays.asList((SimpleFeature) lines1,
//                (SimpleFeature) lines2, (SimpleFeature) lines3));
//
//        Transaction tx = new DefaultTransaction();
//        store.setTransaction(tx);
//        store.addFeatures(type1InitialFeatures);
//        tx.commit();
//
//        CollectIdsVisitor visitor = new CollectIdsVisitor();
//        repo.getHeadTree().accept(visitor);
//
//        initialFeatureVersions = new HashMap<String, String>(visitor.fidToVersionHash);
//        assertEquals(3, initialFeatureVersions.size());
//        visitor.fidToVersionHash.clear();

    }

    private class CollectIdsVisitor implements TreeVisitor {

        private Map<String, String> fidToVersionHash = new HashMap<String, String>();

        @Override
        public boolean visitEntry(Ref ref) {
            if (ref.getType().equals(TYPE.BLOB)) {
                fidToVersionHash.put(ref.getName(), ref.getObjectId().toString());
            } else if (ref.getType().equals(TYPE.TREE)) {
                CollectIdsVisitor subVisitor = new CollectIdsVisitor();
                repo.getTree(ref.getObjectId()).accept(subVisitor);
                fidToVersionHash.putAll(subVisitor.fidToVersionHash);
            }
            return true;
        }

        @Override
        public boolean visitSubTree(int bucket, ObjectId treeId) {
            return true;
        }
    }

    public void testCurrentVersionFeatureIdentifierIsResourceId() throws IOException {
        if (true) {
            System.err.print(getName() + " needs to be fixed ----------------");
            return;
        }

        type1InitialFeatures = source1.getFeatures();
        assertEquals(3, type1InitialFeatures.size());
        FeatureIterator<Feature> features = type1InitialFeatures.features();

        Map<String, String> currentVersions = new HashMap<String, String>();
        while (features.hasNext()) {
            Feature feature = features.next();
            assertTrue(feature.getIdentifier() instanceof ResourceId);
            ResourceId id = (ResourceId) feature.getIdentifier();
            String rid = id.getRid();
            int versionSeparatorIndex = rid.indexOf('@');
            assertTrue(rid, versionSeparatorIndex > 0);

            currentVersions.put(rid.substring(0, versionSeparatorIndex),
                    rid.substring(versionSeparatorIndex + 1));
        }

        System.out.println(currentVersions.toString());
    }
}
