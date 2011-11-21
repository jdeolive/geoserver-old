/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.web.security.config.list;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.geoserver.security.GeoServerSecurityManager;
import org.geoserver.security.config.SecurityNamedServiceConfig;
import org.geoserver.web.GeoServerApplication;
import org.geoserver.web.wicket.GeoServerDataProvider;

/**
 * Page list provider for {@link SecurityNamedServiceConfig} objects
 * 
 * @author christian
 *
 */
public abstract class NamedServiceConfigListProvider<T extends SecurityNamedServiceConfig> extends GeoServerDataProvider<T> {

    private static final long serialVersionUID = 1L;
    public static final Property<SecurityNamedServiceConfig> NAME = new BeanProperty<SecurityNamedServiceConfig>("name", "name");

    public final static String ImplementationPropertyName = "className"; 
    public  class ImplementationProperty implements Property<SecurityNamedServiceConfig>  {
        
        private static final long serialVersionUID = 1L;

        @Override
        public String getName() {
            return ImplementationPropertyName;
        }

        @Override
        public Object getPropertyValue(SecurityNamedServiceConfig item) {
            return new ResourceModel("security."+item.getClassName(), 
                    item.getClassName()).toString();
        }

        @Override
        public IModel getModel(IModel itemModel) {
            return new Model((String) getPropertyValue((SecurityNamedServiceConfig) itemModel.getObject()));
        }

        @Override
        public Comparator<SecurityNamedServiceConfig> getComparator() {
            return new PropertyComparator<SecurityNamedServiceConfig>(this);
        }

        @Override
        public boolean isVisible() {
            return true;
        }

        @Override
        public boolean isSearchable() {
            return true;
        }        
    };

            
    @Override
    protected List<Property<T>> getProperties() {
        List<Property<T >> result =
                new ArrayList<Property<T>>();
        result.add((org.geoserver.web.wicket.GeoServerDataProvider.Property<T>) NAME);
        result.add((org.geoserver.web.wicket.GeoServerDataProvider.Property<T>) new ImplementationProperty());
        return result;
    }

    protected GeoServerSecurityManager getSecurityManager() {
        return GeoServerApplication.get().getSecurityManager();
    }
}
