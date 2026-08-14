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
package org.eclipse.jnosql.mapping.core.config;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


class MappingConfigurationsTest {









    @Nested
    @DisplayName("When the mapping configurations operates")
    class WhenTheMappingConfigurationsOperates {

        @DisplayName("Should return value for key value provider")
        @Test
        void shouldReturnValueForKeyValueProvider() {
            String expectedValue = "jnosql.keyvalue.provider";
            assertThat(MappingConfigurations.KEY_VALUE_PROVIDER.get()).isEqualTo(expectedValue);
        }
        @DisplayName("Should return value for key value database")
        @Test
        void shouldReturnValueForKeyValueDatabase() {
            String expectedValue = "jnosql.keyvalue.database";
            assertThat(MappingConfigurations.KEY_VALUE_DATABASE.get()).isEqualTo(expectedValue);
        }
        @DisplayName("Should return value for document provider")
        @Test
        void shouldReturnValueForDocumentProvider() {
            String expectedValue = "jnosql.document.provider";
            assertThat(MappingConfigurations.DOCUMENT_PROVIDER.get()).isEqualTo(expectedValue);
        }
        @DisplayName("Should return value for document database")
        @Test
        void shouldReturnValueForDocumentDatabase() {
            String expectedValue = "jnosql.document.database";
            assertThat(MappingConfigurations.DOCUMENT_DATABASE.get()).isEqualTo(expectedValue);
        }
        @DisplayName("Should return value for column provider")
        @Test
        void shouldReturnValueForColumnProvider() {
            String expectedValue = "jnosql.column.provider";
            assertThat(MappingConfigurations.COLUMN_PROVIDER.get()).isEqualTo(expectedValue);
        }
        @DisplayName("Should return value for column database")
        @Test
        void shouldReturnValueForColumnDatabase() {
            String expectedValue = "jnosql.column.database";
            assertThat(MappingConfigurations.COLUMN_DATABASE.get()).isEqualTo(expectedValue);
        }
        @DisplayName("Should return value for graph provider")
        @Test
        void shouldReturnValueForGraphProvider() {
            String expectedValue = "jnosql.graph.provider";
            assertThat(MappingConfigurations.GRAPH_PROVIDER.get()).isEqualTo(expectedValue);
        }
        @DisplayName("Should return value for graph transaction automatic")
        @Test
        void shouldReturnValueForGraphTransactionAutomatic() {
            String expectedValue = "jnosql.graph.transaction.automatic";
            assertThat(MappingConfigurations.GRAPH_TRANSACTION_AUTOMATIC.get()).isEqualTo(expectedValue);
        }
    }
}
