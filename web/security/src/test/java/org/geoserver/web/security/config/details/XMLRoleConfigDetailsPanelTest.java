/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */


package org.geoserver.web.security.config.details;

import java.lang.reflect.Method;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.geoserver.security.xml.XMLRoleService;
import org.geoserver.security.xml.XMLRoleServiceConfig;
import org.geoserver.web.security.AbstractSecurityPage;
import org.geoserver.web.security.config.RoleTabbedPage;
import org.geoserver.web.security.config.SecurityServicesTabbedPage;
import org.geoserver.web.security.config.list.RoleServicesPanel;

public  class XMLRoleConfigDetailsPanelTest extends AbstractNamedConfigDetailsPanelTest {

    RoleTabbedPage detailsPage;
    
    @Override
    protected String getDetailsFormComponentId() {
        return "RoleTabbedPage:panel:namedConfig";
    }
    
    @Override
    protected AbstractSecurityPage getTabbedPage() {
        return new SecurityServicesTabbedPage();
    }

    @Override
    protected Integer getTabIndex() {
        return 1;
    }

    @Override
    protected Class<? extends Component> getNamedServicesClass() {
        return RoleServicesPanel.class;
    }
    
    protected void setAdminRoleName(String roleName){
        formTester.setValue("details:config.adminRoleName",roleName);        
    }
    
    protected String getAdminRoleName(){
        return formTester.getForm().get("details:config.adminRoleName").getDefaultModelObjectAsString();
    }
    
    protected void setFileName(String fileName){
        formTester.setValue("details:config.fileName",fileName);        
    }
    
    protected String getFileName(){
        return formTester.getForm().get("details:config.fileName").getDefaultModelObjectAsString();
    }
    
    protected void setCheckInterval(Integer interval){
        formTester.setValue("details:config.checkInterval",interval.toString());        
    }
    
    protected Integer getCheckInterval (){
        String temp= formTester.getForm().get("details:config.checkInterval").getDefaultModelObjectAsString();
        if (temp == null || temp.length()==0) return 0;
        return new Integer(temp);
    }

    protected void setValidating(Boolean flag){
        formTester.setValue("details:config.validating",flag);        
    }
    
