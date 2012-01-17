package org.geoserver.security.web;

import java.util.List;

import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.model.IModel;
import org.geoserver.security.config.SecurityNamedServiceConfig;

public interface SecurityNamedServiceTabbedPanel<T extends SecurityNamedServiceConfig> {

    List<ITab> createTabs(IModel<T> model);

}
