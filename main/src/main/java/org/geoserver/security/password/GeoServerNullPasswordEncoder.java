package org.geoserver.security.password;

import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.encoding.PasswordEncoder;

/**
 * Password encoder that does absolute nothing, only used for testing.
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class GeoServerNullPasswordEncoder extends AbstractGeoserverPasswordEncoder {

    public GeoServerNullPasswordEncoder() {
        setReversible(true);
    }

    @Override
    protected PasswordEncoder getActualEncoder() {
        return new PasswordEncoder() {
            @Override
            public boolean isPasswordValid(String encPass, String rawPass, Object salt)
                    throws DataAccessException {
                return true;
            }
            
            @Override
            public String encodePassword(String rawPass, Object salt) throws DataAccessException {
                return rawPass;
            }
        };
    }

    @Override
    public PasswordEncodingType getEncodingType() {
        return PasswordEncodingType.NULL;
    }

    
}
