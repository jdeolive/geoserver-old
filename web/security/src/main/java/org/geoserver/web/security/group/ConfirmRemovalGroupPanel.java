package org.geoserver.web.security.group;

import java.util.List;
import java.util.SortedSet;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.wicket.model.Model;
import org.geoserver.security.impl.GeoserverRole;
import org.geoserver.security.impl.GeoserverUserGroup;
import org.geoserver.web.GeoServerApplication;
import org.geoserver.web.security.AbstractConfirmRemovalPanel;

public class ConfirmRemovalGroupPanel extends AbstractConfirmRemovalPanel<GeoserverUserGroup> {

    private static final long serialVersionUID = 1L;

    
    public ConfirmRemovalGroupPanel(String id, Model<Boolean> model,List<GeoserverUserGroup> roots) {
        super(id, model,roots);                
    }
    
    public ConfirmRemovalGroupPanel(String id, Model<Boolean> model,GeoserverUserGroup... roots) {
        super(id, model,roots);                
    }

    
    @Override
    protected String getConfirmationMessage(GeoserverUserGroup object) throws Exception{
        StringBuffer buffer = new StringBuffer(BeanUtils.getProperty(object, "groupname"));
        if ((Boolean) getDefaultModelObject()) {
            SortedSet<GeoserverRole> roles =
                GeoServerApplication.get().getSecurityManager()
                    .getActiveRoleService().getRolesForGroup(object.getGroupname());
            buffer.append(" [");
            for (GeoserverRole role: roles) {
                buffer.append(role.getAuthority()).append(" ");
            }
            if (roles.size()>0) { // remove last delimiter
                buffer.setLength(buffer.length()-1);
            }
            buffer.append("]");
        }
        return buffer.toString();
    }
 
}
