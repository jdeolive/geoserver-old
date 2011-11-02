/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.security;

import java.util.LinkedHashMap;
import java.util.List;

import javax.servlet.Filter;

import org.springframework.util.AntPathMatcher;

/**
 * The security filter chain.
 * <p>
 * This class extends the basic map to allow for insertion/removal of filters into various parts
 * of the chain.
 * </p> 
 * @author Justin Deoliveira, OpenGeo
 */
public class GeoServerSecurityFilterChain extends LinkedHashMap<String, List<Filter>> {

    AntPathMatcher matcher = new AntPathMatcher();

    public List<Filter> match(String path) {
        return match(path, true);
    }

    public List<Filter> match(String path, boolean includeCatchAll) {
        for (String pattern : keySet()) {
            if (matcher.match(pattern, path)) {
                if ("/**".equals(pattern) && !includeCatchAll) {
                    continue;
                }
                return get(pattern);
            }
        }
        return null;
    }

    public void insert(String path, Filter filter, Class after, Class before) {
        insert(path, filter, after, before, true);
    }

    public void insert(String path, Filter filter, Class after, Class before, boolean includeCatchAll) {
        List<Filter> filters = match(path, includeCatchAll);
        if (filters == null) {
            throw new IllegalArgumentException("No pattern match"); 
        }

        for (int i = 0; i < filters.size(); i++) {
            Filter f = filters.get(i);
            
        }
    }
}
