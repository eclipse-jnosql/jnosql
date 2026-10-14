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
package org.eclipse.jnosql.mapping.vector;

import jakarta.data.Limit;
import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;
import org.eclipse.jnosql.communication.semistructured.Element;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

@DisplayName("Default vector select query")
class DefaultVectorSelectQueryTest {

    private static final String ENTITY = "Article";

    private static final Vector VECTOR = DenseVector.of(
            0.12F,
            0.45F,
            0.78F
    );

    private static final Limit LIMIT = Limit.of(10);

    @Nested
    @DisplayName("When creating a vector select query")
    class WhenCreatingVectorSelectQuery {

        @Test
        @DisplayName("Should create a query with the required values")
        void shouldCreateQuery() {
            VectorSelectQuery query = new DefaultVectorSelectQuery(
                    ENTITY,
                    null,
                    LIMIT,
                    VECTOR,
                    null
            );

            assertThat(query.name()).isEqualTo(ENTITY);
        }

        @Test
        @DisplayName("Should expose the query vector")
        void shouldExposeVector() {
            VectorSelectQuery query = new DefaultVectorSelectQuery(
                    ENTITY,
                    null,
                    LIMIT,
                    VECTOR,
                    null
            );

            assertThat(query.vector()).isSameAs(VECTOR);
        }

        @Test
        @DisplayName("Should expose the maximum number of results")
        void shouldExposeLimit() {
            VectorSelectQuery query = new DefaultVectorSelectQuery(
                    ENTITY,
                    null,
                    LIMIT,
                    VECTOR,
                    null
            );

            assertThat(query.limit()).isEqualTo(10L);
        }

        @Test
        @DisplayName("Should convert the limit start position to skip")
        void shouldExposeSkip() {
            Limit limit = Limit.range(11, 20);

            VectorSelectQuery query = new DefaultVectorSelectQuery(
                    ENTITY,
                    null,
                    limit,
                    VECTOR,
                    null
            );

            assertThat(query.skip()).isEqualTo(10L);
        }

        @Test
        @DisplayName("Should return all columns")
        void shouldReturnAllColumns() {
            VectorSelectQuery query = new DefaultVectorSelectQuery(
                    ENTITY,
                    null,
                    LIMIT,
                    VECTOR,
                    null
            );

            assertThat(query.columns()).isEmpty();
        }

        @Test
        @DisplayName("Should not define sorting")
        void shouldNotDefineSorting() {
            VectorSelectQuery query = new DefaultVectorSelectQuery(
                    ENTITY,
                    null,
                    LIMIT,
                    VECTOR,
                    null
            );

            assertThat(query.sorts()).isEmpty();
        }
    }

    @Nested
    @DisplayName("When using a condition")
    class WhenUsingCondition {

        @Test
        @DisplayName("Should expose the criteria condition")
        void shouldExposeCondition() {
            CriteriaCondition condition = CriteriaCondition.eq(
                    Element.of("year", 2026)
            );

            VectorSelectQuery query = new DefaultVectorSelectQuery(
                    ENTITY,
                    condition,
                    LIMIT,
                    VECTOR,
                    null
            );

            assertThat(query.condition()).contains(condition);
        }

        @Test
        @DisplayName("Should return empty when condition is absent")
        void shouldReturnEmptyCondition() {
            VectorSelectQuery query = new DefaultVectorSelectQuery(
                    ENTITY,
                    null,
                    LIMIT,
                    VECTOR,
                    null
            );

            assertThat(query.condition()).isEmpty();
        }
    }

    @Nested
    @DisplayName("When using a threshold")
    class WhenUsingThreshold {

        @Test
        @DisplayName("Should expose the threshold")
        void shouldExposeThreshold() {
            VectorSelectQuery query = new DefaultVectorSelectQuery(
                    ENTITY,
                    null,
                    LIMIT,
                    VECTOR,
                    0.85F
            );

            assertThat(query.threshold()).contains(0.85F);
        }

        @Test
        @DisplayName("Should return empty when threshold is absent")
        void shouldReturnEmptyThreshold() {
            VectorSelectQuery query = new DefaultVectorSelectQuery(
                    ENTITY,
                    null,
                    LIMIT,
                    VECTOR,
                    null
            );

            assertThat(query.threshold()).isEmpty();
        }

        @Test
        @DisplayName("Should reject NaN threshold")
        void shouldRejectNaNThreshold() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new DefaultVectorSelectQuery(
                            ENTITY,
                            null,
                            LIMIT,
                            VECTOR,
                            Float.NaN
                    ))
                    .withMessage("threshold must be finite");
        }

        @Test
        @DisplayName("Should reject positive infinity threshold")
        void shouldRejectPositiveInfinityThreshold() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new DefaultVectorSelectQuery(
                            ENTITY,
                            null,
                            LIMIT,
                            VECTOR,
                            Float.POSITIVE_INFINITY
                    ))
                    .withMessage("threshold must be finite");
        }

        @Test
        @DisplayName("Should reject negative infinity threshold")
        void shouldRejectNegativeInfinityThreshold() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new DefaultVectorSelectQuery(
                            ENTITY,
                            null,
                            LIMIT,
                            VECTOR,
                            Float.NEGATIVE_INFINITY
                    ))
                    .withMessage("threshold must be finite");
        }
    }

    @Nested
    @DisplayName("When validating required values")
    class WhenValidatingRequiredValues {

        @Test
        @DisplayName("Should reject a null name")
        void shouldRejectNullName() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new DefaultVectorSelectQuery(
                            null,
                            null,
                            LIMIT,
                            VECTOR,
                            null
                    ))
                    .withMessage("name is required");
        }

        @Test
        @DisplayName("Should reject a blank name")
        void shouldRejectBlankName() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new DefaultVectorSelectQuery(
                            " ",
                            null,
                            LIMIT,
                            VECTOR,
                            null
                    ))
                    .withMessage("name cannot be blank");
        }

        @Test
        @DisplayName("Should reject a null limit")
        void shouldRejectNullLimit() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new DefaultVectorSelectQuery(
                            ENTITY,
                            null,
                            null,
                            VECTOR,
                            null
                    ))
                    .withMessage("limit is required");
        }

        @Test
        @DisplayName("Should reject a null vector")
        void shouldRejectNullVector() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new DefaultVectorSelectQuery(
                            ENTITY,
                            null,
                            LIMIT,
                            null,
                            null
                    ))
                    .withMessage("vector is required");
        }
    }
}