package org.geoserver.catalog.hib;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.CatalogDAO;
import org.geoserver.catalog.CatalogInfo;
import org.geoserver.catalog.DataStoreInfo;
import org.geoserver.catalog.LayerGroupInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.MapInfo;
import org.geoserver.catalog.NamespaceInfo;
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.catalog.StoreInfo;
import org.geoserver.catalog.StyleInfo;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.catalog.impl.DataStoreInfoImpl;
import org.geoserver.catalog.impl.DefaultCatalogDAO;
import org.geoserver.catalog.impl.NamespaceInfoImpl;
import org.geoserver.catalog.impl.WorkspaceInfoImpl;
import org.geotools.util.logging.Logging;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional

public class HibCatalogDAO implements CatalogDAO {

    /**
     * logging instance
     */
    protected final Logger LOGGER = Logging.getLogger("org.geoserver.catalog.hib");
    
    @PersistenceContext
    EntityManager entityManager;
    
    /**
     * the catalog
     */
    Catalog catalog;
    
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    
    public Catalog getCatalog() {
        return catalog;
    }
    
    public void setCatalog(Catalog catalog) {
        this.catalog = catalog;
    }
    
    //
    // workspaces
    //
    
    public WorkspaceInfo add(WorkspaceInfo workspace) {
        return persist(workspace);
    }
    
    public void save(WorkspaceInfo workspace) {
        merge(workspace);
    }
    
    public void remove(WorkspaceInfo workspace) {
        delete(workspace);
    }
    
    public WorkspaceInfo getWorkspace(String id) {
        return (WorkspaceInfo) first(
            query("from ", WorkspaceInfo.class, " where id = ", param(id)));
    }
    
    public WorkspaceInfo getWorkspaceByName(String name) {
        return (WorkspaceInfo) first(
            query("from ", WorkspaceInfo.class, " where name = ", param(name)));
    }
    
    public List<WorkspaceInfo> getWorkspaces() {
        return (List<WorkspaceInfo>) list(WorkspaceInfo.class);
    }
    
    public WorkspaceInfo getDefaultWorkspace() {
        Query query = 
            query("from ", WorkspaceInfoImpl.class, " where default = ", param(Boolean.TRUE));
        return (WorkspaceInfoImpl) first(query);
    }

    public void setDefaultWorkspace(WorkspaceInfo workspace) {
        //TODO: remove the cast to WorkspaceInfoImpl
        WorkspaceInfo old = getDefaultWorkspace();

        if (old != null) {
            ((WorkspaceInfoImpl)old).setDefault(false);
            save(old);
        }
        
        if (workspace != null) {
            ((WorkspaceInfoImpl)workspace).setDefault(true);
            save(workspace);
        }
        
        //fire change event
        catalog.fireModified(catalog, 
            Arrays.asList("defaultWorkspace"), Arrays.asList(old), Arrays.asList(workspace));
    }
    
    //
    // namespaces
    //
    public NamespaceInfo add(NamespaceInfo namespace) {
        return persist(namespace);
    }
    
    public void save(NamespaceInfo namespace) {
        merge(namespace);
    }

    public void remove(NamespaceInfo namespace) {
        delete(namespace);
    }
    
    public NamespaceInfo getNamespace(String id) {
        Query query = query("from ", NamespaceInfo.class, " where id = ", param(id));
        return (NamespaceInfo) first(query);
    }

    public NamespaceInfo getNamespaceByPrefix(String prefix) {
        Query query = query("from ", NamespaceInfo.class, " where prefix = ", param(prefix));
        return (NamespaceInfo) first(query);
    }

    public NamespaceInfo getNamespaceByURI(String uri) {
        Query query = query("from ", NamespaceInfo.class, " where URI = ", param(uri));
        return (NamespaceInfo) first(query);
    }

    public List<NamespaceInfo> getNamespaces() {
        return list(NamespaceInfo.class);
    }
    
    public NamespaceInfo getDefaultNamespace() {
        Query query = query("from ", NamespaceInfo.class, " where default = ", param(Boolean.TRUE));
        return (NamespaceInfo) first(query);
    }
    
