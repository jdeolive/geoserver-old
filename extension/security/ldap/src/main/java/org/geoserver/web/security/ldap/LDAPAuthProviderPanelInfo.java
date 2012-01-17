package org.geoserver.web.security.ldap;

import org.geoserver.security.ldap.LDAPSecurityServiceConfig;
import org.geoserver.security.web.auth.AuthenticationProviderPanelInfo;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;


public class LDAPAuthProviderPanelInfo 
    extends AuthenticationProviderPanelInfo<LDAPSecurityServiceConfig, LDAPAuthProviderPanel> {

    public LDAPAuthProviderPanelInfo() {
        setComponentClass(LDAPAuthProviderPanel.class);
        setServiceClass(LdapAuthenticationProvider.class);
        setServiceConfigClass(LDAPSecurityServiceConfig.class);
    }
}
