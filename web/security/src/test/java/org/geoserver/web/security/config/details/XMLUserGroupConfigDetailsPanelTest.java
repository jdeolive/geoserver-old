/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */


package org.geoserver.web.security.config.details;

import java.lang.reflect.Method;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.geoserver.security.password.GeoserverDigestPasswordEncoder;
import org.geoserver.security.password.GeoserverPlainTextPasswordEncoder;
import org.geoserver.security.password.GeoserverUserPBEPasswordEncoder;
import org.geoserver.security.validation.PasswordValidatorImpl;
import org.geoserver.security.xml.XMLUserGroupService;
import org.geoserver.security.xml.XMLUserGroupServiceConfig;
import org.geoserver.web.security.AbstractSecurityPage;
import org.geoserver.web.security.config.SecurityServicesTabbedPage;
import org.geoserver.web.security.config.UserGroupTabbedPage;
import org.geoserver.web.security.config.list.UserGroupServicesPanel;

public  class XMLUserGroupConfigDetailsPanelTest extends AbstractNamedConfigDetailsPanelTest {

    UserGroupTabbedPage detailsPage;
    
    @Override
    protected String getDetailsFormComponentId() {
        return "UserGroupTabbedPage:panel:namedConfig";
    }
    
    @Override
    protected AbstractSecurityPage getTabbedPage() {
        return new SecurityServicesTabbedPage();
    }

    @Override
    protected Integer getTabIndex() {
        return 0;
    }

    @Override
    protected Class<? extends Component> getNamedServicesClass() {
        return UserGroupServicesPanel.class;
    }
    
    protected void setPasswordEncoderName(String encName){        
        DropDownChoice<String> component = (DropDownChoice<String>)
                formTester.getForm().get("details:config.passwordEncoderName");
        int index = component.getChoices().indexOf(encName);                     
        formTester.select("details:config.passwordEncoderName", index);     

    }
    
    protected String getPasswordEncoderName(){
        return formTester.getForm().get("details:config.passwordEncoderName").getDefaultModelObjectAsString();
    }
    
    protected void setPasswordPolicy(String policyName){
        DropDownChoice<String> component = (DropDownChoice<String>)
                formTester.getForm().get("details:config.passwordPolicyName");
        int index = component.getChoices().indexOf(policyName);                     
        formTester.select("details:config.passwordPolicyName", index);     
    }
    
    protected String getPasswordPolicyName(){
        return formTester.getForm().get("details:config.passwordPolicyName").getDefaultModelObjectAsString();
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
        
        
        tester.assertRenderedPage(UserGroupTabbedPage.class);
        detailsPage = (UserGroupTabbedPage) tester.getLastRenderedPage();
        newFormTester();
        setSecurityConfigClassName(XMLUserGroupService.class.getName());
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
        setSecurityConfigClassName(XMLUserGroupService.class.getName());
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
        
        XMLUserGroupServiceConfig xmlConfig=
                (XMLUserGroupServiceConfig)
                getSecurityNamedServiceConfig("default2");
        assertNotNull(xmlConfig);
        assertEquals("default2",xmlConfig.getName());
        assertEquals(XMLUserGroupService.class.getName(),xmlConfig.getClassName());
        assertEquals(GeoserverDigestPasswordEncoder.BeanName,xmlConfig.getPasswordEncoderName());
        assertEquals(PasswordValidatorImpl.DEFAULT_NAME,xmlConfig.getPasswordPolicyName());
        assertEquals("abc.xml",xmlConfig.getFileName());
        assertEquals(5000,xmlConfig.getCheckInterval());
        assertEquals(true,xmlConfig.isValidating());
        
        // reload from manager
        xmlConfig=
                (XMLUserGroupServiceConfig)
                getSecurityManager().loadUserGroupServiceConfig("default2");
        assertNotNull(xmlConfig);
        assertEquals("default2",xmlConfig.getName());
        assertEquals(GeoserverDigestPasswordEncoder.BeanName,xmlConfig.getPasswordEncoderName());
        assertEquals(PasswordValidatorImpl.DEFAULT_NAME,xmlConfig.getPasswordPolicyName());
        assertEquals("abc.xml",xmlConfig.getFileName());
        assertEquals(5000,xmlConfig.getCheckInterval());
        assertEquals(true,xmlConfig.isValidating());
        
        // test add with name clash        
        clickAddNew();        
        detailsPage = (UserGroupTabbedPage) tester.getLastRenderedPage();
        newFormTester();
        setSecurityConfigClassName(XMLUserGroupService.class.getName());
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
        tester.assertRenderedPage(UserGroupTabbedPage.class);
        detailsPage = (UserGroupTabbedPage) tester.getLastRenderedPage();
        newFormTester();
        setPasswordPolicy(PasswordValidatorImpl.MASTERPASSWORD_NAME);
        setPasswordEncoderName(GeoserverPlainTextPasswordEncoder.BeanName);
        assertEquals(GeoserverDigestPasswordEncoder.BeanName,xmlConfig.getPasswordEncoderName());
        assertEquals(PasswordValidatorImpl.DEFAULT_NAME,xmlConfig.getPasswordPolicyName());

        setCheckInterval(5001);
        setValidating(true);
        clickCancel();
        tester.assertRenderedPage(tabbedPage.getClass());

        xmlConfig=
                (XMLUserGroupServiceConfig)
                getSecurityNamedServiceConfig("default");        
        assertEquals(GeoserverUserPBEPasswordEncoder.PrototypeName,xmlConfig.getPasswordEncoderName());
        assertEquals(PasswordValidatorImpl.DEFAULT_NAME,xmlConfig.getPasswordPolicyName());
        assertEquals("users.xml",xmlConfig.getFileName());
        assertEquals(10000,xmlConfig.getCheckInterval());
        assertEquals(true,xmlConfig.isValidating());
                
        
        clickNamedServiceConfig("default2");
        detailsPage = (UserGroupTabbedPage) tester.getLastRenderedPage();
        newFormTester();
        setPasswordPolicy(PasswordValidatorImpl.MASTERPASSWORD_NAME);
//        setPasswordEncoderName(GeoserverPlainTextPasswordEncoder.BeanName);
        setCheckInterval(5001);
        setValidating(false);                
        clickSave();
        tester.assertRenderedPage(tabbedPage.getClass());
        
        xmlConfig=
                (XMLUserGroupServiceConfig)
                getSecurityNamedServiceConfig("default2");
        assertEquals(GeoserverDigestPasswordEncoder.BeanName,xmlConfig.getPasswordEncoderName());
        assertEquals(PasswordValidatorImpl.MASTERPASSWORD_NAME,xmlConfig.getPasswordPolicyName());
        assertEquals("abc.xml",xmlConfig.getFileName());
        assertEquals(5001,xmlConfig.getCheckInterval());
        assertEquals(false,xmlConfig.isValidating());
        
        // reload from manager
        xmlConfig=(XMLUserGroupServiceConfig)
                getSecurityManager().loadUserGroupServiceConfig("default2");
        assertEquals(GeoserverDigestPasswordEncoder.BeanName,xmlConfig.getPasswordEncoderName());
        assertEquals(PasswordValidatorImpl.MASTERPASSWORD_NAME,xmlConfig.getPasswordPolicyName());
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
        
        assertNull(getSecurityManager().loadUserGroupServiceConfig("default2"));
        assertNotNull(getSecurityManager().loadUserGroupServiceConfig("default"));
    }

}
