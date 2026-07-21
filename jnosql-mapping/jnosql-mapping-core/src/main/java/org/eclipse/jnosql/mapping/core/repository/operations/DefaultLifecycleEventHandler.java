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
package org.eclipse.jnosql.mapping.core.repository.operations;

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
import jakarta.inject.Inject;


@ApplicationScoped
class DefaultLifecycleEventHandler implements LifecycleEventHandler {

    private final Event<PreDeleteEvent<?>> preDeleteEvent;
    private final Event<PreInsertEvent<?>> preInsertEvent;
    private final Event<PreUpdateEvent<?>> preUpdateEvent;
    private final Event<PreUpsertEvent<?>> preUpsertEvent;
    private final Event<PostDeleteEvent<?>> postDeleteEvent;
    private final Event<PostInsertEvent<?>> postInsertEvent;
    private final Event<PostUpdateEvent<?>> postUpdateEvent;
    private final Event<PostUpsertEvent<?>> postUpsertEvent;


    DefaultLifecycleEventHandler() {
        this.preDeleteEvent = null;
        this.preInsertEvent = null;
        this.preUpdateEvent = null;
        this.preUpsertEvent = null;
        this.postDeleteEvent = null;
        this.postInsertEvent = null;
        this.postUpdateEvent = null;
        this.postUpsertEvent = null;
    }

    @Inject
    DefaultLifecycleEventHandler(
            Event<PreDeleteEvent<?>> preDeleteEvent,
            Event<PreInsertEvent<?>> preInsertEvent,
            Event<PreUpdateEvent<?>> preUpdateEvent,
            Event<PreUpsertEvent<?>> preUpsertEvent,
            Event<PostDeleteEvent<?>> postDeleteEvent,
            Event<PostInsertEvent<?>> postInsertEvent,
            Event<PostUpdateEvent<?>> postUpdateEvent,
            Event<PostUpsertEvent<?>> postUpsertEvent) {
        this.preDeleteEvent = preDeleteEvent;
        this.preInsertEvent = preInsertEvent;
        this.preUpdateEvent = preUpdateEvent;
        this.preUpsertEvent = preUpsertEvent;
        this.postDeleteEvent = postDeleteEvent;
        this.postInsertEvent = postInsertEvent;
        this.postUpdateEvent = postUpdateEvent;
        this.postUpsertEvent = postUpsertEvent;
    }

    @Override
    public <T> void preDelete(T entity) {
        preDeleteEvent.fire(new PreDeleteEvent<>(entity));
    }

    @Override
    public <T> void preInsert(T entity) {
        preInsertEvent.fire(new PreInsertEvent<>(entity));
    }

    @Override
    public <T> void preUpdate(T entity) {
        preUpdateEvent.fire(new PreUpdateEvent<>(entity));
    }

    @Override
    public <T> void preUpsert(T entity) {
        preUpsertEvent.fire(new PreUpsertEvent<>(entity));
    }

    @Override
    public <T> void postDelete(T entity) {
        postDeleteEvent.fire(new PostDeleteEvent<>(entity));
    }

    @Override
    public <T> void postInsert(T entity) {
        postInsertEvent.fire(new PostInsertEvent<>(entity));
    }

    @Override
    public <T> void postUpdate(T entity) {
        postUpdateEvent.fire(new PostUpdateEvent<>(entity));
    }

    @Override
    public <T> void postUpsert(T entity) {
        postUpsertEvent.fire(new PostUpsertEvent<>(entity));
    }

}
