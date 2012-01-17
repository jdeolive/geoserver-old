package org.geoserver.security.web.usergroup;

import org.geoserver.security.xml.XMLUserGroupService;
import org.geoserver.security.xml.XMLUserGroupServiceConfig;

public class XMLUserGroupServicePanelInfo 
    extends UserGroupServicePanelInfo<XMLUserGroupServiceConfig, XMLUserGroupServicePanel> {

    public XMLUserGroupServicePanelInfo() {
        setComponentClass(XMLUserGroupServicePanel.class);
        setServiceClass(XMLUserGroupService.class);
        setServiceConfigClass(XMLUserGroupServiceConfig.class);
    }
}
