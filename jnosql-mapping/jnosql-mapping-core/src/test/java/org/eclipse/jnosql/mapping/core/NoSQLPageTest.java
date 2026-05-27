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
                    NoSQLPage.of(Collections.emptyList(), null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("pageRequest is required");
        }

        @Test
        @DisplayName("should reject null entities")
        void shouldRejectNullEntities() {

            assertThatThrownBy(() ->
                    NoSQLPage.of(null, PageRequest.ofPage(1)))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("entities is required");
        }
    }


    @Nested
    @DisplayName("When getting total elements")
    class WhenGetTotalElements {

        @Test
        @DisplayName("should return false when totals are unsupported")
        void shouldReturnFalseWhenTotalsAreUnsupported() {

            Page<Person> page = unsupportedTotalsPage();

            assertThat(page.hasTotals()).isFalse();
        }

        @Test
        @DisplayName("should throw exception when total elements are unsupported")
        void shouldThrowExceptionWhenTotalElementsAreUnsupported() {

            Page<Person> page = unsupportedTotalsPage();

            assertThatThrownBy(page::totalElements)
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("should calculate total pages")
        void shouldCalculateTotalPages() {

            Page<Person> page = pageWithTotals(25L, 10);

            assertThat(page.totalPages())
                    .isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Navigation")
    class Navigation {

        @Test
        @DisplayName("should identify next page")
        void shouldIdentifyNextPage() {

            Page<Person> page = pageWithTotals(30L, 10);

            assertThat(page.hasNext()).isTrue();
        }

        @Test
        @DisplayName("should identify last page")
        void shouldIdentifyLastPage() {

            Page<Person> page = NoSQLPage.of(
                    people(),
                    PageRequest.ofPage(3).size(10),
                    () -> 30L
            );

            assertThat(page.hasNext()).isFalse();
        }

        @Test
        @DisplayName("should identify previous page")
        void shouldIdentifyPreviousPage() {

            Page<Person> page = page(2);

            assertThat(page.hasPrevious()).isTrue();
        }

        @Test
        @DisplayName("should identify first page")
        void shouldIdentifyFirstPage() {

            Page<Person> page = page(1);

            assertThat(page.hasPrevious()).isFalse();
        }

        @Test
        @DisplayName("should navigate to next page")
        void shouldNavigateToNextPage() {

            Page<Person> page = page(2);

            PageRequest next = page.nextPageRequest();

            assertThat(next.page()).isEqualTo(3);
        }

        @Test
        @DisplayName("should navigate to previous page")
        void shouldNavigateToPreviousPage() {

            Page<Person> page = page(2);

            PageRequest previous = page.previousPageRequest();

            assertThat(previous.page()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Content")
    class Content {

        @Test
        @DisplayName("should contain entities")
        void shouldContainEntities() {

            Page<Person> page = page(1);

            assertThat(page.hasContent()).isTrue();
            assertThat(page.numberOfElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("should support empty content")
        void shouldSupportEmptyContent() {

            Page<Person> page = NoSQLPage.of(
                    Collections.emptyList(),
                    PageRequest.ofPage(1)
            );

            assertThat(page.hasContent()).isFalse();
            assertThat(page.numberOfElements()).isZero();
        }

        @Test
        @DisplayName("should expose immutable content")
        void shouldExposeImmutableContent() {

            Page<Person> page = page(1);

            assertThatThrownBy(() -> page.content().add(person()))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("Identity")
    class Identity {

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

    @Nested
    @DisplayName("Skip")
    class Skip {

        @Test
        @DisplayName("should calculate skip")
        void shouldCalculateSkip() {

            long skip = NoSQLPage.skip(
                    PageRequest.ofPage(2).size(10));

            assertThat(skip).isEqualTo(10);
        }

        @Test
        @DisplayName("should reject null page request")
        void shouldRejectNullPageRequest() {

            assertThatThrownBy(() -> NoSQLPage.skip(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("pageRequest is required");
        }
    }

    private static Page<Person> page(long page) {
        return NoSQLPage.of(
                people(),
                PageRequest.ofPage(page)
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