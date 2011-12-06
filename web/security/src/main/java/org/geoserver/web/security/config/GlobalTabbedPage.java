/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.web.security.config;

    



import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.extensions.ajax.markup.html.tabs.AjaxTabbedPanel;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.extensions.markup.html.tabs.TabbedPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;
import org.geoserver.web.security.AbstractSecurityPage;
import org.geoserver.web.security.config.list.AuthenticationServicesPanel;


public class GlobalTabbedPage extends AbstractSecurityPage {
    

    protected TabbedPanel tabbedPanel;
    
    public GlobalTabbedPage() {
        initializeComponents();        
    }
    
    protected void initializeComponents() {
        List<ITab> tabs = new ArrayList<ITab>();
        tabs.add(new AbstractTab(new ResourceModel("managerConfig")) {
            private static final long serialVersionUID = 1L;

            @Override
            public Panel getPanel(String panelId) {
                return new ManagerConfigPanel(panelId);
            }
        });
        
        tabs.add(new AbstractTab(new ResourceModel("rememberMeConfig")) {
            private static final long serialVersionUID = 1L;

            @Override
            public Panel getPanel(String panelId) {
                return new RememberMeConfigPanel(panelId);
            }
        });
        
        tabs.add(new AbstractTab(new ResourceModel("authProviders")) {
            private static final long serialVersionUID = 1L;

            @Override
            public Panel getPanel(String panelId) {
                return new AuthenticationServicesPanel(panelId);
            }
        }); 


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
