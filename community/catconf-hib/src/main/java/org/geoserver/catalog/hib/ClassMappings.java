package org.geoserver.catalog.hib;

import org.geoserver.catalog.CoverageInfo;
import org.geoserver.catalog.CoverageStoreInfo;
import org.geoserver.catalog.DataStoreInfo;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.catalog.LayerGroupInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.MapInfo;
import org.geoserver.catalog.NamespaceInfo;
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.catalog.StoreInfo;
import org.geoserver.catalog.StyleInfo;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.catalog.impl.CoverageInfoImpl;
import org.geoserver.catalog.impl.CoverageStoreInfoImpl;
import org.geoserver.catalog.impl.DataStoreInfoImpl;
import org.geoserver.catalog.impl.FeatureTypeInfoImpl;
import org.geoserver.catalog.impl.LayerGroupInfoImpl;
import org.geoserver.catalog.impl.LayerInfoImpl;
import org.geoserver.catalog.impl.MapInfoImpl;
import org.geoserver.catalog.impl.NamespaceInfoImpl;
import org.geoserver.catalog.impl.ResourceInfoImpl;
import org.geoserver.catalog.impl.StoreInfoImpl;
import org.geoserver.catalog.impl.StyleInfoImpl;
import org.geoserver.catalog.impl.WorkspaceInfoImpl;

public enum ClassMappings {
    
    WORKSPACE {
        @Override public Class getInterface() { return WorkspaceInfo.class; }
        @Override public Class getImpl() { return WorkspaceInfoImpl.class; };
    }, 
    NAMESPACE {
        @Override public Class getInterface() { return NamespaceInfo.class; }
        @Override public Class getImpl() { return NamespaceInfoImpl.class; };
    },
    
    //stores, order matters
    DATASTORE {
        @Override public Class getInterface() { return DataStoreInfo.class; }
        @Override public Class getImpl() { return DataStoreInfoImpl.class; };
    },
    COVERAGESTORE {
        @Override public Class getInterface() { return CoverageStoreInfo.class; }
        @Override public Class getImpl() { return CoverageStoreInfoImpl.class; };
    },
    STORE {
        @Override public Class getInterface() { return StoreInfo.class; }
        @Override public Class getImpl() { return StoreInfoImpl.class; };
    },
    
    //resources, order matters
    FEATURETYPE {
        @Override public Class getInterface() { return FeatureTypeInfo.class; }
        @Override public Class getImpl() { return FeatureTypeInfoImpl.class; };
    },
    COVERAGE {
        @Override public Class getInterface() { return CoverageInfo.class; }
        @Override public Class getImpl() { return CoverageInfoImpl.class; };
    },
    RESOURCE {
        @Override public Class getInterface() { return ResourceInfo.class; }
        @Override public Class getImpl() { return ResourceInfoImpl.class; };
    },
    
    LAYER {
        @Override public Class getInterface() { return LayerInfo.class; }
        @Override public Class getImpl() { return LayerInfoImpl.class; };
    },
    LAYERGROUP {
        @Override public Class getInterface() { return LayerGroupInfo.class; }
        @Override public Class getImpl() { return LayerGroupInfoImpl.class; };
    },
    MAP {
        @Override public Class getInterface() { return MapInfo.class; }
        @Override public Class getImpl() { return MapInfoImpl.class; };
    },
    STYLE {
        @Override public Class getInterface() { return StyleInfo.class; }
        @Override public Class getImpl() { return StyleInfoImpl.class; };
    };

    public abstract Class getInterface();

    public abstract Class getImpl();

    public static ClassMappings fromInterface(Class interfce) {
        for(ClassMappings cm : values()) {
            if (interfce == cm.getInterface()) return cm;
        }
        return null;
    }
    
    public static ClassMappings fromImpl(Class impl) {
        for(ClassMappings cm : values()) {
            if (impl == cm.getImpl()) return cm;
        }
        return null;
    }
}
