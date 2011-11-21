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
import org.apache.wicket.model.Model;
import org.geoserver.web.security.AbstractSecurityPage;


public class AuthServicesTabbedPage extends AbstractSecurityPage {

    protected TabbedPanel tabbedPanel;

    
    public AuthServicesTabbedPage() {
        initializeComponents();
        
        
    }
    
    protected void initializeComponents() {
        //  AuthServicesTabbedPage.providers=Providers

        List<ITab> tabs = new ArrayList<ITab>();
//        tabs.add(new AbstractTab(new Model<String>("first tab")) {
//            
//            @Override
//            public Panel getPanel(String panelId) {
//                return null;
//            }
//        }); 

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
