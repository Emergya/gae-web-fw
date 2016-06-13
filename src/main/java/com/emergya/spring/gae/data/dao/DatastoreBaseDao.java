package com.emergya.spring.gae.data.dao;

import com.emergya.spring.gae.data.model.BaseEntity;
import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.GetRequest;
import com.google.appengine.api.search.GetResponse;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.IndexSpec;
import com.google.appengine.api.search.Query;
import com.google.appengine.api.search.QueryOptions;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import com.google.appengine.api.search.SearchServiceFactory;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
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
import java.util.concurrent.Callable;
import java.util.logging.Logger;

/**
 * Base class implementing generic CRUD methods for instances of classes
 * extending BaseEntity using Objectify and the search index.
 *
 * @author lroman
 * @param <E> The entity class
 */
public abstract class DatastoreBaseDao<E extends BaseEntity> implements BaseDao<E> {

    private static final Logger LOG = Logger.getLogger(DatastoreBaseDao.class.getName());

    private static final Map<Class<? extends BaseEntity>, Class<? extends DatastoreBaseDao>> DAOS_BY_ENTITY = new HashMap<>();
    private static final int MAX_SEARCH_LIMIT = 1000;
    private static final int MAX_COUNT_LIMIT = 25000;
    private static final int WAIT_MSECS = 1000;

    private final Class<E> entityClass;
    private final Index searchIndex;

    /**
     * Constructor.
     */
    public DatastoreBaseDao() {
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
    @Override
    public final E getById(final long id) {
        return tryWithBackoff(new Callable<E>() {
            @Override
            public E call() throws Exception {
                return getQuery().id(id).now();
            }
        });
    }

    /**
     * Deletes an entity.
     *
     * @param entity the entity to be deleted.
     */
    @Override
    public final void delete(E entity) {
        delete(entity.getId());
    }

    /**
     * Deletes an entity by id.
     *
     * @param id the id of the entity to be deleted.
     */
    @Override
    public final void delete(final long id) {

        tryWithBackoff(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                ofy().delete().type(entityClass).id(id);
                searchIndex.delete(id + "");
                return true;
            }
        });

    }

    /**
     * Saves an entity (new or updated).
     *
     * @param entity the entity to create (if doesn't have id) or update (if has
     * id).
     * @return the id of the saved entity
     */
    @Override
    public final Long save(final E entity) {
        long id = tryWithBackoff(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                long id = ofy().save().entity(entity).now().getId();

                searchIndex.put(entity.toDocument());
                return id;
            }
        });

        entity.setId(id);
        afterSave(entity);

        return id;
    }

    /**
     * Saves a batch of entities.
     *
     * @param entities the entities to be saved. *
     */
    @Override
    public final void batchSave(final List<E> entities) {
        ofy().transact(new VoidWork() {
            @Override
            public void vrun() {
                ofy().save().entities(entities);
            }
        });

        searchIndex.putAsync(Lists.transform(entities, new Function<E, Document>() {
            @Override
            public Document apply(E f) {
                return f.toDocument();
            }
        }));
    }

    /**
     * Returns all entites.
     *
     * @return All entities in a list.
     */
    @Override
    public final List<E> list() {
        return tryWithBackoff(new Callable<List<E>>() {
            @Override
            public List<E> call() throws Exception {
                return getQuery().list();
            }
        });
    }

    /**
     * Searchs for an entity given a query string.
     *
     * See https://cloud.google.com/appengine/docs/java/search/query_strings
     *
     * @param queryString the query string
     * @return the entities that match the query
     */
    @Override
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
    @Override
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
     * Searchs for entities given a AppEngine's query string and query options.
     *
     * @param queryString the query string to search for
     * @param options the query options object see
     * https://cloud.google.com/appengine/docs/java/search/options
     * @return the search result.
     */
    public final Collection<E> search(String queryString, QueryOptions options) {
        Query query = Query.newBuilder().setOptions(options).build(queryString);
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
    @Override
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
    public static Class<? extends DatastoreBaseDao> getDaoForEntity(Class<? extends BaseEntity> entityClass) {
        return DAOS_BY_ENTITY.get(entityClass);
    }

    /**
     * Extension point to override to execute custom actions after the entity is
     * saved with save().
     *
     * @param entity the saved entity.
     */
    protected void afterSave(E entity) {

    }

    /**
     * Reindexes the Search index so its contents are on par with the DataStore.
     *
     * @return the number of entities reindexed
     */
    public long reindex() {
        long count = 0;

        GetRequest r = GetRequest.newBuilder().setReturningIdsOnly(true).build();

        GetResponse<Document> toDelete;
        while (true) {
            toDelete = searchIndex.getRange(r);

            if (toDelete.getResults().isEmpty()) {
                break;
            }

            final List<String> ids = new ArrayList<>();
            for (Document d : toDelete.getResults()) {
                ids.add(d.getId());
            }

            searchIndex.delete(ids);
        }

        List<E> list = this.list();
        for (E e : list) {
            searchIndex.put(e.toDocument());
            count++;
        }

        return count;
    }

    private <V> V tryWithBackoff(Callable<V> r) {
        final int maxRetry = 3;
        int attempts = 0;
        int delay = 1;

        V result;
        while (true) {
            try {
                try {
                    result = r.call();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            } catch (RuntimeException e) {
                if (++attempts < maxRetry) {
                    try {
                        // retrying
                        Thread.sleep(delay * WAIT_MSECS);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                    delay *= 2; // easy exponential backoff
                    LOG.info("Retrying operation in " + delay + ". Attempt " + attempts);
                    continue;
                } else {
                    throw e; // otherwise throw
                }
            }
            break;
        }

        return result;
    }
}
