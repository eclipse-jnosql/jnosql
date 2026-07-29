/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *   and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 */
package org.eclipse.jnosql.mapping.repository;

/**
 * Handles Jakarta Data entity lifecycle events triggered by Eclipse JNoSQL
 * repository operations.
 * <p>
 * A lifecycle event handler is invoked before or after an entity is processed
 * by a corresponding datastore operation. Implementations may use these
 * callbacks to integrate Eclipse JNoSQL with event mechanisms such as CDI,
 * auditing, metrics, logging, or application-specific infrastructure.
 * </p>
 *
 * <p>The expected invocation order is:</p>
 *
 * <pre>{@code
 * lifecycleEventHandler.preInsert(entity);
 * datastore.insert(entity);
 * lifecycleEventHandler.postInsert(entity);
 * }</pre>
 *
 * <p>
 * Pre-operation callbacks are invoked immediately before the datastore
 * operation. Post-operation callbacks are invoked only after the corresponding
 * datastore operation completes successfully.
 * </p>
 *
 * <p>
 * Implementations should execute callbacks synchronously when propagating
 * Jakarta Data lifecycle events. The entity instance supplied to these methods
 * might be mutable and is not guaranteed to be safe for concurrent access.
 * Implementations that perform asynchronous work should create an appropriate
 * immutable representation of the entity before transferring data to another
 * thread.
 * </p>
 *
 * <p>
 * Applications may provide an alternative implementation to customize how
 * lifecycle events are propagated. Implementations are encouraged to avoid
 * modifying the supplied entity because mutation during lifecycle notification
 * can result in undefined or datastore-specific behavior.
 * </p>
 */
public interface LifecycleEventHandler {

    /**
     * Handles an event immediately before an entity is deleted from the
     * datastore.
     *
     * @param entity the entity associated with the delete operation
     * @param <T>    the entity type
     */
    <T> void preDelete(T entity);

    /**
     * Handles an event immediately before an entity is inserted into the
     * datastore.
     *
     * @param entity the entity associated with the insert operation
     * @param <T>    the entity type
     */
    <T> void preInsert(T entity);

    /**
     * Handles an event immediately before an existing entity is updated in the
     * datastore.
     *
     * @param entity the entity associated with the update operation
     * @param <T>    the entity type
     */
    <T> void preUpdate(T entity);

    /**
     * Handles an event immediately before an entity is inserted or updated in
     * the datastore.
     * <p>
     * This callback represents an upsert operation for which the repository
     * implementation does not necessarily know whether the datastore will
     * insert a new record or update an existing one.
     * </p>
     *
     * @param entity the entity associated with the upsert operation
     * @param <T>    the entity type
     */
    <T> void preUpsert(T entity);

    /**
     * Handles an event after an entity has been successfully deleted from the
     * datastore.
     *
     * @param entity the entity associated with the completed delete operation
     * @param <T>    the entity type
     */
    <T> void postDelete(T entity);

    /**
     * Handles an event after an entity has been successfully inserted into the
     * datastore.
     *
     * @param entity the entity associated with the completed insert operation
     * @param <T>    the entity type
     */
    <T> void postInsert(T entity);

    /**
     * Handles an event after an existing entity has been successfully updated
     * in the datastore.
     *
     * @param entity the entity associated with the completed update operation
     * @param <T>    the entity type
     */
    <T> void postUpdate(T entity);

    /**
     * Handles an event after an entity has been successfully inserted or
     * updated in the datastore.
     * <p>
     * This callback represents an upsert operation for which the repository
     * implementation does not necessarily know whether the datastore inserted
     * a new record or updated an existing one.
     * </p>
     *
     * @param entity the entity associated with the completed upsert operation
     * @param <T>    the entity type
     */
    <T> void postUpsert(T entity);
}
