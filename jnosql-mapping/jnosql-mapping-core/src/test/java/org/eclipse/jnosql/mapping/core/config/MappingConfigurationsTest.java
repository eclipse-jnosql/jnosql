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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MappingConfigurationsTest {

    @Test
    void shouldReturnValueForKeyValueProvider() {
        String expectedValue = "jnosql.keyvalue.provider";
        assertEquals(expectedValue, MappingConfigurations.KEY_VALUE_PROVIDER.get());
    }

    @Test
    void shouldReturnValueForKeyValueDatabase() {
        String expectedValue = "jnosql.keyvalue.database";
        assertEquals(expectedValue, MappingConfigurations.KEY_VALUE_DATABASE.get());
    }

    @Test
    void shouldReturnValueForDocumentProvider() {
        String expectedValue = "jnosql.document.provider";
        assertEquals(expectedValue, MappingConfigurations.DOCUMENT_PROVIDER.get());
    }

    @Test
    void shouldReturnValueForDocumentDatabase() {
        String expectedValue = "jnosql.document.database";
        assertEquals(expectedValue, MappingConfigurations.DOCUMENT_DATABASE.get());
    }

    @Test
    void shouldReturnValueForColumnProvider() {
        String expectedValue = "jnosql.column.provider";
        assertEquals(expectedValue, MappingConfigurations.COLUMN_PROVIDER.get());
    }

    @Test
    void shouldReturnValueForColumnDatabase() {
        String expectedValue = "jnosql.column.database";
        assertEquals(expectedValue, MappingConfigurations.COLUMN_DATABASE.get());
    }

    @Test
    void shouldReturnValueForGraphProvider() {
        String expectedValue = "jnosql.graph.provider";
        assertEquals(expectedValue, MappingConfigurations.GRAPH_PROVIDER.get());
    }

    @Test
    void shouldReturnValueForGraphTransactionAutomatic() {
        String expectedValue = "jnosql.graph.transaction.automatic";
        assertEquals(expectedValue, MappingConfigurations.GRAPH_TRANSACTION_AUTOMATIC.get());
    }
}