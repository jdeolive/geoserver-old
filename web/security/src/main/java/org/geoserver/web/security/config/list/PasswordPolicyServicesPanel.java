/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.web.security.config.list;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.geoserver.security.config.PasswordPolicyConfig;
import org.geoserver.security.config.SecurityNamedServiceConfig;
import org.geoserver.web.security.AbstractSecurityPage;
import org.geoserver.web.security.config.PasswordPolicyPage;
import org.geoserver.web.wicket.GeoServerDataProvider;
import org.geoserver.web.wicket.GeoServerDataProvider.Property;

public class PasswordPolicyServicesPanel extends NamedServicesPanel {

    public PasswordPolicyServicesPanel(String id) {
        super(id);
        
    }

    private static final long serialVersionUID = 1L;

    class PasswordPolicyServiceTablePanel extends NamedServicesTablePanel<PasswordPolicyConfig> {
        private static final long serialVersionUID = 1L;

        public PasswordPolicyServiceTablePanel(String id,
                GeoServerDataProvider<PasswordPolicyConfig> dataProvider,
                boolean selectable) {
            super(id, dataProvider, selectable);            
        }
        @Override
        protected Component getComponentForProperty(String id, IModel itemModel,
                Property<PasswordPolicyConfig> property) {
            Component comp = super.getComponentForProperty(id, itemModel, property);
            if (comp!=null) return comp;
            throw new RuntimeException("Unknow propterty: "+property.getName());
        }

        
    }
    
    @Override
    protected  AbstractSecurityPage getEditPage(String serviceName) {
        return new PasswordPolicyPage(serviceName,(AbstractSecurityPage) getPage());
    }

    @Override
    protected NamedServicesTablePanel<? extends SecurityNamedServiceConfig> getTablePanel() {
        PasswordPolicyListProvider prov = new PasswordPolicyListProvider();
        return new  PasswordPolicyServiceTablePanel("table",prov,true);
    }

    @Override
    protected AbstractSecurityPage getNewPage() {
        return new PasswordPolicyPage((AbstractSecurityPage) getPage());
    }    
}
