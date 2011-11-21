/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.web.security.config.list;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.geoserver.security.config.SecurityNamedServiceConfig;
import org.geoserver.security.config.SecurityUserGoupServiceConfig;
import org.geoserver.web.security.AbstractSecurityPage;
import org.geoserver.web.security.config.UserGroupTabbedPage;
import org.geoserver.web.wicket.GeoServerDataProvider;
import org.geoserver.web.wicket.GeoServerDataProvider.Property;

public class UserGroupServicesPanel extends NamedServicesPanel {

    public UserGroupServicesPanel(String id) {
        super(id);
        
    }

    private static final long serialVersionUID = 1L;

    class UserGroupServiceTablePanel extends NamedServicesTablePanel<SecurityUserGoupServiceConfig> {
        private static final long serialVersionUID = 1L;

        public UserGroupServiceTablePanel(String id,
                GeoServerDataProvider<SecurityUserGoupServiceConfig> dataProvider,
                boolean selectable) {
            super(id, dataProvider, selectable);            
        }
        @Override
        protected Component getComponentForProperty(String id, IModel itemModel,
                Property<SecurityUserGoupServiceConfig> property) {
            Component comp = super.getComponentForProperty(id, itemModel, property);
            if (comp!=null) return comp;
            if (property == UserGroupServiceListProvider.PASSWORDENCODERNAME) {
                return new Label(id,property.getModel(itemModel).getObject().toString());
            }
            if (property == UserGroupServiceListProvider.PASSWORDPOLICYNAME) {
                return new Label(id,property.getModel(itemModel).getObject().toString());
            }
            throw new RuntimeException("Unknow propterty: "+property.getName());
        }

        
    }
    
    @Override
    protected  AbstractSecurityPage getEditPage(String serviceName) {
        return new UserGroupTabbedPage(serviceName,(AbstractSecurityPage) getPage());
    }

    @Override
    protected NamedServicesTablePanel<? extends SecurityNamedServiceConfig> getTablePanel() {
        UserGroupServiceListProvider prov = new UserGroupServiceListProvider();
        return new  UserGroupServiceTablePanel("table",prov,true);
    }

    @Override
    protected AbstractSecurityPage getNewPage() {
        return new UserGroupTabbedPage((AbstractSecurityPage) getPage());
    }
    
}
