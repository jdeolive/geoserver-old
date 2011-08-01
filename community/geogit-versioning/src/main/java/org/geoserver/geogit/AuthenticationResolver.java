package org.geoserver.geogit;

public interface AuthenticationResolver {

    /**
     * @return {@code null} if annonymous, the name of the current user otherwise
     */
    public abstract String getCurrentUserName();

}