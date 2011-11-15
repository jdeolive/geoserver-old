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

import org.geoserver.security.GeoServerCustomAuthTest.AuthProvider;
import org.geoserver.security.config.SecurityManagerConfig;
import org.geoserver.security.config.SecurityNamedServiceConfig;
import org.geoserver.security.config.impl.SecurityNamedServiceConfigImpl;
import org.geoserver.test.GeoServerTestSupport;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

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

    public void testActive() throws Exception {
        GeoServerSecurityManager secMgr = getSecurityManager();
        SecurityNamedServiceConfigImpl config = new SecurityNamedServiceConfigImpl();
        config.setName("custom");
        config.setClassName(Filter.class.getName());
        secMgr.saveFilter(config);

        SecurityManagerConfig mgrConfig = secMgr.getSecurityConfig();
        mgrConfig.getFilterChain().get("/**").add("custom");
        secMgr.saveSecurityConfig(mgrConfig);
        
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
            return new Filter();
        }
    }

    static class Filter extends GeoServerSecurityFilter {

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {
            ((HttpServletResponse)response).setHeader("foo", "bar");
            chain.doFilter(request, response);
        }
    }
}
