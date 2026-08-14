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
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

class DatabaseMetadataTest {

    @Nested
    @DisplayName("When the metadata is created")
    class WhenTheMetadataIsCreated {

        @Test
        @DisplayName("Should reject a null database annotation")
        void shouldRejectANullDatabaseAnnotation() {
            assertThatNullPointerException().isThrownBy(() -> DatabaseMetadata.of(null));
        }

        @Test
        @DisplayName("Should expose type and provider from the annotation")
        void shouldExposeTypeAndProviderFromTheAnnotation() {
            Database database = Mockito.mock(Database.class);
            Mockito.when(database.value()).thenReturn(DatabaseType.COLUMN);
            Mockito.when(database.provider()).thenReturn("column");
            DatabaseMetadata metadata = DatabaseMetadata.of(database);

            assertSoftly(softly -> {
                softly.assertThat(metadata.getType()).isEqualTo(DatabaseType.COLUMN);
                softly.assertThat(metadata.getProvider()).isEqualTo("column");
            });
        }
    }

    @Nested
    @DisplayName("When the metadata is represented as text")
    class WhenTheMetadataIsRepresentedAsText {

        @Test
        @DisplayName("Should include the provider when it is present")
        void shouldIncludeTheProviderWhenItIsPresent() {
            Database database = Mockito.mock(Database.class);
            Mockito.when(database.value()).thenReturn(DatabaseType.COLUMN);
            Mockito.when(database.provider()).thenReturn("column");
            DatabaseMetadata metadata = DatabaseMetadata.of(database);

            assertThat(metadata).hasToString("COLUMN@column");
        }

        @Test
        @DisplayName("Should omit the provider when it is null")
        void shouldOmitTheProviderWhenItIsNull() {
            Database database = Mockito.mock(Database.class);
            Mockito.when(database.value()).thenReturn(DatabaseType.COLUMN);
            DatabaseMetadata metadata = DatabaseMetadata.of(database);

            assertThat(metadata).hasToString("COLUMN");
        }
    }
}