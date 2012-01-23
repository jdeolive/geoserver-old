/* Copyright (c) 2001 - 2012 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.platform.exception;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Exception point for providing a {@link ResourceBundle} instance.
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public interface ResourceBundleLoader {

    /**
     * Looks up a {@link ResourceBundle} given a base name and a locale.
     * 
     * @param baseName The base name of the resource bundle.
     * @param locale The locale the returned resource bundle is specific to. 
     * 
     * @return The resource bundle, or <code>null</code> if none could be found.
     */
    ResourceBundle load(String baseName, Locale locale, ClassLoader classLoader)
        throws IOException;
}