    public void setDefaultNamespace(NamespaceInfo namespace) {
        //TODO: remove the cast to NamespaceInfoImpl
        NamespaceInfo old = getDefaultNamespace();

        if (old != null) {
            ((NamespaceInfoImpl)old).setDefault(false);
            save(old);
        }
        
        if (namespace != null) {
            ((NamespaceInfoImpl)namespace).setDefault(true);
            save(namespace);
        }
        
        //fire change event
        catalog.fireModified(catalog, 
            Arrays.asList("defaultNamespace"), Arrays.asList(old), Arrays.asList(namespace));
    }
    
    
    //
    // stores
    //
    public StoreInfo add(StoreInfo store) {
        return persist(store);
    }
    
    public void save(StoreInfo store) {
        merge(store);
    }

    public void remove(StoreInfo store) {
        delete(store);
    }
    
    public <T extends StoreInfo> T getStore(String id, Class<T> clazz) {
        return (T) first(query("from ", clazz, " where id = ", param(id)));
    }

    public <T extends StoreInfo> T getStoreByName(WorkspaceInfo workspace, String name,
            Class<T> clazz) {
        Query query = null;
        if (workspace == DefaultCatalogDAO.ANY_WORKSPACE) {
            query = query("from ", clazz, " where name = ", param(name));
        }
        else {
            query = query("from ", clazz, " where name = ", param(name), " and workspace = ", param(workspace));
        }
            
            
        return (T) first(query);
    }

    public <T extends StoreInfo> List<T> getStores(Class<T> clazz) {
        return list(clazz);
    }

    public <T extends StoreInfo> List<T> getStoresByWorkspace(WorkspaceInfo workspace,
            Class<T> clazz) {
        return query("from ", clazz, " where workspace = ", param(workspace)).getResultList();
    }

    public DataStoreInfo getDefaultDataStore(WorkspaceInfo workspace) {
        Query query = 
            query("from ", DataStoreInfoImpl.class, " where workspace = ", param(workspace), 
                 " and default = ", param(Boolean.TRUE));
        return (DataStoreInfo) first(query);
    }

    public void setDefaultDataStore(WorkspaceInfo workspace, DataStoreInfo store) {
        //TODO: remove the cast to DataStoreInfoImpl
        DataStoreInfo old = getDefaultDataStore(workspace);

        if (old != null) {
            ((DataStoreInfoImpl)old).setDefault(false);
            save(old);
        }
        
        if (store != null) {
            ((DataStoreInfoImpl)store).setDefault(true);
            save(store);
        }
        
        //fire change event
        catalog.fireModified(catalog, 
            Arrays.asList("defaultDataStore"), Arrays.asList(old), Arrays.asList(store));
    }

    
    //
    // resources
    //
    public ResourceInfo add(ResourceInfo resource) {
        return persist(resource);
    }
    
    public void save(ResourceInfo resource) {
        merge(resource);
    }
    
    public void remove(ResourceInfo resource) {
        delete(resource);
    }
    
    public <T extends ResourceInfo> T getResource(String id, Class<T> clazz) {
        return (T) first(query("from ", clazz, " where id = ", param(id)));
    }

    public <T extends ResourceInfo> T getResourceByName(NamespaceInfo namespace, String name,
            Class<T> clazz) {
        Query query = query("from ", clazz, " r where name = ", param(name),
            " and r.namespace.prefix = ", param(namespace.getPrefix()));
        return (T) first(query);
    }

    public <T extends ResourceInfo> T getResourceByStore(StoreInfo store, String name,
            Class<T> clazz) {
        Query query = query("from ", clazz, " r where name = ", param(name),
            " and r.store = ", param(store));
        return (T) first(query);
    }

    public <T extends ResourceInfo> List<T> getResources(Class<T> clazz) {
        return (List<T>) list(clazz);
    }

