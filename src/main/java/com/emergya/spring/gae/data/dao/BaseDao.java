/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.emergya.spring.gae.data.dao;

import com.emergya.spring.gae.data.model.BaseEntity;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author lroman
 */
public interface BaseDao<E extends BaseEntity> {

    /**
     * Saves a batch of entities.
     *
     * @param entities the entities to be saved. *
     */
    void batchSave(final List<E> entities);

    /**
     * Counts the total results available for a given query.
     *
     * @param queryString the query
     * @return the total result count
     */
    long count(String queryString);

    /**
     * Deletes an entity.
     *
     * @param entity the entity to be deleted.
     */
    void delete(E entity);

    /**
     * Deletes an entity by id.
     *
     * @param id the id of the entity to be deleted.
     */
    void delete(long id);

    /**
     * Gets an entity by its id.
     *
     * @param id the id of the entity to be retreived.
     * @return the entity, or null if it doesn't exist.
     */
    E getById(long id);

    /**
     * Returns all entites.
     *
     * @return All entities in a list.
     */
    List<E> list();

    /**
     * Saves an entity (new or updated).
     *
     * @param entity the entity to create (if doesn't have id) or update (if has id).
     * @return the id of the saved entity
     */
    Long save(final E entity);

    /**
     * Searchs for an entity given a query string.
     *
     * See https://cloud.google.com/appengine/docs/java/search/query_strings
     *
     * @param queryString the query string
     * @return the entities that match the query
     */
    Collection<E> search(String queryString);

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
    Collection<E> search(String queryString, int offset, int limit);

}
