/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.web.security.config.details;

import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.geoserver.security.GeoServerSecurityManager;
import org.geoserver.security.config.SecurityNamedServiceConfig;
import org.geoserver.web.GeoServerApplication;
import org.geoserver.web.security.config.SecurityNamedConfigModelHelper;

/**
 * Common base class for details panels
 * 
 * @author christian
 *
 */
public abstract class AbstractNamedConfigDetailsPanel extends FormComponentPanel<SecurityNamedConfigModelHelper>{
    private static final long serialVersionUID = 1L;
    CompoundPropertyModel<SecurityNamedConfigModelHelper> model;
    
    public AbstractNamedConfigDetailsPanel(String id, CompoundPropertyModel<SecurityNamedConfigModelHelper> model) {
        super(id);
        this.model=model;
        SecurityNamedConfigModelHelper helper = model.getObject();
        if (helper.isNew()) {
            SecurityNamedServiceConfig newConfig = createNewConfigObject(); 
            SecurityNamedServiceConfig old =  helper.getConfig();
            newConfig.setName(old.getName());
            newConfig.setClassName(old.getClassName());
            model.getObject().setNewConfig(newConfig);
            //model.setObject(new SecurityNamedConfigModelHelper(newConfig, true));
        }

        initializeComponents();
    }
    
    /**
     * Subclasses should create their components here
     */
    protected  abstract void initializeComponents();

    /**
     * create a concrete {@link SecurityNamedServiceConfig} object
     * 
     * @return
     */
    protected  abstract SecurityNamedServiceConfig createNewConfigObject();
    
    GeoServerSecurityManager getSecurityManager() {
        return GeoServerApplication.get().getSecurityManager();
    }
    
    public void updateModel() {
        // do nothing
    }
}
