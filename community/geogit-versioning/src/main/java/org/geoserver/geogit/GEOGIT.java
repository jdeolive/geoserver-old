/* Copyright (c) 2011 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.geogit;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import org.geogit.api.GeoGIT;
import org.geogit.api.RevCommit;
import org.geogit.repository.Index;
import org.geogit.repository.Repository;
import org.geogit.repository.WorkingTree;
import org.geogit.storage.RepositoryDatabase;
import org.geogit.storage.bdbje.EntityStoreConfig;
import org.geogit.storage.bdbje.EnvironmentBuilder;
import org.geogit.storage.bdbje.JERepositoryDatabase;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.config.GeoServerDataDirectory;
import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.task.LongTaskMonitor;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.Query;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.util.NullProgressListener;
import org.geotools.util.logging.Logging;
import org.opengis.feature.type.Name;
import org.opengis.filter.Id;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.util.Assert;

import com.sleepycat.je.Environment;

/**
 * Service facade to GeoServer
 * 
 * @author Gabriel Roldan
 * 
 */
public class GEOGIT implements DisposableBean {

    private static final NullProgressListener NULL_PROGRESS_LISTENER = new NullProgressListener();

    private static final Logger LOGGER = Logging.getLogger(GEOGIT.class);

    public static final String VERSIONING_DATA_ROOT = "versioning_data";

    private static final String GEOGIT_REPO = "geogit_repo";

    private static final String GEOGIT_INDEX = "geogit_index";

    private AuthenticationResolver authResolver;

    private final Catalog catalog;

    private final GeoGIT geoGit;

    public GEOGIT(final Catalog catalog, final GeoServerDataDirectory dataDir) throws IOException {
        this.catalog = catalog;
        this.authResolver = new GeoServerAuthenticationResolver();
        final File geogitRepo = dataDir.findOrCreateDataDir(VERSIONING_DATA_ROOT, GEOGIT_REPO);
        final File indexRepo = dataDir.findOrCreateDataDir(VERSIONING_DATA_ROOT, GEOGIT_INDEX);

        EnvironmentBuilder esb = new EnvironmentBuilder(new EntityStoreConfig());

        Properties bdbEnvProperties = null;
        Environment geogitEnv = esb.buildEnvironment(geogitRepo, bdbEnvProperties);
        Environment indexEnv = esb.buildEnvironment(indexRepo, bdbEnvProperties);

        RepositoryDatabase ggitRepoDb = new JERepositoryDatabase(geogitEnv, indexEnv);

        // RepositoryDatabase ggitRepoDb = new FileSystemRepositoryDatabase(geogitRepo);

        Repository repository = new Repository(ggitRepoDb);
        repository.create();

        this.geoGit = new GeoGIT(repository);

        // StatsConfig config = new StatsConfig();
        // config.setClear(true);
        // System.err.println(geogitEnvironment.getStats(config));
    }

    public static GEOGIT get() {
        GEOGIT singleton = GeoServerExtensions.bean(GEOGIT.class);
        return singleton;
    }

    /**
     * @see org.springframework.beans.factory.DisposableBean#destroy()
     */
    public void destroy() throws Exception {
        Repository repository = geoGit.getRepository();
        repository.close();
    }

    public List<Name> listLayers() throws Exception {
        geoGit.checkout().setName("master").call();
        WorkingTree workingTree = geoGit.getRepository().getWorkingTree();
        List<Name> typeNames = workingTree.getFeatureTypeNames();
        return typeNames;
    }

