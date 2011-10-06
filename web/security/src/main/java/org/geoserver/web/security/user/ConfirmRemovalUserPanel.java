package org.geoserver.web.security.user;

import java.util.List;
import java.util.SortedSet;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.wicket.model.Model;
import org.geoserver.security.impl.GeoserverGrantedAuthority;
import org.geoserver.security.impl.GeoserverUser;
import org.geoserver.security.impl.GeoserverUserDetailsServiceImpl;
import org.geoserver.web.security.AbstractConfirmRemovalPanel;

public class ConfirmRemovalUserPanel extends AbstractConfirmRemovalPanel<GeoserverUser> {

    private static final long serialVersionUID = 1L;
    
    public ConfirmRemovalUserPanel(String id, Model<Boolean> model,List<GeoserverUser> roots) {        
        super(id, model,roots);
    }
    
    public ConfirmRemovalUserPanel(String id, Model<Boolean> model,GeoserverUser... roots) {
        super(id, model,roots);                
    }


    @Override
    protected String getConfirmationMessage(GeoserverUser object) throws Exception{
        StringBuffer buffer = new StringBuffer(BeanUtils.getProperty(object, "username"));
        if ((Boolean)getDefaultModelObject()) {
            SortedSet<GeoserverGrantedAuthority> roles = 
                    GeoserverUserDetailsServiceImpl.get().getGrantedAuthorityService().getRolesForUser(object.getUsername());
            buffer.append(" [");
            for (GeoserverGrantedAuthority role: roles) {
                buffer.append(role.getAuthority());
                buffer.append(" ");
            }
            if (buffer.length()>0) { // remove last delimiter
                buffer.setLength(buffer.length()-1);
            }
            buffer.append("]");
        }
        return buffer.toString();
    }
}