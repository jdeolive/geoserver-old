/**
 * 
 */
package org.geoserver.hibernate.dao;

import java.util.List;

import javax.persistence.Query;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.LayerGroupInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.MapInfo;
import org.geoserver.catalog.NamespaceInfo;
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.catalog.StoreInfo;
import org.geoserver.catalog.StyleInfo;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.catalog.hibernate.beans.LayerGroupInfoImplHb;
import org.geoserver.catalog.hibernate.beans.LayerInfoImplHb;
import org.geoserver.catalog.hibernate.beans.NamespaceInfoImplHb;
import org.geoserver.catalog.hibernate.beans.StyleInfoImplHb;
import org.geoserver.catalog.hibernate.beans.WorkspaceInfoImplHb;
import org.geoserver.catalog.impl.MapInfoImpl;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 */
@Repository
@Transactional
public class CatalogDAOImpl extends AbstractDAOImpl implements CatalogDAO {

    public CatalogDAOImpl() {
        super();
    }

    /**
     * @see Catalog#getDefaultNamespace()
     */
    public NamespaceInfoImplHb getDefaultNamespace() {
        Query query = buildQuery("from ", NamespaceInfoImplHb.class, " where default = ",
                param(Boolean.TRUE));
        return (NamespaceInfoImplHb) first(query);
    }

    /**
     * 
     */
    public <T extends StoreInfo> T getStore(String id, Class<T> clazz) {
        Query query = buildQuery("from ", clazz, " where id = ", param(id));
        return (T) first(query);
    }

    /**
     * 
     */
    @Deprecated
    public <T extends StoreInfo> T getStoreByName(String name, Class<T> clazz) {
        Query query = buildQuery("from ", clazz, " where name = ", param(name));
        return (T) first(query);
    }

    public <T extends StoreInfo> List<T> getStoresByName(String name, Class<T> clazz) {
        Query query = buildQuery("from ", clazz, " where name = ", param(name));
        return query.getResultList();
    }

    public <T extends StoreInfo> T getStoreByName(WorkspaceInfo workspace, String name,
            Class<T> clazz) {
        Query query = buildQuery("from ", clazz, " where name = ", param(name),
                " and workspace = ", param(workspace));
        return (T) first(query);
    }

    /**
     * 
     */
    public <T extends ResourceInfo> T getResource(String id, Class<T> clazz) {
        Query query = buildQuery("from ", clazz, " where id = ", param(id));
        return (T) first(query);
    }

    /**
     * 
     */
    public <T extends ResourceInfo> T getResourceByName(String ns, String name, Class<T> clazz) {
        Query query = buildQuery("from ", clazz, " r where name = ", param(name),
                " and r.namespace.prefix = ", param(ns));
        return (T) first(query);
    }

    public <T extends ResourceInfo> List<T> getResourcesByName(String name, Class<T> clazz) {
        Query query = buildQuery("from ", clazz, " r where name = ", param(name));
        return query.getResultList();
    }

    public <T extends ResourceInfo> T getResourceByStore(StoreInfo store, String name,
            Class<T> clazz) {
        Query query = buildQuery("from ", clazz, " r where name = ", param(name),
                " and r.store = ", param(store));
        return (T) first(query);
    }

    public <T extends ResourceInfo> List<T> getResourcesByStore(StoreInfo store, Class<T> clazz) {
        Query query = buildQuery("from ", clazz, " r where r.store = ", param(store));
        return query.getResultList();
    }

    /**
     */
    public LayerInfo getLayer(String id) {
        Query query = buildQuery("from ", LayerInfoImplHb.class, " where id = ", param(id));
        return (LayerInfo) first(query);
    }

    /**
     * 
     */
    public LayerInfo getLayerByName(String name) {
        Query query = buildQuery("from ", LayerInfoImplHb.class, " where name = ", param(name));
        LayerInfo layer = (LayerInfo) first(query);

        return layer;
    }

