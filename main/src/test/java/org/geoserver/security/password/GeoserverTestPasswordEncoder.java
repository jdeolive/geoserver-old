package org.geoserver.security.password;

import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.authentication.encoding.PlaintextPasswordEncoder;

public class GeoserverTestPasswordEncoder extends  AbstractGeoserverPasswordEncoder{

    @Override
    public String getPrefix() {
        return "plain4711";
    }

    @Override
    protected PasswordEncoder getActualEncoder() {
        return new PlaintextPasswordEncoder();
    }

    @Override
    public PasswordEncodingType getEncodingType() {
        return PasswordEncodingType.PLAIN;
    }

}
