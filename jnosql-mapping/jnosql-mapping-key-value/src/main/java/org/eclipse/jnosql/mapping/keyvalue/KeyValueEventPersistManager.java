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
package org.eclipse.jnosql.mapping.keyvalue;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import org.eclipse.jnosql.communication.keyvalue.KeyValueEntity;
import org.eclipse.jnosql.mapping.EntityPostPersist;
import org.eclipse.jnosql.mapping.EntityPrePersist;

@ApplicationScoped
public class KeyValueEventPersistManager {

    @Inject
    private Event<EntityPrePersist> entityPrePersistEvent;

    @Inject
    private Event<EntityPostPersist> entityPostPersistEvent;

    /**
     * Fire an event once the method is called
     *
     * @param entity the entity
     * @param <T>    the entity type
     */
    public <T> void firePreEntity(T entity) {
        entityPrePersistEvent.fire(EntityPrePersist.of(entity));
    }

    /**
     * Fire an event after convert the {@link KeyValueEntity},
     * from database response, to Entity.
     *
     * @param entity the entity
     * @param <T>    the entity kind
     */
    public <T> void firePostEntity(T entity) {
        entityPostPersistEvent.fire(EntityPostPersist.of(entity));
    }
}
