/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.wfs;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import net.opengis.wfs.AllSomeType;
import net.opengis.wfs.FeatureCollectionType;
import net.opengis.wfs.GetFeatureType;
import net.opengis.wfs.GetFeatureWithLockType;
import net.opengis.wfs.LockFeatureResponseType;
import net.opengis.wfs.LockFeatureType;
import net.opengis.wfs.LockType;
import net.opengis.wfs.QueryType;
import net.opengis.wfs.WfsFactory;
import net.opengis.wfs.XlinkPropertyNameType;

import org.geoserver.catalog.AttributeTypeInfo;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.catalog.NamespaceInfo;
import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.factory.Hints;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.NameImpl;
import org.geotools.feature.SchemaException;
import org.geotools.filter.expression.AbstractExpressionVisitor;
import org.geotools.filter.visitor.AbstractFilterVisitor;
import org.geotools.filter.visitor.SimplifyingFilterVisitor;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.LiteCoordinateSequenceFactory;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.xml.EMFUtils;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.ExpressionVisitor;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.sort.SortBy;
import org.opengis.filter.spatial.BBOX;
import org.opengis.filter.spatial.BinarySpatialOperator;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.xml.sax.helpers.NamespaceSupport;

/**
 * Web Feature Service GetFeature operation.
 * <p>
 * This operation returns an array of {@link org.geotools.feature.FeatureCollection}
 * instances.
 * </p>
 *
 * @author Rob Hranac, TOPP
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 * @version $Id$
 */
public class GetFeature {
    public static final String SQL_VIEW_PARAMS = "GS_SQL_VIEW_PARAMS";
    
    /** Standard logging instance for class */
    private static final Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.vfny.geoserver.requests");

    /** The catalog */
    protected Catalog catalog;

    /** The wfs configuration */
    protected WFSInfo wfs;

    /** filter factory */
    protected FilterFactory2 filterFactory;

    /**
     * Creates the GetFeature operation.
     *
     */
    public GetFeature(WFSInfo wfs, Catalog catalog) {
        this.wfs = wfs;
        this.catalog = catalog;
    }

    /**
     * @return The reference to the GeoServer catalog.
     */
    public Catalog getCatalog() {
        return catalog;
    }
    
    /**      
     * @return NamespaceSupport from Catalog
     */
    public NamespaceSupport getNamespaceSupport() {
        NamespaceSupport ns = new NamespaceSupport();
        Iterator<NamespaceInfo> it = getCatalog().getNamespaces().iterator();
        while (it.hasNext()) {
            NamespaceInfo ni = it.next();
            ns.declarePrefix(ni.getPrefix(), ni.getURI());
        }
        return ns;
    }

    /**
     * @return The reference to the WFS configuration.
     */
    public WFSInfo getWFS() {
        return wfs;
    }

    /**
     * Sets the filter factory to use to create filters.
     *
     * @param filterFactory
     */
    public void setFilterFactory(FilterFactory2 filterFactory) {
        this.filterFactory = filterFactory;
    }

