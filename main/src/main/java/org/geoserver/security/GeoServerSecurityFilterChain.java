/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.security;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * The security filter chain.
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class GeoServerSecurityFilterChain extends LinkedHashMap<String, List<FilterChainEntry>> {

    public static final String SECURITY_CONTEXT_ASC_FILTER = "securityContextAscFilter";
    public static final String SECURITY_CONTEXT_NO_ASC_FILTER = "securityContextNoAscFilter";
    
    public static final String SERVLET_API_SUPPORT_FILTER = "servletApiSupportFilter";

    public static final String FORM_LOGIN_FILTER = "formLoginFilter";

    public static final String REMEMBER_ME_FILTER = "rememberMeFilter";

    public static final String ANONYMOUS_FILTER = "anonymousFilter";

    public static final String BASIC_AUTH_FILTER = "basicAuthFilter";
    public static final String BASIC_AUTH_NO_REMEMBER_ME_FILTER = "basicAuthNoRememberMeFilter";

    public static final String EXCEPTION_TRANSLATION_FILTER = "exceptionTranslationFilter";
    public static final String EXCEPTION_TRANSLATION_OWS_FILTER = "exceptionTranslationOwsFilter";

    public static final String LOGOUT_FILTER = "logoutFilter";

    public static final String FILTER_SECURITY_INTERCEPTOR = "filterSecurityInterceptor";
    public static final String FILTER_SECURITY_REST_INTERCEPTOR = "filterSecurityRestInterceptor";

    public GeoServerSecurityFilterChain() {
    }
    
    public GeoServerSecurityFilterChain(GeoServerSecurityFilterChain other) {
        super(other);
    }
}
