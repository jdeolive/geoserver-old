package org.geoserver.geogit;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class GeoServerAuthenticationResolver implements AuthenticationResolver {

    /**
     * @see org.geoserver.geogit.AuthenticationResolver#getCurrentUserName()
     */
    @Override
    public String getCurrentUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = null;// annonymous
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                userName = ((UserDetails) principal).getUsername();
            }
        }
        return userName;
    }

}
