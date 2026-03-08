/*
 *  Copyright (c) 2022 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.mapping.semistructured;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import org.eclipse.jnosql.mapping.EntityPostPersist;
import org.eclipse.jnosql.mapping.EntityPrePersist;

import java.util.function.Consumer;

/**
 * This class represents the manager of events for entity persistence operations.
 * When an entity is either saved or updated, events will be fired in the following order:
 * 1) {@link EntityPrePersist} event fired before the entity is persisted.
 * 2) {@link EntityPostPersist} event fired after the entity is persisted.
 *
 * @see AbstractSemiStructuredTemplate
 */
@ApplicationScoped
public class EventPersistManager {

    private final Consumer<EntityPrePersist> entityPrePersistEvent;

    private final Consumer<EntityPostPersist> entityPostPersistEvent;

    @Inject
    EventPersistManager(Event<EntityPrePersist> entityPrePersistEvent,
                        Event<EntityPostPersist> entityPostPersistEvent) {
        this.entityPrePersistEvent = entityPrePersistEvent::fire;
        this.entityPostPersistEvent = entityPostPersistEvent::fire;
    }

    /**
     * Package-private constructor for use outside a CDI container.
     * Accepts {@link Consumer} instances as substitutes for CDI {@link Event} objects,
     * enabling instantiation in non-CDI environments such as Spring, Guice, or plain unit tests.
     *
     * <p>The caller is responsible for the lifecycle of the created instance.</p>
     *
     * @param prePersistConsumer  consumer invoked before entity persistence; must not be null
     * @param postPersistConsumer consumer invoked after entity persistence; must not be null
     */
    EventPersistManager(Consumer<EntityPrePersist> prePersistConsumer,
                        Consumer<EntityPostPersist> postPersistConsumer) {
        this.entityPrePersistEvent = prePersistConsumer;
        this.entityPostPersistEvent = postPersistConsumer;
    }

    /**
     * CDI no-arg constructor required for proxy creation.
     */
    EventPersistManager() {
        this(e -> {}, e -> {});
    }

    /**
     * Fires an event before an entity is persisted.
     *
     * @param entity the entity to be persisted
     * @param <T>    the type of the entity
     */
    public <T> void firePreEntity(T entity) {
        entityPrePersistEvent.accept(EntityPrePersist.of(entity));
    }

    /**
     * Fires an event after an entity is persisted.
     *
     * @param entity the persisted entity
     * @param <T>    the type of the entity
     */
    public <T> void firePostEntity(T entity) {
        entityPostPersistEvent.accept(EntityPostPersist.of(entity));
    }

}
