package org.geoserver.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.geoserver.security.config.SecurityManagerConfig;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter;

public class GeoServerSecurityFilterChainProxy extends FilterChainProxy 
    implements SecurityManagerListener  {
    
    static ThreadLocal<HttpServletRequest> REQUEST = new ThreadLocal<HttpServletRequest>();

    //security manager
    GeoServerSecurityManager securityManager;

    //security context integration filters
    SecurityContextPersistenceFilter httpSessionContextWithASCFilter, httpSessionContextWithNoASCFilter;
    SecurityContextHolderAwareRequestFilter securityContextHolderFilter;

    //auth filters
    GeoserverAuthenticationProcessingFilter usernamePasswordAuthFilter;
    RememberMeAuthenticationFilter rememberMeAuthFilter;
    AnonymousAuthenticationFilter anonymousAuthFilter;
    BasicAuthenticationFilter basicAuthFilter, basicAuthNoRememberMeFilter;

    //exception translators
    ExceptionTranslationFilter exceptionTranslationFilter, owsExceptionTranslationFilter;

    //logout
    LogoutFilter logoutFilter;

    //main authentication filters
    FilterSecurityInterceptor filterSecurityInterceptor, restFilterSecurityInterceptor; 

    public GeoServerSecurityFilterChainProxy(GeoServerSecurityManager securityManager) {
        this.securityManager = securityManager;
        this.securityManager.addListener(this);
    }

    public void setHttpSessionContextWithASCFilter(
            SecurityContextPersistenceFilter httpSessionContextWithASCFilter) {
        this.httpSessionContextWithASCFilter = httpSessionContextWithASCFilter;
    }

    public void setHttpSessionContextWithNoASCFilter(
            SecurityContextPersistenceFilter httpSessionContextWithNoASCFilter) {
        this.httpSessionContextWithNoASCFilter = httpSessionContextWithNoASCFilter;
    }

    public void setSecurityContextHolderFilter(
            SecurityContextHolderAwareRequestFilter securityContextHolderFilter) {
        this.securityContextHolderFilter = securityContextHolderFilter;
    }

    public void setUsernamePasswordAuthFilter(
            GeoserverAuthenticationProcessingFilter usernamePasswordAuthFilter) {
        this.usernamePasswordAuthFilter = usernamePasswordAuthFilter;
    }

    public void setRememberMeAuthFilter(RememberMeAuthenticationFilter rememberMeAuthFilter) {
        this.rememberMeAuthFilter = rememberMeAuthFilter;
    }

    public void setAnonymousAuthFilter(AnonymousAuthenticationFilter anonymousAuthFilter) {
        this.anonymousAuthFilter = anonymousAuthFilter;
    }

    public void setBasicAuthFilter(BasicAuthenticationFilter basicAuthFilter) {
        this.basicAuthFilter = basicAuthFilter;
    }

    public void setBasicAuthNoRememberMeFilter(BasicAuthenticationFilter basicAuthNoRememberMeFilter) {
        this.basicAuthNoRememberMeFilter = basicAuthNoRememberMeFilter;
    }

    public void setExceptionTranslationFilter(ExceptionTranslationFilter exceptionTranslationFilter) {
        this.exceptionTranslationFilter = exceptionTranslationFilter;
    }

    public void setOwsExceptionTranslationFilter(
            ExceptionTranslationFilter owsExceptionTranslationFilter) {
        this.owsExceptionTranslationFilter = owsExceptionTranslationFilter;
    }

    public void setLogoutFilter(LogoutFilter logoutFilter) {
        this.logoutFilter = logoutFilter;
    }

    public void setFilterSecurityInterceptor(FilterSecurityInterceptor filterSecurityInterceptor) {
        this.filterSecurityInterceptor = filterSecurityInterceptor;
    }

    public void setRestFilterSecurityInterceptor(FilterSecurityInterceptor restFilterSecurityInterceptor) {
        this.restFilterSecurityInterceptor = restFilterSecurityInterceptor;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        //set the request thread local
        REQUEST.set((HttpServletRequest) request);
        try {
            super.doFilter(request, response, chain);
        }
        finally {
            REQUEST.remove();
        }
    }

    @Override
    public void handlePostChanged(GeoServerSecurityManager securityManager) {
        createFilterChain();
    }

    public void afterPropertiesSet() {
        createFilterChain();
        super.afterPropertiesSet();
    };

    void createFilterChain() {
        SecurityManagerConfig config = securityManager.getSecurityConfig();

        GeoServerSecurityFilterChain filterChain = new GeoServerSecurityFilterChain();
        filterChain.put("/web/**", createWebFilterList(config));
        filterChain.put("/j_spring_security_check/**", createSecurityCheckFilterList(config));
        filterChain.put("/j_spring_security_logout/**", createSecurityLogoutFilterList(config));
        filterChain.put("/rest/**", createRestFilterList(config));
        filterChain.put("/gwc/rest/web/**", createGwcWebFilterList(config));
        filterChain.put("/gwc/rest/**", createGwcRestFilterList(config));
        filterChain.put("/**", createCatchAllFilterList(config));

        //callback to security providers to hack the filter chains
        for (GeoServerAuthenticationProvider authProvider : 
            securityManager.getAuthenticationProviders()) {
            authProvider.configureFilterChain(filterChain);
        }

        synchronized (this) {
            setFilterChainMap(filterChain);
        }
    }

    List<Filter> createWebFilterList(SecurityManagerConfig config) {
        //<sec:filter-chain pattern="/web/**" filters="httpSessionContextIntegrationFilterWithASCTrue,logoutFilter,authenticationProcessingFilter,securityContextHolderAwareRequestFilter,rememberMeProcessingFilter,anonymousProcessingFilter,consoleExceptionTranslationFilter,filterInvocationInterceptor" />
        List<Filter> list = new ArrayList<Filter>();
        list.add(httpSessionContextWithASCFilter);
        list.add(logoutFilter);
        list.add(usernamePasswordAuthFilter);
        list.add(securityContextHolderFilter);
        list.add(rememberMeAuthFilter);
        if (config.isAnonymousAuth()) {
            list.add(anonymousAuthFilter);
        }
        list.add(exceptionTranslationFilter);
        list.add(filterSecurityInterceptor);
        return list;
    }

    List<Filter> createSecurityCheckFilterList(SecurityManagerConfig config) {
        //<sec:filter-chain pattern="/j_spring_security_check/**" filters="consoleExceptionTranslationFilter,filterInvocationInterceptor" />
        List<Filter> list = new ArrayList<Filter>();
        list.add(httpSessionContextWithASCFilter);
        list.add(logoutFilter);
        list.add(usernamePasswordAuthFilter);
        list.add(securityContextHolderFilter);
        list.add(rememberMeAuthFilter);
        if (config.isAnonymousAuth()) {
            list.add(anonymousAuthFilter);
        }
        list.add(exceptionTranslationFilter);
        list.add(filterSecurityInterceptor);
        return list;
    }

    List<Filter> createSecurityLogoutFilterList(SecurityManagerConfig config) {
        //<sec:filter-chain pattern="/j_spring_security_logout/**" filters="consoleExceptionTranslationFilter,filterInvocationInterceptor" />
        return createSecurityCheckFilterList(config);
    }

    List<Filter> createRestFilterList(SecurityManagerConfig config) {
        //<sec:filter-chain pattern="/rest/**" filters="httpSessionContextIntegrationFilterWithASCFalse,basicProcessingFilter,anonymousProcessingFilter,owsExceptionTranslationFilter,restFilterInvocationInterceptor" />
        List<Filter> list = new ArrayList<Filter>();
        list.add(httpSessionContextWithNoASCFilter);
        list.add(logoutFilter);
        list.add(usernamePasswordAuthFilter);
        list.add(securityContextHolderFilter);
        list.add(rememberMeAuthFilter);
        if (config.isAnonymousAuth()) {
            list.add(anonymousAuthFilter);
        }
        list.add(owsExceptionTranslationFilter);
        list.add(restFilterSecurityInterceptor);
        return list;
    }

    List<Filter> createGwcWebFilterList(SecurityManagerConfig config) {
        //<sec:filter-chain pattern="/gwc/rest/web/**" filters="anonymousProcessingFilter,consoleExceptionTranslationFilter,filterInvocationInterceptor" />
        List<Filter> list = new ArrayList<Filter>();
        if (config.isAnonymousAuth()) {
            list.add(anonymousAuthFilter);
        }
        list.add(exceptionTranslationFilter);
        list.add(filterSecurityInterceptor);
        return list;
    }

    List<Filter> createGwcRestFilterList(SecurityManagerConfig config) {
        //<sec:filter-chain pattern="/gwc/rest/**" filters="httpSessionContextIntegrationFilterWithASCFalse,basicProcessingFilterWithoutRememberMeService,owsExceptionTranslationFilter,restFilterInvocationInterceptor" />
        List<Filter> list = new ArrayList<Filter>();
        list.add(httpSessionContextWithNoASCFilter);
        list.add(basicAuthNoRememberMeFilter);
        list.add(owsExceptionTranslationFilter);
        list.add(restFilterSecurityInterceptor);
        return list;
    }

    List<Filter> createCatchAllFilterList(SecurityManagerConfig config) {
        //<sec:filter-chain pattern="/**" filters="httpSessionContextIntegrationFilterWithASCFalse,basicProcessingFilter,anonymousProcessingFilter,owsExceptionTranslationFilter,filterInvocationInterceptor" />
        List<Filter> list = new ArrayList<Filter>();
        list.add(httpSessionContextWithNoASCFilter);
        list.add(basicAuthFilter);
        if (config.isAnonymousAuth()) {
            list.add(anonymousAuthFilter);
        }
        list.add(owsExceptionTranslationFilter);
        list.add(filterSecurityInterceptor);
        return list;
    }
}
