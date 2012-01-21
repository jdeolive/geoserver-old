/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.web.security.config.list;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.geoserver.security.config.SecurityNamedServiceConfig;
import org.geoserver.web.security.AbstractSecurityPage;
import org.geoserver.web.security.config.details.SelectionNamedServiceRemovalLink;
import org.geoserver.web.wicket.GeoServerDataProvider;
import org.geoserver.web.wicket.GeoServerDataProvider.Property;
import org.geoserver.web.wicket.GeoServerDialog;
import org.geoserver.web.wicket.GeoServerTablePanel;
import org.geoserver.web.wicket.SimpleAjaxLink;

/**
 * A page listing roles, allowing for removal, addition and linking to an edit page
 */
@SuppressWarnings("serial")
public abstract class NamedServicesPanel extends Panel {

    class NamedServicesTablePanel<T extends SecurityNamedServiceConfig> extends GeoServerTablePanel<T> {

        public NamedServicesTablePanel(String id, GeoServerDataProvider<T> dataProvider, final boolean selectable) {
            super(id, dataProvider,selectable);
            
        }
        
        
        @Override
        protected Component getComponentForProperty(String id, IModel itemModel,
                Property<T> property) {
            if (property == NamedServiceConfigListProvider.NAME) {
                return editLink(id, itemModel, property);
            } else if (NamedServiceConfigListProvider.ImplementationPropertyName.equals(property.getName())) {
                    //return new Label(property.getModel(itemModel).;
                    return new Label(id,property.getModel(itemModel).getObject().toString());
            } 
            return null;
        }
        
        @Override
        protected void onSelectionUpdate(AjaxRequestTarget target) {
            removal.setEnabled(namedServices.getSelection().size() > 0);               
            target.addComponent(removal);
        }

        Component editLink(String id, IModel<SecurityNamedServiceConfig> itemModel, Property<T> property) {
            return new SimpleAjaxLink<SecurityNamedServiceConfig>(id, itemModel, property.getModel(itemModel)) {
                @Override
                protected void onClick(AjaxRequestTarget target) {
                    setResponsePage(getEditPage(getModelObject().getName()));
                }

            };
        }
        
    };
    
    protected GeoServerTablePanel<? extends SecurityNamedServiceConfig> namedServices;
    protected GeoServerDialog dialog;
    protected SelectionNamedServiceRemovalLink removal;
    protected Link<?> add;

    protected abstract NamedServicesTablePanel<? extends SecurityNamedServiceConfig> getTablePanel();
    protected abstract AbstractSecurityPage getEditPage(String serviceName);
    protected abstract AbstractSecurityPage getNewPage();
    protected abstract Class<?> getServiceClass();
    
    public NamedServicesPanel(String id) {
        super(id);
                        
        add(namedServices=getTablePanel());
        namedServices.setOutputMarkupId(true);
        add(dialog = new GeoServerDialog("dialog"));
        headerPanel();

    }
    
    protected void headerPanel() {
        add(add=new Link<Object>("addNew") {
            @Override
            public void onClick() {
                setResponsePage(getNewPage());
            }                        
        });        
                                
        // the removal button
        add(removal = new SelectionNamedServiceRemovalLink("removeSelected", 
                (GeoServerTablePanel<SecurityNamedServiceConfig>) namedServices, dialog,getServiceClass()));
        removal.setOutputMarkupId(true);
        removal.setEnabled(false);
        

    }


    


}
