package org.geoserver.catalog.impl;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.MultiHashMap;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.CatalogInfo;
import org.geoserver.catalog.CoverageDimensionInfo;
import org.geoserver.catalog.CoverageInfo;
import org.geoserver.catalog.DataStoreInfo;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.catalog.LayerGroupInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.MapInfo;
import org.geoserver.catalog.MetadataMap;
import org.geoserver.catalog.NamespaceInfo;
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.catalog.StoreInfo;
import org.geoserver.catalog.StyleInfo;
import org.geoserver.catalog.WMSLayerInfo;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.ows.util.ClassProperties;
import org.geoserver.ows.util.OwsUtils;

/**
 * Default catalog dao implementation in which all objects are stored in memory.
 * 
 * @author Justin Deoliveira, OpenGeo
 *
 * TODO: look for any exceptions, move them back to catlaog as they indicate logic
 */
public class DefaultCatalogDAO {

    /**
     * Contains the stores keyed by implementation class
     */
    protected MultiHashMap/* <Class> */stores = new MultiHashMap();
    
    /**
     * The default store keyed by workspace id
     */
    protected Map<String, DataStoreInfo> defaultStores = new HashMap<String, DataStoreInfo>();

    /**
     * resources
     */
    protected MultiHashMap/* <Class> */resources = new MultiHashMap();

    /**
     * namespaces
     */
    protected HashMap<String, NamespaceInfo> namespaces = new HashMap<String, NamespaceInfo>();

    /**
     * workspaces
     */
    protected HashMap<String, WorkspaceInfo> workspaces = new HashMap<String, WorkspaceInfo>();
    
    /**
     * layers
     */
    protected List<LayerInfo> layers = new ArrayList();

    /**
     * maps
     */
    protected List<MapInfo> maps = new ArrayList<MapInfo>();

    /**
     * layer groups
     */
    protected List<LayerGroupInfo> layerGroups = new ArrayList<LayerGroupInfo>();
    
    /**
     * styles
     */
    protected List<StyleInfo> styles = new ArrayList();

    /**
     * the catalog
     */
    private CatalogImpl catalog;
    
    //
    // Stores
    //
    public void add(StoreInfo store) {
        stores.put(store.getClass(), store);
    }
    
    public void remove(StoreInfo store) {
        store = unwrap(store);
        
        
        synchronized(stores) {
            stores.remove(store.getClass(),store);
            
            WorkspaceInfo workspace = store.getWorkspace();
            DataStoreInfo defaultStore = getDefaultDataStore(workspace);
            if (store.equals(defaultStore)) {
                defaultStores.remove(workspace.getId());
                
                // default removed, choose another store to become default if possible
                List dstores = getStoresByWorkspace(workspace, DataStoreInfo.class);
                if (!dstores.isEmpty()) {
                    setDefaultDataStore(workspace, (DataStoreInfo) dstores.get(0));
                }
            }
        }
    }
    
    public void save(StoreInfo store) {
        if ( store.getId() == null ) {
            //add it instead of saving
            add( store );
            return;
        }
        
        saved(store);
    }
    
    public <T extends StoreInfo> T getStore(String id, Class<T> clazz) {
        List l = lookup(clazz, stores);
        for (Iterator i = l.iterator(); i.hasNext();) {
            StoreInfo store = (StoreInfo) i.next();
            if (id.equals(store.getId())) {
                return ModificationProxy.create( (T) store, clazz );
                //return store;
            }
        }

        return null;
    }

    public <T extends StoreInfo> T getStoreByName(String name, Class<T> clazz) {
        T store = getStoreByName( (WorkspaceInfo) null, name, clazz );
        if ( store != null ) {
            return store;
        }
        
        //look for secondary match
        List l = lookup(clazz, stores);
        ArrayList matches = new ArrayList();
        for (Iterator i = l.iterator(); i.hasNext();) {
            store = (T) i.next();
            if ( name.equals( store.getName() ) ) {
                matches.add( store );
            }
        }
        
        if ( matches.size() == 1 ) {
            return ModificationProxy.create( (T) matches.get( 0 ), clazz);
        }
        
        return null;
    }
    
