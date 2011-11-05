package org.geoserver.security.password;

public class TestMasterPasswordProvider implements MasterPasswordProvider {

    @Override
    public String getMasterPassword() {
        return "masterpw";
    }
}
