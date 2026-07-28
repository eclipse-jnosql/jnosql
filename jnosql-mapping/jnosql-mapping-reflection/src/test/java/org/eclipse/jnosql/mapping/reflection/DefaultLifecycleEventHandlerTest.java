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

import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import org.assertj.core.api.Assertions;
import org.eclipse.jnosql.mapping.reflection.entities.Person;
import org.eclipse.jnosql.mapping.repository.LifecycleEventHandler;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.mockito.Mockito.mock;

@EnableAutoWeld
@AddPackages(value = LifecycleEventHandler.class)
@AddPackages(value = BookObserver.class)
class DefaultLifecycleEventHandlerTest {

    @Inject
    private LifecycleEventHandler lifecycleEventHandler;

    @Inject
    private BookObserver bookObserver;

    @Test
    @DisplayName("Should create instance using default constructor")
    void shouldHaveDefaultConstructor() {
        var handler = new org.eclipse.jnosql.mapping.reflection.DefaultLifecycleEventHandler();
        Assertions.assertThat(handler).isNotNull();
    }

    @Test
    @DisplayName("Should create instance using constructor with event parameter")
    void shouldHaveConstructorWithEventParameter() {
        Event<Object> events = mock(Event.class);
        var handler = new org.eclipse.jnosql.mapping.reflection.DefaultLifecycleEventHandler(events);
        Assertions.assertThat(handler).isNotNull();
    }

    @Test
    @DisplayName("Should fire pre delete event")
    void shouldFirePreDeleteEvent() {
        var book = Book.builder().build();
        lifecycleEventHandler.preDelete(book);
        assertNotifiedOnly(bookObserver.preDelete(), book);
    }

    @Test
    @DisplayName("Should fire pre insert event")
    void shouldFirePreInsertEvent() {
        var book = Book.builder().build();
        lifecycleEventHandler.preInsert(book);
        assertNotifiedOnly(bookObserver.preInsert(), book);
    }

    @Test
    @DisplayName("Should fire pre update event")
    void shouldFirePreUpdateEvent() {
        var book = Book.builder().build();
        lifecycleEventHandler.preUpdate(book);
        assertNotifiedOnly(bookObserver.preUpdate(), book);
    }

    @Test
    @DisplayName("Should fire pre upsert event")
    void shouldFirePreUpsertEvent() {
        var book = Book.builder().build();
        lifecycleEventHandler.preUpsert(book);
        assertNotifiedOnly(bookObserver.preUpsert(), book);
    }

    @Test
    @DisplayName("Should fire post delete event")
    void shouldFirePostDeleteEvent() {
        var book = Book.builder().build();
        lifecycleEventHandler.postDelete(book);
        assertNotifiedOnly(bookObserver.postDelete(), book);
    }

    @Test
    @DisplayName("Should fire post insert event")
    void shouldFirePostInsertEvent() {
        var book = Book.builder().build();
        lifecycleEventHandler.postInsert(book);
        assertNotifiedOnly(bookObserver.postInsert(), book);
    }

    @Test
    @DisplayName("Should fire post update event")
    void shouldFirePostUpdateEvent() {
        var book = Book.builder().build();
        lifecycleEventHandler.postUpdate(book);
        assertNotifiedOnly(bookObserver.postUpdate(), book);
    }

    @Test
    @DisplayName("Should fire post upsert event")
    void shouldFirePostUpsertEvent() {
        var book = Book.builder().build();
        lifecycleEventHandler.postUpsert(book);
        assertNotifiedOnly(bookObserver.postUpsert(), book);
    }

    @Test
    @DisplayName("Should not notify an observer declared for another entity type")
    void shouldNotNotifyObserverOfAnotherEntityType() {
        var person = Person.builder().build();
        callbacks().forEach(callback -> callback.accept(person));
        Assertions.assertThat(references().map(AtomicReference::get))
                .as("no Book observer is notified for a Person entity")
                .containsOnlyNulls();
    }

    @Test
    @DisplayName("Should require a non null entity")
    void shouldRequireNonNullEntity() {
        callbacks().forEach(callback -> Assertions.assertThatNullPointerException()
                .isThrownBy(() -> callback.accept(null))
                .withMessage("entity must not be null"));
    }

    private void assertNotifiedOnly(AtomicReference<Book> expected, Book book) {
        Assertions.assertThat(expected.get())
                .as("the observer of the fired event is notified with the entity")
                .isSameAs(book);
        Assertions.assertThat(references().filter(reference -> reference != expected).map(AtomicReference::get))
                .as("no other lifecycle observer is notified")
                .containsOnlyNulls();
    }

    private Stream<AtomicReference<Book>> references() {
        return List.of(bookObserver.preDelete(),
                bookObserver.preInsert(),
                bookObserver.preUpdate(),
                bookObserver.preUpsert(),
                bookObserver.postDelete(),
                bookObserver.postInsert(),
                bookObserver.postUpdate(),
                bookObserver.postUpsert()).stream();
    }

    private List<Consumer<Object>> callbacks() {
        return List.of(lifecycleEventHandler::preDelete,
                lifecycleEventHandler::preInsert,
                lifecycleEventHandler::preUpdate,
                lifecycleEventHandler::preUpsert,
                lifecycleEventHandler::postDelete,
                lifecycleEventHandler::postInsert,
                lifecycleEventHandler::postUpdate,
                lifecycleEventHandler::postUpsert);
    }
}
