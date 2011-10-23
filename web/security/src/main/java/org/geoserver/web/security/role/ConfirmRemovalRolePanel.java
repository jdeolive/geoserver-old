package org.geoserver.web.security.role;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.geoserver.security.impl.GeoserverRole;
import org.geoserver.web.security.AbstractConfirmRemovalPanel;

public class ConfirmRemovalRolePanel extends  AbstractConfirmRemovalPanel<GeoserverRole> {

    private static final long serialVersionUID = 1L;

    
    public ConfirmRemovalRolePanel(String id, List<GeoserverRole> roots) {
        super(id, roots);        
    }
    
    public ConfirmRemovalRolePanel(String id, GeoserverRole... roots) {
        this(id, Arrays.asList(roots));
    }

    @Override
    protected String getConfirmationMessage(GeoserverRole object) throws Exception{
        return (String) BeanUtils.getProperty(object, "authority");
    }
 

}
