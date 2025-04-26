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


    @Inject
    private Event<EntityPrePersist> entityPrePersistEvent;

    @Inject
    private Event<EntityPostPersist> entityPostPersistEvent;

    /**
     * Fires an event before an entity is persisted.
     *
     * @param entity the entity to be persisted
     * @param <T>    the type of the entity
     */
    public <T> void firePreEntity(T entity) {
        entityPrePersistEvent.fire(EntityPrePersist.of(entity));
    }

    /**
     * Fires an event after an entity is persisted.
     *
     * @param entity the persisted entity
     * @param <T>    the type of the entity
     */
    public <T> void firePostEntity(T entity) {
        entityPostPersistEvent.fire(EntityPostPersist.of(entity));
    }

}