    public FeatureCollectionType run(GetFeatureType request)
        throws WFSException {
        List queries = request.getQuery();

        if (queries.isEmpty()) {
            throw new WFSException("No query specified");
        }

        if (EMFUtils.isUnset(queries, "typeName")) {
            String msg = "No feature types specified";
            throw new WFSException(msg);
        }

        // Optimization Idea
        //
        // We should be able to reduce this to a two pass opperations.
        //
        // Pass #1 execute
        // - Attempt to Locks Fids during the first pass
        // - Also collect Bounds information during the first pass
        //
        // Pass #2 writeTo
        // - Using the Bounds to describe our FeatureCollections
        // - Iterate through FeatureResults producing GML
        //
        // And allways remember to release locks if we are failing:
        // - if we fail to aquire all the locks we will need to fail and
        //   itterate through the the FeatureSources to release the locks
        //
        if (request.getMaxFeatures() == null) {
            request.setMaxFeatures(BigInteger.valueOf(Integer.MAX_VALUE));
        }

        // take into consideration the wfs max features
        int maxFeatures = Math.min(request.getMaxFeatures().intValue(),
                wfs.getMaxFeatures());
        
        // grab the view params is any
        List<Map<String, String>> viewParams = null;
        if(request.getMetadata() != null) {
            viewParams = (List<Map<String, String>>) request.getMetadata().get(SQL_VIEW_PARAMS);
        }

        int count = 0; //should probably be long
        List results = new ArrayList();
        try {
            for (int i = 0; (i < request.getQuery().size()) && (count < maxFeatures); i++) {
                QueryType query = (QueryType) request.getQuery().get(i);

                FeatureTypeInfo meta = null;

                if (query.getTypeName().size() == 1) {
                    meta = featureTypeInfo((QName) query.getTypeName().get(0));
                } else {
                    //TODO: a join is taking place
                }

                FeatureSource<? extends FeatureType, ? extends Feature> source = meta.getFeatureSource(null,null);
              
                //make sure property names are cool
                NamespaceSupport ns = getNamespaceSupport();
                
                List<PropertyName> propNames = null;
                List<PropertyName> allPropNames = null;
                                
                if (!query.getPropertyName().isEmpty()){
                    
                    propNames = new ArrayList<PropertyName>();
                    
                    for (Iterator iter = query.getPropertyName().iterator(); iter.hasNext();) {
                        PropertyName propName = createPropertyName((String) iter.next(), ns);
    
                        //if (!attNames.contains(propName)) {
                        if ( propName.evaluate(meta.getFeatureType()) == null) {
                            String mesg = "Requested property: " + propName + " is " + "not available "
                                + "for " + query.getTypeName() + ".  ";
                            
                            if (meta.getFeatureType() instanceof SimpleFeatureType) {
                                List<AttributeTypeInfo> atts = meta.attributes();
                                List attNames = new ArrayList( atts.size() );
                                for ( AttributeTypeInfo att : atts ) {
                                    attNames.add( att.getName() );
                                }
                                mesg += "The possible propertyName values are: " + attNames;
                            }
    
                            throw new WFSException(mesg);
                        }
                        
                        propNames.add(propName);
                    }
                    
                    // if we need to force feature bounds computation, we have to load 
                    // all of the geometries, but we'll have to remove them in the 
                    // returned feature type
                    if(wfs.isFeatureBounding()) {
                        allPropNames = addGeometryProperties(meta, propNames);                        
                    } else {
                        allPropNames = propNames;
                    }     
                    
                    //we must also include any properties that are mandatory ( even if not requested ),
                    // ie. those with minOccurs > 0
                    //only do this for simple features, complex mandatory features are handled by app-schema
                    if (meta.getFeatureType() instanceof SimpleFeatureType) {
                        allPropNames = DataUtilities.addMandatoryProperties((SimpleFeatureType) meta.getFeatureType(), allPropNames);
                        propNames = DataUtilities.addMandatoryProperties((SimpleFeatureType) meta.getFeatureType(), propNames);
                    }
                    //for complex features, mandatory properties need to be handled by datastore.
                    
                }                

                //make sure filters are sane
                //
                // Validation of filters on non-simple feature types is not yet supported.
                // FIXME: Support validation of filters on non-simple feature types:
                // need to consider xpath properties and how to configure namespace prefixes in
                // GeoTools app-schema FeaturePropertyAccessorFactory.
                if (query.getFilter() != null && source.getSchema() instanceof SimpleFeatureType) {
                    
                    //1. ensure any property name refers to a property that 
                    // actually exists
                    final FeatureType featureType = source.getSchema();
                    ExpressionVisitor visitor = new AbstractExpressionVisitor() {
                            public Object visit(PropertyName name, Object data) {
                                // case of multiple geometries being returned
                                if (name.evaluate(featureType) == null) {
                                    throw new WFSException("Illegal property name: "
                                        + name.getPropertyName(), "InvalidParameterValue");
                                }

                                return name;
                            }
                            ;
                        };
                    query.getFilter().accept(new AbstractFilterVisitor(visitor), null);
                    
                    //2. ensure any spatial predicate is made against a property 
                    // that is actually special
                    AbstractFilterVisitor fvisitor = new AbstractFilterVisitor() {
                      
                        protected Object visit( BinarySpatialOperator filter, Object data ) {
                            PropertyName name = null;
                            if ( filter.getExpression1() instanceof PropertyName ) {
                                name = (PropertyName) filter.getExpression1();
                            }
                            else if ( filter.getExpression2() instanceof PropertyName ) {
                                name = (PropertyName) filter.getExpression2();
                            }
                            
                            if ( name != null ) {
                                //check against fetaure type to make sure its
                                // a geometric type
                                AttributeDescriptor att = (AttributeDescriptor) name.evaluate(featureType);
                                if ( !( att instanceof GeometryDescriptor ) ) {
                                    throw new WFSException("Property " + name + " is not geometric", "InvalidParameterValue");
                                }
                            }
                            
                            return filter;
                        }
                    };
                    query.getFilter().accept(fvisitor, null);
                    
                    //3. ensure that any bounds specified as part of the query
                    // are valid with respect to the srs defined on the query
                    if ( wfs.isCiteCompliant() ) {
                        
                        if ( query.getSrsName() != null ) {
                            final QueryType fquery = query;
                            fvisitor = new AbstractFilterVisitor() {
                                public Object visit(BBOX filter, Object data) {
                                    if ( filter.getSRS() != null && 
                                            !fquery.getSrsName().toString().equals( filter.getSRS() ) ) {
                                        
                                        //back project bounding box into geographic coordinates
                                        CoordinateReferenceSystem geo = DefaultGeographicCRS.WGS84;
                                        
                                        GeneralEnvelope e = new GeneralEnvelope( 
                                            new double[] { filter.getMinX(), filter.getMinY()},
                                            new double[] { filter.getMaxX(), filter.getMaxY()}
                                        );
                                        CoordinateReferenceSystem crs = null;
                                        try {
                                            crs = CRS.decode( filter.getSRS() );
                                            e = CRS.transform(CRS.findMathTransform(crs, geo, true), e);
                                        } 
                                        catch( Exception ex ) {
                                            throw new WFSException( ex );
                                        }
                                        
                                        //ensure within bounds defined by srs specified on 
                                        // query
                                        try {
                                            crs = CRS.decode( fquery.getSrsName().toString() );
                                        } 
                                        catch( Exception ex ) {
                                            throw new WFSException( ex );
                                        }
                                        
                                        GeographicBoundingBox valid = 
                                            (GeographicBoundingBox) crs.getDomainOfValidity()
                                            .getGeographicElements().iterator().next();
                                        
                                        if ( e.getMinimum(0) < valid.getWestBoundLongitude() || 
                                            e.getMinimum(0) > valid.getEastBoundLongitude() || 
                                            e.getMaximum(0) < valid.getWestBoundLongitude() || 
                                            e.getMaximum(0) > valid.getEastBoundLongitude() ||
                                            e.getMinimum(1) < valid.getSouthBoundLatitude() || 
                                            e.getMinimum(1) > valid.getNorthBoundLatitude() || 
                                            e.getMaximum(1) < valid.getSouthBoundLatitude() || 
                                            e.getMaximum(1) > valid.getNorthBoundLatitude() ) {
                                                
                                            throw new WFSException( "bounding box out of valid range of crs", "InvalidParameterValue");
                                        }
                                    }
                                    
                                    return data;
                                } 
                            };
                            
                            query.getFilter().accept(fvisitor, null);
                        }
                    }   
                }

                // handle local maximum
                int queryMaxFeatures = maxFeatures - count;
                if(meta.getMaxFeatures() > 0 && meta.getMaxFeatures() < queryMaxFeatures)
                    queryMaxFeatures = meta.getMaxFeatures();
                Map<String, String> viewParam = viewParams != null ? viewParams.get(i) : null;
                org.geotools.data.Query gtQuery = toDataQuery(query, queryMaxFeatures, source, request, allPropNames, viewParam);
                
                LOGGER.fine("Query is " + query + "\n To gt2: " + gtQuery);

                FeatureCollection<? extends FeatureType, ? extends Feature> features = getFeatures(request, source, gtQuery);
                // For complex features, we need the targetCrs and version in scenario where we have
                // a top level feature that does not contain a geometry(therefore no crs) and has a
                // nested feature that contains geometry as its property.Furthermore it is possible
                // for each nested feature to have different crs hence we need to reproject on each
                // feature accordingly.
                if (!(meta.getFeatureType() instanceof SimpleFeatureType)) {
                    features.getSchema().getUserData().put("targetCrs", query.getSrsName());
                    features.getSchema().getUserData().put("targetVersion", request.getVersion());
                }
                // optimization: WFS 1.0 does not require count unless we have multiple query elements
                // and we are asked to perform a global limit on the results returned
                if(("1.0".equals(request.getVersion()) || "1.0.0".equals(request.getVersion())) && 
                        (request.getQuery().size() == 1 || maxFeatures == Integer.MAX_VALUE)) {
                    // skip the count update, in this case we don't need it
                } else {
                	count += features.size();
                }
                
                // we may need to shave off geometries we did load only to make bounds
                // computation happy
                // TODO: support non-SimpleFeature geometry shaving
                if(features.getSchema() instanceof SimpleFeatureType && propNames!=null && propNames.size() < allPropNames.size()) {
                    String[] residualNames = new String[propNames.size()];
                    Iterator<PropertyName> it = propNames.iterator();
                    int j =0;
                    while (it.hasNext()) {
                        residualNames[j] = it.next().getPropertyName();
                        j++;
                    }
                    SimpleFeatureType targetType = DataUtilities.createSubType((SimpleFeatureType) features.getSchema(), residualNames);
                    features = new FeatureBoundsFeatureCollection((SimpleFeatureCollection) features, targetType);
                }

                //JD: TODO reoptimize
                //                if ( i == request.getQuery().size() - 1 ) { 
                //                	//DJB: dont calculate feature count if you dont have to. The MaxFeatureReader will take care of the last iteration
                //                	maxFeatures -= features.getCount();
                //                }

                //GR: I don't know if the featuresults should be added here for later
                //encoding if it was a lock request. may be after ensuring the lock
                //succeed?
                results.add(features);
            }
        } catch (IOException e) {
            throw new WFSException("Error occurred getting features", e, request.getHandle());
        } catch (SchemaException e) {
            throw new WFSException("Error occurred getting features", e, request.getHandle());
        }

        //locking
        String lockId = null;
        if (request instanceof GetFeatureWithLockType) {
            GetFeatureWithLockType withLockRequest = (GetFeatureWithLockType) request;

            LockFeatureType lockRequest = WfsFactory.eINSTANCE.createLockFeatureType();
            lockRequest.setExpiry(withLockRequest.getExpiry());
            lockRequest.setHandle(withLockRequest.getHandle());
            lockRequest.setLockAction(AllSomeType.ALL_LITERAL);

            for (int i = 0; i < request.getQuery().size(); i++) {
                QueryType query = (QueryType) request.getQuery().get(i);

                LockType lock = WfsFactory.eINSTANCE.createLockType();
                lock.setFilter(query.getFilter());
                lock.setHandle(query.getHandle());

                //TODO: joins?
                lock.setTypeName((QName) query.getTypeName().get(0));
                lockRequest.getLock().add(lock);
            }

            LockFeature lockFeature = new LockFeature(wfs, catalog);
            lockFeature.setFilterFactory(filterFactory);

            LockFeatureResponseType response = lockFeature.lockFeature(lockRequest);
            lockId = response.getLockId();
        }

        return buildResults(count, results, lockId);
    }

