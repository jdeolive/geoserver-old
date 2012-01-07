package org.geoserver.web.security.role;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.geoserver.security.impl.GeoServerRole;
import org.geoserver.web.security.AbstractConfirmRemovalPanel;

public class ConfirmRemovalRolePanel extends  AbstractConfirmRemovalPanel<GeoServerRole> {

    private static final long serialVersionUID = 1L;

    
    public ConfirmRemovalRolePanel(String id, List<GeoServerRole> roots) {
        super(id, roots);        
    }
    
    public ConfirmRemovalRolePanel(String id, GeoServerRole... roots) {
        this(id, Arrays.asList(roots));
    }

    @Override
    protected String getConfirmationMessage(GeoServerRole object) throws Exception{
        return (String) BeanUtils.getProperty(object, "authority");
    }
 

}
