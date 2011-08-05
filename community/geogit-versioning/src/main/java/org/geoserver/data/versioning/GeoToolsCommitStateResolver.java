package org.geoserver.data.versioning;

import org.geogit.api.CommitStateResolver;
import org.geogit.api.PlatformResolver;
import org.geotools.data.Transaction;

public class GeoToolsCommitStateResolver extends PlatformResolver implements CommitStateResolver {

    public static final String GEOGIT_USER_NAME = "GEOGIT_USER_NAME";

    public static final String GEOGIT_COMMIT_MESSAGE = "GEOGIT_COMMIT_MESSAGE";

    static final ThreadLocal<Transaction> CURRENT_TRANSACTION = new ThreadLocal<Transaction>();

    private static Transaction getTransaction() {
        Transaction transaction = CURRENT_TRANSACTION.get();
        return transaction;
    }

    @Override
    public String getAuthor() {
        String user = null;
        Transaction transaction = getTransaction();
        if (transaction != null) {
            user = (String) transaction.getProperty(GEOGIT_USER_NAME);
            if (user == null) {
                user = (String) transaction.getProperty("VersioningCommitAuthor");
            }
        }
        if (user == null) {
            user = "anonymous";
        }
        return user;
    }

    @Override
    public String getCommitMessage() {
        String message = null;
        Transaction transaction = getTransaction();
        if (transaction != null) {
            message = (String) transaction.getProperty(GEOGIT_COMMIT_MESSAGE);
            if (message == null) {
                message = (String) transaction.getProperty("VersioningCommitMessage");
            }
        }
        if (message == null) {
            message = "No commit message provided";
        }
        return message;
    }

}
