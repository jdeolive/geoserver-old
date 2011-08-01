package org.geoserver.geogit;

public class PlatformAuthenticationResolver implements AuthenticationResolver {

    /**
     * @see org.geoserver.geogit.AuthenticationResolver#getCurrentUserName()
     */
    @Override
    public String getCurrentUserName() {
        String userName = System.getProperty("user.name", "anonymous");
        return userName;
    }

}
