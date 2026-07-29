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
import org.eclipse.jnosql.mapping.reflection.entities.Book;

import java.util.concurrent.atomic.AtomicReference;

@ApplicationScoped
class BookObserver {

    private final AtomicReference<Book> preDelete = new AtomicReference<>();

    private final AtomicReference<Book> preInsert = new AtomicReference<>();

    private final AtomicReference<Book> preUpdate = new AtomicReference<>();

    private final AtomicReference<Book> preUpsert = new AtomicReference<>();

    private final AtomicReference<Book> postDelete = new AtomicReference<>();

    private final AtomicReference<Book> postInsert = new AtomicReference<>();

    private final AtomicReference<Book> postUpdate = new AtomicReference<>();

    private final AtomicReference<Book> postUpsert = new AtomicReference<>();

    public void onPreDelete(@Observes PreDeleteEvent<Book> event) {
        this.preDelete.set(event.entity());
    }

    public void onPreInsert(@Observes PreInsertEvent<Book> event) {
        this.preInsert.set(event.entity());
    }

    public void onPreUpdate(@Observes PreUpdateEvent<Book> event) {
        this.preUpdate.set(event.entity());
    }

    public void onPreUpsert(@Observes PreUpsertEvent<Book> event) {
        this.preUpsert.set(event.entity());
    }

    public void onPostDelete(@Observes PostDeleteEvent<Book> event) {
        this.postDelete.set(event.entity());
    }

    public void onPostInsert(@Observes PostInsertEvent<Book> event) {
        this.postInsert.set(event.entity());
    }

    public void onPostUpdate(@Observes PostUpdateEvent<Book> event) {
        this.postUpdate.set(event.entity());
    }

    public void onPostUpsert(@Observes PostUpsertEvent<Book> event) {
        this.postUpsert.set(event.entity());
    }

    public AtomicReference<Book> preDelete() {
        return preDelete;
    }

    public AtomicReference<Book> preInsert() {
        return preInsert;
    }

    public AtomicReference<Book> preUpdate() {
        return preUpdate;
    }

    public AtomicReference<Book> preUpsert() {
        return preUpsert;
    }

    public AtomicReference<Book> postDelete() {
        return postDelete;
    }

    public AtomicReference<Book> postInsert() {
        return postInsert;
    }

    public AtomicReference<Book> postUpdate() {
        return postUpdate;
    }

    public AtomicReference<Book> postUpsert() {
        return postUpsert;
    }
}
