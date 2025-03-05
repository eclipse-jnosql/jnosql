/*
 *  Copyright (c) 2025 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.mapping.graph;


import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

class DefaultEdgeTest {


    @Test
    void shouldCreateEdgeWithSourceTargetAndLabel() {
        Person person = new Person("John Doe");
        Book book = new Book("Domain-Driven Design");

        DefaultEdge<Person, Book> edge = new DefaultEdge<>(person, book, "READS", Map.of());

        assertSoftly(soft -> {
            soft.assertThat(edge.source()).isEqualTo(person);
            soft.assertThat(edge.target()).isEqualTo(book);
            soft.assertThat(edge.label()).isEqualTo("READS");
            soft.assertThat(edge.properties()).isEmpty();
        });
    }

    @Test
    void shouldCreateEdgeWithProperties() {
        Person person = new Person("Alice");
        Book book = new Book("Effective Java");

        DefaultEdge<Person, Book> edge = new DefaultEdge<>(person, book, "READS",
                Map.of("since", 2018, "medium", "paperback"));

        assertSoftly(soft -> {
            soft.assertThat(edge.property("since", Integer.class)).contains(2018);
            soft.assertThat(edge.property("medium", String.class)).contains("paperback");
        });
    }

    @Test
    void shouldReturnEmptyOptionalForId() {
        DefaultEdge<Person, Book> edge = new DefaultEdge<>(new Person("Bob"), new Book("Refactoring"), "READS", Map.of());

        assertSoftly(soft -> {
            soft.assertThat(edge.id()).isEmpty();
            soft.assertThat(edge.id(String.class)).isEmpty();
        });
    }

    @Test
    void shouldReturnUnmodifiableProperties() {
        DefaultEdge<Person, Book> edge = new DefaultEdge<>(new Person("Eve"), new Book("Clean Code"), "READS",
                Map.of("rating", 5));

        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> edge.properties().put("newProperty", "test"));
    }

    @Test
    void shouldThrowExceptionWhenPropertyKeyIsNull() {
        DefaultEdge<Person, Book> edge = new DefaultEdge<>(new Person("Alice"), new Book("DDD"), "READS", Map.of());

        assertThatThrownBy(() -> edge.property(null, String.class))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("key is required");
    }

    @Test
    void shouldThrowExceptionWhenPropertyTypeIsNull() {
        DefaultEdge<Person, Book> edge = new DefaultEdge<>(new Person("Alice"), new Book("DDD"), "READS", Map.of());

        assertThatThrownBy(() -> edge.property("since", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("type is required");
    }

    // Sample domain classes
    static class Person {
        private final String name;

        Person(String name) {
            this.name = name;
        }
    }

    static class Book {
        private final String title;

        Book(String title) {
            this.title = title;
        }
    }
}