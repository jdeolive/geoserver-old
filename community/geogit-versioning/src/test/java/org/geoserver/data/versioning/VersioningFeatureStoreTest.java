package org.geoserver.data.versioning;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureSource;
import org.geotools.data.Transaction;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.identity.ResourceId;

public class VersioningFeatureStoreTest extends VersioningTestSupport {

    public void test1() throws IOException {
        versioningStore.createSchema(featureType1);
        FeatureSource source = versioningStore.getFeatureSource(featureType1.getName());
        assertTrue(source instanceof VersioningFeatureStore);

        VersioningFeatureStore store = (VersioningFeatureStore) source;

        Transaction tx = new DefaultTransaction();
        store.setTransaction(tx);
        FeatureCollection collection = DataUtilities.collection(Arrays.asList(
                (SimpleFeature) feature1_1, (SimpleFeature) feature1_2));
        store.addFeatures(collection);
        tx.commit();

        collection = store.getFeatures();
        assertEquals(2, collection.size());
        FeatureIterator<Feature> features = collection.features();
        Set<String> rids = new HashSet<String>();
        while (features.hasNext()) {
            Feature feature = features.next();
            assertTrue(feature.getIdentifier() instanceof ResourceId);
            ResourceId rid = (ResourceId) feature.getIdentifier();
            rids.add(rid.getRid());
        }
    }
}
