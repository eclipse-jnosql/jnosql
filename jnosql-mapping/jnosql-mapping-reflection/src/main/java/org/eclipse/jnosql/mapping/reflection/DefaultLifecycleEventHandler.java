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
package org.eclipse.jnosql.mapping.reflection;

import jakarta.data.event.LifecycleEvent;
import jakarta.data.event.PostDeleteEvent;
import jakarta.data.event.PostInsertEvent;
import jakarta.data.event.PostUpdateEvent;
import jakarta.data.event.PostUpsertEvent;
import jakarta.data.event.PreDeleteEvent;
import jakarta.data.event.PreInsertEvent;
import jakarta.data.event.PreUpdateEvent;
import jakarta.data.event.PreUpsertEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.inject.Any;
import jakarta.inject.Inject;
import org.eclipse.jnosql.mapping.repository.LifecycleEventHandler;

import java.util.Objects;
import java.util.function.Function;

/**
 * Fires Jakarta Data lifecycle events as CDI events.
 * <p>
 * Each event is fired with its type argument resolved to the runtime class of the entity, so that
 * an observer declared as {@code @Observes PreInsertEvent<Book>} is notified. See
 * {@link LifecycleEventTypeLiteral} for how the type is resolved and for the runtimes where it
 * cannot be.
 * </p>
 */
@ApplicationScoped
class DefaultLifecycleEventHandler implements LifecycleEventHandler {

    private final Event<Object> events;

    DefaultLifecycleEventHandler() {
        this.events = null;
    }

    @Inject
    DefaultLifecycleEventHandler(@Any Event<Object> events) {
        this.events = events;
    }

    @Override
    public <T> void preDelete(T entity) {
        fire(PreDeleteEvent.class, entity, PreDeleteEvent::new);
    }

    @Override
    public <T> void preInsert(T entity) {
        fire(PreInsertEvent.class, entity, PreInsertEvent::new);
    }

    @Override
    public <T> void preUpdate(T entity) {
        fire(PreUpdateEvent.class, entity, PreUpdateEvent::new);
    }

    @Override
    public <T> void preUpsert(T entity) {
        fire(PreUpsertEvent.class, entity, PreUpsertEvent::new);
    }

    @Override
    public <T> void postDelete(T entity) {
        fire(PostDeleteEvent.class, entity, PostDeleteEvent::new);
    }

    @Override
    public <T> void postInsert(T entity) {
        fire(PostInsertEvent.class, entity, PostInsertEvent::new);
    }

    @Override
    public <T> void postUpdate(T entity) {
        fire(PostUpdateEvent.class, entity, PostUpdateEvent::new);
    }

    @Override
    public <T> void postUpsert(T entity) {
        fire(PostUpsertEvent.class, entity, PostUpsertEvent::new);
    }

    private <T> void fire(Class<?> eventType, T entity, Function<T, ? extends LifecycleEvent<T>> factory) {
        T safeEntity = requireEntity(entity);
        LifecycleEvent<T> event = factory.apply(safeEntity);
        LifecycleEventTypeLiteral.<LifecycleEvent<T>>of(eventType, safeEntity.getClass())
                .ifPresentOrElse(literal -> events.select(literal).fire(event), () -> events.fire(event));
    }

    private static <T> T requireEntity(T entity) {
        return Objects.requireNonNull(entity, "entity must not be null");
    }
}