    public <T extends StoreInfo> T getStoreByName(WorkspaceInfo workspace,
            String name, Class<T> clazz) {
        
        if ( workspace == null ) {
            workspace = getDefaultWorkspace();
        }
        
        if(name == null || name.equals(Catalog.DEFAULT)) {
            return (T) getDefaultDataStore(workspace);
        }
        
        List l = lookup(clazz, stores);
        for (Iterator i = l.iterator(); i.hasNext();) {
            StoreInfo store = (StoreInfo) i.next();
            if (name.equals(store.getName()) && store.getWorkspace().equals( workspace )) {
                return ModificationProxy.create( (T) store, clazz );
            }
        }
        
        return null;
    }
    
    public <T extends StoreInfo> List<T> getStoresByWorkspace(
            WorkspaceInfo workspace, Class<T> clazz) {

        if ( workspace == null ) {
            workspace = getDefaultWorkspace();
        }

        List all = lookup(clazz, stores);
        List matches = new ArrayList();

        for (Iterator s = all.iterator(); s.hasNext();) {
            StoreInfo store = (StoreInfo) s.next();
            if (workspace.equals(store.getWorkspace())) {
                matches.add(store);
            }
        }

        return ModificationProxy.createList(matches,clazz);
    }
    
    public List getStores(Class clazz) {
        return ModificationProxy.createList(lookup(clazz, stores) , clazz);
    }
    
    public DataStoreInfo getDefaultDataStore(WorkspaceInfo workspace) {
        if(defaultStores.containsKey(workspace.getId())) {
            DataStoreInfo defaultStore = defaultStores.get(workspace.getId());
            return ModificationProxy.create(defaultStore, DataStoreInfo.class);
        } else {
            return null;
        }
    }
    
    public void setDefaultDataStore(WorkspaceInfo workspace, DataStoreInfo store) {
        DataStoreInfo old = defaultStores.get(workspace.getId());
        defaultStores.put(workspace.getId(), store);
        
        //fire change event
        catalog.fireModified(catalog, 
            Arrays.asList("defaultDataStore"), Arrays.asList(old), Arrays.asList(store));
    }
    
    //
    // Resources
    //
    public void add(ResourceInfo resource) {
        resources.put(resource.getClass(), resource);
    }
    
    public void save(ResourceInfo resource) {
        saved(resource);
    }
    
    public <T extends ResourceInfo> T getResource(String id, Class<T> clazz) {
        List l = lookup(clazz, resources);
        for (Iterator i = l.iterator(); i.hasNext();) {
            ResourceInfo resource = (ResourceInfo) i.next();
            if (id.equals(resource.getId())) {
                return ModificationProxy.create((T) resource, clazz );
            }
        }

        return null;
    }
    
    public <T extends ResourceInfo> T getResourceByName(String ns, String name, Class<T> clazz) {
        NamespaceInfo namespace = null;
        if ("".equals( ns ) ) {
            ns = null;
        }
        if ( ns == null ) {
            //if namespace was null, try the default namespace
            if ( getDefaultNamespace() != null ) {
                namespace = getDefaultNamespace();
            }
        }
        else {
            namespace = getNamespaceByPrefix( ns );
            if ( namespace == null ) {
                namespace = getNamespaceByURI( ns );
            }
        }
        
        List l = lookup(clazz, resources);
        if ( namespace != null ) {
            for (Iterator i = l.iterator(); i.hasNext();) {
                ResourceInfo resource = (ResourceInfo) i.next();
                if (name.equals(resource.getName())) {
                    NamespaceInfo namespace1 = resource.getNamespace();
                    if (namespace1 != null && namespace1.equals( namespace )) {
                            return ModificationProxy.create( (T) resource, clazz );
                    }
                }
            }
        }

        if ( ns == null ) {
            // no namespace was specified, so do an exhaustive lookup
            List matches = new ArrayList();
            for (Iterator i = l.iterator(); i.hasNext();) {
                ResourceInfo resource = (ResourceInfo) i.next();
                if (name.equals(resource.getName())) {
                    matches.add( resource );
                }
            }
            
            if ( matches.size() == 1 ) {
                return ModificationProxy.create( (T) matches.get( 0 ), clazz );
            }
        }
        return null;
    }
    
