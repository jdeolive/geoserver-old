package org.geoserver.web.security.user;

import java.io.IOException;

import org.geoserver.security.jdbc.H2RoleServiceTest;
import org.geoserver.security.jdbc.H2UserGroupServiceTest;

public class JDBCNewUserPageTest extends NewUserPageTest {

    public void testFill() throws Exception{
        initializeForJDBC();
        doTestFill();
    }
    
    public void testFill3() throws Exception{
        initializeForJDBC();
        doTestFill3();
    }

    public void testFill2() throws Exception{
        initializeForJDBC();
        doTestFill2();
    }

    void initializeForJDBC() throws IOException {
        initialize(new H2UserGroupServiceTest(), new H2RoleServiceTest());
    }
}
