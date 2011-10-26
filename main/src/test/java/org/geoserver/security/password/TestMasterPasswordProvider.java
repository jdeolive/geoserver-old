package org.geoserver.security.password;

public class TestMasterPasswordProvider extends MasterPasswordProvider {

    @Override
    public String getMasterPassword() {
        return "masterpw";
    }
}