    public List getResources(Class clazz) {
        return ModificationProxy.createList( lookup(clazz,resources), clazz );
    }
    
    public List getResourcesByNamespace(NamespaceInfo namespace, Class clazz) {
        List all = lookup(clazz, resources);
        List matches = new ArrayList();

        if ( namespace == null ) {
            namespace = getDefaultNamespace();
        }

        for (Iterator r = all.iterator(); r.hasNext();) {
            ResourceInfo resource = (ResourceInfo) r.next();
            if (namespace != null ) {
                if (namespace.equals(resource.getNamespace())) {
                    matches.add( resource );
                }
            }
            else if ( resource.getNamespace() == null ) {
                matches.add(resource);
            }
        }

        return ModificationProxy.createList( matches, clazz );
    }
    
    public <T extends ResourceInfo> T getResourceByStore(StoreInfo store,
            String name, Class<T> clazz) {
        List all = lookup(clazz,resources);
        for (Iterator r = all.iterator(); r.hasNext(); ) {
            ResourceInfo resource = (ResourceInfo) r.next();
            if ( name.equals( resource.getName() ) && store.equals( resource.getStore() ) ) {
                return ModificationProxy.create((T)resource, clazz);
            }
                
        }
        
        return null;
    }
    
    public <T extends ResourceInfo> List<T> getResourcesByStore(
            StoreInfo store, Class<T> clazz) {
        List all = lookup(clazz,resources);
        List matches = new ArrayList();
        
        for (Iterator r = all.iterator(); r.hasNext();) {
            ResourceInfo resource = (ResourceInfo) r.next();
            if (store.equals(resource.getStore())) {
                matches.add(resource);
            }
        }

        return  ModificationProxy.createList( matches, clazz );
    }
    
    //
    // Layers
    //
    public void add(LayerInfo layer) {
        layers.add(layer);
    }
    
    public void remove(LayerInfo layer) {
        //ensure no references to the layer
        for ( LayerGroupInfo lg : layerGroups ) {
            if ( lg.getLayers().contains( layer ) ) {
                String msg = "Unable to delete layer referenced by layer group '"+lg.getName()+"'";
                throw new IllegalArgumentException( msg );
            }
        }
        layers.remove(unwrap(layer));
    }
    
    public void save(LayerInfo layer) {
        saved(layer);
    }
    
    public LayerInfo getLayer(String id) {
        for (Iterator l = layers.iterator(); l.hasNext();) {
            LayerInfo layer = (LayerInfo) l.next();
            if (id.equals(layer.getId())) {
                return ModificationProxy.create( layer, LayerInfo.class );
            }
        }

        return null;
    }
    
    public LayerInfo getLayerByName(String name) {
        String prefix = null;
        String resource = null;
        
        int colon = name.indexOf( ':' );
        if ( colon != -1 ) {
            //search by resource name
            prefix = name.substring( 0, colon );
            resource = name.substring( colon + 1 );
            
            for (Iterator l = layers.iterator(); l.hasNext();) {
                LayerInfo layer = (LayerInfo) l.next();
                ResourceInfo r = layer.getResource();
                
                if ( prefix.equals( r.getNamespace().getPrefix() ) && resource.equals( r.getName() ) ) {
                    return ModificationProxy.create( layer, LayerInfo.class );
                }
            }
        }
        else {
            //search by layer name
            for (Iterator l = layers.iterator(); l.hasNext();) {
                LayerInfo layer = (LayerInfo) l.next();
                if ( name.equals( layer.getName() ) ) {
                    return ModificationProxy.create( layer, LayerInfo.class );
                }
            }
        }
    
        return null;
    }
    
