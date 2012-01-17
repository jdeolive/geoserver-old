package org.geoserver.security.web.usergroup;

import org.geoserver.security.config.SecurityUserGroupServiceConfig;
import org.geoserver.security.web.SecurityNamedServicePanelInfo;

public class UserGroupServicePanelInfo 
    <C extends SecurityUserGroupServiceConfig, T extends UserGroupServicePanel<C>>
    extends SecurityNamedServicePanelInfo<C,T>{

}
