package org.geoserver.web.security.role;

import java.io.IOException;

import org.geoserver.security.jdbc.H2RoleServiceTest;
import org.geoserver.security.jdbc.H2UserGroupServiceTest;

public class JDBCEditRolePageTest extends EditRolePageTest {

    public void testFill() throws Exception{
        initializeForJDBC();
        doTestFill();
    }

    public void testFill2() throws Exception{
        initializeForJDBC();
        doTestFill2();
    }

    void initializeForJDBC() throws IOException {
        initialize(new H2UserGroupServiceTest(), new H2RoleServiceTest());
    }
}
