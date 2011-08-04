package org.geoserver.geogit;

import java.util.logging.Logger;

import org.geogit.api.GeoGIT;
import org.geogit.api.RevCommit;
import org.geogit.repository.WorkingTree;
import org.geoserver.task.LongTask;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.util.logging.Logging;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.util.ProgressListener;

class ImportVersionedLayerTask extends LongTask<RevCommit> {

    private static final Logger LOGGER = Logging.getLogger(ImportVersionedLayerTask.class);

    final GeoGIT geoGit;

    @SuppressWarnings("rawtypes")
    private final FeatureSource featureSource;

    private final Name featureTypeName;

    @SuppressWarnings("rawtypes")
    public ImportVersionedLayerTask(final FeatureSource featureSource, final GeoGIT geoGit) {
        super();
        this.featureSource = featureSource;
        this.geoGit = geoGit;

        this.featureTypeName = featureSource.getName();

        final String title = "Import " + featureTypeName.getLocalPart() + " as Versioned";
        final String description = "GeoSynchronizationService is creating the initial import of this FeatureType";
        setTitle(title);
        setDescription(description);
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected RevCommit callInternal(final ProgressListener listener) throws Exception {

        final FeatureType schema = featureSource.getSchema();
        final FeatureCollection features = featureSource.getFeatures();

        final String commitMessage = "Initial import of FeatureType " + featureTypeName.getURI();

        // geoGit.checkout();TODO: check out master branch
        WorkingTree workingTree = geoGit.getRepository().getWorkingTree();
        workingTree.init(schema);
        try {
            workingTree.insert(features, listener);
        } catch (Exception e) {
            e.printStackTrace();
            workingTree.delete(featureTypeName);
            throw e;
        }

        RevCommit revCommit = null;
        if (listener.isCanceled()) {
            LOGGER.warning("Import process for " + featureTypeName.getLocalPart() + " cancelled.");
        } else {
            final Name name = schema.getName();
            final String nsUri = name.getNamespaceURI();
            final String typeName = name.getLocalPart();
            // add only the features of this type, other imports may be running in parallel and we
            // don't want to commit them all
            // geoGit.add().addPattern(pattern).call();
            geoGit.add().call();
            revCommit = geoGit.commit().setMessage(commitMessage).call();
            LOGGER.info("Initial commit of " + featureTypeName + ": " + revCommit.getId());
        }
        return revCommit;
    }

}
