/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.web.security.config.list;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.geoserver.security.GeoserverRoleService;
import org.geoserver.security.config.SecurityNamedServiceConfig;
import org.geoserver.security.config.SecurityRoleServiceConfig;
import org.geoserver.web.security.AbstractSecurityPage;
import org.geoserver.web.security.config.RoleTabbedPage;
import org.geoserver.web.wicket.GeoServerDataProvider;
import org.geoserver.web.wicket.GeoServerDataProvider.Property;

public class RoleServicesPanel extends NamedServicesPanel {

    public RoleServicesPanel(String id) {
        super(id);
        
    }

    private static final long serialVersionUID = 1L;

    class RoleServiceTablePanel extends NamedServicesTablePanel<SecurityRoleServiceConfig> {
        private static final long serialVersionUID = 1L;

        public RoleServiceTablePanel(String id,
                GeoServerDataProvider<SecurityRoleServiceConfig> dataProvider,
                boolean selectable) {
            super(id, dataProvider, selectable);            
        }
        @Override
        protected Component getComponentForProperty(String id, IModel itemModel,
                Property<SecurityRoleServiceConfig> property) {
            Component comp = super.getComponentForProperty(id, itemModel, property);
            if (comp!=null) return comp;
            if (property == RoleServiceListProvider.ADMINROLENAME) {
                return new Label(id,property.getModel(itemModel).getObject().toString());
            }
            throw new RuntimeException("Unknow propterty: "+property.getName());
        }

        
    }

    @Override
    protected Class<?> getServiceClass() {
        return GeoserverRoleService.class;
    }

    @Override
    protected  AbstractSecurityPage getEditPage(String serviceName) {
        return new RoleTabbedPage(serviceName,(AbstractSecurityPage) getPage());
    }

    @Override
    protected NamedServicesTablePanel<? extends SecurityNamedServiceConfig> getTablePanel() {
        RoleServiceListProvider prov = new RoleServiceListProvider();
        return new  RoleServiceTablePanel("table",prov,true);
    }

    @Override
    protected AbstractSecurityPage getNewPage() {
        return new RoleTabbedPage((AbstractSecurityPage) getPage());
    }    
}
