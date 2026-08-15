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
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

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
    void shouldRejectNullTotalSupplier() {
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> NoSQLPage.of(Collections.emptyList(), PageRequest.ofPage(1), null));

        assertEquals("totalSupplier is required", exception.getMessage());
    }

    @Test
    void shouldReportUnavailableTotalsForCompatibilityFactory() {
        Page<Person> page = NoSQLPage.of(Collections.singletonList(Person.builder().withName("Otavio").build()),
                PageRequest.ofPage(2));

        Assertions.assertFalse(page.hasTotals());
        assertThrows(IllegalStateException.class, page::totalPages);
        assertThrows(IllegalStateException.class, page::totalElements);
    }

    @Test
    void shouldLoadAndCacheRequestedTotals() {
        AtomicInteger invocations = new AtomicInteger();
        Page<Person> page = NoSQLPage.of(Collections.singletonList(Person.builder().withName("Otavio").build()),
                PageRequest.ofPage(2, 10, true), () -> {
                    invocations.incrementAndGet();
                    return 21L;
                });

        assertEquals(0, invocations.get());
        assertEquals(21L, page.totalElements());
        assertEquals(3L, page.totalPages());
        Assertions.assertTrue(page.hasTotals());
        Assertions.assertTrue(page.hasNext());
        assertEquals(1, invocations.get());
    }

    @Test
    void shouldCalculateLargeTotalPagesWithoutLosingPrecision() {
        Page<Person> page = NoSQLPage.of(Collections.emptyList(),
                PageRequest.ofPage(1, 10, true), () -> Long.MAX_VALUE);

        assertEquals(Long.MAX_VALUE / 10 + 1, page.totalPages());
    }

    @Test
    void shouldNotLoadTotalsWhenRequestDisablesThem() {
        AtomicInteger invocations = new AtomicInteger();
        PageRequest request = PageRequest.ofPage(2).size(1).withoutTotal();
        Page<Person> page = NoSQLPage.of(Collections.singletonList(Person.builder().withName("Otavio").build()),
                request, () -> {
                    invocations.incrementAndGet();
                    return 20L;
                });

        Assertions.assertFalse(page.hasTotals());
        Assertions.assertTrue(page.hasNext());
        assertThrows(IllegalStateException.class, page::totalElements);
        assertThrows(IllegalStateException.class, page::totalPages);
        Assertions.assertFalse(page.nextPageRequest().requestTotal());
        assertEquals(0, invocations.get());
    }

    @Test
    void shouldHandleAndCacheUnsupportedProviderCount() {
        AtomicInteger invocations = new AtomicInteger();
        Page<Person> page = NoSQLPage.of(Collections.singletonList(Person.builder().withName("Otavio").build()),
                PageRequest.ofPage(2, 1, true), () -> {
                    invocations.incrementAndGet();
                    throw new UnsupportedOperationException("count is not supported");
                });

        Assertions.assertFalse(page.hasTotals());
        assertThrows(IllegalStateException.class, page::totalElements);
        assertThrows(IllegalStateException.class, page::totalPages);
        Assertions.assertTrue(page.hasNext());
        assertThat(page.content()).hasSize(1);
        assertEquals(1, invocations.get());
    }

    @Test
    void shouldUseKnownTotalsForNavigation() {
        Page<Person> page = NoSQLPage.of(Collections.singletonList(Person.builder().withName("Otavio").build()),
                PageRequest.ofPage(2, 10, true), () -> 11L);

        Assertions.assertFalse(page.hasNext());
        Assertions.assertTrue(page.hasPrevious());
        assertThrows(NoSuchElementException.class, page::nextPageRequest);

        Page<Person> firstPage = NoSQLPage.of(Collections.emptyList(),
                PageRequest.ofPage(1, 10, true), () -> 0L);
        Assertions.assertFalse(firstPage.hasPrevious());
        assertThrows(NoSuchElementException.class, firstPage::previousPageRequest);
    }

    @Test
    void shouldRejectNextPageRequestFromShortPageWithoutTotals() {
        AtomicInteger invocations = new AtomicInteger();
        Page<Person> page = NoSQLPage.of(Collections.singletonList(Person.builder().withName("Otavio").build()),
                PageRequest.ofPage(1).size(10).withoutTotal(), () -> {
                    invocations.incrementAndGet();
                    return 30L;
                });

        Assertions.assertFalse(page.hasNext());
        assertThrows(NoSuchElementException.class, page::nextPageRequest);
        assertEquals(0, invocations.get());
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
                PageRequest.ofPage(2).size(1));
        PageRequest pageRequest = page.nextPageRequest();
        assertEquals(PageRequest.ofPage(3).size(1), pageRequest);
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
                PageRequest.ofPage(2).size(1));
        PageRequest pageRequest = page.nextPageRequest();

        SoftAssertions.assertSoftly(soft ->{
            soft.assertThat(pageRequest.page()).isEqualTo(3);
            soft.assertThat(pageRequest.size()).isEqualTo(1);
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
