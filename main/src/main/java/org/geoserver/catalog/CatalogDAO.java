package org.geoserver.catalog;

import java.util.List;

import org.geoserver.catalog.impl.DefaultCatalogDAO;

//TODO: document this inteface
//  -> which methods throw which events?
public interface CatalogDAO {

    Catalog getCatalog();
        
    void setCatalog(Catalog catalog);

    //
    // Stores
    //
    StoreInfo add(StoreInfo store);

    void remove(StoreInfo store);

    void save(StoreInfo store);

    <T extends StoreInfo> T getStore(String id, Class<T> clazz);

    <T extends StoreInfo> T getStoreByName(WorkspaceInfo workspace, String name,
            Class<T> clazz);

    <T extends StoreInfo> List<T> getStoresByWorkspace(WorkspaceInfo workspace,
            Class<T> clazz);

    <T extends StoreInfo> List<T> getStores(Class<T> clazz);

    DataStoreInfo getDefaultDataStore(WorkspaceInfo workspace);

    void setDefaultDataStore(WorkspaceInfo workspace, DataStoreInfo store);

    //
    // Resources
    //
    ResourceInfo add(ResourceInfo resource);

    void remove(ResourceInfo resource);

    void save(ResourceInfo resource);

    <T extends ResourceInfo> T getResource(String id, Class<T> clazz);

    <T extends ResourceInfo> T getResourceByName(NamespaceInfo namespace,
            String name, Class<T> clazz);

    <T extends ResourceInfo> List<T> getResources(Class<T> clazz);

    <T extends ResourceInfo> List<T> getResourcesByNamespace(NamespaceInfo namespace, Class<T> clazz);

    <T extends ResourceInfo> T getResourceByStore(StoreInfo store, String name,
            Class<T> clazz);

    <T extends ResourceInfo> List<T> getResourcesByStore(StoreInfo store,
            Class<T> clazz);

    //
    // Layers
    //
    LayerInfo add(LayerInfo layer);

    void remove(LayerInfo layer);

    void save(LayerInfo layer);

    LayerInfo getLayer(String id);

    LayerInfo getLayerByName(String name);

    List<LayerInfo> getLayers(ResourceInfo resource);

    List<LayerInfo> getLayers(StyleInfo style);

    List<LayerInfo> getLayers();

    //
    // Maps
    //
    MapInfo add(MapInfo map);

    void remove(MapInfo map);

    void save(MapInfo map);

    MapInfo getMap(String id);

    MapInfo getMapByName(String name);

    List<MapInfo> getMaps();

    //
    // Layer groups
    //
    LayerGroupInfo add(LayerGroupInfo layerGroup);

    void remove(LayerGroupInfo layerGroup);

    void save(LayerGroupInfo layerGroup);

    List<LayerGroupInfo> getLayerGroups();

    LayerGroupInfo getLayerGroup(String id);

    LayerGroupInfo getLayerGroupByName(String name);

    //
    // Namespaces
    //
    NamespaceInfo add(NamespaceInfo namespace);

    void remove(NamespaceInfo namespace);

    void save(NamespaceInfo namespace);

    NamespaceInfo getDefaultNamespace();

    void setDefaultNamespace(NamespaceInfo defaultNamespace);

    NamespaceInfo getNamespace(String id);

    NamespaceInfo getNamespaceByPrefix(String prefix);

    NamespaceInfo getNamespaceByURI(String uri);

    List<NamespaceInfo> getNamespaces();

    //
    // Workspaces
    //
    // Workspace methods
    WorkspaceInfo add(WorkspaceInfo workspace);

    void remove(WorkspaceInfo workspace);

    void save(WorkspaceInfo workspace);

    WorkspaceInfo getDefaultWorkspace();

    void setDefaultWorkspace(WorkspaceInfo workspace);

    List<WorkspaceInfo> getWorkspaces();

    WorkspaceInfo getWorkspace(String id);

    WorkspaceInfo getWorkspaceByName(String name);

    //
    // Styles
    //
    StyleInfo add(StyleInfo style);

    void remove(StyleInfo style);

    void save(StyleInfo style);

    StyleInfo getStyle(String id);

    StyleInfo getStyleByName(String name);

    List<StyleInfo> getStyles();

    void dispose();

    void resolve();

    void sync(CatalogDAO other);
}
