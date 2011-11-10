package org.geoserver.web.security.group;

import java.io.IOException;

import org.geoserver.security.jdbc.H2RoleServiceTest;
import org.geoserver.security.jdbc.H2UserGroupServiceTest;

public class JDBCEditGroupPageTest extends EditGroupPageTest {

    public void testFill() throws Exception{
        initializeForJDBC();
        doTestFill();
    }
    
    public void testReadOnlyUserGroupService() throws Exception {
        initializeForJDBC();
        doTestReadOnlyUserGroupService();
    }
    
    public void testReadOnlyRoleService() throws Exception {
        initializeForJDBC();
        doTestReadOnlyRoleService();
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
