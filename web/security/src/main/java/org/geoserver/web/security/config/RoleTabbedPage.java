/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.web.security.config;

    
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.extensions.ajax.markup.html.tabs.AjaxTabbedPanel;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.extensions.markup.html.tabs.TabbedPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;
import org.geoserver.security.GeoserverRoleService;
import org.geoserver.security.config.SecurityRoleServiceConfig;
import org.geoserver.security.config.impl.SecurityNamedServiceConfigImpl;
import org.geoserver.web.GeoServerApplication;
import org.geoserver.web.security.AbstractSecurityPage;
import org.geoserver.web.security.role.RolePanel;


public class RoleTabbedPage extends AbstractSecurityPage {
    
    protected TabbedPanel tabbedPanel;
    protected String serviceName;
    protected AbstractSecurityPage responsePage;
    

    public RoleTabbedPage(AbstractSecurityPage responsePage) {
        this(null,responsePage);
    }
    

    public RoleTabbedPage(final String serviceName,AbstractSecurityPage responsePage) {
        this.serviceName=serviceName;
        this.responsePage=responsePage;
        initializeComponents();        
    }
    
    protected void initializeComponents() {        
        List<ITab> tabs = new ArrayList<ITab>();
        
        // add the config panel first
        tabs.add(new AbstractTab(new ResourceModel("config")) {            
            private static final long serialVersionUID = 1L;

            @Override
            public Panel getPanel(String panelId) {
                try {
                    
                    SecurityRoleServiceConfig config = null;
                    if (serviceName !=null && serviceName.isEmpty()==false)
                            config = GeoServerApplication.get().getSecurityManager().
                                loadRoleServiceConfig(serviceName);
                    
                    SecurityNamedConfigModelHelper helper = null;
                    if (config==null)
                        helper = new SecurityNamedConfigModelHelper(new SecurityNamedServiceConfigImpl(),true);
                    else
                        helper = new SecurityNamedConfigModelHelper(config,false);
                    
                    return  new NamedConfigPanel(panelId,helper,
                            GeoserverRoleService.class,responsePage);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }); 

        // Check if service is working
       boolean isWorking = false;
       if (serviceName!=null) {
           try {
               getSecurityManager().loadRoleService(serviceName);
               isWorking=true;
           } catch (IOException ex) {               
           }
       }

        if (isWorking) {                        
            tabs.add(new AbstractTab(new ResourceModel("roles")) {            
                private static final long serialVersionUID = 1L;
                @Override
                public Panel getPanel(String panelId) {
                        return  new RolePanel(panelId,serviceName);
                }
            });
        }


        Integer selectedTab = null;
        if (tabbedPanel!=null)
            selectedTab = tabbedPanel.getSelectedTab();        
        addOrReplace(tabbedPanel=new AjaxTabbedPanel(this.getClass().getSimpleName(), tabs));
        if (selectedTab!=null) {
            tabbedPanel.setSelectedTab(selectedTab);
        }             
    }
    
    
        
    @Override
    protected void onBeforeRender() {
        if (isDirty()) {
            initializeComponents();
            setDirty(false);
        }
        super.onBeforeRender();

    }
}
