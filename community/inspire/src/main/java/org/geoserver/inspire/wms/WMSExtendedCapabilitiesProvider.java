package org.geoserver.inspire.wms;

import java.io.IOException;

import org.geoserver.wms.ExtendedCapabilitiesProvider;
import org.geoserver.wms.WMSInfo;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.NamespaceSupport;

import static org.geoserver.inspire.wms.InspireMetadata.*;

public class WMSExtendedCapabilitiesProvider implements ExtendedCapabilitiesProvider {

    public static final String NAMESPACE =  "http://inspire.europa.eu/networkservice/view/1.0";
    
    public String[] getSchemaLocations() {
        return new String[]{NAMESPACE, "www/inspire/inspire_vs.xsd"};
    }

    public void registerNamespaces(NamespaceSupport namespaces) {
        namespaces.declarePrefix("inspire_vs", NAMESPACE);
        namespaces.declarePrefix("gmd", "http://schemas.opengis.net/iso/19139/20060504/gmd/gmd.xsd");
        namespaces.declarePrefix("gco", "http://schemas.opengis.net/iso/19139/20060504/gco/gco.xsd");
        namespaces.declarePrefix("srv", "http://schemas.opengis.net/iso/19139/20060504/srv/srv.xsd");
    }

    public void encode(Translator tx, WMSInfo wms) throws IOException {
        tx.start("inspire_vs:ExtendedCapabilities");
    
        /*tx.start("inspire_vs:ResourceLocator");
        tx.start("gmd:linkage");
        tx.start("gmd:URL");
        tx.chars("http://inspire.europa.eu/info</gmd:URL");
        tx.end("gmd:URL");
        tx.end("gmd:linkage");
        tx.end("inspire_vs:ResourceLocator");*/
        
        String metadataURL = (String) wms.getMetadata().get(METADATA_URL.key);
        String language = (String) wms.getMetadata().get(LANGUAGE.key);
        
        if (metadataURL != null) {
            tx.start("inspire_vs:MetadataUrl");
            tx.start("gmd:linkage");
            tx.start("gmd:URL");
            tx.chars(metadataURL);
            tx.end("gmd:URL");
            tx.end("gmd:linkage");
            tx.end("inspire_vs:MetadataUrl");
        }
        
        language = language != null ? language : "eng";
        
        AttributesImpl atts = new AttributesImpl();
        atts.addAttribute(NAMESPACE, "default", "inspire_vs:default", null, "true");
        tx.start("inspire_vs:Language", atts);
        tx.chars(language);
        tx.end("inspire_vs:Language");
        
        tx.end("inspire_vs:ExtendedCapabilities");
    }

}