    /**
     * Allows subclasses to alter the result generation
     * @param count
     * @param results
     * @param lockId
     * @return
     */
    protected FeatureCollectionType buildResults(int count, List results,
            String lockId) {
        FeatureCollectionType result = WfsFactory.eINSTANCE.createFeatureCollectionType();
        result.setNumberOfFeatures(BigInteger.valueOf(count));
        result.setTimeStamp(Calendar.getInstance());
        result.setLockId(lockId);
        result.getFeature().addAll(results);
        return result;
    }

    /**
     * Allows subclasses to poke with the feature collection extraction
     * @param source
     * @param gtQuery
     * @return
     * @throws IOException
     */
    protected FeatureCollection<? extends FeatureType, ? extends Feature> getFeatures(
            GetFeatureType request, FeatureSource<? extends FeatureType, ? extends Feature> source,
            org.geotools.data.Query gtQuery)
            throws IOException {
        return source.getFeatures(gtQuery);
    }

    /**
     * Get this query as a geotools Query.
     *
     * <p>
     * if maxFeatures is a not positive value Query.DEFAULT_MAX will be
     * used.
     * </p>
     *
     * <p>
     * The method name is changed to toDataQuery since this is a one way
     * conversion.
     * </p>
     *
     * @param maxFeatures number of features, or 0 for Query.DEFAULT_MAX
     *
     * @return A Query for use with the FeatureSource interface
     *
     */
    public org.geotools.data.Query toDataQuery(QueryType query, int maxFeatures,
        FeatureSource<? extends FeatureType, ? extends Feature> source, GetFeatureType request, 
        List<PropertyName> props, Map<String, String> viewParams) throws WFSException {
        
        String wfsVersion = request.getVersion();
        
        if (maxFeatures <= 0) {
            maxFeatures = Query.DEFAULT_MAX;
        }

        Filter filter = (Filter) query.getFilter();

        if (filter == null) {
            filter = Filter.INCLUDE;
        } else {
            // Gentlemen, we can rebuild it. We have the technology!
            SimplifyingFilterVisitor visitor = new SimplifyingFilterVisitor();
            filter = (Filter) filter.accept(visitor, null);
        }
        
        //figure out the crs the data is in
        CoordinateReferenceSystem crs = source.getSchema().getCoordinateReferenceSystem();
            
        // gather declared CRS
        CoordinateReferenceSystem declaredCRS = WFSReprojectionUtil.getDeclaredCrs(crs, wfsVersion);
        
        // make sure every bbox and geometry that does not have an attached crs will use
        // the declared crs, and then reproject it to the native crs
        Filter transformedFilter = filter;
        if(declaredCRS != null)
            transformedFilter = WFSReprojectionUtil.normalizeFilterCRS(filter, source.getSchema(), declaredCRS);

        //only handle non-joins for now
        QName typeName = (QName) query.getTypeName().get(0);
        Query dataQuery = new Query(typeName.getLocalPart(), transformedFilter, maxFeatures,
                props, query.getHandle());
        
        //handle reprojection
        CoordinateReferenceSystem target;
        if (query.getSrsName() != null) {
            try {
                target = CRS.decode(query.getSrsName().toString());
            } catch (Exception e) {
                String msg = "Unable to support srsName: " + query.getSrsName();
                throw new WFSException(msg, e);
            }
        } else {
            target = declaredCRS;
        }
        //if the crs are not equal, then reproject
        if (target != null && declaredCRS != null && !CRS.equalsIgnoreMetadata(crs, target)) {
            dataQuery.setCoordinateSystemReproject(target);
        }
        
        //handle sorting
        if (query.getSortBy() != null) {
            List sortBy = query.getSortBy();
            dataQuery.setSortBy((SortBy[]) sortBy.toArray(new SortBy[sortBy.size()]));
        }

        //handle version, datastore may be able to use it
        if (query.getFeatureVersion() != null) {
            dataQuery.setVersion(query.getFeatureVersion());
        }

        //create the Hints to set at the end
        final Hints hints = new Hints();
                
        //handle xlink traversal depth
        if (request.getTraverseXlinkDepth() != null) {
            //TODO: make this an integer in the model, and have hte NumericKvpParser
            // handle '*' as max value
            Integer traverseXlinkDepth = traverseXlinkDepth( request.getTraverseXlinkDepth() );
            
            //set the depth as a hint on the query
            hints.put(Hints.ASSOCIATION_TRAVERSAL_DEPTH, traverseXlinkDepth);
        }
        
        //handle xlink properties
        if (!query.getXlinkPropertyName().isEmpty() ) {
            for ( Iterator x = query.getXlinkPropertyName().iterator(); x.hasNext(); ) {
                XlinkPropertyNameType xlinkProperty = (XlinkPropertyNameType) x.next();
                
                Integer traverseXlinkDepth = traverseXlinkDepth( xlinkProperty.getTraverseXlinkDepth() );
                
                //set the depth and property as hints on the query
                hints.put(Hints.ASSOCIATION_TRAVERSAL_DEPTH, traverseXlinkDepth );
                
                PropertyName xlinkPropertyName = filterFactory.property( xlinkProperty.getValue() );
                hints.put(Hints.ASSOCIATION_PROPERTY, xlinkPropertyName );
                
                dataQuery.setHints( hints );
                
                //TODO: support multiple properties
                break;
            }
        }
        
        //tell the datastore to use a lite coordinate sequence factory, if possible
        hints.put(Hints.JTS_COORDINATE_SEQUENCE_FACTORY, new LiteCoordinateSequenceFactory());
        
        // check for sql view parameters
        if(viewParams != null) {
            hints.put(Hints.VIRTUAL_TABLE_PARAMETERS, viewParams);
        }
        
        //currently only used by app-schema, produce mandatory properties
        hints.put(Query.INCLUDE_MANDATORY_PROPS, true);

        //finally, set the hints
        dataQuery.setHints(hints);
               
        return dataQuery;
    }

