/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.wfs;

import net.opengis.wfs.FeatureCollectionType;
import net.opengis.wfs20.DescribeFeatureTypeType;
import net.opengis.wfs20.GetCapabilitiesType;
import net.opengis.wfs20.GetFeatureType;
import net.opengis.wfs20.GetFeatureWithLockType;
import net.opengis.wfs20.TransactionResponseType;
import net.opengis.wfs20.TransactionType;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.config.GeoServer;
import org.geotools.xml.transform.TransformerBase;
import org.opengis.filter.FilterFactory2;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class DefaultWebFeatureService20 implements WebFeatureService20,  ApplicationContextAware {

    /**
     * GeoServer configuration
     */
    protected GeoServer geoServer;

    /** filter factory */
    protected FilterFactory2 filterFactory;

    /**
     * The spring application context, used to look up transaction listeners, plugins and
     * element handlers
     */
    protected ApplicationContext context;

    public DefaultWebFeatureService20(GeoServer geoServer) {
        this.geoServer = geoServer;
    }

    public void setFilterFactory(FilterFactory2 filterFactory) {
        this.filterFactory = filterFactory;
    }
    
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
    
    public WFSInfo getServiceInfo() {
        return geoServer.getService(WFSInfo.class);
    }
    
    public Catalog getCatalog() {
        return geoServer.getCatalog();
    }

    public TransformerBase getCapabilities(GetCapabilitiesType request) throws WFSException {
        return new GetCapabilities(getServiceInfo(), getCatalog(), handler()).run(request);
    }
    
    public FeatureTypeInfo[] describeFeatureType(DescribeFeatureTypeType request)
            throws WFSException {
        return new DescribeFeatureType(getServiceInfo(), getCatalog(), handler()).run(request);
    }

    public FeatureCollectionType getFeature(GetFeatureType request) throws WFSException {
        GetFeature gf = new GetFeature(getServiceInfo(), getCatalog(), handler());
        gf.setFilterFactory(filterFactory);
        
        return gf.run(request);
    }
    
    public FeatureCollectionType getFeatureWithLock(GetFeatureWithLockType request)
            throws WFSException {
        return getFeature(request);
    }
    
    public TransactionResponseType transaction(TransactionType request) throws WFSException {
        Transaction tx = new Transaction(getServiceInfo(), getCatalog(), context, handler());
        tx.setFilterFactory(filterFactory);
        return (TransactionResponseType) tx.transaction(request);
    }
    
    //the following operations are not part of the spec
    public void releaseLock(String lockId) throws WFSException {
        new LockFeature(getServiceInfo(), getCatalog()).release(lockId);
    }

    RequestObjectHandler handler() {
        return new RequestObjectHandler.WFS_20();
    }

}
