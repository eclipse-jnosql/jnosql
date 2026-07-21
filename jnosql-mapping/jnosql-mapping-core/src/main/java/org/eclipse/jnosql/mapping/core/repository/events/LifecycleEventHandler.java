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
package org.eclipse.jnosql.mapping.core.repository.events;

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

    <T> void preDelete(T entity);
    <T> void preInsert(T entity);
    <T> void preUpdate(T entity);
    <T> void preUpsert(T entity);


    <T> void postDelete(T entity);
    <T> void postInsert(T entity);
    <T> void postUpdate(T entity);
    <T> void postUpsert(T entity);
}
