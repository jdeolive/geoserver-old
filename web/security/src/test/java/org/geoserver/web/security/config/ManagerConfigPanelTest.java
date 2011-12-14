/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.web.security.config;

import java.util.List;

import org.apache.wicket.extensions.markup.html.form.palette.component.Recorder;
import org.apache.wicket.util.tester.FormTester;
import org.geoserver.security.GeoServerAuthenticationProvider;
import org.geoserver.security.GeoServerSecurityManager;
import org.geoserver.security.UsernamePasswordAuthenticationProvider;
import org.geoserver.security.password.GeoserverConfigPBEPasswordEncoder;
import org.geoserver.security.password.GeoserverConfigPlainTextPasswordEncoder;
import org.geoserver.web.security.AbstractSecurityPage;
import org.geoserver.web.security.AbstractSecurityWicketTestSupport;
import org.springframework.security.authentication.AnonymousAuthenticationProvider;

public  class ManagerConfigPanelTest extends AbstractSecurityWicketTestSupport {

    protected GlobalTabbedPage page;
    protected FormTester form; 
    protected String formComponentId = AbstractSecurityPage.TabbedPanelId+":panel:ManagerConfigPanelForm";
    GeoServerSecurityManager manager; 
    
    protected void newFormTester() {
        form = tester.newFormTester(formComponentId);
    }


    public void testMangerConfigPanel() throws Exception {
        initializeForXML();
        createUserPasswordAuthProvider("default2", "default");
        activateRORoleService();
        manager= getSecurityManager();
        
        tester.startPage(page=new GlobalTabbedPage());
        tester.assertRenderedPage(GlobalTabbedPage.class);
        tester.clickLink(AbstractSecurityPage.TabbedPanelId+":tabs-container:tabs:0:link",true);
        tester.assertComponent(AbstractSecurityPage.TabbedPanelId+":panel", ManagerConfigPanel.class);
        tester.assertComponent(formComponentId+":config.authProviderNames:recorder", Recorder.class);
        
        tester.assertModelValue(formComponentId+":config.anonymousAuth",true);
        tester.assertModelValue(formComponentId+":config.encryptingUrlParams",false);
        tester.assertModelValue(formComponentId+":config.roleServiceName","default");
        tester.assertModelValue(formComponentId+":config.configPasswordEncrypterName",
                GeoserverConfigPBEPasswordEncoder.BeanName);
        List<String> selected = (List<String>)(page.get(formComponentId+":config.authProviderNames")).getDefaultModelObject();
        assertEquals(1, selected.size());
        assertTrue(selected.contains("default"));
        assertTrue(hasAuthProviderImpl(AnonymousAuthenticationProvider.class));

        newFormTester();
        form.setValue("config.anonymousAuth", false);
        form.setValue("config.encryptingUrlParams", true);
        form.setValue("config.roleServiceName",getRORoleServiceName());
        form.setValue("config.configPasswordEncrypterName", GeoserverConfigPlainTextPasswordEncoder.BeanName);
        form.setValue("config.authProviderNames:recorder", "default2");
        

        form.submit("save");
        
        assertEquals(false,hasAuthProviderImpl(AnonymousAuthenticationProvider.class));
        assertEquals(true,manager.isEncryptingUrlParams());
        assertEquals("strongConfigPasswordEncoder",manager.getConfigPasswordEncrypterName());
        assertEquals(getRORoleServiceName(),manager.getActiveRoleService().getName());
        
        boolean authProvFound = false;
        for (GeoServerAuthenticationProvider prov : manager.getAuthenticationProviders()) {
            if (UsernamePasswordAuthenticationProvider.class.isAssignableFrom(prov.getClass())) {
                if (((UsernamePasswordAuthenticationProvider)prov).getName().equals("default2")) {
                    authProvFound=true;
                    break;
                }
                        
            }
        }
        assertTrue(authProvFound);
        //print(page,true,true);
                                                
    }
    
    protected void assignAuthProvider(String providerName) throws Exception {
        form.setValue("config.authProviderNames:recorder", providerName);
//        tester.executeAjaxEvent(formComponentId+":config.authProviderNames:recorder", "onchange");
//        newFormTester();
    }

    protected boolean hasAuthProviderImpl(Class<?>  aClass) {
        for (Object o : manager.getProviders()) {
            if (o.getClass()==aClass)
                return true;
        }
        return false;
    }

}
