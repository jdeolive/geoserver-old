/* Copyright (c) 2001 - 2012 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.platform.exception;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * A resource bundle loader implementation that looks up a property file relative to a specific 
 * class.
 * <p>
 * The property file naming convention used is <pre>&lt;baseName>[_&lt;language>].properties</pre>.
 * Examples:
 * <ul>
 *  <li>baseName = "foo", locale = {@link Locale#ENGLISH}, filename = "foo_en.properties"</li>
 *  <li>baseName = "foo", locale = {@link Locale#FRENCH}, filename = "foo_fr.properties"</li>
 *  <li>baseName = "foo", locale = null, filename = "foo.properties"</li>
 * </ul>
 * When a properties file is found, a {@link PropertyResourceBundle} instance is returned.
 * </p>
 * @author Justin Deoliveira, OpenGeo
 *
 */
public class ClassResourceBundleLoader implements ResourceBundleLoader {

    Class clazz;

    public ClassResourceBundleLoader(Class clazz) {
        this.clazz = clazz;
    }

    @Override
    public ResourceBundle load(String baseName, Locale locale,
            ClassLoader classLoader) throws IOException {

        //look for properties file
        String lang = locale.getLanguage();
        String filename = baseName;
        if (lang != null && !"".equals(lang)) {
            filename += "_" + lang;
        }
        filename += ".properties";

        InputStream stream = clazz.getResourceAsStream(filename);
        return stream != null ? new PropertyResourceBundle(stream) : null;
    }

}
