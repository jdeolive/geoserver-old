/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.security;

/**
 * Entry in a {@link GeoServerSecurityFilterChain}.
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class FilterChainEntry {

    public static enum Position {
        FIRST, LAST, BEFORE, AFTER;
    }

    /** name of the filter */
    String filterName;

    /** position in chain */
    Position position;

    /** relative */
    String relativeTo;

    public FilterChainEntry(String filterName, Position position) {
        this(filterName, position, null);
    }

    public FilterChainEntry(String filterName, Position position, String relativeTo) {
        this.filterName = filterName;
        this.position = position;
        this.relativeTo = relativeTo;

        if (filterName == null) {
            throw new NullPointerException("filterName must not be null");
        }
        if (position == null) {
            throw new NullPointerException("position must not be null");
        }
        if (relativeTo == null && (position == Position.AFTER || position == Position.BEFORE)) {
            throw new IllegalArgumentException("relativeTo must be specified with position " + position);
        }
    }

    /**
     * The name of the filter.
     * <p>
     * This name corresponds to either the name of a bean in the spring context, or a named filter
     * configuration managed by the security manager.
     * </p>
     */
    public String getFilterName() {
        return filterName;
    }

    /**
     * The position of the filter in the chain.
     */
    public Position getPosition() {
        return position;
    }

    /**
     * The filter that this filter is relative to when position is one of {@link Position#BEFORE} or
     * {@link  Position#AFTER}.
     */
    public String getRelativeTo() {
        return relativeTo;
    }

    @Override
    public String toString() {
        return filterName;
    }
}