    public List<LayerInfo> getLayers(ResourceInfo resource) {
        List<LayerInfo> matches = new ArrayList<LayerInfo>();
        for (Iterator l = layers.iterator(); l.hasNext();) {
            LayerInfo layer = (LayerInfo) l.next();
            if ( resource.equals( layer.getResource() ) ) {
                matches.add( layer );
            }
        }

        return ModificationProxy.createList(matches,LayerInfo.class);
    }
    
    public List<LayerInfo> getLayers(StyleInfo style) {
        List<LayerInfo> matches = new ArrayList<LayerInfo>();
        for (Iterator l = layers.iterator(); l.hasNext();) {
            LayerInfo layer = (LayerInfo) l.next();
            if ( style.equals( layer.getDefaultStyle() ) || layer.getStyles().contains( style ) ) {
                matches.add( layer );
            }
        }

        return ModificationProxy.createList(matches,LayerInfo.class);
    }
    
    public List getLayers() {
        return ModificationProxy.createList( new ArrayList(layers), LayerInfo.class );
    }
    
    //
    // Maps
    //
    public void add(MapInfo map) {
        maps.add(map);
    }

    public void remove(MapInfo map) {
        maps.remove(unwrap(map));
    }

    public void save(MapInfo map) {
        saved( map );
    }
    
    public MapInfo getMap(String id) {
        for (MapInfo map : maps) {
            if (id.equals(map.getId())) {
                return ModificationProxy.create(map,MapInfo.class);
            }
        }

        return null;
    }

    public MapInfo getMapByName(String name) {
        for (MapInfo map : maps) {
            if (name.equals(map.getName())) {
                return ModificationProxy.create(map,MapInfo.class);
            }
        }

        return null;
    }
    
    public List<MapInfo> getMaps() {
        return ModificationProxy.createList( new ArrayList(maps), MapInfo.class );
    }
    
    //
    // Layer groups
    //
    public void add (LayerGroupInfo layerGroup) {
        layerGroups.add( layerGroup );
    }
    
    public void remove(LayerGroupInfo layerGroup) {
        layerGroups.remove( unwrap(layerGroup) );
    }
    
    public void save(LayerGroupInfo layerGroup) {
        saved(layerGroup);
    }
    
    public List<LayerGroupInfo> getLayerGroups() {
        return ModificationProxy.createList( new ArrayList(layerGroups), LayerGroupInfo.class );
    }
    
    public LayerGroupInfo getLayerGroup(String id) {
        for (LayerGroupInfo layerGroup : layerGroups ) {
            if ( id.equals( layerGroup.getId() ) ) {
                return ModificationProxy.create(layerGroup,LayerGroupInfo.class);
            }
        }
        
        return null;
    }
    
    public LayerGroupInfo getLayerGroupByName(String name) {
        for (LayerGroupInfo layerGroup : layerGroups ) {
            if ( name.equals( layerGroup.getName() ) ) {
                return ModificationProxy.create(layerGroup,LayerGroupInfo.class);
            }
        }
        
        return null;
    }
    
    //
    // Namespaces
    //
    public void add(NamespaceInfo namespace) {
        namespaces.put(namespace.getPrefix(),namespace);
    }
    
    public void remove(NamespaceInfo namespace) {
   
        NamespaceInfo defaultNamespace = getDefaultNamespace();
        if (namespace.equals(defaultNamespace)) {
            namespaces.remove(null);
            namespaces.remove(Catalog.DEFAULT);
        }
        
        namespaces.remove(namespace.getPrefix());
    }

    public void save(NamespaceInfo namespace) {
        ModificationProxy h = 
            (ModificationProxy) Proxy.getInvocationHandler(namespace);
        
        NamespaceInfo ns = (NamespaceInfo) h.getProxyObject();
        if ( !namespace.getPrefix().equals( ns.getPrefix() ) ) {
            synchronized (namespaces) {
                namespaces.remove( ns.getPrefix() );
                namespaces.put( namespace.getPrefix(), ns );
            }
        }
        
        saved(namespace);
    }
    
