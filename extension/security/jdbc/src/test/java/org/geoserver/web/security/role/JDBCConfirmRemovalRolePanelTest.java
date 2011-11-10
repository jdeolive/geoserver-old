/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.web.security.role;

import java.io.IOException;

import org.geoserver.security.jdbc.H2RoleServiceTest;
import org.geoserver.security.jdbc.H2UserGroupServiceTest;

public class JDBCConfirmRemovalRolePanelTest extends ConfirmRemovalRolePanelTest {
 
    private static final long serialVersionUID = -7197515540318374854L;
    
    public void testRemoveRole() throws Exception {
        initializeForJDBC();
        removeObject();
    }

    void initializeForJDBC() throws IOException {
        initialize(new H2UserGroupServiceTest(), new H2RoleServiceTest());
    }
    
    public String getRoleServiceName() {
        return "h2";
    }
    public String getUserGroupServiceName() {
        return "h2";
    }

}
