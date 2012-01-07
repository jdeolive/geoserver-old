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
import org.geoserver.security.GeoserverUserGroupService;
import org.geoserver.security.config.BaseSecurityNamedServiceConfig;
import org.geoserver.security.config.SecurityUserGroupServiceConfig;
import org.geoserver.web.GeoServerApplication;
import org.geoserver.web.security.AbstractSecurityPage;
import org.geoserver.web.security.group.GroupPanel;
import org.geoserver.web.security.user.UserPanel;
import org.geoserver.web.wicket.ParamResourceModel;


public class UserGroupTabbedPage extends AbstractSecurityPage {
    
    protected TabbedPanel tabbedPanel;
    protected String serviceName;
    protected AbstractSecurityPage responsePage;
    protected IOException exception;
    

    public UserGroupTabbedPage(AbstractSecurityPage responsePage) {
        this(null,responsePage);
    }
    

    public UserGroupTabbedPage(final String serviceName,AbstractSecurityPage responsePage) {
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
                    
                    SecurityUserGroupServiceConfig config = null;
                    if (serviceName !=null && serviceName.isEmpty()==false)
                            config = GeoServerApplication.get().getSecurityManager().
                                loadUserGroupServiceConfig(serviceName);
                    
                    SecurityNamedConfigModelHelper helper = null;
                    if (config==null)
                        helper = new SecurityNamedConfigModelHelper(new BaseSecurityNamedServiceConfig(),true);
                    else
                        helper = new SecurityNamedConfigModelHelper(config,false);
                    
                    return  new NamedConfigPanel(panelId,helper,
                            GeoserverUserGroupService.class,responsePage);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }); 

         // Check if service is working
        exception = null;
        if (serviceName!=null) {
           try {
               getSecurityManager().loadUserGroupService(serviceName);               
           } catch (IOException ex) {               
               exception=ex;
           }
        }
        
        if (exception == null) {
            tabs.add(new AbstractTab(new ResourceModel("users")) {            
                private static final long serialVersionUID = 1L;
    
                @Override
                public Panel getPanel(String panelId) {
                    try {
                        return  new UserPanel(panelId,serviceName);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }); 
    
            tabs.add(new AbstractTab(new ResourceModel("groups")) {            
                private static final long serialVersionUID = 1L;
    
                @Override
                public Panel getPanel(String panelId) {
                    try {
                        return new GroupPanel(panelId,serviceName);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        } else {                 
            tabs.add(new AbstractTab(new ParamResourceModel("problem",this,serviceName== null ? "" : serviceName)) {            
                private static final long serialVersionUID = 1L;
                @Override
                public Panel getPanel(String panelId) {
                    return  new ErrorPanel(panelId,exception);
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