    static Integer traverseXlinkDepth( String raw ) {
        Integer traverseXlinkDepth = null;
        try {
            traverseXlinkDepth = new Integer( raw );
        }
        catch( NumberFormatException nfe ) {
            //try handling *
            if ( "*".equals( raw ) ) {
                //TODO: JD: not sure what this value should be? i think it 
                // might be reported in teh acapabilitis document, using 
                // INteger.MAX_VALUE will result in stack overflow... for now
                // we just use 10
                traverseXlinkDepth = new Integer( 2 );
            }
            else {
                //not wildcard case, throw original exception
                throw nfe;
            }
        }
        
        return traverseXlinkDepth;
    }
    
    FeatureTypeInfo featureTypeInfo(QName name) throws WFSException, IOException {
        FeatureTypeInfo meta = catalog.getFeatureTypeByName(name.getNamespaceURI(), name.getLocalPart());

        if (meta == null) {
            String msg = "Could not locate " + name + " in catalog.";
            throw new WFSException(msg);
        }

        return meta;
    }
    
    
    protected PropertyName createPropertyName (String path, NamespaceSupport namespaceContext) {
        if (path.contains("/")) {
            return filterFactory.property(path, namespaceContext);
        } else {
            if (path.contains(":")) {
                int i = path.indexOf(":");
                return filterFactory.property(new NameImpl(namespaceContext.getURI(path.substring(0, i)), path.substring(i+1) ));
            } else {
                return filterFactory.property(path);
            }
        }
        
    }
     
    protected List<PropertyName> addGeometryProperties (FeatureTypeInfo meta, List<PropertyName> oldProperties) throws IOException {
        List<AttributeTypeInfo> atts = meta.attributes();
        Iterator ii = atts.iterator();
        
        List<PropertyName> properties = new ArrayList<PropertyName>(oldProperties);

        while (ii.hasNext()) {
            AttributeTypeInfo ati = (AttributeTypeInfo) ii.next();
            PropertyName propName = filterFactory.property (ati.getName());
            
            if(meta.getFeatureType().getDescriptor(ati.getName()) instanceof GeometryDescriptor
                    && !properties.contains(propName) ) {
                properties.add(propName);
            }
        }
        
        return properties;
    }
}
