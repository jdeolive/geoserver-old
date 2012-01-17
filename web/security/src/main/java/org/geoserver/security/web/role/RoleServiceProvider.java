package org.geoserver.security.web.role;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.geoserver.security.config.SecurityRoleServiceConfig;
import org.geoserver.security.config.SecurityUserGroupServiceConfig;
import org.geoserver.security.web.SecurityNamedServiceProvider;
import org.geoserver.web.wicket.GeoServerDataProvider.BeanProperty;
import org.geoserver.web.wicket.GeoServerDataProvider.Property;

public class RoleServiceProvider extends SecurityNamedServiceProvider<SecurityRoleServiceConfig> {

    public static final Property<SecurityRoleServiceConfig> ADMIN_ROLE = 
            new BeanProperty("adminRoleName", "adminRoleName");
    
    @Override
    protected List<SecurityRoleServiceConfig> getItems() {
        List <SecurityRoleServiceConfig> result = new ArrayList<SecurityRoleServiceConfig>();
        try {
            for (String name : getSecurityManager().listRoleServices()) {
                result.add(getSecurityManager().loadRoleServiceConfig(name));
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return result;
    }

    @Override
    protected List<Property<SecurityRoleServiceConfig>> getProperties() {
        List props = new ArrayList(super.getProperties());
        props.add(ADMIN_ROLE);
        return props;
    }
}
