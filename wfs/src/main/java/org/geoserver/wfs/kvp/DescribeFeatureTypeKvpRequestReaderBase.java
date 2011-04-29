/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.wfs.kvp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.eclipse.emf.ecore.EFactory;
import org.geoserver.catalog.Catalog;
import org.geoserver.wfs.RequestObjectHandler;
import org.xml.sax.helpers.NamespaceSupport;


public class DescribeFeatureTypeKvpRequestReaderBase extends WFSKvpRequestReader {

    private final Catalog catalog;
    private final RequestObjectHandler handler;
    
    public DescribeFeatureTypeKvpRequestReaderBase(final Catalog catalog, Class requestBean, 
        EFactory factory, RequestObjectHandler handler) {
        
        super(requestBean, factory);
        this.catalog = catalog;
        this.handler = handler;
    }

    @SuppressWarnings("unchecked")
    public Object read(Object request, Map kvp, Map rawKvp) throws Exception {
        //let super do its thing
        request = super.read(request, kvp, rawKvp);

        //do an additional check for outputFormat, because the default 
        // in wfs 1.1 is not the default for wfs 1.0

        if (!handler.isSetOutputFormat(request)) {
            if (handler.getVersion(request).startsWith("2.0")) {
                //set 2.0 default
                handler.setOutputFormat(request, "text/xml; subtype=gml/3.2");
            }
            else if (handler.getVersion(request).startsWith("1.1")) {
                //set 1.1 default
                handler.setOutputFormat(request, "text/xml; subtype=gml/3.1.1");
            } else {
                //set 1.0 default
                handler.setOutputFormat(request, "XMLSCHEMA");
            }
        }

        // did the user supply alternate namespace prefixes?
        NamespaceSupport namespaces = null;
        if (kvp.containsKey("NAMESPACE")) {
            if (kvp.get("NAMESPACE") instanceof NamespaceSupport) {
                namespaces = (NamespaceSupport) kvp.get("namespace");
            } else {
                LOGGER.warning("There's a namespace parameter but it seems it wasn't parsed to a "
                        + NamespaceSupport.class.getName() + ": " + kvp.get("namespace"));
            }
        }
        if (namespaces != null) {
            List<QName> typeNames = handler.getTypeNames(request);
            List<QName> newList = new ArrayList<QName>(typeNames.size());
            for(QName name : typeNames){
                String localPart = name.getLocalPart();
                String prefix = name.getPrefix();
                String namespaceURI = name.getNamespaceURI();
                if (XMLConstants.DEFAULT_NS_PREFIX.equals(prefix)) {
                    //no prefix specified, did the request specify a default namespace?
                    namespaceURI = namespaces.getURI(XMLConstants.DEFAULT_NS_PREFIX);
                } else if (XMLConstants.NULL_NS_URI.equals(namespaceURI)) {
                    //prefix specified, does a namespace mapping were declared for it?
                    if(namespaces.getURI(prefix) != null){
                        namespaceURI = namespaces.getURI(prefix);
                    }
                }
                if(catalog.getNamespaceByURI(namespaceURI) != null){
                    prefix = catalog.getNamespaceByURI(namespaceURI).getPrefix();
                }
                newList.add(new QName(namespaceURI, localPart, prefix));
            }
            handler.setTypeNames(request, newList);
        }
        return request;
    }
}
