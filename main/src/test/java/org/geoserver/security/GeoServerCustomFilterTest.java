package org.geoserver.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.geoserver.security.FilterChainEntry.Position;
import org.geoserver.security.GeoServerCustomAuthTest.AuthProvider;
import org.geoserver.security.config.SecurityManagerConfig;
import org.geoserver.security.config.SecurityNamedServiceConfig;
import org.geoserver.security.config.impl.SecurityNamedServiceConfigImpl;
import org.geoserver.test.GeoServerTestSupport;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.mockrunner.mock.web.MockHttpServletRequest;
import com.mockrunner.mock.web.MockHttpServletResponse;

public class GeoServerCustomFilterTest extends GeoServerTestSupport {

    @Override
    protected String[] getSpringContextLocations() {
        List<String> list = new ArrayList<String>(Arrays.asList(super.getSpringContextLocations()));
        list.add(getClass().getResource(getClass().getSimpleName() + "-context.xml").toString());
        return list.toArray(new String[list.size()]);
    }

    public void testInactive() throws Exception {
        HttpServletRequest request = createRequest("/foo");
        MockHttpServletResponse response = dispatch(request);
        assertNull(response.getHeader("foo"));
    }

    void setupFilterEntry(Position pos, String relativeTo, boolean assertSecurityContext) 
        throws Exception {
        
        GeoServerSecurityManager secMgr = getSecurityManager();
        
        FilterConfig config = new FilterConfig();
        config.setName("custom");
        config.setClassName(Filter.class.getName());
        config.setAssertAuth(assertSecurityContext);
        secMgr.saveFilter(config);

        SecurityManagerConfig mgrConfig = secMgr.getSecurityConfig();

        List<FilterChainEntry> filterEntries = new ArrayList<FilterChainEntry>();
        filterEntries.add(new FilterChainEntry("custom", pos, relativeTo));
        mgrConfig.getFilterChain().put("/**", filterEntries);
        secMgr.saveSecurityConfig(mgrConfig);
    }

    public void testFirst() throws Exception {
        setupFilterEntry(Position.FIRST, null, false);

        HttpServletRequest request = createRequest("/foo");
        MockHttpServletResponse response = dispatch(request);
        assertEquals("bar", response.getHeader("foo"));
    }

    public void testLast() throws Exception {
        setupFilterEntry(Position.LAST, null, true);

        HttpServletRequest request = createRequest("/foo");
        MockHttpServletResponse response = dispatch(request);
        assertEquals("bar", response.getHeader("foo"));
    }

    public void testBefore() throws Exception {
        setupFilterEntry(Position.BEFORE, 
            GeoServerSecurityFilterChain.ANONYMOUS_FILTER, false);

        HttpServletRequest request = createRequest("/foo");
        MockHttpServletResponse response = dispatch(request);
        assertEquals("bar", response.getHeader("foo"));
    }

    public void testAfter() throws Exception {
        setupFilterEntry(Position.AFTER, 
            GeoServerSecurityFilterChain.ANONYMOUS_FILTER, true);

        HttpServletRequest request = createRequest("/foo");
        MockHttpServletResponse response = dispatch(request);
        assertEquals("bar", response.getHeader("foo"));
    }

    @Override
    protected List<javax.servlet.Filter> getFilters() {
        return Arrays.asList((javax.servlet.Filter)applicationContext.getBean(GeoServerSecurityFilterChainProxy.class));
    }

    static class SecurityProvider extends GeoServerSecurityProvider {
        @Override
        public Class<? extends GeoServerSecurityFilter> getFilterClass() {
            return Filter.class;
        }
        @Override
        public GeoServerSecurityFilter createFilter(SecurityNamedServiceConfig config) {
            Filter f = new Filter();
            f.setAssertAuth(((FilterConfig)config).isAssertSecurityContext());
            return f;
        }
    }

    static class FilterConfig extends SecurityNamedServiceConfigImpl {
        boolean assertAuth = true;

        public void setAssertAuth(boolean assertAuth) {
            this.assertAuth = assertAuth;
        }

        public boolean isAssertSecurityContext() {
            return assertAuth;
        }
    }

    static class Filter extends GeoServerSecurityFilter {

        boolean assertAuth = true;

        public Filter() {
        }

        public void setAssertAuth(boolean assertAuth) {
            this.assertAuth = assertAuth;
        }

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (assertAuth) {
                assertNotNull(auth);
            }
            else {
                assertNull(auth);
            }
            ((HttpServletResponse)response).setHeader("foo", "bar");
            chain.doFilter(request, response);
        }
    }
}
