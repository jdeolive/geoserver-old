/* Copyright (c) 2001 - 2012 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.security.web;

import java.util.logging.Logger;

import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.geoserver.security.GeoServerSecurityManager;
import org.geoserver.security.config.SecurityNamedServiceConfig;
import org.geoserver.web.GeoServerApplication;
import org.geotools.util.logging.Logging;

/**
 * Base class for configuration panels of a specific class of named security service.
 * 
 * @author Justin Deoliveira, OpenGeo
 *
 */
public abstract class SecurityNamedServicePanel<T extends SecurityNamedServiceConfig> 
    extends FormComponentPanel {

    /**
     * logger
     */
    protected static Logger LOGGER = Logging.getLogger("org.geoserver.web.security");

    /**
     * feedback panel for info and error messages 
     */
    protected FeedbackPanel feedbackPanel;

    /**
     * model for underlying config
     */
    protected IModel<T> configModel;

    public SecurityNamedServicePanel(String id, IModel<T> model) {
        super(id, new Model());
        this.configModel = model;

        setOutputMarkupId(true);
        add(new TextField("name").setRequired(true).setEnabled(model.getObject().getId() == null));

        add(feedbackPanel = (FeedbackPanel) new FeedbackPanel("feedback").setOutputMarkupId(true));
    }

    protected GeoServerSecurityManager getSecurityManager() {
        return GeoServerApplication.get().getSecurityManager();
    }

    /**
     * Determines if the configuration object represents a new configuration, or an existing one. 
     */
    protected boolean isNew() {
        return configModel.getObject().getId() == null;
    }

    public abstract void doSave(T config) throws Exception;

    public abstract void doLoad(T config) throws Exception;
}
