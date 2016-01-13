package com.emergya.spring.gae.data.dao;

import com.emergya.spring.gae.data.model.BaseEntity;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.IndexSpec;
import com.google.appengine.api.search.Query;
import com.google.appengine.api.search.QueryOptions;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import com.google.appengine.api.search.SearchServiceFactory;
import com.googlecode.objectify.ObjectifyService;
import static com.googlecode.objectify.ObjectifyService.ofy;
import com.googlecode.objectify.VoidWork;
import com.googlecode.objectify.cmd.LoadType;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class implementing generic CRUD methods for instances of classes extending BaseEntity using Objectify and the search index.
 *
 * @author lroman
 * @param <E> The entity class
 */
public abstract class BaseDao<E extends BaseEntity> {

    private static final Map<Class<? extends BaseEntity>, Class<? extends BaseDao>> DAOS_BY_ENTITY = new HashMap<>();
    private static final int MAX_SEARCH_LIMIT = 1000;
    private static final int MAX_COUNT_LIMIT = 25000;

    private final Class<E> entityClass;
    private final Index searchIndex;

    /**
     * Constructor.
     */
    public BaseDao() {
        entityClass = (Class<E>) ((ParameterizedType) getClass().getGenericSuperclass())
                .getActualTypeArguments()[0];
        ObjectifyService.factory().register(entityClass);

        IndexSpec indexSpec = IndexSpec.newBuilder().setName(entityClass.getSimpleName()).build();
        searchIndex = SearchServiceFactory.getSearchService().getIndex(indexSpec);

        DAOS_BY_ENTITY.put(entityClass, this.getClass());
    }

    /**
     * Gets an entity by its id.
     *
     * @param id the id of the entity to be retreived.
     * @return the entity, or null if it doesn't exist.
     */
    public final E getById(long id) {

        return getQuery().id(id).now();
    }

    /**
     * Deletes an entity.
     *
     * @param entity the entity to be deleted.
     */
    public final void delete(E entity) {
        delete(entity.getId());
    }

    /**
     * Deletes an entity by id.
     *
     * @param id the id of the entity to be deleted.
     */
    public final void delete(long id) {
        ofy().delete().type(entityClass).id(id);

        searchIndex.delete(id + "");
    }

    /**
     * Saves an entity (new or updated).
     *
     * @param entity the entity to create (if doesn't have id) or update (if has id).
     * @return the id of the saved entity
     */
    public final Long save(final E entity) {
        long id = ofy().save().entity(entity).now().getId();
        entity.setId(id);

        searchIndex.put(entity.toDocument());
        return id;
    }

    /**
     * Saves a batch of entities.
     *
     * @param entities the entities to be saved. *
     */
    public final void batchSave(final List<E> entities) {
        ofy().transact(new VoidWork() {
            @Override
            public void vrun() {
                ofy().save().entities(entities);
            }
        });
    }

    /**
     * Returns all entites.
     *
     * @return All entities in a list.
     */
    public final List<E> list() {
        return getQuery().list();
    }

    /**
     * Searchs for an entity given a query string.
     *
     * See https://cloud.google.com/appengine/docs/java/search/query_strings
     *
     * @param queryString the query string
     * @return the entities that match the query
     */
    public final Collection<E> search(String queryString) {
        return this.search(queryString, 0, MAX_SEARCH_LIMIT);
    }

    /**
     * Paginated searchs for an entity given a query string.
     *
     * See https://cloud.google.com/appengine/docs/java/search/query_strings
     *
     * @param queryString the query string
     * @param offset the index of the first result to be retrieved
     * @param limit the number of results to be retrieved (page size)
     * @return the entities that match the query
     */
    public final Collection<E> search(String queryString, int offset, int limit) {
        Query query = Query.newBuilder().setOptions(
                QueryOptions.newBuilder()
                .setLimit(limit)
                .setOffset(offset)
                .setFieldsToReturn("id"))
                .build(queryString);
        Results<ScoredDocument> searchResults = searchIndex.search(query);

        List<Long> ids = new ArrayList<>();
        for (ScoredDocument result : searchResults) {
            ids.add(Long.valueOf(result.getId()));
        }

        Map<Long, E> entities = getQuery().ids(ids);
        return entities.values();
    }

    /**
     * Counts the total results available for a given query.
     *
     * @param queryString the query
     * @return the total result count
     */
    public final long count(String queryString) {
        Query query = Query.newBuilder().setOptions(QueryOptions.newBuilder()
                .setLimit(1)
                .setFieldsToReturn("id")
                .setNumberFoundAccuracy(MAX_COUNT_LIMIT))
                .build(queryString);
        Results<ScoredDocument> searchResults = searchIndex.search(query);
        return searchResults.getNumberFound();
    }

    /**
     * Returns a query object to operate on DataStore.
     *
     * @return the query object.
     */
    protected final LoadType<E> getQuery() {
        return ofy().load().type(entityClass);
    }

    /**
     * Returns the dao class associated to a given entity.
     *
     * @param entityClass the full name of the entity class
     * @return the dao class.
     */
    public static Class<? extends BaseDao> getDaoForEntity(Class<? extends BaseEntity> entityClass) {
        return DAOS_BY_ENTITY.get(entityClass);
    }

}
