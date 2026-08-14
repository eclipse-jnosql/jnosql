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
package org.eclipse.jnosql.mapping.column;

import org.eclipse.jnosql.mapping.DatabaseQualifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.eclipse.jnosql.mapping.DatabaseType.COLUMN;
import static org.eclipse.jnosql.mapping.DatabaseType.DOCUMENT;
import static org.eclipse.jnosql.mapping.DatabaseType.GRAPH;
import static org.eclipse.jnosql.mapping.DatabaseType.KEY_VALUE;

@DisplayName("Database qualifier")
class DatabaseQualifierTest {

    @Nested
    @DisplayName("When creating a column qualifier")
    class WhenTheColumnQualifierCreation {

        @Test
        @DisplayName("Should create the default qualifier")
        void shouldCreateDefaultQualifier() {

            // When
            DatabaseQualifier qualifier = DatabaseQualifier.ofColumn();

            // Then
            assertSoftly(softly -> {
                softly.assertThat(qualifier.provider()).isEmpty();
                softly.assertThat(qualifier.value()).isEqualTo(COLUMN);
            });
        }

        @Test
        @DisplayName("Should create a provider qualifier")
        void shouldCreateProviderQualifier() {

            // Given
            String provider = "provider";

            // When
            DatabaseQualifier qualifier = DatabaseQualifier.ofColumn(provider);

            // Then
            assertSoftly(softly -> {
                softly.assertThat(qualifier.provider()).isEqualTo(provider);
                softly.assertThat(qualifier.value()).isEqualTo(COLUMN);
            });
        }

        @Test
        @DisplayName("Should require a provider")
        void shouldRequireProvider() {

            // When / Then
            assertThatExceptionOfType(NullPointerException.class)
                    .isThrownBy(() -> DatabaseQualifier.ofColumn(null));
        }
    }

    @Nested
    @DisplayName("When creating a document qualifier")
    class WhenTheDocumentQualifierCreation {

        @Test
        @DisplayName("Should create the default qualifier")
        void shouldCreateDefaultQualifier() {

            // When
            DatabaseQualifier qualifier = DatabaseQualifier.ofDocument();

            // Then
            assertSoftly(softly -> {
                softly.assertThat(qualifier.provider()).isEmpty();
                softly.assertThat(qualifier.value()).isEqualTo(DOCUMENT);
            });
        }

        @Test
        @DisplayName("Should create a provider qualifier")
        void shouldCreateProviderQualifier() {

            // Given
            String provider = "provider";

            // When
            DatabaseQualifier qualifier = DatabaseQualifier.ofDocument(provider);

            // Then
            assertSoftly(softly -> {
                softly.assertThat(qualifier.provider()).isEqualTo(provider);
                softly.assertThat(qualifier.value()).isEqualTo(DOCUMENT);
            });
        }

        @Test
        @DisplayName("Should require a provider")
        void shouldRequireProvider() {

            // When / Then
            assertThatExceptionOfType(NullPointerException.class)
                    .isThrownBy(() -> DatabaseQualifier.ofDocument(null));
        }
    }

    @Nested
    @DisplayName("When creating a key-value qualifier")
    class WhenTheKeyValueQualifierCreation {

        @Test
        @DisplayName("Should create the default qualifier")
        void shouldCreateDefaultQualifier() {

            // When
            DatabaseQualifier qualifier = DatabaseQualifier.ofKeyValue();

            // Then
            assertSoftly(softly -> {
                softly.assertThat(qualifier.provider()).isEmpty();
                softly.assertThat(qualifier.value()).isEqualTo(KEY_VALUE);
            });
        }

        @Test
        @DisplayName("Should create a provider qualifier")
        void shouldCreateProviderQualifier() {

            // Given
            String provider = "provider";

            // When
            DatabaseQualifier qualifier = DatabaseQualifier.ofKeyValue(provider);

            // Then
            assertSoftly(softly -> {
                softly.assertThat(qualifier.provider()).isEqualTo(provider);
                softly.assertThat(qualifier.value()).isEqualTo(KEY_VALUE);
            });
        }

        @Test
        @DisplayName("Should require a provider")
        void shouldRequireProvider() {

            // When / Then
            assertThatExceptionOfType(NullPointerException.class)
                    .isThrownBy(() -> DatabaseQualifier.ofKeyValue(null));
        }
    }

    @Nested
    @DisplayName("When creating a graph qualifier")
    class WhenTheGraphQualifierCreation {

        @Test
        @DisplayName("Should create the default qualifier")
        void shouldCreateDefaultQualifier() {

            // When
            DatabaseQualifier qualifier = DatabaseQualifier.ofGraph();

            // Then
            assertSoftly(softly -> {
                softly.assertThat(qualifier.provider()).isEmpty();
                softly.assertThat(qualifier.value()).isEqualTo(GRAPH);
            });
        }

        @Test
        @DisplayName("Should create a provider qualifier")
        void shouldCreateProviderQualifier() {

            // Given
            String provider = "provider";

            // When
            DatabaseQualifier qualifier = DatabaseQualifier.ofGraph(provider);

            // Then
            assertSoftly(softly -> {
                softly.assertThat(qualifier.provider()).isEqualTo(provider);
                softly.assertThat(qualifier.value()).isEqualTo(GRAPH);
            });
        }

        @Test
        @DisplayName("Should require a provider")
        void shouldRequireProvider() {

            // When / Then
            assertThatExceptionOfType(NullPointerException.class)
                    .isThrownBy(() -> DatabaseQualifier.ofGraph(null));
        }
    }
}