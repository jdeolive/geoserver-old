package org.geoserver.wms;

import java.io.IOException;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.NamespaceSupport;

/**
 * Extension point that allows plugins to dynamically contribute extended properties
 * to the WMS capabilities document.
 *  
 * @author Justin Deoliveira, OpenGeo
 *
 */
public interface ExtendedCapabilitiesProvider {

    /**
     * Returns the locations of any references schema for the extended capabilities.
     * <p>
     * The returned String array must consist of namespace,location pairs in which the namespace
     * is the full namespace uri of the schema, and location is the url to where the schema defintion
     * is located.
     * </p>
     * <p>
     * The location may be specified as a canonical external url. For example 
     * <tt>http://schemas.opengis.net/foo/foo.xsd</tt>. Or if the schema is bundled within the 
     * server the location can be a relative path such as <tt>foo/foo.xsd</tt>. In the latter
     * case the path will be appended to the base url from which the capabilities document is being
     * requested from.
     * </p>
     */
    String[] getSchemaLocations();
    
    /**
     * Registers the xmlns namespace prefix:uri mappings for any elements used by 
     * the extended capabilities. 
     */
    void registerNamespaces(NamespaceSupport namespaces);
    
    /**
     * Encodes the extended capabilities.
     */
    void encode(Translator tx, WMSInfo wms) throws IOException;
    
    public interface Translator {
        
        void start(String element);
        
        void start(String element, Attributes attributes);
        
        void chars(String text);
        
        void end(String element);
    }
}
