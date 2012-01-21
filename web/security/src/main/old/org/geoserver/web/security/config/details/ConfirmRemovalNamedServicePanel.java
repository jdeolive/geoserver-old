package org.geoserver.web.security.config.details;

import java.util.Arrays;
import java.util.List;

import org.geoserver.security.config.SecurityNamedServiceConfig;
import org.geoserver.web.security.AbstractConfirmRemovalPanel;

public class ConfirmRemovalNamedServicePanel extends  AbstractConfirmRemovalPanel<SecurityNamedServiceConfig> {

    private static final long serialVersionUID = 1L;

    
    public ConfirmRemovalNamedServicePanel(String id, List<SecurityNamedServiceConfig> roots) {
        super(id, roots);        
    }
    
    public ConfirmRemovalNamedServicePanel(String id, SecurityNamedServiceConfig... roots) {
        this(id, Arrays.asList(roots));
    }

    @Override
    protected String getConfirmationMessage(SecurityNamedServiceConfig object) throws Exception{
        return object.getName();
    }
 

}
