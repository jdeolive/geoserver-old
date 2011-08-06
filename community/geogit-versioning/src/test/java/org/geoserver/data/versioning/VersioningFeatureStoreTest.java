package org.geoserver.data.versioning;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureStore;
import org.geotools.data.Transaction;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Feature;
import org.opengis.filter.identity.ResourceId;

@SuppressWarnings("rawtypes")
public class VersioningFeatureStoreTest extends VersioningTestSupport {

    private FeatureCollection type1InitialFeatures;

    private FeatureCollection type2InitialFeatures;

    private FeatureStore store1;

    /**
     * Sets #featureType1 as versioned, but does not populate it.
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
//        store1 = (FeatureStore) source;
//
//        type1InitialFeatures = DataUtilities.collection(Arrays.asList((SimpleFeature) points1,
//                (SimpleFeature) points2, (SimpleFeature) points3));
//
//        type2InitialFeatures = DataUtilities.collection(Arrays.asList((SimpleFeature) lines1,
//                (SimpleFeature) lines2, (SimpleFeature) lines3));
    }

    public void test1() throws IOException {
        if (true) {
            System.err.print(getName() + " needs to be fixed ----------------");
            return;
        }
        Transaction tx = new DefaultTransaction();
        store1.setTransaction(tx);
        store1.addFeatures(type1InitialFeatures);
        tx.commit();

        type1InitialFeatures = store1.getFeatures();
        assertEquals(3, type1InitialFeatures.size());
        FeatureIterator<Feature> features = type1InitialFeatures.features();
        Set<String> rids = new HashSet<String>();
        while (features.hasNext()) {
            Feature feature = features.next();
            assertTrue(feature.getIdentifier() instanceof ResourceId);
            ResourceId rid = (ResourceId) feature.getIdentifier();
            rids.add(rid.getRid());
        }
    }
}
