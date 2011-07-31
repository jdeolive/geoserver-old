package org.geoserver.wfs.v2_0.versioning;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.Test;

import org.opengis.filter.identity.ResourceId;

/**
 * Functional test suite for the {@code GetFeature} WFS 2.0.0 operation with {@link ResourceId}
 * filter predicates.
 * 
 * @author groldan
 * 
 */
public class GetFeatureVersioningTest extends WFS20VersioningTestSupport {

    private static final String BASE_REQUEST_PATH = "/ows?service=GSS&version=1.0.0&request=GetEntries";

    private static final String REPLICATION_FEED_BASE = BASE_REQUEST_PATH + "&FEED=REPLICATIONFEED";

    private static final String RESOLUTION_FEED_BASE = BASE_REQUEST_PATH + "&FEED=RESOLUTIONFEED";

    /**
     * The ordered list of all values for the {@code /atom:feed/atom:entry/atom:title} XPath in the
     * REPLICATIONFEED as set up at {@link #oneTimeSetUp()}
     */
    private final List<String> ALL_REPLICATION_TITLES = Collections.unmodifiableList(Arrays.asList( //
            "Insert of Feature Bridges.1107531599613", //
            "Insert of Feature Buildings.1107531701011", //
            "Insert of Feature Buildings.1107531701010", //
            "Update of Feature Bridges.1107531599613",//
            "Update of Feature Buildings.1107531701011",//
            "Delte of Feature Buildings.1107531701010"//
    ));

    /**
     * The ordered list of all values for the {@code /atom:feed/atom:entry/atom:summary} XPath in
     * the REPLICATIONFEED as set up at {@link #oneTimeSetUp()}
     */
    private final List<String> ALL_REPLICATION_SUMMARIES = Collections.unmodifiableList(Arrays
            .asList(//
            "Initial import of FeatureType http://www.opengis.net/cite:Bridges",//
            "Initial import of FeatureType http://www.opengis.net/cite:Buildings",//
                    "Initial import of FeatureType http://www.opengis.net/cite:Buildings",//
                    "Change Cam Bridge",//
                    "Moved building",//
                    "Deleted building"//
            ));

    /**
     * The ordered list of all values for the {@code /atom:feed/atom:entry/atom:content} XPath in
     * the RESOLUTIONFEED as set up at {@link #oneTimeSetUp()}
     */
    private final List<String> ALL_RESOLUTION_CONTENTS = Collections.unmodifiableList(Arrays
            .asList(//
            "Initial import of FeatureType http://www.opengis.net/cite:Bridges",//
            "Initial import of FeatureType http://www.opengis.net/cite:Buildings",//
                    "Change Cam Bridge",//
                    "Moved building",//
                    "Deleted building"//
            ));

    /**
     * This is a READ ONLY TEST so we can use one time setup
     */
    public static Test suite() {
        return new OneTimeTestSetup(new GetFeatureVersioningTest());
    }
    
    public void testGetFeature(){
        
    }
}
