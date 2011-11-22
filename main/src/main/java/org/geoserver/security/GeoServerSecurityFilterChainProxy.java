package org.geoserver.security;

import static org.geoserver.security.GeoServerSecurityFilterChain.ANONYMOUS_FILTER;
import static org.geoserver.security.GeoServerSecurityFilterChain.BASIC_AUTH_FILTER;
import static org.geoserver.security.GeoServerSecurityFilterChain.BASIC_AUTH_NO_REMEMBER_ME_FILTER;
import static org.geoserver.security.GeoServerSecurityFilterChain.EXCEPTION_TRANSLATION_FILTER;
import static org.geoserver.security.GeoServerSecurityFilterChain.EXCEPTION_TRANSLATION_OWS_FILTER;
import static org.geoserver.security.GeoServerSecurityFilterChain.FILTER_SECURITY_INTERCEPTOR;
import static org.geoserver.security.GeoServerSecurityFilterChain.FILTER_SECURITY_REST_INTERCEPTOR;
import static org.geoserver.security.GeoServerSecurityFilterChain.FORM_LOGIN_FILTER;
import static org.geoserver.security.GeoServerSecurityFilterChain.LOGOUT_FILTER;
import static org.geoserver.security.GeoServerSecurityFilterChain.REMEMBER_ME_FILTER;
import static org.geoserver.security.GeoServerSecurityFilterChain.SECURITY_CONTEXT_ASC_FILTER;
import static org.geoserver.security.GeoServerSecurityFilterChain.SECURITY_CONTEXT_NO_ASC_FILTER;
import static org.geoserver.security.GeoServerSecurityFilterChain.SERVLET_API_SUPPORT_FILTER;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.security.FilterChainEntry.Position;
import org.geoserver.security.config.SecurityManagerConfig;
import org.geotools.util.logging.Logging;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
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
    implements SecurityManagerListener, ApplicationContextAware  {
    
    static Logger LOGGER = Logging.getLogger("org.geoserver.security");

    static ThreadLocal<HttpServletRequest> REQUEST = new ThreadLocal<HttpServletRequest>();

    // the default/built-in filter chain
    Map<String,List<String>> defaultFilterChain;

    //security manager
    GeoServerSecurityManager securityManager;

    //app context
    ApplicationContext appContext;

    public GeoServerSecurityFilterChainProxy(GeoServerSecurityManager securityManager) {
        this.securityManager = securityManager;
        this.securityManager.addListener(this);
        defaultFilterChain = createDefaultFilterChain();
    }

    Map<String,List<String>> createDefaultFilterChain() {
        Map<String,List<String>> filterChain = new LinkedHashMap<String, List<String>>();
        
        filterChain.put("/web/**", Arrays.asList(SECURITY_CONTEXT_ASC_FILTER, LOGOUT_FILTER, 
            FORM_LOGIN_FILTER, SERVLET_API_SUPPORT_FILTER, REMEMBER_ME_FILTER, ANONYMOUS_FILTER, 
            EXCEPTION_TRANSLATION_FILTER, FILTER_SECURITY_INTERCEPTOR));

        filterChain.put("/j_spring_security_check/**", Arrays.asList(SECURITY_CONTEXT_ASC_FILTER, 
            LOGOUT_FILTER, FORM_LOGIN_FILTER, SERVLET_API_SUPPORT_FILTER, REMEMBER_ME_FILTER, 
            ANONYMOUS_FILTER, EXCEPTION_TRANSLATION_FILTER, FILTER_SECURITY_INTERCEPTOR));
        
        filterChain.put("/j_spring_security_logout/**", Arrays.asList(SECURITY_CONTEXT_ASC_FILTER, 
            LOGOUT_FILTER, FORM_LOGIN_FILTER, SERVLET_API_SUPPORT_FILTER, REMEMBER_ME_FILTER, 
            ANONYMOUS_FILTER, EXCEPTION_TRANSLATION_FILTER, FILTER_SECURITY_INTERCEPTOR));
        
        filterChain.put("/rest/**", Arrays.asList(SECURITY_CONTEXT_NO_ASC_FILTER, BASIC_AUTH_FILTER,
            ANONYMOUS_FILTER, EXCEPTION_TRANSLATION_OWS_FILTER, FILTER_SECURITY_REST_INTERCEPTOR));

        filterChain.put("/gwc/rest/web/**", Arrays.asList(ANONYMOUS_FILTER, 
            EXCEPTION_TRANSLATION_FILTER, FILTER_SECURITY_INTERCEPTOR));

        filterChain.put("/gwc/rest/**", Arrays.asList(SECURITY_CONTEXT_NO_ASC_FILTER, 
            BASIC_AUTH_NO_REMEMBER_ME_FILTER, EXCEPTION_TRANSLATION_OWS_FILTER, 
            FILTER_SECURITY_REST_INTERCEPTOR));

        filterChain.put("/**", Arrays.asList(SECURITY_CONTEXT_NO_ASC_FILTER, BASIC_AUTH_FILTER, 
            ANONYMOUS_FILTER, EXCEPTION_TRANSLATION_OWS_FILTER, FILTER_SECURITY_INTERCEPTOR));

        return filterChain;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.appContext = applicationContext;
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
        //start with the built-in filter chain
        Map<String,List<String>> filterChain = new LinkedHashMap(defaultFilterChain);

        //merge in the user configured filters
        SecurityManagerConfig config = securityManager.getSecurityConfig(); 
        GeoServerSecurityFilterChain userFilterChain = config.getFilterChain();
        for (Map.Entry<String,List<FilterChainEntry>> e : userFilterChain.entrySet()) {
            String path = e.getKey();
            List<FilterChainEntry> filterEntries = e.getValue();

            if (!defaultFilterChain.containsKey(path)) {
                //TODO: create a new filter list, starting with some sensible default
                LOGGER.warning(String.format("Path '%s' for security filter does not exist, ignoring"
                    + " filters: %s", path, filterEntries));
                continue;
            }

            List<String> filterNames = new ArrayList(filterChain.get(path));

            //loop through added filters and merge into appropriate position
            int first = 0, last = 0;
            for (FilterChainEntry filterEntry : filterEntries) {
                String filterName = filterEntry.getFilterName();
                if (filterEntry.getPosition() == Position.FIRST) {
                    if (++first > 1) {
                        LOGGER.warning(String.format("Illegal position for filter '%s' in chain '%s', "
                          +  "FIRST must not be specified more than once", filterName, path));
                        continue;
                    }
                    filterNames.add(0, filterEntry.getFilterName());
                }
                else if (filterEntry.getPosition() == Position.LAST) {
                    if (++last > 1) {
                        LOGGER.warning(String.format("Illegal position for filter '%s' in chain '%s', "
                          +  "LAST must not be specified more than once", filterName, path));
                          continue;
                    }
                    filterNames.add(filterEntry.getFilterName());
                }
                else {
                    //relative filter
                    String relativeTo = filterEntry.getRelativeTo();
                    int i = 0;
                    for (; i < filterNames.size(); i++) {
                        if (filterNames.get(i).equalsIgnoreCase(relativeTo)) {
                            break;
                        }
                    }
                    if (i == filterNames.size()) {
                        LOGGER.warning(String.format("Illegal position for filter '%s' in chain '%s', "
                          +  "relative filter '%s' not found", filterName, path, relativeTo));
                        continue;
                    }

                    if (filterEntry.getPosition() == Position.BEFORE) {
                        filterNames.add(i, filterName);
                    }
                    else {
                        filterNames.add(i+1, filterName);
                    }
                }
            }

            //update the filter chain
            filterChain.put(path, filterNames);
        }

        //build up the map with actual filters, by processing bean names from the filter chain
        LinkedHashMap<String, List<Filter>> filterChainMap = new LinkedHashMap();
        
        for (Map.Entry<String,List<String>> e : filterChain.entrySet()) {
            List<Filter> filters = new ArrayList<Filter>();
            for (String filterName : e.getValue()) {
                try {
                    Filter filter = lookupFilter(filterName);
                    if (filter == null) {
                        throw new NullPointerException("No filter named " + filterName +" could " +
                            "be found");
                    }

                    //check for anonymous auth flag
                    if (filter instanceof AnonymousAuthenticationFilter && !config.isAnonymousAuth()) {
                        continue;
                    }
                    filters.add(filter);
                }
                catch(Exception ex) {
                    LOGGER.log(Level.SEVERE, "Error loading filter: " + filterName, ex);
                }
            }
            filterChainMap.put(e.getKey(), filters);
        }

        synchronized (this) {
            setFilterChainMap(filterChainMap);
        }
    }

    /**
     * looks up a named filter, first trying a provided named filter based on configuration, and 
     * then looking up a named bean in the application context.  
     */
    Filter lookupFilter(String filterName) throws IOException {
        Filter filter = securityManager.loadFilter(filterName);
        if (filter == null) {
            filter = (Filter) GeoServerExtensions.bean(filterName, appContext);
        }
        return filter;
    }
}