    public NamespaceInfo getDefaultNamespace() {
        return namespaces.containsKey(null) ? 
                ModificationProxy.create(namespaces.get( null ),NamespaceInfo.class) : null;
    }

    public void setDefaultNamespace(NamespaceInfo defaultNamespace) {
        NamespaceInfo ns = namespaces.get( defaultNamespace.getPrefix() );
        if ( ns == null ) {
            throw new IllegalArgumentException( "No such namespace: '" + defaultNamespace.getPrefix() + "'" );
        }
        
        NamespaceInfo old = namespaces.get(null);
        namespaces.put( null, ns );
        namespaces.put( Catalog.DEFAULT, ns );
        
        //fire change event
        catalog.fireModified(catalog, 
            Arrays.asList("defaultNamespace"), Arrays.asList(old), Arrays.asList(defaultNamespace));
        
    }
    
    public NamespaceInfo getNamespace(String id) {
        for (NamespaceInfo namespace : namespaces.values() ) {
            if (id.equals(namespace.getId())) {
                return ModificationProxy.create( namespace, NamespaceInfo.class ); 
            }
        }

        return null;
    }

    public NamespaceInfo getNamespaceByPrefix(String prefix) {
        NamespaceInfo ns = namespaces.get( prefix ); 
        return ns != null ? ModificationProxy.create(ns, NamespaceInfo.class ) : null;
    }

    public NamespaceInfo getNamespaceByURI(String uri) {
        for (NamespaceInfo namespace : namespaces.values() ) {
            if (uri.equals(namespace.getURI())) {
                return ModificationProxy.create( namespace, NamespaceInfo.class );
            }
        }

        return null;
    }

    public List getNamespaces() {
        ArrayList<NamespaceInfo> ns = new ArrayList<NamespaceInfo>();
        for ( Map.Entry<String,NamespaceInfo> e : namespaces.entrySet() ) {
            if ( e.getKey() == null || e.getKey().equals(Catalog.DEFAULT)) 
                continue;
            ns.add( e.getValue() );
        }
        
        return ModificationProxy.createList( ns, NamespaceInfo.class );
    }

    //
    // Workspaces
    //
    // Workspace methods
    public void add(WorkspaceInfo workspace) {
        synchronized (workspaces) {
            workspaces.put( workspace.getName(), workspace );
            // if there is no default workspace use this one as the default
            if ( workspaces.get( null ) == null ) {
                setDefaultWorkspace(workspace);
            }
        }
    }
    
    public void remove(WorkspaceInfo workspace) {
        workspaces.remove( workspace.getName() );
        
        WorkspaceInfo defaultWorkspace = getDefaultWorkspace();
        if (workspace.equals(defaultWorkspace)) {
            workspaces.remove(null);
            workspaces.remove(Catalog.DEFAULT);
            
            //default removed, choose another workspace to become default
            if (!workspaces.isEmpty()) {
                setDefaultWorkspace(workspaces.values().iterator().next());
            }
        }
        
    }
    
    public void save(WorkspaceInfo workspace) {
        ModificationProxy h = 
            (ModificationProxy) Proxy.getInvocationHandler(workspace);
        
        WorkspaceInfo ws = (WorkspaceInfo) h.getProxyObject();
        if ( !workspace.getName().equals( ws.getName() ) ) {
            synchronized (workspaces) {
                workspaces.remove( ws.getName() );
                workspaces.put( workspace.getName(), ws );
            }
        }
        
        saved(workspace);
    }

    public WorkspaceInfo getDefaultWorkspace() {
        return workspaces.containsKey( null ) ? 
                ModificationProxy.create( workspaces.get( null ), WorkspaceInfo.class ) : null;
    }
    
