package org.geoserver.web.security.group;

import org.geoserver.security.jdbc.H2RoleServiceTest;
import org.geoserver.security.jdbc.H2UserGroupServiceTest;

public class JDBCGroupListPageTest extends GroupListPageTest {
    public void testRemoveWithRoles() throws Exception {
        withRoles=true;
        initializeForJDBC();
        insertValues();
        addAdditonalData();
        doRemove(getTabbedPanelPath()+":panel:removeSelectedWithRoles");
    }

    public void testRemoveJDBC() throws Exception {
        initializeForJDBC();
        insertValues();
        addAdditonalData();
        doRemove(getTabbedPanelPath()+":panel:removeSelected");
    }

    void initializeForJDBC() throws Exception {
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
