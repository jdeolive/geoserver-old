package org.geoserver.security.web.role;

import org.geoserver.security.config.SecurityRoleServiceConfig;
import org.geoserver.security.web.SecurityNamedServicePanelInfo;

public class RoleServicePanelInfo 
    <C extends SecurityRoleServiceConfig, T extends RoleServicePanel<C>>
    extends SecurityNamedServicePanelInfo<C,T>{

}
