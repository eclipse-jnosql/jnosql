/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License 2.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *   and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 */
package org.eclipse.jnosql.mapping.semistructured;

import jakarta.data.page.PageRequest;
import jakarta.data.page.impl.CursoredPageRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

class MappedCursoredPageTest {

    @Nested
    @DisplayName("When mapping cursor page content")
    class WhenTheContentIsMapped {

        @Test
        @DisplayName("Should preserve the page behavior")
        void shouldPreserveThePageBehavior() {
            var cursor = PageRequest.Cursor.forKey("Ada");
            var pageRequest = PageRequest.ofPage(2).size(1).withTotal();
            var nextPageRequest = PageRequest.afterCursor(cursor, 3, 1, true);
            var previousPageRequest = PageRequest.beforeCursor(cursor, 1, 1, true);
            var delegate = new CursoredPageRecord<>(
                    List.of(1), List.of(cursor), 3, pageRequest, nextPageRequest, previousPageRequest);

            var page = new MappedCursoredPage<>(List.of("Ada"), delegate);

            assertSoftly(softly -> {
                softly.assertThat(page.content()).as("mapped content").containsExactly("Ada");
                softly.assertThat(page).as("iterable content").containsExactly("Ada");
                softly.assertThat(page.hasContent()).as("content availability").isTrue();
                softly.assertThat(page.numberOfElements()).as("number of elements").isOne();
                softly.assertThat(page.pageRequest()).as("current page request").isEqualTo(pageRequest);
                softly.assertThat(page.hasNext()).as("next page availability").isTrue();
                softly.assertThat(page.nextPageRequest()).as("next page request").isEqualTo(nextPageRequest);
                softly.assertThat(page.hasPrevious()).as("previous page availability").isTrue();
                softly.assertThat(page.previousPageRequest()).as("previous page request")
                        .isEqualTo(previousPageRequest);
                softly.assertThat(page.cursor(0)).as("element cursor").isEqualTo(cursor);
                softly.assertThat(page.hasTotals()).as("total availability").isTrue();
                softly.assertThat(page.totalElements()).as("total elements").isEqualTo(3);
                softly.assertThat(page.totalPages()).as("total pages").isEqualTo(3);
            });
        }

        @Test
        @DisplayName("Should copy the mapped content")
        void shouldCopyTheMappedContent() {
            var content = new ArrayList<>(List.of("Ada"));
            var delegate = new CursoredPageRecord<>(
                    List.of(1), List.of(PageRequest.Cursor.forKey("Ada")), -1,
                    PageRequest.ofSize(1), true, true);

            var page = new MappedCursoredPage<>(content, delegate);
            content.clear();

            assertSoftly(softly -> {
                softly.assertThat(page.content()).as("copied content").containsExactly("Ada");
                softly.assertThatThrownBy(() -> page.content().add("Grace"))
                        .as("immutable content")
                        .isInstanceOf(UnsupportedOperationException.class);
            });
        }
    }

    @Nested
    @DisplayName("When page metadata is unavailable")
    class WhenTheMetadataIsUnavailable {

        @Test
        @DisplayName("Should preserve unsupported cursor navigation")
        void shouldPreserveUnsupportedCursorNavigation() {
            var pageRequest = PageRequest.ofSize(1);
            var nextPageRequest = PageRequest.afterCursor(PageRequest.Cursor.forKey("Ada"), 2, 1, false);
            var delegate = new CursoredPageRecord<>(
                    List.of(1), List.of(), -1, pageRequest, nextPageRequest, null);

            var page = new MappedCursoredPage<>(List.of("Ada"), delegate);

            assertSoftly(softly -> {
                softly.assertThat(page.hasNext()).as("next page availability").isTrue();
                softly.assertThatThrownBy(page::nextPageRequest)
                        .as("unsupported cursor navigation")
                        .isInstanceOf(UnsupportedOperationException.class);
            });
        }

        @Test
        @DisplayName("Should preserve unavailable totals")
        void shouldPreserveUnavailableTotals() {
            var delegate = new CursoredPageRecord<>(
                    List.of(1), List.of(PageRequest.Cursor.forKey("Ada")), -1,
                    PageRequest.ofSize(1), true, true);

            var page = new MappedCursoredPage<>(List.of("Ada"), delegate);

            assertSoftly(softly -> {
                softly.assertThat(page.hasTotals()).as("total availability").isFalse();
                softly.assertThatIllegalStateException()
                        .as("unavailable total elements")
                        .isThrownBy(page::totalElements);
                softly.assertThatIllegalStateException()
                        .as("unavailable total pages")
                        .isThrownBy(page::totalPages);
            });
        }
    }

    @Nested
    @DisplayName("When creating a mapped cursor page")
    class WhenThePageIsCreated {

        @Test
        @DisplayName("Should reject a null delegate")
        void shouldRejectNullDelegate() {
            assertThatNullPointerException()
                    .as("null delegate")
                    .isThrownBy(() -> new MappedCursoredPage<>(List.of("Ada"), null))
                    .withMessage("delegate is required");
        }
    }
}
