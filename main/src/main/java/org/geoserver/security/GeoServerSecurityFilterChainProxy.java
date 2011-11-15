package org.geoserver.security;

import java.io.IOException;
import java.util.ArrayList;
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

    //security manager
    GeoServerSecurityManager securityManager;

    //app context
    ApplicationContext appContext;

    public GeoServerSecurityFilterChainProxy(GeoServerSecurityManager securityManager) {
        this.securityManager = securityManager;
        this.securityManager.addListener(this);
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
        SecurityManagerConfig config = securityManager.getSecurityConfig(); 
        GeoServerSecurityFilterChain filterChain = config.getFilterChain();

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