    protected Boolean getValidating(){
        String temp= formTester.getForm().get("details:config.validating").getDefaultModelObjectAsString();
        return Boolean.valueOf(temp);
    }
        
                                    
    public void testAddModifyRemove() throws Exception{
        initializeForXML();
        
        activatePanel();
                
        assertEquals(2, countItmes());
        assertNotNull(getSecurityNamedServiceConfig("default"));
        assertNotNull(getSecurityNamedServiceConfig("test"));
        assertNull(getSecurityNamedServiceConfig("xxxxxxxx"));
        
        // Test simple add
        clickAddNew();
        
        
        tester.assertRenderedPage(RoleTabbedPage.class);
        detailsPage = (RoleTabbedPage) tester.getLastRenderedPage();
        newFormTester();
        setSecurityConfigClassName(XMLRoleService.class.getName());
        newFormTester();
        setSecurityConfigName("default2");                        
        setFileName("abc.xml");
        setCheckInterval(5000);
        setValidating(true);
        clickCancel();
        
        tester.assertRenderedPage(tabbedPage.getClass());
        assertEquals(2, countItmes());
        assertNotNull(getSecurityNamedServiceConfig("default"));
        
        clickAddNew();
        newFormTester();
        setSecurityConfigClassName(XMLRoleService.class.getName());
        newFormTester();
        setSecurityConfigName("default2");        
        setFileName("abc.xml");
        setCheckInterval(5000);
        setValidating(true);
        tester.assertRenderedPage(detailsPage.getClass());
        clickSave();
        
        tester.assertRenderedPage(tabbedPage.getClass());
        assertEquals(3, countItmes());        
        assertNotNull(getSecurityNamedServiceConfig("default"));
        
        XMLRoleServiceConfig xmlConfig=
                (XMLRoleServiceConfig)
                getSecurityNamedServiceConfig("default2");
        assertNotNull(xmlConfig);
        assertEquals("default2",xmlConfig.getName());
        assertEquals(XMLRoleService.class.getName(),xmlConfig.getClassName());
        assertNull(xmlConfig.getAdminRoleName());
        assertEquals("abc.xml",xmlConfig.getFileName());
        assertEquals(5000,xmlConfig.getCheckInterval());
        assertEquals(true,xmlConfig.isValidating());
        
        // reload from manager
        xmlConfig=
                (XMLRoleServiceConfig)
                getSecurityManager().loadRoleServiceConfig("default2");
        assertNotNull(xmlConfig);
        assertEquals("default2",xmlConfig.getName());
        assertEquals(XMLRoleService.class.getName(),xmlConfig.getClassName());
        assertNull(xmlConfig.getAdminRoleName());
        assertEquals("abc.xml",xmlConfig.getFileName());
        assertEquals(5000,xmlConfig.getCheckInterval());
        assertEquals(true,xmlConfig.isValidating());
        
        // test add with name clash        
        clickAddNew();        
        detailsPage = (RoleTabbedPage) tester.getLastRenderedPage();
        newFormTester();
        setSecurityConfigClassName(XMLRoleService.class.getName());
        newFormTester();
        setSecurityConfigName("default2");                
        clickSave(); // should not work
        tester.assertRenderedPage(detailsPage.getClass());
        testErrorMessagesWithRegExp(".*default2.*");
        clickCancel();
        tester.assertRenderedPage(tabbedPage.getClass());
        // end test add with name clash        
        
        // start test modify        
        clickNamedServiceConfig("default");
        tester.assertRenderedPage(RoleTabbedPage.class);
        detailsPage = (RoleTabbedPage) tester.getLastRenderedPage();
        newFormTester();
        setAdminRoleName("ROLE_ADMINISTRATOR");
        //setFileName("abcd.xml");
        setCheckInterval(5001);
        setValidating(true);
        clickCancel();
        tester.assertRenderedPage(tabbedPage.getClass());

        xmlConfig=
                (XMLRoleServiceConfig)
                getSecurityNamedServiceConfig("default");        
        assertEquals("ROLE_ADMINISTRATOR",xmlConfig.getAdminRoleName());
        assertEquals("roles.xml",xmlConfig.getFileName());
        assertEquals(10000,xmlConfig.getCheckInterval());
        assertEquals(true,xmlConfig.isValidating());
                
        
        clickNamedServiceConfig("default2");
        detailsPage = (RoleTabbedPage) tester.getLastRenderedPage();
        newFormTester();
        setAdminRoleName(null);
        //setFileName("abcd.xml");
        setCheckInterval(5001);
        setValidating(false);                
        clickSave();
        tester.assertRenderedPage(tabbedPage.getClass());
        
        xmlConfig=
                (XMLRoleServiceConfig)
                getSecurityNamedServiceConfig("default2");
        assertNull(xmlConfig.getAdminRoleName());
        assertEquals("abc.xml",xmlConfig.getFileName());
        assertEquals(5001,xmlConfig.getCheckInterval());
        assertEquals(false,xmlConfig.isValidating());
        
        // reload from manager
        xmlConfig=(XMLRoleServiceConfig)
                getSecurityManager().loadRoleServiceConfig("default2");
        assertNull(xmlConfig.getAdminRoleName());
        assertEquals("abc.xml",xmlConfig.getFileName());
        assertEquals(5001,xmlConfig.getCheckInterval());
        assertEquals(false,xmlConfig.isValidating());
                        
        doRemove("tabbedPanel:panel:removeSelected");
    }
    
    @Override
    protected void simulateDeleteSubmit() throws Exception {
        SelectionNamedServiceRemovalLink link =   
                (SelectionNamedServiceRemovalLink)  getRemoveLink();
        Method m = link.getDelegate().getClass().getDeclaredMethod("onSubmit", AjaxRequestTarget.class,Component.class);
        m.invoke(link.getDelegate(), null,null);
        
        assertNull(getSecurityManager().loadRoleServiceConfig("default2"));
        assertNotNull(getSecurityManager().loadRoleServiceConfig("default"));
    }

}
