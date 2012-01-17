package org.geoserver.security.web;

import org.geoserver.security.config.SecurityNamedServiceConfig;
import org.geoserver.web.ComponentInfo;

public class SecurityNamedServicePanelInfo
    <C extends SecurityNamedServiceConfig, T extends SecurityNamedServicePanel<C>> 
    extends ComponentInfo<T> {

    String shortTitleKey;
    Class serviceClass;
    Class<C> serviceConfigClass;

    public String getShortTitleKey() {
        return shortTitleKey;
    }
    
    public void setShortTitleKey(String shortTitleKey) {
        this.shortTitleKey = shortTitleKey;
    }

    public Class getServiceClass() {
        return serviceClass;
    }

    public void setServiceClass(Class serviceClass) {
        this.serviceClass = serviceClass;
    }

    public Class<C> getServiceConfigClass() {
        return serviceConfigClass;
    }

    public void setServiceConfigClass(Class<C> serviceConfigClass) {
        this.serviceConfigClass = serviceConfigClass;
    }
}
