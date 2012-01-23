/* Copyright (c) 2001 - 2012 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.platform.exception;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geoserver.platform.GeoServerExtensions;

public class GeoServerExceptions {

    static Logger LOGGER = Logger.getLogger("org.geoserver.platform.exception");

    static Control control = new Control();

    /**
     * Returns a localized message for the specific exception for the default system locale.
     * 
     * @see #localize(GeoServerException, Locale)
     */
    public static String localize(GeoServerException e) {
        return localize(e, Locale.getDefault());
    }

    /**
     * Returns a localized message for the specific exception, given the specified
     * locale.
     * <p>
     * This method processes the {@link ResourceBundleLoader} extension point to find the 
     * appropriate {@link ResourceBundle} containing the localized message. The base name used
     * to look up the message is the name of the exception class. First the fully qualified
     * exception name is used, and if no bundle found, the non qualified name is used. 
     * </p>
     * @param e The exception whose message to localize.
     * @param locale The locale to use.
     * 
     * @return The localized message, or <code>null</code> if none could be found.
     */
    public static String localize(GeoServerException e, Locale locale) {
        Class<? extends GeoServerException> clazz = e.getClass();
        while(clazz != null) {
            String localized = doLocalize(e.getId(), e.getArgs(), clazz, locale);
            if (localized != null) {
                return localized;
            }

            //could not find string, if the exception parent class is also a geoserver exception
            // move up the hierarchy and try that
            
            if (GeoServerException.class.isAssignableFrom(clazz.getSuperclass()) ) {
                clazz = (Class<? extends GeoServerException>) clazz.getSuperclass();
            }
            else {
                clazz = null;
            }
        }
        return null;
    }
    
    static String doLocalize(String id, Object[] args, Class<? extends GeoServerException> clazz, 
            Locale locale) {
        ResourceBundle bundle = null;
        try {
            bundle = ResourceBundle.getBundle(clazz.getCanonicalName(), locale, control);
        }
        catch(MissingResourceException ex) {}
        
        if (bundle == null) {
            //look up by non qualified class name
            try {
                bundle = ResourceBundle.getBundle(clazz.getSimpleName(), locale, control);
            }
            catch(MissingResourceException ex) {}
        }

        if (bundle == null) {
            //could not locate a bundle
            return null;
        }
        
        //get the message
        String localized = null;
        try {
            localized = bundle.getString(id);
        }
        catch(MissingResourceException ex) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "Resource lookup failed for key" + id + " in bundle "+
                    bundle, ex);
            }
        }
        if (localized == null) {
            return null;
        }

        //if there are arguments, format the message accordingly
        if (args != null && args.length > 0) {
            localized = MessageFormat.format(localized, args);
        }
        
        return localized;
    }

    static class Control extends ResourceBundle.Control {
        static final List<String> FORMATS = Arrays.asList("java.properties");

        @Override
        public List<String> getFormats(String baseName) {
            if (baseName == null) {
                throw new NullPointerException();
            }
            return FORMATS;
        }

        @Override
        public ResourceBundle newBundle(String baseName, Locale locale,
                String format, ClassLoader loader, boolean reload)
                throws IllegalAccessException, InstantiationException,
                IOException {

            for (ResourceBundleLoader rbl : 
                GeoServerExtensions.extensions(ResourceBundleLoader.class)) {
                try {
                    ResourceBundle rb = rbl.load(baseName, locale, loader);
                    if (rb != null) {
                        return rb;
                    }
                }
                catch(Throwable t) {
                    LOGGER.log(Level.WARNING, "Error loading bundle", t);
                }
            }
            return null;
        }
    }
}
