package org.geoserver.platform.exception;

import java.util.Locale;

import org.geoserver.platform.GeoServerExtensions;
import org.springframework.context.support.GenericApplicationContext;

import junit.framework.TestCase;

public class ClassResourceBundleLoaderTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        GenericApplicationContext ctx = new GenericApplicationContext();
        ctx.getBeanFactory().registerSingleton("rbl", new ClassResourceBundleLoader(Marker.class));
        new GeoServerExtensions().setApplicationContext(ctx);
    }

    public void test() throws Exception {
        GeoServerException e = new TestException().id("hi");
        assertEquals("hello", GeoServerExceptions.localize(e));
    }

    public void testFallback() throws Exception {
        GeoServerException e = new TestException2().id("hi");
        //assertEquals("hello", GeoServerExceptions.localize(e));
        //assertEquals("hello", GeoServerExceptions.localize(e, Locale.ENGLISH));
        assertEquals("hello", GeoServerExceptions.localize(e, Locale.FRENCH));
    }

    public void testLocale() throws Exception {
        GeoServerException e = new TestException().id("hi");
        assertEquals("bonjour", GeoServerExceptions.localize(e, Locale.FRENCH));
    }

    public void testUnknownLocale() throws Exception {
        GeoServerException e = new TestException().id("hi");
        assertEquals("hello", GeoServerExceptions.localize(e, Locale.GERMAN));
    }

    public void testWithArgs() throws Exception {
        GeoServerException e = new TestException().id("hey").args("neo");
        assertEquals("hello neo", GeoServerExceptions.localize(e));
        assertEquals("bonjour neo", GeoServerExceptions.localize(e, Locale.FRENCH));
    }

    public void testWithNewDefault() throws Exception {
        Locale old = Locale.getDefault();
        Locale.setDefault(Locale.FRENCH);
        try {
            assertEquals("bonjour", GeoServerExceptions.localize(new TestException().id("hi")));
        }
        finally {
            Locale.setDefault(old);
        }
    }

    static class Marker {
    }
}
