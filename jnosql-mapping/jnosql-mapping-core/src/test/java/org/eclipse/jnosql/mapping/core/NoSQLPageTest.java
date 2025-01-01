/*
 *  Copyright (c) 2023 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.mapping.core;

import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.mapping.core.entities.Person;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NoSQLPageTest {

    @Test
    void shouldReturnErrorWhenNull() {
        assertThrows(NullPointerException.class, ()->
                NoSQLPage.of(Collections.emptyList(), null));

        assertThrows(NullPointerException.class, ()->
                NoSQLPage.of(null, PageRequest.ofPage(2)));
    }

    @Test
    void shouldReturnUnsupportedOperation() {
        Page<Person> page = NoSQLPage.of(Collections.singletonList(Person.builder().withName("Otavio").build()),
                PageRequest.ofPage(2));

        assertThrows(UnsupportedOperationException.class, page::totalPages);
        assertThrows(UnsupportedOperationException.class, page::totalElements);
        assertThrows(UnsupportedOperationException.class, page::hasTotals);
    }

    @Test
    void shouldReturnTrueHasNext(){
        Page<Person> page = NoSQLPage.of(Collections.singletonList(Person.builder().withName("Otavio").build()),
                PageRequest.ofPage(2).size(1));

        org.assertj.core.api.Assertions.assertThat(page.hasNext()).isTrue();
    }

    @Test
    void shouldReturnFalseHasNextWhenIsEmpty(){
        var page = NoSQLPage.of(Collections.emptyList(), PageRequest.ofPage(2).size(1));
        org.assertj.core.api.Assertions.assertThat(page.hasNext()).isFalse();
    }

    @Test
    void shouldReturnFalseHasNextWhenElementHasLessThanSize(){
        var page = NoSQLPage.of(Collections.singletonList(Person.builder().withName("Otavio").build()),
                PageRequest.ofPage(2).size(2));
        org.assertj.core.api.Assertions.assertThat(page.hasNext()).isFalse();
    }

    @Test
    void shouldReturnTrueHasPrevious(){
        Page<Person> page = NoSQLPage.of(Collections.singletonList(Person.builder().withName("Otavio").build()),
                PageRequest.ofPage(2));

        org.assertj.core.api.Assertions.assertThat(page.hasPrevious()).isTrue();
    }

    @Test
    void shouldReturnHasContent() {

        Page<Person> page = NoSQLPage.of(Collections.singletonList(Person.builder().withName("Otavio").build()),
                PageRequest.ofPage(2));

        Assertions.assertTrue(page.hasContent());
        page = NoSQLPage.of(Collections.emptyList(),
                PageRequest.ofPage(2));
        Assertions.assertFalse(page.hasContent());
    }

    @Test
    void shouldNumberOfElements() {

        Page<Person> page = NoSQLPage.of(Collections.singletonList(Person.builder().withName("Otavio").build()),
                PageRequest.ofPage(2));

        assertEquals(1, page.numberOfElements());
    }

    @Test
    void shouldIterator() {
        Page<Person> page = NoSQLPage.of(Collections.singletonList(Person.builder().withName("Otavio").build()),
                PageRequest.ofPage(2));
        Assertions.assertNotNull(page.iterator());
    }

    @Test
    void shouldPageRequest() {
        Page<Person> page = NoSQLPage.of(Collections.singletonList(Person.builder().withName("Otavio").build()),
                PageRequest.ofPage(2));
        PageRequest pageRequest = page.pageRequest();
        Assertions.assertNotNull(pageRequest);
        assertEquals(PageRequest.ofPage(2), pageRequest);
    }

    @Test
    void shouldNextPageRequest() {
        Page<Person> page = NoSQLPage.of(Collections.singletonList(Person.builder().withName("Otavio").build()),
                PageRequest.ofPage(2));
        PageRequest pageRequest = page.nextPageRequest();
        assertEquals(PageRequest.ofPage(3), pageRequest);
    }

    @Test
    void shouldThrowNullPointerExceptionWhenPageRequestIsNull() {
        assertThrows(NullPointerException.class, () -> NoSQLPage.skip(null));
    }

    @Test
    void shouldCalculateSkip() {
        long skipValue = NoSQLPage.skip(PageRequest.ofPage(2).size(10));
        assertEquals(10, skipValue);
    }

    @Test
    void shouldCalculateSkipForFirstPage() {
        // Create a PageRequest with page=1 and size=5
        long skipValue = NoSQLPage.skip(PageRequest.ofPage(1).size(5));
        assertEquals(0, skipValue);
    }

    @Test
    void shouldToString(){
        Page<Person> page = NoSQLPage.of(Collections.singletonList(Person.builder().withName("Otavio").build()),
                PageRequest.ofPage(2));

        assertThat(page.toString()).isNotBlank();
    }

    @Test
    void shouldEqualsHasCode(){
        Page<Person> page = NoSQLPage.of(Collections.singletonList(Person.builder().withName("Otavio").build()),
                PageRequest.ofPage(2));
        Page<Person> page2 = NoSQLPage.of(Collections.singletonList(Person.builder().withName("Otavio").build()),
                PageRequest.ofPage(2));

        assertEquals(page, page2);
        assertEquals(page.hashCode(), page2.hashCode());

    }

    @Test
    void shouldNext(){
        Page<Person> page = NoSQLPage.of(Collections.singletonList(Person.builder().withName("Otavio").build()),
                PageRequest.ofPage(2));
        PageRequest pageRequest = page.nextPageRequest();

        SoftAssertions.assertSoftly(soft ->{
            soft.assertThat(pageRequest.page()).isEqualTo(3);
            soft.assertThat(pageRequest.size()).isEqualTo(10);
        });
    }

    @Test
    void shouldPrevious(){
        Page<Person> page = NoSQLPage.of(Collections.singletonList(Person.builder().withName("Otavio").build()),
                PageRequest.ofPage(2));
        PageRequest pageRequest = page.previousPageRequest();

        SoftAssertions.assertSoftly(soft ->{
            soft.assertThat(pageRequest.page()).isEqualTo(1);
            soft.assertThat(pageRequest.size()).isEqualTo(10);
        });
    }

}