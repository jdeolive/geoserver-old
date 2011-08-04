package org.geoserver.data.versioning;

import org.geogit.api.AuthenticationResolver;
import org.geotools.data.Transaction;

public class GeoToolsAuthenticationResolver implements AuthenticationResolver {

    public static final String GEOGIT_USER_NAME = "GEOGIT_USER_NAME";

    public static final String GEOGIT_COMMIT_MESSAGE = "GEOGIT_COMMIT_MESSAGE";

    private final Transaction tx;

    public GeoToolsAuthenticationResolver(Transaction tx) {
        this.tx = tx;
    }

    @Override
    public String getAuthor() {
        String user = (String) tx.getProperty(GEOGIT_USER_NAME);
        if (user == null) {
            user = (String) tx.getProperty("VersioningCommitAuthor");
        }
        if (user == null) {
            user = "anonymous";
        }
        return user;
    }

    @Override
    public String getCommitMessage() {
        String message = (String) tx.getProperty(GEOGIT_COMMIT_MESSAGE);
        if (message == null) {
            message = (String) tx.getProperty("VersioningCommitMessage");
        }
        if (message == null) {
            message = "No commit message provided";
        }
        return message;
    }

    @Override
    public String getCommitter() {
        return getAuthor();
    }

}
