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

import jakarta.data.event.PostDeleteEvent;
import jakarta.data.event.PostInsertEvent;
import jakarta.data.event.PostUpdateEvent;
import jakarta.data.event.PostUpsertEvent;
import jakarta.data.event.PreDeleteEvent;
import jakarta.data.event.PreInsertEvent;
import jakarta.data.event.PreUpdateEvent;
import jakarta.data.event.PreUpsertEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
class VinylRecordLifecycleObserver {

    private final List<ObservedEvent> events = new ArrayList<>();

    void onPreInsert(@Observes PreInsertEvent<VinylRecord> event) {
        events.add(new ObservedEvent(
                LifecycleEventType.PRE_INSERT,
                event.entity()));
    }

    void onPostInsert(@Observes PostInsertEvent<VinylRecord> event) {
        events.add(new ObservedEvent(
                LifecycleEventType.POST_INSERT,
                event.entity()));
    }

    void onPreUpdate(@Observes PreUpdateEvent<VinylRecord> event) {
        events.add(new ObservedEvent(
                LifecycleEventType.PRE_UPDATE,
                event.entity()));
    }

    void onPostUpdate(@Observes PostUpdateEvent<VinylRecord> event) {
        events.add(new ObservedEvent(
                LifecycleEventType.POST_UPDATE,
                event.entity()));
    }

    void onPreUpsert(@Observes PreUpsertEvent<VinylRecord> event) {
        events.add(new ObservedEvent(
                LifecycleEventType.PRE_UPSERT,
                event.entity()));
    }

    void onPostUpsert(@Observes PostUpsertEvent<VinylRecord> event) {
        events.add(new ObservedEvent(
                LifecycleEventType.POST_UPSERT,
                event.entity()));
    }

    void onPreDelete(@Observes PreDeleteEvent<VinylRecord> event) {
        events.add(new ObservedEvent(
                LifecycleEventType.PRE_DELETE,
                event.entity()));
    }

    void onPostDelete(@Observes PostDeleteEvent<VinylRecord> event) {
        events.add(new ObservedEvent(
                LifecycleEventType.POST_DELETE,
                event.entity()));
    }

    List<ObservedEvent> events() {
        return List.copyOf(events);
    }

    void reset() {
        events.clear();
    }
}