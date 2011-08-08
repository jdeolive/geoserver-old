package org.geoserver.wfsv;

import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.data.VersioningPlugin;
import org.geotools.data.simple.SimpleFeatureSource;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class WFSVPlugin extends VersioningPlugin {

    @Override
    public SimpleFeatureSource wrap(SimpleFeatureSource featureSource,
            SimpleFeatureType featureType, FeatureTypeInfo info, CoordinateReferenceSystem crs) {
        throw new UnsupportedOperationException("Implement!");
    }

}
