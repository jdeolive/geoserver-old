package org.geoserver.web.security.data;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.geoserver.security.impl.DataAccessRule;
import org.geoserver.web.security.AbstractConfirmRemovalPanel;

public class ConfirmRemovalDataAccessRulePanel extends AbstractConfirmRemovalPanel<DataAccessRule> {

    private static final long serialVersionUID = 1L;

    
    public ConfirmRemovalDataAccessRulePanel(String id, List<DataAccessRule> roots) {
        super(id, roots);        
    }
    
    public ConfirmRemovalDataAccessRulePanel(String id,DataAccessRule... roots) {
        this(id, Arrays.asList(roots));
    }

    @Override
    protected String getConfirmationMessage(DataAccessRule object) throws Exception{
        return (String) BeanUtils.getProperty(object, "workspace") + "."
                + (String) BeanUtils.getProperty(object, "layer") + "."
                + (String) BeanUtils.getProperty(object, "accessMode") + "="
                + (String) BeanUtils.getProperty(object, "roles");
    }


}
