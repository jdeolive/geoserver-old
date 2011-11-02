package org.geoserver.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import junit.framework.TestCase;

public class GeoServerSecurityFilterChainTest extends TestCase {

    GeoServerSecurityFilterChain filterChain;

    protected void setUp() throws Exception {
        filterChain = new GeoServerSecurityFilterChain();
        
        filterChain.put("/foo/bar/**", (List) Arrays.asList(new Filter1()));
        filterChain.put("/foo/**", (List)Arrays.asList(new Filter2()));
        filterChain.put("/bar/**", (List)Arrays.asList(new Filter1(), new Filter2()));
        filterChain.put("/**", (List)Arrays.asList(new Filter3()));
    }

    public void testMatch() {
        List<Filter> list = filterChain.match("/foo/bar/**");
        assertNotNull(list);

        assertEquals(1, list.size());
        assertTrue(list.get(0) instanceof Filter1);

        list = filterChain.match("/baz");
        assertNotNull(list);

        assertEquals(1, list.size());
        assertTrue(list.get(0) instanceof Filter3);

        list = filterChain.match("/baz", false);
        assertNull(list);
    }

    class DummyFilter implements Filter {
        @Override
        public void init(FilterConfig filterConfig) throws ServletException {
        }
        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {
        }
        @Override
        public void destroy() {
        }
    }
    class Filter1 extends DummyFilter {
    }
    class Filter2 extends DummyFilter {
    }
    class Filter3 extends DummyFilter {
    }
}
