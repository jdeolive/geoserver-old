package org.geoserver.web.security.role;

import java.io.IOException;

import org.geoserver.security.jdbc.H2RoleServiceTest;
import org.geoserver.security.jdbc.H2UserGroupServiceTest;

public class JDBCRoleListPageTest extends RoleListPageTest {

    public void testRemove() throws Exception {
        initializeForJDBC();
        insertValues();
        addAdditonalData();
        doRemove("headerPanel:removeSelected");
    }

    void initializeForJDBC() throws IOException {
        initialize(new H2UserGroupServiceTest(), new H2RoleServiceTest());
    }
    
    @Override
    public String getRoleServiceName() {
        return "h2";
    }

    @Override
    public String getUserGroupServiceName() {
        return "h2";
    }

}
