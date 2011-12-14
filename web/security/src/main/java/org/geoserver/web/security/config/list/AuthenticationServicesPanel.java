/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.web.security.config.list;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.geoserver.security.GeoServerAuthenticationProvider;
import org.geoserver.security.config.SecurityAuthProviderConfig;
import org.geoserver.security.config.SecurityNamedServiceConfig;
import org.geoserver.web.security.AbstractSecurityPage;
import org.geoserver.web.security.config.AuthenticationProviderPage;
import org.geoserver.web.wicket.GeoServerDataProvider;
import org.geoserver.web.wicket.GeoServerDataProvider.Property;

public class AuthenticationServicesPanel extends NamedServicesPanel {

    public AuthenticationServicesPanel(String id) {
        super(id);
        
    }

    private static final long serialVersionUID = 1L;

    class AuthenticationServiceTablePanel extends NamedServicesTablePanel<SecurityAuthProviderConfig> {
        private static final long serialVersionUID = 1L;

        public AuthenticationServiceTablePanel(String id,
                GeoServerDataProvider<SecurityAuthProviderConfig> dataProvider,
                boolean selectable) {
            super(id, dataProvider, selectable);            
        }
        @Override
        protected Component getComponentForProperty(String id, IModel itemModel,
                Property<SecurityAuthProviderConfig> property) {
            Component comp = super.getComponentForProperty(id, itemModel, property);
            if (comp!=null) return comp;
            if (property == AuthProviderListProvider.USERGROUPSERVICENAME) {
                return new Label(id,property.getModel(itemModel).getObject().toString());
            }

            throw new RuntimeException("Unknow propterty: "+property.getName());
        }

        
    }
    
    @Override
    protected Class<?> getServiceClass() {
        return GeoServerAuthenticationProvider.class;
    }
    
    @Override
    protected  AbstractSecurityPage getEditPage(String serviceName) {
        return new AuthenticationProviderPage(serviceName,(AbstractSecurityPage) getPage());
    }

    @Override
    protected NamedServicesTablePanel<? extends SecurityNamedServiceConfig> getTablePanel() {
        AuthProviderListProvider prov = new AuthProviderListProvider();
        return new  AuthenticationServiceTablePanel("table",prov,true);
    }

    @Override
    protected AbstractSecurityPage getNewPage() {
        return new AuthenticationProviderPage((AbstractSecurityPage) getPage());
    }    
}
