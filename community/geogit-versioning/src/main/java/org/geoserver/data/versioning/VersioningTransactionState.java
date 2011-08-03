package org.geoserver.data.versioning;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import org.geogit.api.CommitOp;
import org.geogit.api.GeoGIT;
import org.geogit.api.RevCommit;
import org.geogit.repository.Index;
import org.geogit.repository.WorkingTree;
import org.geotools.data.Transaction;
import org.geotools.feature.FeatureCollection;
import org.geotools.util.NullProgressListener;
import org.geotools.util.logging.Logging;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.util.ProgressListener;

import com.google.common.base.Throwables;

@SuppressWarnings("rawtypes")
public class VersioningTransactionState implements Transaction.State {

    public static final VersioningTransactionState VOID = new VersioningTransactionState(null) {

        @Override
        public void stageInsert(final Name typeName, FeatureCollection affectedFeatures)
                throws Exception {

        }

        @Override
        public void stageUpdate(final FeatureCollection affectedFeatures) throws Exception {
        }

        @Override
        public void stageDelete(Name typeName, Filter filter, FeatureCollection affectedFeatures)
                throws Exception {
        }

        @Override
        public void stageRename(final Name typeName, final String oldFid, final String newFid) {
        }

    };

    private static final ProgressListener NULL_PROGRESS_LISTENER = new NullProgressListener();

    private static final Logger LOGGER = Logging.getLogger(VersioningTransactionState.class);

    private Transaction transaction;

    private GeoGIT geoGit;

    private String id;

    public VersioningTransactionState(final GeoGIT geoGit) {
        this.geoGit = geoGit;
        this.id = UUID.randomUUID().toString();
    }

    @Override
    public void setTransaction(final Transaction transaction) {
        if (transaction != null) {
            // configure
            this.transaction = transaction;
        } else {
            this.transaction = null;
            // TODO: is there some cleanup to do here?
        }
    }

    @Override
    public void addAuthorization(String AuthID) throws IOException {
        // TODO Auto-generated method stub
    }

    @Override
    public void commit() throws IOException {
        final String userName = getCurrentUserName();
        final String commitMsg = getCommitMessage();
        LOGGER.info("Committing changeset " + id + " by user " + userName);

        // final Ref branch = geoGit.checkout().setName(transactionID).call();
        // commit to the branch
        RevCommit commit = null;
        // checkout master
        // final Ref master = geoGit.checkout().setName("master").call();
        // merge branch to master
        // MergeResult mergeResult = geoGit.merge().include(branch).call();
        // TODO: check mergeResult is success?
        // geoGit.branchDelete().setName(transactionID).call();
        try {
            CommitOp commitOp = geoGit.commit().setAuthor(userName).setCommitter("geoserver")
                    .setMessage(commitMsg);
            commit = commitOp.call();
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            Throwables.propagate(e);
        }
        LOGGER.info("New commit: " + commit);
    }

    private String getCommitMessage() {
        String message = (String) transaction.getProperty("GEOGIT_COMMIT_MESSAGE");
        if (message == null) {
            message = (String) transaction.getProperty("VersioningCommitMessage");
        }
        if (message == null) {
            message = "No commit message provided";
        }
        return message;
    }

    private String getCurrentUserName() {
        String user = (String) transaction.getProperty("GEOGIT_USER_NAME");
        if (user == null) {
            user = (String) transaction.getProperty("VersioningCommitAuthor");
        }
        if (user == null) {
            user = "anonymous";
        }
        return user;
    }

    @Override
    public void rollback() throws IOException {
        // TODO Auto-generated method stub

    }

    /**
     * @param transactionID
     * @param typeName
     * @param affectedFeatures
     * @return the list of feature ids of the inserted features, in the order they were added
     * @throws Exception
     */
    public void stageInsert(final Name typeName, FeatureCollection affectedFeatures)
            throws Exception {

        // geoGit.checkout().setName(id).call();
        WorkingTree workingTree = geoGit.getRepository().getWorkingTree();
        workingTree.insert(affectedFeatures, NULL_PROGRESS_LISTENER);
        geoGit.add().call();
    }

    public void stageUpdate(final FeatureCollection newValues) throws Exception {

        // geoGit.checkout().setName(id).call();
        WorkingTree workingTree = geoGit.getRepository().getWorkingTree();
        workingTree.update(newValues, NULL_PROGRESS_LISTENER);
        geoGit.add().call();
    }

    public void stageDelete(final Name typeName, final Filter filter,
            final FeatureCollection affectedFeatures) throws Exception {

        // geoGit.checkout().setName(id).call();
        WorkingTree workingTree = geoGit.getRepository().getWorkingTree();
        workingTree.delete(typeName, filter, affectedFeatures);
        geoGit.add().call();
    }

    public void stageRename(final Name typeName, final String oldFid, final String newFid) {

        Index index = geoGit.getRepository().getIndex();

        final String namespaceURI = typeName.getNamespaceURI();
        final String localPart = typeName.getLocalPart();

        List<String> from = Arrays.asList(namespaceURI, localPart, oldFid);
        List<String> to = Arrays.asList(namespaceURI, localPart, newFid);

        index.renamed(from, to);
    }

}
