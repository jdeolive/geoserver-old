/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.geoserver.security.jdbc;

public class MySqlGrantedAuthorityServiceTest extends JDBCGrantedAuthorityServiceTest {

    @Override
    protected String getFixtureId() {
        return "mysql";
    }

}
