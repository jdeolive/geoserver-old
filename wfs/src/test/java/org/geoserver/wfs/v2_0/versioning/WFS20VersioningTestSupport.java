package org.geoserver.wfs.v2_0.versioning;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.XpathEngine;
import org.geogit.api.RevCommit;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.data.test.MockData;
import org.geoserver.geogit.GEOGIT;
import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.wfs.v2_0.WFS20TestSupport;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.NameImpl;
import org.geotools.util.logging.Logging;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.PropertyName;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.WKTReader;

/**
 * 
 * Upon startup, the repository contains the following commits:
 * 
 * <ul>
 * <li>Insert of:
 * <code>Bridges.1107531599613[the_geom=POINT (0.0002 0.0007), FID="110",NAME="Cam Bridge"]</code>
 * <li>Insert of:
 * <ul>
 * <li>
 * <code>Buildings.1107531701010[the_geom=MULTIPOLYGON (((0.0008 0.0005, 0.0008 0.0007, 0.0012 0.0007, 0.0012 0.0005, 0.0008 0.0005))),FID="113", ADDRESS="123 Main Street"]</code>
 * </li>
 * <li>
 * <code>Buildings.1107531701011[the_geom=MULTIPOLYGON (((0.002 0.0008, 0.002 0.001, 0.0024 0.001, 0.0024 0.0008, 0.002 0.0008))), FID="114", ADDRESS="215 Main Street"]</code>
 * </li>
 * </ul>
 * </li>
 * <li>Commit Message: "Change Cam Bridge", Update of:
 * <code>Bridges.1107531599613[the_geom=POINT (0.0001 0.0006), NAME="Cam Bridge2"]</code></li>
 * <li>Commit Message: "Moved building", Update of
 * <code>Buildings.1107531701011[the_geom=MULTIPOLYGON (((0.002 0.0007, 0.0024 0.0007, 0.0024 0.0005, 0.002 0.0005, 0.002 0.0007)))]</code>
 * </li>
 * <li>Commit Message: "Deleted building", Delete of <code>Buildings.1107531701010</code></li>
 * </ul>
 * 
 * @author groldan
 * 
 */
public abstract class WFS20VersioningTestSupport extends WFS20TestSupport {

    protected static final Logger LOGGER = Logging.getLogger(WFS20VersioningTestSupport.class);

    protected static final Name CITE_BRIDGES = new NameImpl(MockData.BRIDGES);

    protected static final Name CITE_BUILDINGS = new NameImpl(MockData.BUILDINGS);

    protected static final FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);

    private XpathEngine xpath;

    @Override
    public void oneTimeSetUp() throws Exception {
        super.oneTimeSetUp();
        xpath = XMLUnit.newXpathEngine();
        
        GEOGIT ggitFacade = GeoServerExtensions.bean(GEOGIT.class, applicationContext);
        ggitFacade.setAuthenticationResolver(new org.geoserver.geogit.GeoServerAuthenticationResolver() {
            @Override
            public String getCurrentUserName() {
                return "admin";
            }
        });

        // insert the single bridge in cite:Bridges
        assertTrue(makeVersioned(ggitFacade, CITE_BRIDGES) instanceof RevCommit);

        // insert the two buildings in cite:Buildings
        assertTrue(makeVersioned(ggitFacade, CITE_BUILDINGS) instanceof RevCommit);

        GeometryFactory gf = new GeometryFactory();

        Filter filter;
        List<String> properties;
        List<Object> newValues;
        String commitMessage;

        // update the bridge
        properties = Arrays.asList("NAME", "the_geom");
        newValues = Arrays.asList("Cam Bridge2",
                (Object) gf.createPoint(new Coordinate(0.0001, 0.0006)));
        commitMessage = "Change Cam Bridge";
        filter = Filter.INCLUDE;
        recordUpdateCommit(ggitFacade, CITE_BRIDGES, filter, properties, newValues, commitMessage);

        FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);
        // update second building
        filter = ff.id(Collections.singleton(ff.featureId("Buildings.1107531701011")));
        Geometry movedBuilding = new WKTReader()
                .read("MULTIPOLYGON (((0.002 0.0007, 0.0024 0.0007, 0.0024 0.0005, 0.002 0.0005, 0.002 0.0007)))");

        properties = Arrays.asList("the_geom");
        newValues = Arrays.asList((Object) movedBuilding);
        commitMessage = "Moved building";
        recordUpdateCommit(ggitFacade, CITE_BUILDINGS, filter, properties, newValues, commitMessage);

        // delete first building
        filter = ff.id(Collections.singleton(ff.featureId("Buildings.1107531701010")));
        recordDeleteCommit(ggitFacade, CITE_BUILDINGS, filter, "Deleted building");

    }

    private void recordDeleteCommit(final GEOGIT facade, final Name typeName, final Filter filter,
            final String commitMessage) throws Exception {

        FeatureTypeInfo typeInfo = getCatalog().getFeatureTypeByName(typeName);
        SimpleFeatureStore store = (SimpleFeatureStore) typeInfo.getFeatureSource(null, null);

        @SuppressWarnings("rawtypes")
        FeatureCollection affectedFeatures = store.getFeatures(filter);
        assertTrue("affectedFeatures" + affectedFeatures.size(), affectedFeatures.size() > 0);

        LOGGER.info("Creating commit '" + commitMessage + "'");
        //facade.stageDelete("d1", typeName, filter, affectedFeatures);

        store.removeFeatures(filter);

        assertNotNull(facade.commitChangeSet("d1", commitMessage));
        LOGGER.info("Delete committed");
    }

    private void recordUpdateCommit(final GEOGIT facade, final Name typeName, final Filter filter,
            final List<String> properties, final List<Object> newValues, final String commitMessage)
            throws Exception {

        FeatureTypeInfo typeInfo = getCatalog().getFeatureTypeByName(typeName);
        SimpleFeatureStore store = (SimpleFeatureStore) typeInfo.getFeatureSource(null, null);

        @SuppressWarnings("rawtypes")
        FeatureCollection affectedFeatures = store.getFeatures(filter);
        assertTrue("affectedFeatures" + affectedFeatures.size(), affectedFeatures.size() > 0);

        List<PropertyName> updatedProperties = Arrays.asList(ff.property("NAME"),
                ff.property("the_geom"));

        store.modifyFeatures(properties.toArray(new String[properties.size()]),
                newValues.toArray(), filter);

        LOGGER.info("Creating commit '" + commitMessage + "'");
        //facade.stageUpdate("t1", typeName, filter, updatedProperties, newValues, affectedFeatures);

        assertNotNull(facade.commitChangeSet("t1", commitMessage));
        LOGGER.info("Update committed");
    }

    private Object makeVersioned(final GEOGIT facade, final Name featureTypeName) throws Exception {
        LOGGER.info("Importing FeatureType as versioned: " + featureTypeName);
        Future<?> future = facade.initialize(featureTypeName);
        future.get();// lock until imported
        assertTrue(facade.isReplicated(featureTypeName));
        return future.get();
    }

    protected List<String> evaluateAll(final String xpathStr, final Document dom) throws Exception {
        NodeList matchingNodes = xpath.getMatchingNodes(xpathStr, dom);
        int length = matchingNodes.getLength();
        List<String> matches = new ArrayList<String>(length);
        for (int i = 0; i < length; i++) {
            Node item = matchingNodes.item(i);
            String nodeValue = item.getTextContent();
            matches.add(nodeValue);
        }
        return matches;
    }

}
