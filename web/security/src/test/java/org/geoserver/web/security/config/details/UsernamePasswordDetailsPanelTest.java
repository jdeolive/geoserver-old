/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */


package org.geoserver.web.security.config.details;

import java.lang.reflect.Method;
import java.util.SortedSet;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.geoserver.security.UsernamePasswordAuthenticationProvider;
import org.geoserver.security.config.impl.UsernamePasswordAuthenticationProviderConfig;
import org.geoserver.security.impl.GeoserverUserGroup;
import org.geoserver.web.security.AbstractSecurityPage;
import org.geoserver.web.security.config.AuthenticationProviderPage;
import org.geoserver.web.security.config.GlobalTabbedPage;
import org.geoserver.web.security.config.list.AuthenticationServicesPanel;
import org.geoserver.web.security.group.SelectionGroupRemovalLink;

public  class UsernamePasswordDetailsPanelTest extends AbstractNamedConfigDetailsPanelTest {

    AuthenticationProviderPage detailsPage;
    
    @Override
    protected String getDetailsFormComponentId() {
        return "authenticationProviderPanel:namedConfig";
    }
    
    @Override
    protected AbstractSecurityPage getTabbedPage() {
        return new GlobalTabbedPage();
    }

    @Override
    protected Integer getTabIndex() {
        return 2;
    }

    @Override
    protected Class<? extends Component> getNamedServicesClass() {
        return AuthenticationServicesPanel.class;
    }
    
    protected void setUGName(String serviceName){
        form.setValue("details:config.userGroupServiceName", serviceName);
    }
    
    protected String getUGServiceName(){
        return form.getForm().get("details:config.userGroupServiceName").getDefaultModelObjectAsString();
    }
    
    
                                
    public void testAdd() throws Exception{
        initializeForXML();
        
        activatePanel();
                
        assertEquals(1, countItmes());
        assertNotNull(getSecurityNamedServiceConfig("default"));
        assertNull(getSecurityNamedServiceConfig("xxxxxxxx"));
        
        // Test simple add
        clickAddNew();
        
        tester.assertRenderedPage(AuthenticationProviderPage.class);
        detailsPage = (AuthenticationProviderPage) tester.getLastRenderedPage();
        newFormTester();
        
        setSecurityConfigName("default2");
        setSecurityConfigClassName(UsernamePasswordAuthenticationProvider.class.getName());
        setUGName("default");
        clickCancel();
        
        tester.assertRenderedPage(tabbedPage.getClass());
        assertEquals(1, countItmes());
        assertNotNull(getSecurityNamedServiceConfig("default"));
        
        clickAddNew();
        newFormTester();
        setSecurityConfigName("default2");
        setSecurityConfigClassName(UsernamePasswordAuthenticationProvider.class.getName());
        setUGName("default");
        clickSave();
        
        
        tester.assertRenderedPage(tabbedPage.getClass());
        assertEquals(2, countItmes());        
        assertNotNull(getSecurityNamedServiceConfig("default"));
        UsernamePasswordAuthenticationProviderConfig authConfig=
                (UsernamePasswordAuthenticationProviderConfig)
                getSecurityNamedServiceConfig("default2");
        assertNotNull(authConfig);
        assertEquals("default2",authConfig.getName());
        assertEquals(UsernamePasswordAuthenticationProvider.class.getName(),authConfig.getClassName());
        assertEquals("default",authConfig.getUserGroupServiceName());
        
        // test add with name clash        
        clickAddNew();        
        detailsPage = (AuthenticationProviderPage) tester.getLastRenderedPage();
        newFormTester();
        setSecurityConfigName("default2");
        setSecurityConfigClassName(UsernamePasswordAuthenticationProvider.class.getName());
        setUGName("default");
        clickSave(); // should not work
        tester.assertRenderedPage(detailsPage.getClass());
        testErrorMessagesWithRegExp(".*default2.*");
        clickCancel();
        tester.assertRenderedPage(tabbedPage.getClass());
        // end test add with name clash        
        
        // start test modify        
        clickNamedServiceConfig("default");
        tester.assertRenderedPage(AuthenticationProviderPage.class);
        detailsPage = (AuthenticationProviderPage) tester.getLastRenderedPage();
        newFormTester();
        setUGName("test");
        clickCancel();
        tester.assertRenderedPage(tabbedPage.getClass());

        authConfig=
                (UsernamePasswordAuthenticationProviderConfig)
                getSecurityNamedServiceConfig("default");
        assertEquals("default",authConfig.getUserGroupServiceName());
        
        clickNamedServiceConfig("default2");
        detailsPage = (AuthenticationProviderPage) tester.getLastRenderedPage();
        newFormTester();
        setUGName("test");
        clickSave();
        tester.assertRenderedPage(tabbedPage.getClass());
        
        authConfig=
                (UsernamePasswordAuthenticationProviderConfig)
                getSecurityNamedServiceConfig("default2");
        assertEquals("test",authConfig.getUserGroupServiceName());
        
        doRemove("tabbedPanel:panel:removeSelected","default2");                
    }
    
    @Override
    protected void simulateDeleteSubmit() throws Exception {
        SelectionNamedServiceRemovalLink link =   
                (SelectionNamedServiceRemovalLink)  getRemoveLink();
        Method m = link.getDelegate().getClass().getDeclaredMethod("onSubmit", AjaxRequestTarget.class,Component.class);
        m.invoke(link.getDelegate(), null,null);        
    }

}
