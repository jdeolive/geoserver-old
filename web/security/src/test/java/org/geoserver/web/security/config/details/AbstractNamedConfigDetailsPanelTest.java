/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */


package org.geoserver.web.security.config.details;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.ajax.markup.html.tabs.AjaxTabbedPanel;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.util.tester.FormTester;
import org.geoserver.security.GeoServerSecurityManager;
import org.geoserver.security.config.SecurityNamedServiceConfig;
import org.geoserver.web.ComponentBuilder;
import org.geoserver.web.security.AbstractSecurityPage;
import org.geoserver.web.security.AbstractSecurityWicketTestSupport;
import org.geoserver.web.security.GeoserverTablePanelTestPage;
import org.geoserver.web.security.config.list.NamedServicesPanel;

public abstract class AbstractNamedConfigDetailsPanelTest extends AbstractSecurityWicketTestSupport {

    
    public static final String FIRST_COLUM_PATH="itemProperties:0:component:link";
    public static final String CHECKBOX_PATH="selectItemContainer:selectItem";
    protected AbstractSecurityPage tabbedPage;
    protected FormTester formTester;
    
    GeoServerSecurityManager manager; 
    
    protected void newFormTester() {
        formTester = tester.newFormTester(getDetailsFormComponentId());
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
    
    void clickRemove() {
        tester.clickLink("tabbedPanel:panel:removeSelected");
    }

    protected Component getRemoveLink() {
        Component result =tester.getLastRenderedPage().get("tabbedPanel:panel:removeSelected");
        assertNotNull(result);
        return result;
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
    
    protected void clickNamedServiceConfig(String name) {
        //<SecurityNamedServiceConfig>
       Iterator<Item<SecurityNamedServiceConfig>> it = getDataView().getItems();
       while (it.hasNext()) {
           Item<SecurityNamedServiceConfig> item = it.next();
           if (name.equals(item.getModelObject().getName()))
               tester.clickLink(item.getPageRelativePath()+":"+FIRST_COLUM_PATH);
       }
       
    }
    

    protected void checkNamedServiceConfig(String name) {
        //<SecurityNamedServiceConfig>
       Iterator<Item<SecurityNamedServiceConfig>> it = getDataView().getItems();
       while (it.hasNext()) {
           Item<SecurityNamedServiceConfig> item = it.next();
           if (name.equals(item.getModelObject().getName()))
               tester.executeAjaxEvent(item.getPageRelativePath()+":"+CHECKBOX_PATH,"onclick");
       }       
    }

    
    protected void doRemove(String pathForLink, String ... serviceNames) throws Exception {
        
        
        GeoserverTablePanelTestPage testPage = 
         new GeoserverTablePanelTestPage(new ComponentBuilder() {            
            private static final long serialVersionUID = 1L;

            public Component buildComponent(String id) {
                try {
                    return tabbedPage;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
        
        tester.startPage(testPage);
        
                
        //form:0:tabbedPanel:panel:table:listContainer:items:5:selectItemContainer:selectItem
        if (serviceNames.length==0) {
            String selectAllPath = testPage.getWicketPath()+":tabbedPanel:panel:table:listContainer:selectAllContainer:selectAll";        
            tester.assertComponent(selectAllPath, CheckBox.class);        
            FormTester ft = tester.newFormTester(GeoserverTablePanelTestPage.FORM);
            ft.setValue(testPage.getComponentId()+":tabbedPanel:panel:table:listContainer:selectAllContainer:selectAll", "true");
            tester.executeAjaxEvent(selectAllPath, "onclick");
        } 
        else {
            DataView<SecurityNamedServiceConfig> dataview = (DataView<SecurityNamedServiceConfig>)
                    testPage.get("form:0:tabbedPanel:panel:table:listContainer:items");
            List<String> nameList = Arrays.asList(serviceNames);
            FormTester ft = tester.newFormTester(GeoserverTablePanelTestPage.FORM);
            print(testPage,true,true);
            Iterator<Item<SecurityNamedServiceConfig>> it = getDataView().getItems();
            while (it.hasNext()) {
                Item<SecurityNamedServiceConfig> item = it.next();
                if (nameList.contains(item.getModelObject().getName())) {
                    String checkBoxPath=item.getPageRelativePath()+":"+CHECKBOX_PATH;
                    tester.assertComponent(checkBoxPath, CheckBox.class);
                    ft.setValue(testPage.getComponentId()+":"+checkBoxPath.replace("form:0:", ""), true);
                    tester.executeAjaxEvent(checkBoxPath, "onclick");
                }
            }
        }
        
        
        
        ModalWindow w  = (ModalWindow) testPage.get(
                testPage.getWicketPath()+":tabbedPanel:panel:dialog:dialog");
        assertNull(w.getTitle()); // window was not opened
        tester.clickLink(pathForLink);
        assertNotNull(w.getTitle()); // window was opened        
        simulateDeleteSubmit();        
        executeModalWindowCloseButtonCallback(w);
    }

    protected abstract void simulateDeleteSubmit() throws Exception;
    

    protected void setSecurityConfigName(String aName) {
        formTester.setValue("config.name", aName);
    }
    
    protected String getSecurityConfigName() {
        return formTester.getForm().get("config.name").getDefaultModelObjectAsString();
    }

    protected String setSecurityConfigClassName() {
        return formTester.getForm().get("config.className").getDefaultModelObjectAsString();
    }
    
    protected void setSecurityConfigClassName(String aName) {
        formTester.setValue("config.className", aName);
        tester.executeAjaxEvent(formTester.getForm().getPageRelativePath()+":config.className", "onchange");
    }

    protected void clickSave() {        
//        formTester.setValue("save", "save");
//        formTester.submit();
          formTester.submit("save");
    }
    protected void clickCancel() {        
        formTester.submitLink("cancel",false);
    }
    
}
