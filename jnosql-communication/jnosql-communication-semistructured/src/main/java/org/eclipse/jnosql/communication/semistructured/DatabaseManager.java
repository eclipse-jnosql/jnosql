/*
 *  Copyright (c) 2024 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses.
 *
 */
package org.eclipse.jnosql.communication.semistructured;


import jakarta.data.exceptions.NonUniqueResultException;
import jakarta.data.page.CursoredPage;
import jakarta.data.page.PageRequest;

import java.time.Duration;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * The manager instance bridges between Jakarta NoSQL and the NoSQL vendor, providing operations
 * to interact with a database.
 *
 * @see CommunicationEntity
 */
public interface DatabaseManager extends IdFieldNameSupplier, AutoCloseable {

    /**
     * Returns the name of the managed database.
     *
     * @return the name of the database
     */
    String name();

    /**
     * Inserts an entity into the database.
     *
     * <p>If an entity with the same unique identifier already exists in the database and the database
     * supports ACID transactions, this method throws an exception. In databases following the BASE model
     * or using an append model to write data, no exception is thrown.</p>
     *
     * <p>The returned entity includes all values written to the database, including automatically generated
     * or incremented values. After calling this method, avoid using the supplied entity instance, as this
     * method makes no guarantees about its state.</p>
     *
     * @param entity the entity to be saved
     * @return the saved entity
     * @throws NullPointerException when the provided entity is null
     */
    CommunicationEntity insert(CommunicationEntity entity);

    /**
     * Inserts an entity into the database with a specified time-to-live (TTL).
     *
     * <p>If an entity with the same unique identifier already exists in the database and the database
     * supports ACID transactions, this method raises an error. In databases following the BASE model
     * or using an append model to write data, no exception is thrown.</p>
     *
     * <p>The returned entity includes all values written to the database, including automatically generated
     * or incremented values. After calling this method, avoid using the supplied entity instance, as this
     * method makes no guarantees about its state.</p>
     *
     * <p>Time-to-live (TTL) is a feature provided by some NoSQL databases where data is automatically removed
     * from the database after a specified duration. If the database does not support TTL or if the TTL feature
     * is not enabled, this operation will not have any effect on the entity's expiration.</p>
     *
     * @param entity the entity to insert
     * @param ttl    time to live
     * @return the inserted entity
     * @throws NullPointerException          if the entity is null
     * @throws UnsupportedOperationException when the database does not support TTL
     */
    CommunicationEntity insert(CommunicationEntity entity, Duration ttl);

    /**
     * Inserts multiple entities into the database.
     *
     * <p>If any entity with the same unique identifier as any of the given entities already exists in the database
     * and the database supports ACID transactions, this method raises an error. In databases following the BASE model
     * or using an append model to write data, no exception is thrown.</p>
     *
     * <p>The returned iterable contains all inserted entities, including all automatically generated or incremented
     * values. After calling this method, avoid using the supplied entity instances, as this method makes no guarantees
     * about their state.</p>
     *
     * @param entities entities to insert
     * @return an iterable containing the inserted entities
     * @throws NullPointerException if the iterable is null or any element is null
     */
    Iterable<CommunicationEntity> insert(Iterable<CommunicationEntity> entities);

    /**
     * Inserts multiple entities into the database with a specified time-to-live (TTL).
     *
     * <p>If any entity with the same unique identifier as any of the given entities already exists in the database
     * and the database supports ACID transactions, this method raises an error. In databases following the BASE model
     * or using an append model to write data, no exception is thrown.</p>
     *
     * <p>The returned iterable contains all inserted entities, including all automatically generated or incremented
     * values. After calling this method, avoid using the supplied entity instances, as this method makes no guarantees
     * about their state.</p>
     *
     * <p>Time-to-live (TTL) is a feature provided by some NoSQL databases where data is automatically removed
     * from the database after a specified duration. If the database does not support TTL or if the TTL feature
     * is not enabled, this operation will not have any effect on the expiration of the entities.</p>
     *
     * @param entities entities to insert
     * @param ttl      time to live
     * @return an iterable containing the inserted entities
     * @throws NullPointerException          if the iterable is null or any element is null
     * @throws UnsupportedOperationException when the database does not support TTL
     */
    Iterable<CommunicationEntity> insert(Iterable<CommunicationEntity> entities, Duration ttl);

    /**
     * Modifies an existing entity in the database.
     *
     * <p>To perform an update, a matching entity with the same unique identifier must exist in the database.
     * In databases using an append model to write data or following the BASE model, this method behaves
     * the same as the {@link #insert} method.</p>
     *
     * <p>If the entity is versioned (e.g., with an annotation or by convention from the entity model),
     * the version must also match. The version is automatically incremented during the update.</p>
     *
     * <p>Non-matching entities are ignored and do not cause an error.</p>
     *
     * @param entity the entity to update
     * @return the updated entity
     * @throws NullPointerException if the entity is null
     */
    CommunicationEntity update(CommunicationEntity entity);

