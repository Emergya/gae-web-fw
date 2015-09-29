package com.emergya.spring.gae.data.dao;

import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.IndexSpec;
import com.google.appengine.api.search.Query;
import com.google.appengine.api.search.QueryOptions;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import com.google.appengine.api.search.SearchServiceFactory;
import com.emergya.spring.gae.data.model.BaseEntity;

import static com.googlecode.objectify.ObjectifyService.ofy;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class implementing generic CRUD methods for instances of classes
 * extending BaseEntity using Objectify.
 *
 * @author lroman
 * @param <E> The entity class
 */
public abstract class BaseDao<E extends BaseEntity> {

    protected Class<E> entityClass;

    protected Index searchIndex;

    private static final Map<Class<? extends BaseEntity>, Class<? extends BaseDao>> daosByEntity = new HashMap<>();

    public BaseDao() {
        entityClass = (Class<E>) ((ParameterizedType) getClass().getGenericSuperclass())
                .getActualTypeArguments()[0];

        IndexSpec indexSpec = IndexSpec.newBuilder().setName(entityClass.getSimpleName()).build();
        searchIndex = SearchServiceFactory.getSearchService().getIndex(indexSpec);

        daosByEntity.put(entityClass, this.getClass());
    }

    public E getById(long id) {

        return ofy().load().type(entityClass).id(id).now();
    }

    public void delete(E entity) {
        delete(entity.getId());
    }

    public void delete(long id) {
        ofy().delete().type(entityClass).id(id);

        searchIndex.delete(id + "");
    }

    public Long save(final E entity) {
        long id = ofy().save().entity(entity).now().getId();
        entity.setId(id);

        searchIndex.put(entity.toDocument());

        return id;

    }

    public List<E> list() {
        return ofy().load().type(entityClass).list();
    }

    public Collection<E> search(String queryString) {
        return this.search(queryString, 0, 1000);
    }

    public Collection<E> search(String queryString, int offset, int limit) {
        Query query = Query.newBuilder().setOptions(QueryOptions.newBuilder().setLimit(limit).setOffset(offset).setFieldsToReturn("id")).build(queryString);
        Results<ScoredDocument> searchResults = searchIndex.search(query);

        List<Long> ids = new ArrayList<>();
        for (ScoredDocument result : searchResults) {
            ids.add(Long.valueOf(result.getId()));
        }

        Map<Long, E> entities = ofy().load().type(entityClass).ids(ids);
        return entities.values();
    }

    public long count(String queryString) {
        Query query = Query.newBuilder().setOptions(QueryOptions.newBuilder().setLimit(1).setFieldsToReturn("id").setNumberFoundAccuracy(25000)).build(queryString);
        Results<ScoredDocument> searchResults = searchIndex.search(query);
        return searchResults.getNumberFound();
    }

    public static Class<? extends BaseDao> getDaoForEntity(Class<? extends BaseEntity> entityClass) {
        return daosByEntity.get(entityClass);
    }
}
