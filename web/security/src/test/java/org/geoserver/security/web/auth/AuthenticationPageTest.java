package org.geoserver.security.web.auth;

import java.util.List;

import org.apache.wicket.extensions.markup.html.form.palette.component.Recorder;
import org.apache.wicket.util.tester.FormTester;
import org.geoserver.security.GeoServerAuthenticationProvider;
import org.geoserver.security.UsernamePasswordAuthenticationProvider;
import org.geoserver.security.config.SecurityManagerConfig;
import org.geoserver.security.web.AbstractSecurityWicketTestSupport;
import org.geoserver.security.web.SecuritySettingsPage;
import org.springframework.security.authentication.AnonymousAuthenticationProvider;

public class AuthenticationPageTest extends AbstractSecurityWicketTestSupport {

    AuthenticationPage page;

    public void test() throws Exception {
        initializeForXML();
        createUserPasswordAuthProvider("default2", "default");
        activateRORoleService();
        
        tester.startPage(page = new AuthenticationPage());

        SecurityManagerConfig config = getSecurityManager().getSecurityConfig();
        tester.assertModelValue("form:anonymousAuth", config.isAnonymousAuth());
        tester.assertComponent("form:authChain:authProviderNames:recorder", Recorder.class);

        List<String> selected = (List<String>) (page.get("form:authChain:authProviderNames")).getDefaultModelObject();
        assertEquals(1, selected.size());
        assertTrue(selected.contains("default"));
        assertTrue(hasAuthProviderImpl(AnonymousAuthenticationProvider.class));

        FormTester form = tester.newFormTester("form");
        form.setValue("anonymousAuth", false);
        form.setValue("authChain:authProviderNames:recorder", "default2");
        form.submit("save");
        tester.assertNoErrorMessage();

        assertEquals(false,
            hasAuthProviderImpl(AnonymousAuthenticationProvider.class));

        boolean authProvFound = false;
        for (GeoServerAuthenticationProvider prov : getSecurityManager()
                .getAuthenticationProviders()) {
            if (UsernamePasswordAuthenticationProvider.class.isAssignableFrom(prov
                    .getClass())) {
                if (((UsernamePasswordAuthenticationProvider) prov).getName()
                        .equals("default2")) {
                    authProvFound = true;
                    break;
                }
    
            }
        }
        assertTrue(authProvFound);

    }
    
//    protected void assignAuthProvider(String providerName) throws Exception {
//        form.setValue("config.authProviderNames:recorder", providerName);
//        // tester.executeAjaxEvent(formComponentId+":config.authProviderNames:recorder",
//        // "onchange");
//        // newFormTester();
//    }
//    
    protected boolean hasAuthProviderImpl(Class<?> aClass) {
        for (Object o : getSecurityManager().getProviders()) {
            if (o.getClass() == aClass)
                return true;
        }
        return false;
    }
}