    /**
     * Adds the GeoSever Catalog's FeatureType named after {@code featureTypeName} to the master
     * branch.
     * 
     * @param featureTypeName
     * @return
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    public Future<?> initialize(final Name featureTypeName) throws Exception {
        final String user = getCurrentUserName();
        Assert.notNull(user, "This operation shall be invoked by a logged in user");

        final FeatureTypeInfo featureTypeInfo = catalog.getFeatureTypeByName(featureTypeName);
        Assert.notNull(featureTypeInfo, "No FeatureType named " + featureTypeName
                + " found in the Catalog");

        final FeatureSource featureSource = featureTypeInfo.getFeatureSource(null, null);
        if (featureSource == null) {
            throw new NullPointerException(featureTypeInfo + " didn't return a FeatureSource");
        }
        if (!(featureSource instanceof FeatureStore)) {
            throw new IllegalArgumentException("Can't version "
                    + featureTypeInfo.getQualifiedName() + " because it is read only");
        }

        ImportVersionedLayerTask importTask;
        importTask = new ImportVersionedLayerTask(user, featureSource, geoGit);
        LongTaskMonitor monitor = GeoServerExtensions.bean(LongTaskMonitor.class);
        Future<RevCommit> future = monitor.dispatch(importTask);
        return future;
    }

    public boolean isReplicated(final Name featureTypeName) {
        return geoGit.getRepository().getWorkingTree().hasRoot(featureTypeName);
    }

    public void initChangeSet(final String transactionID) throws Exception {
        // branch master
        geoGit.checkout().setName("master").call();
        geoGit.branchCreate().setName(transactionID).call();
    }

    public void stageRename(final Name typeName, final String oldFid, final String newFid) {

        Index index = geoGit.getRepository().getIndex();

        final String namespaceURI = typeName.getNamespaceURI();
        final String localPart = typeName.getLocalPart();

        List<String> from = Arrays.asList(namespaceURI, localPart, oldFid);
        List<String> to = Arrays.asList(namespaceURI, localPart, newFid);

        index.renamed(from, to);
    }

    /**
     * Merges branch named after {@code transactionID} back to master and commits.
     * 
     * @param transactionID
     * @param commitMsg
     * @return
     * @throws Exception
     */
    public RevCommit commitChangeSet(final String transactionID, final String commitMsg)
            throws Exception {
        String userName = getCurrentUserName();
        LOGGER.info("Committing changeset " + transactionID + " by user " + userName);

        // final Ref branch = geoGit.checkout().setName(transactionID).call();
        // commit to the branch
        RevCommit commit;
        // checkout master
        // final Ref master = geoGit.checkout().setName("master").call();
        // merge branch to master
        // MergeResult mergeResult = geoGit.merge().include(branch).call();
        // TODO: check mergeResult is success?
        // geoGit.branchDelete().setName(transactionID).call();
        commit = geoGit.commit().setAuthor(userName).setCommitter("geoserver")
                .setMessage(commitMsg).call();
        return commit;
    }

    /**
     * Discards branch named after {@code transactionID}.
     * 
     * @param transactionID
     * @throws Exception
     */
    public void rollBackChangeSet(final String transactionID) throws Exception {
        String userName = getCurrentUserName();
        System.err.println("Rolling back changeset " + transactionID + " by user " + userName);

        // TODO: implement ResetOp instead?!
        geoGit.getRepository().getIndex().reset();

        String deletedBranch = geoGit.branchDelete().setName(transactionID).setForce(true).call();
        if (deletedBranch == null) {
            LOGGER.info("Tried to delete branch " + transactionID + " but it didn't exist");
        }
    }

    /**
     * @return {@code null} if annonymous, the name of the current user otherwise
     */
    public String getCurrentUserName() {
        return authResolver.getCurrentUserName();
    }

    /**
     * Set an alternate auth resolver, mainly used to aid in unit testing code that depends on this
     * class.
     */
    public void setAuthenticationResolver(AuthenticationResolver resolver) {
        this.authResolver = resolver;
    }

    public GeoGIT getGeoGit() {
        return this.geoGit;
    }

    public Repository getRepository() {
        return this.geoGit.getRepository();
    }

    public ReferencedEnvelope getBounds(Name name, Id versioningFilter, Query query) {
        // TODO Auto-generated method stub
        return null;
    }

}
