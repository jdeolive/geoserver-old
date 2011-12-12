/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */


package org.geoserver.web.security.config.details;

import java.util.Iterator;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.ajax.markup.html.tabs.AjaxTabbedPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.util.tester.FormTester;
import org.geoserver.security.GeoServerSecurityManager;
import org.geoserver.security.config.SecurityNamedServiceConfig;
import org.geoserver.web.security.AbstractSecurityPage;
import org.geoserver.web.security.AbstractSecurityWicketTestSupport;
import org.geoserver.web.security.config.list.NamedServicesPanel;

public abstract class AbstractNamedConfigDetailsPanelTest extends AbstractSecurityWicketTestSupport {

    protected AbstractSecurityPage tabbedPage;
    protected FormTester form;
    
    GeoServerSecurityManager manager; 
    
    protected void newFormTester() {
        form = tester.newFormTester(getDetailsFormComponentId());
    }

    @Override
    protected void setUpInternal() throws Exception {
        super.setUpInternal();
        manager= getSecurityManager();
    }
    
    protected abstract AbstractSecurityPage getTabbedPage();
    protected abstract Integer getTabIndex();
    protected abstract Class<? extends Component> getNamedServicesClass();
    protected abstract String getDetailsFormComponentId();

    

    
    protected void activatePanel() {
        
        
        tabbedPage=getTabbedPage();
        tester.startPage(tabbedPage);
        tester.assertRenderedPage(tabbedPage.getPageClass());
        String linkId = getTabbedPanel().getId()+":tabs-container:tabs:"+getTabIndex()+":link";
        tester.clickLink(linkId,true);
        assertEquals(getNamedServicesClass(), getNamedServicesPanel().getClass());

        
    }
    
    protected AjaxTabbedPanel getTabbedPanel() {
        return (AjaxTabbedPanel) tabbedPage.get(AbstractSecurityPage.TabbedPanelId);
    }
    
    protected NamedServicesPanel getNamedServicesPanel() {
        return (NamedServicesPanel) tabbedPage.get(getTabbedPanel().getId()+":panel");
        
    }
    
    void clickAddNew() {
        tester.clickLink("tabbedPanel:panel:addNew");
    }
    
    protected DataView<SecurityNamedServiceConfig> getDataView() {
        return (DataView<SecurityNamedServiceConfig>)
                getNamedServicesPanel().get("table:listContainer:items");
    }
    
    protected int countItmes() {
        return  getDataView().getItemCount();
    }
    
    protected SecurityNamedServiceConfig getSecurityNamedServiceConfig(String name) {
        //<SecurityNamedServiceConfig>
       Iterator<Item<SecurityNamedServiceConfig>> it = getDataView().getItems();
       while (it.hasNext()) {
           Item<SecurityNamedServiceConfig> item = it.next();
           if (name.equals(item.getModelObject().getName()))
               return item.getModelObject();
       }
    return null;   
    }

    protected void setSecurityConfigName(String aName) {
        form.setValue("config.name", aName);
    }
    
    protected String getSecurityConfigName() {
        return form.getForm().get("config.name").getDefaultModelObjectAsString();
    }

    protected String setSecurityConfigClassName() {
        return form.getForm().get("config.className").getDefaultModelObjectAsString();
    }
    
    protected void setSecurityConfigClassName(String aName) {
        form.setValue("config.className", aName);
    }

    protected void clickSave() {
        form.submit("save");
    }
    protected void clickCancel() {        
        form.submitLink("cancel",false);
    }
    
}