    /**
     * Modifies multiple entities that already exist in the database.
     *
     * <p>To perform an update to an entity, a matching entity with the same unique identifier must exist
     * in the database. In databases using an append model to write data or following the BASE model,
     * this method behaves the same as the {@link #insert(Iterable)} method.</p>
     *
     * <p>If the entity is versioned (e.g., with an annotation or by convention from the entity model),
     * the version must also match. The version is automatically incremented during the update.</p>
     *
     * <p>Non-matching entities are ignored and do not cause an error.</p>
     *
     * @param entities entities to update
     * @return the number of matching entities that were found in the database and updated
     * @throws NullPointerException if the iterable is null or any element is null
     */
    Iterable<CommunicationEntity> update(Iterable<CommunicationEntity> entities);

    /**
     * Modifies existing entities in the database based on the specified query.
     *
     * <p>This default implementation executes the update operation in memory.
     * It first selects the matching entities, applies the requested changes
     * to each entity instance, and then persists the updated entities
     * individually. This approach is intentionally generic and portable,
     * but may result in multiple read and write operations.</p>
     *
     * <p>Because this method performs updates by materializing entities
     * before persisting them, the overall execution time and the visibility
     * of changes may vary depending on the underlying database and its
     * consistency model. In databases with eventual consistency or
     * asynchronous write behavior, updates performed by this method may
     * not be immediately observable after the method returns.</p>
     *
     * <p>To improve performance and reduce latency, database drivers are
     * strongly encouraged to override this method and execute update
     * operations directly at the database level whenever possible.
     * Provider-specific implementations may leverage native update
     * mechanisms, batch operations, or server-side execution strategies.</p>
     *
     * <p>For databases that use an append-only storage model or follow
     * BASE principles, and where direct update semantics are not available,
     * this method behaves similarly to repeated insert operations when
     * not overridden.</p>
     *
     * <p>Entities that do not match the selection criteria are ignored
     * and do not result in an error.</p>
     *
     * @param query the query used to select entities to update
     * @throws NullPointerException if {@code query} is {@code null}
     */
    default void update(UpdateQuery query) {
        Objects.requireNonNull(query, "query is required");
        Stream<CommunicationEntity> entities = select(query.toSelectQuery());
        entities.forEach(entity -> {
                    entity.addAll(query.sets());
                    update(entity);
        });
    }

    /**
     * Deletes entities from the database based on the specified query.
     *
     * @param query the query used to select entities to be deleted
     * @throws NullPointerException          when either the query or the collection is null
     * @throws UnsupportedOperationException if the database does not support any operation specified in the query
     */
    void delete(DeleteQuery query);

    /**
     * Finds entities in the database based on the specified query.
     *
     * @param query the query used to select entities
     * @return a stream of entities found by the query
     * @throws NullPointerException          when the query is null
     * @throws UnsupportedOperationException if the database does not support any operation specified in the query
     */
    Stream<CommunicationEntity> select(SelectQuery query);

    /**
     * Returns the number of entities in the database that match the specified query.
     *
     * @param query the query
     * @return the number of entities returned by the query
     * @throws NullPointerException when the query is null
     */
    default long count(SelectQuery query) {
        Objects.requireNonNull(query, "query is required");
        return this.select(DefaultSelectQuery.countBy(query)).count();
    }

    /**
     * Checks if an entity matching the specified query exists in the database.
     *
     * @param query the query
     * @return true if an entity with the given query exists, false otherwise
     * @throws NullPointerException when the query is null
     */
    default boolean exists(SelectQuery query) {
        Objects.requireNonNull(query, "query is required");
        return this.select(DefaultSelectQuery.existsBy(query)).findAny().isPresent();
    }

    /**
     * Executes a query and returns the result. If the query is an insert, update, or select command,
     * it returns the result of the operation. If the query is a delete command, it returns an empty collection.
     *
     * @param query the query as a string
     * @return the result of the operation; for delete operations, an empty list is returned
     * @throws NullPointerException     when the query is null
     * @throws IllegalArgumentException when the query contains value parameters
     * @throws IllegalStateException    when there is no {@link QueryParser} available
     */
    default Stream<CommunicationEntity> query(String query) {
      return query(query, null);
    }

