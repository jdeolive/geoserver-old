package org.geoserver.security.web;

import org.geoserver.security.web.role.RoleServicesPanel;
import org.geoserver.security.web.usergroup.UserGroupServicesPanel;
import org.geoserver.web.security.AbstractSecurityPage;
import org.geoserver.web.wicket.HelpLink;

public class UserGroupRoleServicesPage extends AbstractSecurityPage {

    public UserGroupRoleServicesPage() {
        add(new UserGroupServicesPanel("userGroupServices"));
        add(new HelpLink("userGroupServicesHelp").setDialog(dialog));
        
        add(new RoleServicesPanel("roleServices"));
        add(new HelpLink("roleServicesHelp").setDialog(dialog));
    }
}