    public <T extends ResourceInfo> List<T> getResourcesByNamespace(NamespaceInfo namespace, Class<T> clazz) {
        Query query = query("select r from ", clazz, " r, ", NamespaceInfo.class, " n",
                " where r.namespace = n and n.prefix = ", param(namespace.getPrefix()));
        return query.getResultList();
    }

    public <T extends ResourceInfo> List<T> getResourcesByStore(StoreInfo store, Class<T> clazz) {
        Query query = query("from ", clazz, " r where r.store = ", param(store));
        return query.getResultList();
    }
    
    //
    // styles
    //
    public StyleInfo add(StyleInfo style) {
        return persist(style);
    }
    
    public void save(StyleInfo style) {
        merge(style);
    }

    public void remove(StyleInfo style) {
        delete(style);
    }
   
    public StyleInfo getStyle(String id) {
        Query query = query("from ", StyleInfo.class, " where id = ", param(id));
        return (StyleInfo) first(query);
    }

    public StyleInfo getStyleByName(String name) {
        Query query = query("from ", StyleInfo.class, " where name = ", param(name));
        return (StyleInfo) first(query);
    }

    public List<StyleInfo> getStyles() {
        return (List<StyleInfo>) list(StyleInfo.class);
    }
    
    //
    // layers
    //
    public LayerInfo add(LayerInfo layer) {
        
        // FIXME we are replacing some referenced object here because hib would recognized original
        // ones as unattached.
        if (layer.getResource().getId() != null) {
            Query query = query("from ", layer.getResource().getClass(), " where id = ",
                    param(layer.getResource().getId()));
            layer.setResource((ResourceInfo) first(query));
        }

        // FIXME we are replacing some referenced object here because hib would recognized original
        // ones as unattached.
        if (layer.getDefaultStyle() != null) {
            Query query = query("from ", StyleInfo.class, " where id = ", 
                param(layer.getDefaultStyle().getId()));
            layer.setDefaultStyle((StyleInfo) first(query));
        }

        
        return persist(layer);
    }
    
    public void save(LayerInfo layer) {
        merge(layer);
    }
    
    public void remove(LayerInfo layer) {
        delete(layer);
    }
    
    public LayerInfo getLayer(String id) {
        Query query = query("from ", LayerInfo.class, " where id = ", param(id));
        return (LayerInfo) first(query);
    }

    public LayerInfo getLayerByName(String name) {
        Query query = query("from ", LayerInfo.class, " where resource.name = ", param(name));
        return (LayerInfo) first(query);
    }
    
    public List<LayerInfo> getLayers(ResourceInfo resource) {
        Query query = 
            query("from ", LayerInfo.class, " where resource.id = ", param(resource.getId()));
        return (List<LayerInfo>) query.getResultList();
    }

    public List<LayerInfo> getLayers(StyleInfo style) {
        Query query = query("from ", LayerInfo.class, " where defaultStyle.id = ", param(style.getId()));
          
        //TODO: we need to check layer.styles as well, nto sure how to do this with hql...
        //  "or style in elements(layer.styles)", 
        //   " and style.id = ", param(style.getId()));
        return (List<LayerInfo>) query.getResultList();
    }

    public List<LayerInfo> getLayers() {
        return list(LayerInfo.class);
    }
    
    //
    // layer groups
    //
    public LayerGroupInfo add(LayerGroupInfo layerGroup) {
        return persist(layerGroup);
    }

    public void save(LayerGroupInfo layerGroup) {
        merge(layerGroup);
    }
    
    public void remove(LayerGroupInfo layerGroup) {
        delete(layerGroup);
    }
    
    public LayerGroupInfo getLayerGroup(String id) {
        Query query = query("from ", LayerGroupInfo.class, " where id = ", param(id));
        return (LayerGroupInfo) first(query);
    }

    public LayerGroupInfo getLayerGroupByName(String name) {
        Query query = query("from ", LayerGroupInfo.class, " where name = ", param(name));
        return (LayerGroupInfo) first(query);
    }

    public List<LayerGroupInfo> getLayerGroups() {
        return list(LayerGroupInfo.class);
    }
    
    //
    // Utilities
    //
    protected <T extends CatalogInfo> T persist(T entity) {
        entityManager.persist(entity);
        return entity;
    }
    