    public LayerInfo getLayerByName(String nsprefix, String name) {
        Query query = buildQuery("from ", LayerInfoImplHb.class, " l where l.name = ", param(name),
                " and l.resource.namespace.prefix = ", param(nsprefix));
        return (LayerInfo) first(query);
    }

    public List<LayerInfo> getLayersByResourceId(String resid) {
        Query query = buildQuery("from ", LayerInfoImplHb.class, " where resource.id = ",
                param(resid));
        return (List<LayerInfo>) query.getResultList();
    }

    /**
     */
    public <T extends StoreInfo> List<T> getStores(Class<T> clazz) {
        return (List<T>) list(clazz);
    }

    /**
     */
    public <T extends ResourceInfo> List<T> getResources(Class<T> clazz) {
        return (List<T>) list(clazz);
    }

    /**
     */
    public <T extends ResourceInfo> List<T> getResourcesByNamespace(NamespaceInfo namespace,
            Class<T> clazz) {
        Query query = buildQuery("select r from ", clazz, " r, ", NamespaceInfoImplHb.class, " n",
                " where r.namespace = n and n.prefix = ", param(namespace.getPrefix()));
        return query.getResultList();
    }

    /**
     * 
     */
    public MapInfo getMap(String id) {
        Query query = buildQuery("from ", MapInfoImpl.class, " where id = ", param(id));
        return (MapInfo) first(query);
    }

    /**
     * 
     */
    public MapInfo getMapByName(String name) {
        Query query = buildQuery("from ", MapInfoImpl.class, " where name = ", param(name));
        return (MapInfo) first(query);
    }

    /**
     * 
     */
    public List<MapInfo> getMaps() {
        return (List<MapInfo>) list(MapInfoImpl.class);
    }

    /**
     * 
     */
    public List<LayerInfo> getLayers() {
        return (List<LayerInfo>) list(LayerInfoImplHb.class);
    }

    /**
     * 
     */
    public StyleInfo getStyle(String id) {
        Query query = buildQuery("from ", StyleInfoImplHb.class, " where id = ", param(id));
        return (StyleInfo) first(query);
    }

    /**
     * 
     */
    public StyleInfo getStyleByName(String name) {
        Query query = buildQuery("from ", StyleInfoImplHb.class, " where name = ", param(name));
        return (StyleInfo) first(query);
    }

    /**
     * 
     */
    public List<StyleInfo> getStyles() {
        return (List<StyleInfo>) list(StyleInfoImplHb.class);
    }

    /**
     * 
     */
    public NamespaceInfoImplHb getNamespace(String id) {
        Query query = buildQuery("from ", NamespaceInfoImplHb.class, " where id = ", param(id));
        return (NamespaceInfoImplHb) first(query);
    }

    /**
     * 
     */
    public NamespaceInfoImplHb getNamespaceByPrefix(String prefix) {
        Query query = buildQuery("from ", NamespaceInfoImplHb.class, " where prefix = ",
                param(prefix));
        return (NamespaceInfoImplHb) first(query);
    }

    /**
     * 
     */
    public NamespaceInfo getNamespaceByURI(String uri) {
        Query query = buildQuery("from ", NamespaceInfoImplHb.class, " where URI = ", param(uri));
        return (NamespaceInfoImplHb) first(query);
    }

    /**
     * 
     */
    public List<NamespaceInfo> getNamespaces() {
        return (List<NamespaceInfo>) list(NamespaceInfoImplHb.class);
    }

    /**
     */
    public WorkspaceInfoImplHb getDefaultWorkspace() {
        Query query = buildQuery("from ", WorkspaceInfoImplHb.class, " where default = ",
                param(Boolean.TRUE));
        return (WorkspaceInfoImplHb) first(query);
    }

    /**
     */
    public List<LayerGroupInfo> getLayerGroups() {
        return (List<LayerGroupInfo>) list(LayerGroupInfoImplHb.class);
    }

    public LayerGroupInfo getLayerGroup(String id) {
        Query query = buildQuery("from ", LayerGroupInfoImplHb.class, " where id = ", param(id));
        return (LayerGroupInfo) first(query);
    }