    public void setDefaultWorkspace(WorkspaceInfo workspace) {
        WorkspaceInfo old = workspaces.get(null);
        workspaces.put( null, workspace );
        workspaces.put( "default", workspace );
        
        //fire change event
        catalog.fireModified(catalog, 
            Arrays.asList("defaultWorkspace"), Arrays.asList(old), Arrays.asList(workspace));
    }
    
    public List<WorkspaceInfo> getWorkspaces() {
        ArrayList<WorkspaceInfo> ws = new ArrayList<WorkspaceInfo>();
        
        //strip out default namespace
        for ( Map.Entry<String, WorkspaceInfo> e : workspaces.entrySet() ) {
            if ( e.getKey() == null || e.getKey().equals(Catalog.DEFAULT) ) {
                continue;
            }
            
            ws.add( e.getValue() );
        }
        
        return ModificationProxy.createList( ws, WorkspaceInfo.class );
    }
    
    public WorkspaceInfo getWorkspace(String id) {
        for ( WorkspaceInfo ws : workspaces.values() ) {
            if ( id.equals( ws.getId() ) ) {
                return ModificationProxy.create(ws,WorkspaceInfo.class);
            }
        }
        
        return null;
    }
    
    public WorkspaceInfo getWorkspaceByName(String name) {
        return workspaces.containsKey(name) ? 
                ModificationProxy.create( workspaces.get( name ), WorkspaceInfo.class ) : null;
    }
    
    //
    // Styles
    //
    public void add(StyleInfo style) {
        styles.add(style);
    }

    public void remove(StyleInfo style) {
        //ensure no references to the style
        for ( LayerInfo l : layers ) {
            if ( style.equals( l.getDefaultStyle() ) || l.getStyles().contains( style )) {
                throw new IllegalArgumentException( "Unable to delete style referenced by '"+ l.getName()+"'");
            }
        }
        styles.remove(unwrap(style));
    }

    public void save(StyleInfo style) {
        saved( style );
    }
    
    public StyleInfo getStyle(String id) {
        for (Iterator s = styles.iterator(); s.hasNext();) {
            StyleInfo style = (StyleInfo) s.next();
            if (id.equals(style.getId())) {
                return ModificationProxy.create(style,StyleInfo.class);
            }
        }

        return null;
    }

    public StyleInfo getStyleByName(String name) {
        for (Iterator s = styles.iterator(); s.hasNext();) {
            StyleInfo style = (StyleInfo) s.next();
            if (name.equals(style.getName())) {
                return ModificationProxy.create(style,StyleInfo.class);
            }
        }

        return null;
    }

    public List getStyles() {
        return ModificationProxy.createList(styles,StyleInfo.class);
    }
    
    //
    // Utilities
    //
    public void remove(ResourceInfo resource) {
        resource = unwrap(resource);
        resources.remove(resource.getClass(), resource);
    }
    
    public static <T> T unwrap(T obj) {
        return ModificationProxy.unwrap(obj);
    }
    
    protected void saved(CatalogInfo object) {
        //this object is a proxy
        ModificationProxy h = 
            (ModificationProxy) Proxy.getInvocationHandler(object);
        
        //get the real object
        CatalogInfo real = (CatalogInfo) h.getProxyObject();
        
        //fire out what changed
        List propertyNames = h.getPropertyNames();
        List newValues = h.getNewValues();
        List oldValues = h.getOldValues();
        
        //TODO: protect this original object, perhaps with another proxy 
        catalog.fireModified( real, propertyNames, oldValues, newValues );
        
        //commit to the original object
        h.commit();    
        
        //resolve to do a sync on the object
        //syncIdWithName(real);
        
        //fire the post modify event
        catalog.firePostModified( real );
    }
    
    List lookup(Class clazz, MultiHashMap map) {
        ArrayList result = new ArrayList();
        for (Iterator k = map.keySet().iterator(); k.hasNext();) {
            Class key = (Class) k.next();
            if (clazz.isAssignableFrom(key)) {
                result.addAll(map.getCollection(key));
            }
        }

        return result;
    }