    protected void merge(CatalogInfo entity) {
        entityManager.merge(entity);
    }
    
    protected void delete(CatalogInfo entity) {
        CatalogInfo attached = entityManager.merge(entity);
        entityManager.remove(attached);
    }
    
    protected static QueryParam param(Object param) {
        return new QueryParam(param);
    }
    
    /** Simple wrapper to tell which objects are bindable query parameters */
    protected static class QueryParam {
        Object param;

        public QueryParam(Object param) {
            this.param = param;
        }
    }
    
    protected Query query(Object... elems) {
        final StringBuilder builder = new StringBuilder();
        int cnt = 0;
        for (Object elem : elems) {
            if (elem instanceof String) {
                builder.append(elem);
            }
            else if (elem instanceof Class) {
                Class clazz = (Class) elem;
                ClassMappings map = ClassMappings.fromInterface(clazz); 
                if (map != null) {
                    clazz = map.getImpl();
                }
                
                builder.append(clazz.getSimpleName());
            } 
            else if (elem instanceof QueryParam) {
                builder.append(":param").append(cnt++);
            }
        }

        Query query = entityManager.createQuery(builder.toString());
        query.setHint("org.hibernate.cacheable", true);
        cnt = 0;
        
        for (Object elem : elems) {
            if (elem instanceof QueryParam) {
                query.setParameter("param" + (cnt++), ((QueryParam) elem).param);
            }
        }

        return query;
    }

    protected Object first(final Query query) {
        return first(query, true);
    }

    protected Object first(final Query query, boolean doWarn) {
        query.setMaxResults(doWarn ? 2 : 1);
        query.setHint("org.hibernate.cacheable", true);
        
        List<?> result = query.getResultList();
        if (result.isEmpty()) {
            return null;
        } 
        else {
            //TODO: add a flag to control exception
            if (result.size() > 1) {
                throw new RuntimeException("Expected 1 result from " + query + " but got " + result.size());
                
            }
//            if (doWarn && result.size() > 1) {
//                LOGGER.log(Level.WARNING, "Found too many items in result", new RuntimeException(
//                        "Trace: Found too many items in query"));
//            }

            Object ret = result.get(0);
            if (ret instanceof HibernateProxy) {
                HibernateProxy proxy = (HibernateProxy) ret;
                ret = proxy.getHibernateLazyInitializer().getImplementation();
            }

            if (LOGGER.isLoggable(Level.FINE)){
                StringBuilder callerChain = new StringBuilder();
                for (StackTraceElement stackTraceElement : new Throwable().getStackTrace()) {
                    if ("first".equals(stackTraceElement.getMethodName()))
                        continue;
                    String cname = stackTraceElement.getClassName();
                    if (cname.startsWith("org.spring"))
                        continue;
                    cname = cname.substring(cname.lastIndexOf(".") + 1);
                    callerChain.append(cname).append('.').append(stackTraceElement.getMethodName())
                            .append(':').append(stackTraceElement.getLineNumber()).append(' ');
                    // if(++num==10) break;
                }               
                LOGGER.fine("FIRST -->" + ret.getClass().getSimpleName() + " --- " + ret + " { "
                        + callerChain + "}");
            }
            return ret;
        }
    }
    
    protected <T> List<T> list(Class<T> clazz) {
        Query query = query("from ", clazz);
        query.setHint("org.hibernate.cacheable", true);
        List<?> result = query.getResultList();
        return Collections.unmodifiableList((List<T>) result);
    }
    
   

    

    public MapInfo add(MapInfo map) {
        return null;
    }

    
   

    public void dispose() {
    }

    public MapInfo getMap(String id) {
        return null;
    }

    public MapInfo getMapByName(String name) {
        return null;
    }

    public List<MapInfo> getMaps() {
        return null;
    }


    

    public void remove(MapInfo map) {
    }

   
    public void resolve() {
    }

    

    public void save(MapInfo map) {
    }

    public void sync(CatalogDAO other) {
        
    }
    
}
