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
package org.eclipse.jnosql.mapping.core;

import org.junit.jupiter.api.Nested;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;

import org.eclipse.jnosql.mapping.Database;
import org.eclipse.jnosql.mapping.DatabaseMetadata;
import org.eclipse.jnosql.mapping.DatabaseType;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;


class DatabaseMetadataTest {





    @Nested
    @DisplayName("When the database metadata operates")
    class WhenTheDatabaseMetadataOperates {

        @DisplayName("Should return error when database is null")
        @Test
        void shouldReturnErrorWhenDatabaseIsNull() {
            assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> DatabaseMetadata.of(null));
        }
        @DisplayName("Should return metadata")
        @Test
        void shouldReturnMetadata() {
            Database database = Mockito.mock(Database.class);
            Mockito.when(database.value()).thenReturn(DatabaseType.COLUMN);
            Mockito.when(database.provider()).thenReturn("column");
            DatabaseMetadata metadata = DatabaseMetadata.of(database);
            assertThat(metadata.getType()).isEqualTo(DatabaseType.COLUMN);
            assertThat(metadata.getProvider()).isEqualTo("column");
        }
        @DisplayName("Should return to string")
        @Test
        void shouldReturnToString() {
            Database database = Mockito.mock(Database.class);
            Mockito.when(database.value()).thenReturn(DatabaseType.COLUMN);
            Mockito.when(database.provider()).thenReturn("column");
            DatabaseMetadata metadata = DatabaseMetadata.of(database);
            assertThat(metadata.toString()).isEqualTo("COLUMN@column");
        }
        @DisplayName("Should return to string2")
        @Test
        void shouldReturnToString2() {
            Database database = Mockito.mock(Database.class);
            Mockito.when(database.value()).thenReturn(DatabaseType.COLUMN);
            DatabaseMetadata metadata = DatabaseMetadata.of(database);
            assertThat(metadata.toString()).isEqualTo("COLUMN");
        }
    }
}
