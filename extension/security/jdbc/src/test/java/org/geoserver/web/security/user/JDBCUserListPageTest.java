package org.geoserver.web.security.user;

import java.io.IOException;

import org.geoserver.security.jdbc.H2RoleServiceTest;
import org.geoserver.security.jdbc.H2UserGroupServiceTest;

public class JDBCUserListPageTest extends UserListPageTest {
    public void testRemoveWithRoles() throws Exception {
        withRoles=true;
        initializeForJDBC();
        insertValues();
        addAdditonalData();
        doRemove("headerPanel:removeSelectedWithRoles");
    }
    
    public void testRemoveJDBC() throws Exception {
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
