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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NoSQLPageTest {


    @Nested
    @DisplayName("When creating a page")
    class WhenCreatePage {

        @Test
        @DisplayName("should reject null page request")
        void shouldRejectNullPageRequest() {

            assertThatThrownBy(() ->
                    NoSQLPage.of(Collections.emptyList(), null,
                            () -> {
                                throw new UnsupportedOperationException(
                                        "JNoSQL has no support for this feature yet");
                            }))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("pageRequest is required");
        }

        @Test
        @DisplayName("should reject null entities")
        void shouldRejectNullEntities() {

            assertThatThrownBy(() ->
                    NoSQLPage.of(null, PageRequest.ofPage(1), () -> 10))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("entities is required");
        }
    }

    @Nested
    @DisplayName("When getting total elements")
    class WhenGetTotalElements {

        @Test
        @DisplayName("should return total elements")
        void shouldReturnTotalElements() {

            Page<Person> page = pageWithTotals(20L, 10);

            assertThat(page.totalElements())
                    .isEqualTo(20L);
        }

        @Test
        @DisplayName("should throw exception when totals are unsupported")
        void shouldThrowExceptionWhenTotalsAreUnsupported() {

            Page<Person> page = unsupportedTotalsPage();

            assertThatThrownBy(page::totalElements)
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("should execute supplier only once")
        void shouldExecuteSupplierOnlyOnce() {

            AtomicInteger counter = new AtomicInteger();

            Page<Person> page = NoSQLPage.of(
                    people(),
                    PageRequest.ofPage(1),
                    () -> {
                        counter.incrementAndGet();
                        return 100L;
                    }
            );

            page.totalElements();
            page.totalElements();

            assertThat(counter.get()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("When getting total pages")
    class WhenGetTotalPages {

        @Test
        @DisplayName("should calculate total pages")
        void shouldCalculateTotalPages() {

            Page<Person> page = pageWithTotals(25L, 10);

            assertThat(page.totalPages())
                    .isEqualTo(3L);
        }

        @Test
        @DisplayName("should round total pages")
        void shouldRoundTotalPages() {

            Page<Person> page = pageWithTotals(21L, 10);

            assertThat(page.totalPages())
                    .isEqualTo(3L);
        }

        @Test
        @DisplayName("should return zero when there are no elements")
        void shouldReturnZeroWhenThereAreNoElements() {

            Page<Person> page = pageWithTotals(0L, 10);

            assertThat(page.totalPages())
                    .isZero();
        }

        @Test
        @DisplayName("should throw exception when totals are unsupported")
        void shouldThrowExceptionWhenTotalsAreUnsupported() {

            Page<Person> page = unsupportedTotalsPage();

            assertThatThrownBy(page::totalPages)
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("When checking totals availability")
    class WhenCheckTotalsAvailability {

        @Test
        @DisplayName("should return true when totals are supported")
        void shouldReturnTrueWhenTotalsAreSupported() {

            Page<Person> page = pageWithTotals(10L, 10);

            assertThat(page.hasTotals()).isTrue();
        }

        @Test
        @DisplayName("should return false when totals are unsupported")
        void shouldReturnFalseWhenTotalsAreUnsupported() {

            Page<Person> page = unsupportedTotalsPage();

            assertThat(page.hasTotals()).isFalse();
        }
    }

    @Nested
    @DisplayName("When checking next page")
    class WhenCheckNextPage {

        @Test
        @DisplayName("should return true when totals indicate another page")
        void shouldReturnTrueWhenTotalsIndicateAnotherPage() {

            Page<Person> page = NoSQLPage.of(
                    people(),
                    PageRequest.ofPage(1).size(10),
                    () -> 30L
            );

            assertThat(page.hasNext()).isTrue();
        }

        @Test
        @DisplayName("should return false when current page is the last page")
        void shouldReturnFalseWhenCurrentPageIsTheLastPage() {

            Page<Person> page = NoSQLPage.of(
                    people(),
                    PageRequest.ofPage(3).size(10),
                    () -> 30L
            );

            assertThat(page.hasNext()).isFalse();
        }

        @Test
        @DisplayName("should use heuristic navigation when totals are unsupported")
        void shouldUseHeuristicNavigationWhenTotalsAreUnsupported() {

            Page<Person> page = NoSQLPage.of(
                    people(),
                    PageRequest.ofPage(1).size(1),
                    () -> {
                        throw new UnsupportedOperationException(
                                "JNoSQL has no support for this feature yet");
                    }
            );

            assertThat(page.hasNext()).isTrue();
        }

        @Test
        @DisplayName("should return false when page content is smaller than requested size")
        void shouldReturnFalseWhenPageContentIsSmallerThanRequestedSize() {

            Page<Person> page = NoSQLPage.of(
                    people(),
                    PageRequest.ofPage(1).size(10),
                    () -> {
                        throw new UnsupportedOperationException(
                                "JNoSQL has no support for this feature yet");
                    }
            );

            assertThat(page.hasNext()).isFalse();
        }
    }

    @Nested
    @DisplayName("When checking previous page")
    class WhenCheckPreviousPage {

        @Test
        @DisplayName("should return true when current page is greater than one")
        void shouldReturnTrueWhenCurrentPageIsGreaterThanOne() {

            Page<Person> page = page(2);

            assertThat(page.hasPrevious()).isTrue();
        }

        @Test
        @DisplayName("should return false when current page is the first page")
        void shouldReturnFalseWhenCurrentPageIsTheFirstPage() {

            Page<Person> page = page(1);

            assertThat(page.hasPrevious()).isFalse();
        }
    }

    @Nested
    @DisplayName("When requesting next page")
    class WhenRequestNextPage {

        @Test
        @DisplayName("should return next page request when totals support navigation")
        void shouldReturnNextPageRequestWhenTotalsSupportNavigation() {

            Page<Person> page = NoSQLPage.of(
                    people(),
                    PageRequest.ofPage(1).size(10),
                    () -> 30L
            );

            PageRequest next = page.nextPageRequest();

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(next.page()).isEqualTo(2);
                softly.assertThat(next.size()).isEqualTo(10);
            });
        }

        @Test
        @DisplayName("should throw exception when totals indicate there is no next page")
        void shouldThrowExceptionWhenTotalsIndicateThereIsNoNextPage() {

            Page<Person> page = NoSQLPage.of(
                    people(),
                    PageRequest.ofPage(3).size(10),
                    () -> 30L
            );

            assertThatThrownBy(page::nextPageRequest)
                    .isInstanceOf(NoSuchElementException.class)
                    .hasMessageContaining("Current page: 3")
                    .hasMessageContaining("total pages: 3");
        }

        @Test
        @DisplayName("should allow exploratory navigation when totals are unsupported")
        void shouldAllowExploratoryNavigationWhenTotalsAreUnsupported() {

            Page<Person> page = NoSQLPage.of(
                    people(),
                    PageRequest.ofPage(1).size(1),
                    () -> {
                        throw new UnsupportedOperationException(
                                "JNoSQL has no support for this feature yet");
                    }
            );

            PageRequest next = page.nextPageRequest();

            assertThat(next.page()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("When requesting previous page")
    class WhenRequestPreviousPage {

        @Test
        @DisplayName("should return previous page request")
        void shouldReturnPreviousPageRequest() {

            Page<Person> page = page(2);

            PageRequest previous = page.previousPageRequest();

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(previous.page()).isEqualTo(1);
                softly.assertThat(previous.size()).isEqualTo(10);
            });
        }

        @Test
        @DisplayName("should throw exception when previous page does not exist")
        void shouldThrowExceptionWhenPreviousPageDoesNotExist() {

            Page<Person> page = page(1);

            assertThatThrownBy(page::previousPageRequest)
                    .isInstanceOf(NoSuchElementException.class)
                    .hasMessageContaining("Current page: 1")
                    .hasMessageContaining("Page numbers start at 1");
        }
    }

    @Nested
    @DisplayName("When reading content")
    class WhenReadContent {

        @Test
        @DisplayName("should return content")
        void shouldReturnContent() {

            Page<Person> page = page(1);

            assertThat(page.content())
                    .hasSize(1);
        }

        @Test
        @DisplayName("should identify content existence")
        void shouldIdentifyContentExistence() {

            Page<Person> page = page(1);

            assertThat(page.hasContent()).isTrue();
        }

        @Test
        @DisplayName("should support empty content")
        void shouldSupportEmptyContent() {

            Page<Person> page = NoSQLPage.of(
                    Collections.emptyList(),
                    PageRequest.ofPage(1),
                    () -> {
                    throw new UnsupportedOperationException(
                            "JNoSQL has no support for this feature yet");
                    }
            );

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(page.hasContent()).isFalse();
                softly.assertThat(page.numberOfElements()).isZero();
            });
        }

        @Test
        @DisplayName("should expose immutable content")
        void shouldExposeImmutableContent() {

            Page<Person> page = page(1);

            assertThatThrownBy(() ->
                    page.content().add(person()))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("should expose iterator")
        void shouldExposeIterator() {

            Page<Person> page = page(1);

            assertThat(page.iterator()).isNotNull();
        }
    }

    @Nested
    @DisplayName("When calculating skip")
    class WhenCalculateSkip {

        @Test
        @DisplayName("should calculate skip")
        void shouldCalculateSkip() {

            long skip = NoSQLPage.skip(
                    PageRequest.ofPage(2).size(10));

            assertThat(skip).isEqualTo(10);
        }

        @Test
        @DisplayName("should calculate zero for first page")
        void shouldCalculateZeroForFirstPage() {

            long skip = NoSQLPage.skip(
                    PageRequest.ofPage(1).size(10));

            assertThat(skip).isZero();
        }

        @Test
        @DisplayName("should reject null page request")
        void shouldRejectNullPageRequest() {

            assertThatThrownBy(() -> NoSQLPage.skip(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("pageRequest is required");
        }
    }

    @Nested
    @DisplayName("When comparing pages")
    class WhenComparePages {

        @Test
        @DisplayName("should implement equals and hashcode")
        void shouldImplementEqualsAndHashcode() {

            Page<Person> first = page(1);
            Page<Person> second = page(1);

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(first).isEqualTo(second);
                softly.assertThat(first.hashCode())
                        .isEqualTo(second.hashCode());
            });
        }

        @Test
        @DisplayName("should implement toString")
        void shouldImplementToString() {

            Page<Person> page = page(1);

            assertThat(page.toString()).isNotBlank();
        }
    }

    private static Page<Person> page(long page) {
        return NoSQLPage.of(
                people(),
                PageRequest.ofPage(page),
                () -> {
                    throw new UnsupportedOperationException(
                            "JNoSQL has no support for this feature yet");
                }
        );
    }

    private static Page<Person> pageWithTotals(long total, int size) {
        return NoSQLPage.of(
                people(),
                PageRequest.ofPage(1).size(size),
                () -> total
        );
    }

    private static Page<Person> unsupportedTotalsPage() {
        return NoSQLPage.of(
                people(),
                PageRequest.ofPage(1),
                () -> {
                    throw new UnsupportedOperationException();
                }
        );
    }

    private static List<Person> people() {
        return Collections.singletonList(person());
    }

    private static Person person() {
        return Person.builder()
                .withName("Otavio")
                .build();
    }
}