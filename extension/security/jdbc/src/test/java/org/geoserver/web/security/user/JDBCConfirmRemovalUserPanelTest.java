/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.web.security.user;

import java.io.IOException;

import org.geoserver.security.jdbc.H2RoleServiceTest;
import org.geoserver.security.jdbc.H2UserGroupServiceTest;

public class JDBCConfirmRemovalUserPanelTest extends ConfirmRemovalUserPanelTest{

    public void testRemoveUser() throws Exception {
        disassociateRoles=false;
        initializeForJDBC();
        removeObject();
    }

    public void testRemoveUserWithRoles() throws Exception {
        disassociateRoles=true;
        initializeForJDBC();
        removeObject();
    }

    void initializeForJDBC() throws IOException {
        initialize(new H2UserGroupServiceTest(), new H2RoleServiceTest());
    }
}
