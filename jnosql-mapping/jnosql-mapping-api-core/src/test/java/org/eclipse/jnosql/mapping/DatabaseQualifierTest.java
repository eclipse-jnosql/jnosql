/*
 *  Copyright (c) 2022 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.mapping;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.eclipse.jnosql.mapping.DatabaseType.COLUMN;
import static org.eclipse.jnosql.mapping.DatabaseType.DOCUMENT;
import static org.eclipse.jnosql.mapping.DatabaseType.GRAPH;
import static org.eclipse.jnosql.mapping.DatabaseType.KEY_VALUE;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.SoftAssertions.assertSoftly;


class DatabaseQualifierTest {

    @Nested
    @DisplayName("When the column qualifier is requested")
    class WhenTheColumnQualifierIsRequested {

        @Test
        @DisplayName("Should return the default column qualifier")
        void shouldReturnTheDefaultColumnQualifier() {
            DatabaseQualifier qualifier = DatabaseQualifier.ofColumn();

            assertSoftly(softly -> {
                softly.assertThat(qualifier.provider()).isEmpty();
                softly.assertThat(qualifier.value()).isEqualTo(COLUMN);
            });
        }

        @Test
        @DisplayName("Should return a column qualifier with the given provider")
        void shouldReturnAColumnQualifierWithTheGivenProvider() {
            String provider = "provider";
            DatabaseQualifier qualifier = DatabaseQualifier.ofColumn(provider);

            assertSoftly(softly -> {
                softly.assertThat(qualifier.provider()).isEqualTo(provider);
                softly.assertThat(qualifier.value()).isEqualTo(COLUMN);
            });
        }

        @Test
        @DisplayName("Should reject a null provider")
        void shouldRejectANullProvider() {
            assertThatNullPointerException().isThrownBy(() -> DatabaseQualifier.ofColumn(null));
        }
    }

    @Nested
    @DisplayName("When the document qualifier is requested")
    class WhenTheDocumentQualifierIsRequested {

        @Test
        @DisplayName("Should return the default document qualifier")
        void shouldReturnTheDefaultDocumentQualifier() {
            DatabaseQualifier qualifier = DatabaseQualifier.ofDocument();

            assertSoftly(softly -> {
                softly.assertThat(qualifier.provider()).isEmpty();
                softly.assertThat(qualifier.value()).isEqualTo(DOCUMENT);
            });
        }

        @Test
        @DisplayName("Should return a document qualifier with the given provider")
        void shouldReturnADocumentQualifierWithTheGivenProvider() {
            String provider = "provider";
            DatabaseQualifier qualifier = DatabaseQualifier.ofDocument(provider);

            assertSoftly(softly -> {
                softly.assertThat(qualifier.provider()).isEqualTo(provider);
                softly.assertThat(qualifier.value()).isEqualTo(DOCUMENT);
            });
        }

        @Test
        @DisplayName("Should reject a null provider")
        void shouldRejectANullProvider() {
            assertThatNullPointerException().isThrownBy(() -> DatabaseQualifier.ofDocument(null));
        }
    }

    @Nested
    @DisplayName("When the key value qualifier is requested")
    class WhenTheKeyValueQualifierIsRequested {

        @Test
        @DisplayName("Should reject a null provider")
        void shouldRejectANullProvider() {
            assertThatNullPointerException().isThrownBy(() -> DatabaseQualifier.ofKeyValue(null));
        }

        @Test
        @DisplayName("Should return a key value qualifier with the given provider")
        void shouldReturnAKeyValueQualifierWithTheGivenProvider() {
            String provider = "provider";
            DatabaseQualifier qualifier = DatabaseQualifier.ofKeyValue(provider);

            assertSoftly(softly -> {
                softly.assertThat(qualifier.provider()).isEqualTo(provider);
                softly.assertThat(qualifier.value()).isEqualTo(KEY_VALUE);
            });
        }

        @Test
        @DisplayName("Should return the default key value qualifier")
        void shouldReturnTheDefaultKeyValueQualifier() {
            DatabaseQualifier qualifier = DatabaseQualifier.ofKeyValue();

            assertSoftly(softly -> {
                softly.assertThat(qualifier.provider()).isEmpty();
                softly.assertThat(qualifier.value()).isEqualTo(KEY_VALUE);
            });
        }
    }

    @Nested
    @DisplayName("When the graph qualifier is requested")
    class WhenTheGraphQualifierIsRequested {

        @Test
        @DisplayName("Should reject a null provider")
        void shouldRejectANullProvider() {
            assertThatNullPointerException().isThrownBy(() -> DatabaseQualifier.ofGraph(null));
        }

        @Test
        @DisplayName("Should return a graph qualifier with the given provider")
        void shouldReturnAGraphQualifierWithTheGivenProvider() {
            String provider = "provider";
            DatabaseQualifier qualifier = DatabaseQualifier.ofGraph(provider);

            assertSoftly(softly -> {
                softly.assertThat(qualifier.provider()).isEqualTo(provider);
                softly.assertThat(qualifier.value()).isEqualTo(GRAPH);
            });
        }

        @Test
        @DisplayName("Should return the default graph qualifier")
        void shouldReturnTheDefaultGraphQualifier() {
            DatabaseQualifier qualifier = DatabaseQualifier.ofGraph();

            assertSoftly(softly -> {
                softly.assertThat(qualifier.provider()).isEmpty();
                softly.assertThat(qualifier.value()).isEqualTo(GRAPH);
            });
        }
    }
}