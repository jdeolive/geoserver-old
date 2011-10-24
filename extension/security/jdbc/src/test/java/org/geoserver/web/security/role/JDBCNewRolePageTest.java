package org.geoserver.web.security.role;

import java.io.IOException;

import org.geoserver.security.jdbc.H2RoleServiceTest;
import org.geoserver.security.jdbc.H2UserGroupServiceTest;

public class JDBCNewRolePageTest extends NewRolePageTest {

    public void testFill() throws Exception{
        initializeForJDBC();
        doTestFill();
    }

    void initializeForJDBC() throws IOException {
        initialize(new H2UserGroupServiceTest(), new H2RoleServiceTest());
    }
}
