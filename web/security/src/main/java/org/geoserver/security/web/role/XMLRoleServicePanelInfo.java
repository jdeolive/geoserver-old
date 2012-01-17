package org.geoserver.security.web.role;

import org.geoserver.security.xml.XMLRoleService;
import org.geoserver.security.xml.XMLRoleServiceConfig;

public class XMLRoleServicePanelInfo 
    extends RoleServicePanelInfo<XMLRoleServiceConfig, XMLRoleServicePanel> {

    public XMLRoleServicePanelInfo() {
        setComponentClass(XMLRoleServicePanel.class);
        setServiceClass(XMLRoleService.class);
        setServiceConfigClass(XMLRoleServiceConfig.class);
    }
}
