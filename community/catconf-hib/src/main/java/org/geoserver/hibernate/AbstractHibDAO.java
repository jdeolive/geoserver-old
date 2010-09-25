package org.geoserver.hibernate;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.geoserver.catalog.Info;
import org.geotools.util.logging.Logging;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class AbstractHibDAO {

    /**
     * logging instance
     */
    protected final Logger LOGGER = Logging.getLogger("org.geoserver.hibernate");
    
    
    @PersistenceContext
    protected EntityManager entityManager;
    
    
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    
    /** Simple wrapper to tell which objects are bindable query parameters */
    protected static class QueryParam {
        Object param;

        public QueryParam(Object param) {
            this.param = param;
        }
    }
    
    protected static QueryParam param(Object param) {
        return new QueryParam(param);
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
    
    protected <T extends Info> T persist(T entity) {
        entityManager.persist(entity);
        return entity;
    }
    
    protected void merge(Info entity) {
        entityManager.merge(entity);
    }
    
    protected void delete(Info entity) {
        Info attached = entityManager.merge(entity);
        entityManager.remove(attached);
    }

}