    public LayerGroupInfo getLayerGroupByName(String name) {
        Query query = buildQuery("from ", LayerGroupInfoImplHb.class, " where name = ", param(name));
        return (LayerGroupInfo) first(query);
    }

    /**
     */
    public <T extends StoreInfo> List<T> getStoresByWorkspace(WorkspaceInfo workspace,
            Class<T> clazz) {
        Query query = buildQuery("from ", clazz, " where workspace = ", param(workspace));
        return query.getResultList();
    }

    /**
     * 
     */
    public WorkspaceInfo getWorkspace(String id) {
        return (WorkspaceInfo) first(buildQuery("from ", WorkspaceInfo.class, " where id = ",
                param(id)));
    }

    /**
     * 
     */
    public WorkspaceInfo getWorkspaceByName(String name) {
        WorkspaceInfo ws = (WorkspaceInfo) first(buildQuery("from ", WorkspaceInfo.class,
                " where name = ", param(name)));
        return ws;
    }

    /**
     * 
     */
    public List<WorkspaceInfo> getWorkspaces() {
        return (List<WorkspaceInfo>) list(WorkspaceInfo.class);
    }

    public void save(StoreInfo entity) {
        super.save(entity);
    }

    public void delete(StoreInfo entity) {
        super.delete(entity);
    }

    public StoreInfo update(StoreInfo entity) {
        return super.merge(entity);
    }

    public void save(ResourceInfo entity) {
        super.save(entity);
        entityManager.flush(); // TODO useless??
        entityManager.refresh(entity); // TODO useless??
    }

    public void delete(ResourceInfo entity) {
        super.delete(entity);
    }

    public ResourceInfo update(ResourceInfo entity) {
        return super.merge(entity);
    }

    public void save(NamespaceInfo entity) {
        super.save(entity);
    }

    public void delete(NamespaceInfo entity) {
        super.delete(entity);
        entityManager.flush();// TODO useless??
    }

    public NamespaceInfo update(NamespaceInfo entity) {
        return super.merge(entity);
    }

    public void save(LayerInfo entity) {

        // FIXME we are replacing some referenced object here because hib would recognized original
        // ones as unattached.
        if (entity.getResource().getId() != null) {
            Query query = buildQuery("from ", entity.getResource().getClass(), " where id = ",
                    param(entity.getResource().getId()));
            ResourceInfo ri = (ResourceInfo) first(query);
            entity.setResource(ri);
        }

        // FIXME we are replacing some referenced object here because hib would recognized original
        // ones as unattached.
        if (entity.getDefaultStyle() != null) {
            Query query = buildQuery("from ", StyleInfo.class, " where id = ", param(entity
                    .getDefaultStyle().getId()));
            StyleInfo style = (StyleInfo) first(query);
            entity.setDefaultStyle(style);
        }

        super.save(entity);
    }

    public void delete(LayerInfo entity) {
        super.delete(entity);
    }

    public LayerInfo update(LayerInfo entity) {
        return super.merge(entity);
    }

    public void save(MapInfo entity) {
        super.save(entity);
    }

    public void delete(MapInfo entity) {
        super.delete(entity);
    }

    public MapInfo update(MapInfo entity) {
        return super.merge(entity);
    }

    public void save(StyleInfo entity) {
        super.save(entity);
    }

    public void delete(StyleInfo entity) {
        super.delete(entity);
    }

    public StyleInfo update(StyleInfo entity) {
        return super.merge(entity);
    }

    public void save(LayerGroupInfo entity) {
        super.save(entity);
    }

    public void delete(LayerGroupInfo entity) {
        super.delete(entity);
    }

    public LayerGroupInfo update(LayerGroupInfo entity) {
        return super.merge(entity);
    }

    public void save(WorkspaceInfo entity) {
        super.save(entity);
        entityManager.flush();// TODO useless??
        entityManager.refresh(entity);// TODO useless??
    }

    public void delete(WorkspaceInfo entity) {
        super.delete(entity);
    }

    public WorkspaceInfo update(WorkspaceInfo entity) {
        return super.merge(entity);
    }

}