/* Copyright (c) 2001 - 2012 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.security.web;

import org.geoserver.security.web.role.RoleServicesPanel;
import org.geoserver.security.web.usergroup.UserGroupServicesPanel;
import org.geoserver.web.wicket.HelpLink;

/**
 * Main menu page for user, group, and role services.
 *  
 * @author Justin Deoliveira, OpenGeo
 */
public class UserGroupRoleServicesPage extends AbstractSecurityPage {

    public UserGroupRoleServicesPage() {
        add(new UserGroupServicesPanel("userGroupServices"));
        add(new HelpLink("userGroupServicesHelp").setDialog(dialog));
        
        add(new RoleServicesPanel("roleServices"));
        add(new HelpLink("roleServicesHelp").setDialog(dialog));
    }
}
