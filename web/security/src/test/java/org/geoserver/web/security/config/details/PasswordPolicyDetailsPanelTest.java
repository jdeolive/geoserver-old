/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */


package org.geoserver.web.security.config.details;

import java.lang.reflect.Method;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.geoserver.security.config.PasswordPolicyConfig;
import org.geoserver.security.validation.PasswordValidatorImpl;
import org.geoserver.web.security.AbstractSecurityPage;
import org.geoserver.web.security.config.PasswordPolicyPage;
import org.geoserver.web.security.config.SecurityServicesTabbedPage;
import org.geoserver.web.security.config.list.PasswordPolicyServicesPanel;

public  class PasswordPolicyDetailsPanelTest extends AbstractNamedConfigDetailsPanelTest {

    PasswordPolicyPage detailsPage;
    
    @Override
    protected String getDetailsFormComponentId() {
        return "passwordPolicyPanel:namedConfig";
    }
    
    @Override
    protected AbstractSecurityPage getTabbedPage() {
        return new SecurityServicesTabbedPage();
    }

    @Override
    protected Integer getTabIndex() {
        return 2;
    }

    @Override
    protected Class<? extends Component> getNamedServicesClass() {
        return PasswordPolicyServicesPanel.class;
    }
    
    protected void setDigitRequired(boolean value){
        formTester.setValue("details:config.digitRequired", value);
    }
    
    protected boolean getDigitRequired(boolean value){
        return (Boolean) formTester.getForm().get("details:config.digitRequired").getDefaultModelObject();
    }

    protected void setUpperCaseRequired(boolean value){
        formTester.setValue("details:config.uppercaseRequired", value);
    }
    
    protected boolean getUpperCaseRequired(boolean value){
        return (Boolean) formTester.getForm().get("details:config.uppercaseRequired").getDefaultModelObject();
    }
    
    protected void setLowerCaseRequired(boolean value){
        formTester.setValue("details:config.lowercaseRequired", value);
    }
    
    protected boolean getLowerCaseRequired(boolean value){
        return (Boolean) formTester.getForm().get("details:config.lowercaseRequired").getDefaultModelObject();
    }
    
    protected void setUnlimted(boolean value){
        formTester.setValue("details:unlimited", value);
        tester.executeAjaxEvent("passwordPolicyPanel:namedConfig:details:unlimited","onclick");
                                 
    }
    
    protected boolean getUnlimted(boolean value){
        return (Boolean) formTester.getForm().get("details:unlimited").getDefaultModelObject();
    }
    
    protected void setMinLength(int value){
        formTester.setValue("details:config.minLength", new Integer(value).toString());
    }
    
    protected int getMinLength(int value){
        return (Integer) formTester.getForm().get("details:config.minLength").getDefaultModelObject();
    }

    protected void setMaxLength(int value){
        formTester.setValue("details:config.maxLength", new Integer(value).toString());
    }
    
