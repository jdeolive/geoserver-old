/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.security;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

import org.geoserver.security.config.SecurityManagerConfig;
import org.geoserver.security.impl.AbstractGeoServerSecurityService;

/**
 * Extension of {@link Filter} for the geoserver security subsystem.
 * <p>
 * Instances of this class are provided by {@link GeoServerSecurityProvider}, or may also be 
 * contribute via a spring context. Filters are configured via name through 
 * {@link SecurityManagerConfig#getFilterChain()}.The referenced name will be matched to a named 
 * security configuration through {@link GeoServerSecurityManager#loadFilter(String)} or matched
 * to a bean name in the application context.
 * </p>
 * 
 * @author Justin Deoliveira, OpenGeo
 * 
 */
public abstract class GeoServerSecurityFilter extends AbstractGeoServerSecurityService 
    implements Filter {

    /**
     * Not used, these filters are not plugged in via web.xml
     */
    @Override
    public final void init(FilterConfig filterConfig) throws ServletException {
    }

    /**
     * Does nothing, subclasses may override.
     */
    @Override
    public void destroy() {
    }

}