    public void dispose() {
        if ( stores != null ) stores.clear();
        if ( defaultStores != null ) defaultStores.clear();
        if ( resources != null ) resources.clear();
        if ( namespaces != null ) namespaces.clear();
        if ( workspaces != null ) workspaces.clear();
        if ( layers != null ) layers.clear();
        if ( layerGroups != null ) layerGroups.clear();
        if ( maps != null ) maps.clear();
        if ( styles != null ) styles.clear();
    }
    
    public void resolve() {
        //JD creation checks are done here b/c when xstream depersists 
        // some members may be left null
        
        //workspaces
        if ( workspaces == null ) {
            workspaces = new HashMap<String, WorkspaceInfo>();
        }
        for ( WorkspaceInfo ws : workspaces.values() ) {
            resolve(ws);
        }
        
        //namespaces
        if ( namespaces == null ) {
            namespaces = new HashMap<String, NamespaceInfo>();
        }
        for ( NamespaceInfo ns : namespaces.values() ) {
            resolve(ns);
        }
        
        //stores
        if ( stores == null ) {
            stores = new MultiHashMap();
        }
        for ( Object o : stores.values() ) {
            resolve((StoreInfoImpl)o);
        }
        
        //styles
        if ( styles == null ) {
            styles = new ArrayList<StyleInfo>();
        }
        for ( StyleInfo s : styles ) {
            resolve(s);
        }
        
        //resources
        if ( resources == null ) {
            resources = new MultiHashMap();    
        }
        for( Object o : resources.values() ) {
            resolve((ResourceInfo)o);
        }
        
        //layers
        if ( layers == null ) {
            layers = new ArrayList<LayerInfo>();    
        }
        for ( LayerInfo l : layers ) { 
            resolve(l);
        }
        
        //layer groups
        if ( layerGroups == null ) {
            layerGroups = new ArrayList<LayerGroupInfo>();    
        }
        for ( LayerGroupInfo lg : layerGroups ) {
            resolve(lg);
        }
        
        //maps
        if ( maps == null ) {
            maps = new ArrayList<MapInfo>();
        }
        for ( MapInfo m : maps ) {
            resolve(m);
        }
    }
    
    protected void resolve(WorkspaceInfo workspace) {
        setId(workspace);
        resolveCollections(workspace);
    }
    
    protected void resolve(NamespaceInfo namespace) {
        setId(namespace);
        resolveCollections(namespace);
    }
    
    protected void resolve(StoreInfo store) {
        setId(store);
        StoreInfoImpl s = (StoreInfoImpl) store;
        
        //resolve the workspace
        WorkspaceInfo resolved = ResolvingProxy.resolve( catalog, s.getWorkspace());
        if ( resolved != null ) {
            s.setWorkspace(  resolved );    
        }
        else {
            //this means the workspace has not yet been added to the catalog, keep the proxy around
        }
        resolveCollections(s);
        
        s.setCatalog( catalog );
    }

    protected void resolve(ResourceInfo resource) {
        setId(resource);
        ResourceInfoImpl r = (ResourceInfoImpl) resource;
        
        //resolve the store
        StoreInfo resolved = ResolvingProxy.resolve( catalog, r.getStore() );
        if ( resolved != null ) {
            r.setStore( resolved );
        }
        
        if ( resource instanceof FeatureTypeInfo ) {
            resolve( (FeatureTypeInfo) resource );
        }
        if(r instanceof CoverageInfo){
            resolve((CoverageInfo) resource);
        }
        if(r instanceof WMSLayerInfo){
            resolve((WMSLayerInfo) resource);
        }
        r.setCatalog(catalog);
    }

    private void resolve(CoverageInfo r) {
        CoverageInfoImpl c = (CoverageInfoImpl)r;
        if(c.getDimensions() == null) {
            c.setDimensions(new ArrayList<CoverageDimensionInfo>());
        } else {
            for (CoverageDimensionInfo dim : c.getDimensions()) {
                if(dim.getNullValues() == null)
                    ((CoverageDimensionImpl) dim).setNullValues(new ArrayList<Double>());
            }
        }
        resolveCollections(r);
    }
    