    /**
     * Executes a query and returns the result. If the query is an insert, update, or select command,
     * it returns the result of the operation. If the query is a delete command, it returns an empty collection.
     *
     * @param query  the query as a string
     * @param entity the entity name
     * @return the result of the operation; for delete operations, an empty list is returned
     * @throws NullPointerException     when the query is null
     * @throws IllegalArgumentException when the query contains value parameters
     * @throws IllegalStateException    when there is no {@link QueryParser} available
     */
    default Stream<CommunicationEntity> query(String query, String entity) {
        Objects.requireNonNull(query, "query is required");
        return QueryParser.INSTANCE.query(query, entity, this, CommunicationObserverParser.EMPTY);
    }

    /**
     * Prepares a query for execution.
     *
     * @param query the query as a string
     * @return a {@link CommunicationPreparedStatement} instance
     * @throws NullPointerException  when the query is null
     * @throws IllegalStateException when there is no {@link QueryParser} available
     */
    default CommunicationPreparedStatement prepare(String query) {
       return prepare(query, null);
    }

    /**
     * Prepares a query for execution.
     *
     * @param query  the query as a string
     * @param entity the entity name
     * @return a {@link CommunicationPreparedStatement} instance
     * @throws NullPointerException  when the query is null
     * @throws IllegalStateException when there is no {@link QueryParser} available
     */
    default CommunicationPreparedStatement prepare(String query, String entity) {
        Objects.requireNonNull(query, "query is required");
        QueryParser parser = new QueryParser();
        return parser.prepare(query, entity,this, CommunicationObserverParser.EMPTY);
    }

    /**
     * Returns a single entity from the database based on the specified query.
     *
     * @param query the query used to select the entity
     * @return an entity wrapped in an {@link Optional}, or {@link Optional#empty()} if no entity is found
     * @throws NonUniqueResultException when more than one entity is returned by the query
     * @throws NullPointerException     when the query is null
     */
    default Optional<CommunicationEntity> singleResult(SelectQuery query) {
        Objects.requireNonNull(query, "query is required");
        Stream<CommunicationEntity> entities = select(query);
        final Iterator<CommunicationEntity> iterator = entities.iterator();
        if (!iterator.hasNext()) {
            return Optional.empty();
        }
        final CommunicationEntity entity = iterator.next();
        if (!iterator.hasNext()) {
            return Optional.of(entity);
        }
        throw new NonUniqueResultException("More than one entity was returned by the query: " + query);
    }

    /**
     * Select entities using pagination with cursor-based paging.
     *
     * <p>This method retrieves entities based on cursor-based paging, where the cursor acts as a bookmark for the next or previous page of results.
     * The method strictly supports cursor-based pagination and does not handle offset-based pagination. If the provided {@link PageRequest} is
     * in {@link jakarta.data.page.PageRequest.Mode#OFFSET}, this method should not be used.</p>
     *
     * <p>The {@link SelectQuery} parameter will be overwritten based on the {@link PageRequest}, specifically using the cursor information to
     * adjust the query condition accordingly. This method ignores the skip value in {@link PageRequest} since skip is not applicable in cursor-based
     * pagination.</p>
     *
     * <p>For cursor-based pagination, at least one sort field must be specified in the {@link SelectQuery} order clause; otherwise, an
     * {@link IllegalArgumentException} will be thrown.</p>
     *
     * @param query         the query to retrieve entities
     * @param pageRequest   the page request defining the cursor-based paging
     * @return a {@link CursoredPage} instance containing the entities within the specified page
     * @throws NullPointerException     if the query or pageRequest is null
     * @throws IllegalArgumentException if cursor-based pagination is used without any sort field specified or if the cursor size does not match
     *                                  the sort size
     */
    default CursoredPage<CommunicationEntity> selectCursor(SelectQuery query, PageRequest pageRequest){
        Objects.requireNonNull(query, "query is required");
        Objects.requireNonNull(pageRequest, "pageRequest is required");
        if(query.sorts().isEmpty()){
            throw new IllegalArgumentException("To execute a cursor pagination, it is necessary to define at least one sort field." +
                    query);
        }
        CursorExecutor executor = CursorExecutor.of(pageRequest.mode());
        return executor.cursor(query, pageRequest, this);
    }

    /**
     * Returns the number of entities in the database.
     *
     * @param entity the entity name
     * @return the number of entities
     * @throws NullPointerException when the entity name is null
     * @throws UnsupportedOperationException if the database does not support this operation
     */
    long count(String entity);

    /**
     * The id field name to be used in the communication layer for the specific database implementation.
     * If not present, the value of the {@code @Id} annotation will be used.
     * @return the field name
     */
    @Override
    default Optional<String> defaultIdFieldName() {
        return Optional.empty();
    }

    /**
     * Closes the database manager and releases any associated resources.
     */
    @Override
    void close();
}