    protected int getMaxLength(int value){
        return (Integer) formTester.getForm().get("details:config.maxLength").getDefaultModelObject();
    }
    
                                
    public void testAddModifyRemove() throws Exception{
        initializeForXML();
        
        activatePanel();
                
        assertEquals(2, countItmes());
        assertNotNull(getSecurityNamedServiceConfig("default"));
        assertNull(getSecurityNamedServiceConfig("xxxxxxxx"));
        
        // Test simple add
        clickAddNew();
        
        tester.assertRenderedPage(PasswordPolicyPage.class);
        detailsPage = (PasswordPolicyPage) tester.getLastRenderedPage();
        newFormTester();
        setSecurityConfigClassName(PasswordValidatorImpl.class.getName());
        newFormTester();
        
        setSecurityConfigName("default2");        
        setMinLength(5);
        clickCancel();
        
        tester.assertRenderedPage(tabbedPage.getClass());
        assertEquals(2, countItmes());
        assertNotNull(getSecurityNamedServiceConfig("default"));
        assertNotNull(getSecurityNamedServiceConfig("master"));
        
        clickAddNew();
        //detailsPage = (PasswordPolicyPage) tester.getLastRenderedPage();
        newFormTester();
        setSecurityConfigClassName(PasswordValidatorImpl.class.getName());
        setUnlimted(false);
        tester.assertVisible("passwordPolicyPanel:namedConfig:details:config.maxLength");
        tester.assertVisible("passwordPolicyPanel:namedConfig:details:maxLengthLabel");
        newFormTester();
        setSecurityConfigName("default2");        
        setDigitRequired(true);
        setUpperCaseRequired(true);
        setLowerCaseRequired(true);        
        setMinLength(2);        
        setMaxLength(4);
        clickSave();
        
        assertEquals(tabbedPage.getClass(),tester.getLastRenderedPage().getClass());
        assertEquals(3, countItmes());        
        assertNotNull(getSecurityNamedServiceConfig("default"));
        assertNotNull(getSecurityNamedServiceConfig("master"));
        PasswordPolicyConfig pwConfig=
                (PasswordPolicyConfig)
                getSecurityNamedServiceConfig("default2");
        assertNotNull(pwConfig);
        assertEquals("default2",pwConfig.getName());
        assertEquals(PasswordValidatorImpl.class.getName(),pwConfig.getClassName());
        assertTrue(pwConfig.isDigitRequired());
        assertTrue(pwConfig.isLowercaseRequired());
        assertTrue(pwConfig.isUppercaseRequired());
        assertEquals(2, pwConfig.getMinLength());
        assertEquals(4, pwConfig.getMaxLength());
        
        // reload from manager
        pwConfig= (PasswordPolicyConfig)
                getSecurityManager().loadPasswordPolicyConfig("default2");
        assertNotNull(pwConfig);
        assertEquals("default2",pwConfig.getName());
        assertEquals(PasswordValidatorImpl.class.getName(),pwConfig.getClassName());
        assertTrue(pwConfig.isDigitRequired());
        assertTrue(pwConfig.isLowercaseRequired());
        assertTrue(pwConfig.isUppercaseRequired());
        assertEquals(2, pwConfig.getMinLength());
        assertEquals(4, pwConfig.getMaxLength());
        

        // test add with name clash        
        clickAddNew();        
        detailsPage = (PasswordPolicyPage) tester.getLastRenderedPage();
        newFormTester();
        setSecurityConfigClassName(PasswordValidatorImpl.class.getName());        
        newFormTester();
        setSecurityConfigName("default2");
        clickSave(); // should not work
        tester.assertRenderedPage(detailsPage.getClass());
        testErrorMessagesWithRegExp(".*default2.*");
        clickCancel();
        tester.assertRenderedPage(tabbedPage.getClass());
        // end test add with name clash        
        
        // start test modify        
        clickNamedServiceConfig("default2");
        tester.assertRenderedPage(PasswordPolicyPage.class);
        detailsPage = (PasswordPolicyPage) tester.getLastRenderedPage();
        newFormTester();
        setMaxLength(27);
        clickCancel();
        tester.assertRenderedPage(tabbedPage.getClass());

        pwConfig=
                (PasswordPolicyConfig)
                getSecurityNamedServiceConfig("default2");
        assertEquals(4,pwConfig.getMaxLength());
        
        clickNamedServiceConfig("default2");
        detailsPage = (PasswordPolicyPage) tester.getLastRenderedPage();
        newFormTester();
        setUnlimted(true);
        tester.assertInvisible("passwordPolicyPanel:namedConfig:details:config.maxLength");
        tester.assertInvisible("passwordPolicyPanel:namedConfig:details:maxLengthLabel");
        newFormTester();
        setDigitRequired(false);
        setUpperCaseRequired(false);
        setLowerCaseRequired(false);

        setMinLength(3);        
        
        clickSave();
        tester.assertRenderedPage(tabbedPage.getClass());
        
        pwConfig=
                (PasswordPolicyConfig )
                getSecurityNamedServiceConfig("default2");
        
        assertFalse(pwConfig.isDigitRequired());
        assertFalse(pwConfig.isLowercaseRequired());
        assertFalse(pwConfig.isUppercaseRequired());
        assertEquals(3, pwConfig.getMinLength());
        assertEquals(-1, pwConfig.getMaxLength());
        
        pwConfig=getSecurityManager().loadPasswordPolicyConfig("default2");

        assertFalse(pwConfig.isDigitRequired());
        assertFalse(pwConfig.isLowercaseRequired());
        assertFalse(pwConfig.isUppercaseRequired());
        assertEquals(3, pwConfig.getMinLength());
        assertEquals(-1, pwConfig.getMaxLength());

        
        doRemove("tabbedPanel:panel:removeSelected");
    }
    
    @Override
    protected void simulateDeleteSubmit() throws Exception {
        SelectionNamedServiceRemovalLink link =   
                (SelectionNamedServiceRemovalLink)  getRemoveLink();
        Method m = link.getDelegate().getClass().getDeclaredMethod("onSubmit", AjaxRequestTarget.class,Component.class);
        m.invoke(link.getDelegate(), null,null);
        
        assertNull(getSecurityManager().loadPasswordPolicyConfig("default2"));
        assertNotNull(getSecurityManager().loadPasswordPolicyConfig("default"));
        assertNotNull(getSecurityManager().loadPasswordPolicyConfig("master"));
    }

}