    /**
     * We don't want the world to be able and call this without 
     * going trough {@link #resolve(ResourceInfo)}
     * @param featureType
     */
    private void resolve(FeatureTypeInfo featureType) {
        FeatureTypeInfoImpl ft = (FeatureTypeInfoImpl) featureType;
        resolveCollections(ft);
    }
    
    private void resolve(WMSLayerInfo wmsLayer) {
        WMSLayerInfoImpl impl = (WMSLayerInfoImpl) wmsLayer;
        resolveCollections(impl);
    }

    protected void resolve(LayerInfo layer) {
        setId(layer);
        if (layer.getAttribution() == null) {
            layer.setAttribution(catalog.getFactory().createAttribution());
        }
        resolveCollections(layer);
    }
    
    protected void resolve(LayerGroupInfo layerGroup) {
        setId(layerGroup);
        resolveCollections(layerGroup);
        LayerGroupInfoImpl lg = (LayerGroupInfoImpl) layerGroup;
        
        for ( int i = 0; i < lg.getLayers().size(); i++ ) {
            LayerInfo l = lg.getLayers().get( i );
            LayerInfo resolved = ResolvingProxy.resolve( catalog, l );
            lg.getLayers().set( i, resolved );
        }
        
        for ( int i = 0; i < lg.getStyles().size(); i++ ) {
            StyleInfo s = lg.getStyles().get( i );
            if(s != null) {
                StyleInfo resolved = ResolvingProxy.resolve( catalog, s );
                lg.getStyles().set( i, resolved );
            }
        }
        
    }
    
    protected void resolve(StyleInfo style) {
        setId(style);
        ((StyleInfoImpl)style).setCatalog( catalog );
    }
    
    protected void resolve(MapInfo map) {
        setId(map);
    }
    
    /**
     * Method which reflectively sets all collections when they are null.
     */
    protected void resolveCollections(Object object) {
        ClassProperties properties = OwsUtils.getClassProperties( object.getClass() );
        for ( String property : properties.properties() ) {
            Method g = properties.getter( property, null );
            if ( g == null ) {
                continue;
            }
            
            Class type = g.getReturnType();
            //only continue if this is a collection or a map
            if (  !(Map.class.isAssignableFrom( type ) || Collection.class.isAssignableFrom( type ) ) ) {
                continue;
            }
            
            //only continue if there is also a setter as well
            Method s = properties.setter( property, null );
            if ( s == null ) {
                continue;
            }
            
            //if the getter returns null, call the setter
            try {
                Object value = g.invoke( object, null );
                if ( value == null ) {
                    if ( Map.class.isAssignableFrom( type ) ) {
                        if ( MetadataMap.class.isAssignableFrom( type ) ) {
                            value = new MetadataMap();
                        }
                        else {
                            value = new HashMap();
                        }
                    }
                    else if ( List.class.isAssignableFrom( type ) ) {
                        value = new ArrayList();
                    }
                    else if ( Set.class.isAssignableFrom( type ) ) {
                        value = new HashSet();
                    }
                    else {
                        throw new RuntimeException( "Unknown collection type:" + type.getName() );
                    }
                  
                    //initialize
                    s.invoke( object, value );
                }
            } 
            catch (Exception e) {
                throw new RuntimeException( e );
            }
        }
    }
    
    protected void setId( Object o ) {
        if ( OwsUtils.get( o, "id") == null ) {
            String uid = new UID().toString();
            OwsUtils.set( o, "id", o.getClass().getSimpleName() + "-"+uid );
        }
    }
    
    public void sync(DefaultCatalogDAO other) {
        stores = other.stores;
        defaultStores = other.defaultStores;
        resources = other.resources;
        namespaces = other.namespaces;
        workspaces = other.workspaces;
        layers = other.layers;
        maps = other.maps;
        layerGroups = other.layerGroups;
        styles = other.styles;
    }
}

