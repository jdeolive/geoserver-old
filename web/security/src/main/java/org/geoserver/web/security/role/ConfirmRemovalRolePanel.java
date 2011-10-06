package org.geoserver.web.security.role;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.geoserver.security.impl.GeoserverGrantedAuthority;
import org.geoserver.web.security.AbstractConfirmRemovalPanel;

public class ConfirmRemovalRolePanel extends  AbstractConfirmRemovalPanel<GeoserverGrantedAuthority> {

    private static final long serialVersionUID = 1L;

    
    public ConfirmRemovalRolePanel(String id, List<GeoserverGrantedAuthority> roots) {
        super(id, roots);        
    }
    
    public ConfirmRemovalRolePanel(String id, GeoserverGrantedAuthority... roots) {
        this(id, Arrays.asList(roots));
    }

    @Override
    protected String getConfirmationMessage(GeoserverGrantedAuthority object) throws Exception{
        return (String) BeanUtils.getProperty(object, "authority");
    }
 

}
