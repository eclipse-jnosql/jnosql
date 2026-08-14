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
package org.eclipse.jnosql.mapping.semistructured;

import org.eclipse.jnosql.mapping.DatabaseQualifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.eclipse.jnosql.mapping.DatabaseType.COLUMN;
import static org.eclipse.jnosql.mapping.DatabaseType.DOCUMENT;
import static org.eclipse.jnosql.mapping.DatabaseType.GRAPH;
import static org.eclipse.jnosql.mapping.DatabaseType.KEY_VALUE;

class DatabaseQualifierTest {

    @DisplayName("Should return default column")
    @Test
    void shouldReturnDefaultColumn() {
        DatabaseQualifier qualifier = DatabaseQualifier.ofColumn();
        assertThat(qualifier.provider()).isEqualTo("");
        assertThat(qualifier.value()).isEqualTo(COLUMN);
    }

    @DisplayName("Should return column provider")
    @Test
    void shouldReturnColumnProvider() {
        String provider = "provider";
        DatabaseQualifier qualifier = DatabaseQualifier.ofColumn(provider);
        assertThat(qualifier.provider()).isEqualTo(provider);
        assertThat(qualifier.value()).isEqualTo(COLUMN);
    }

    @DisplayName("Should return error when column null")
    @Test
    void shouldReturnErrorWhenColumnNull() {
        assertThatThrownBy(() -> DatabaseQualifier.ofColumn(null)).isInstanceOf(NullPointerException.class);
    }

    @DisplayName("Should return default document")
    @Test
    void shouldReturnDefaultDocument() {
        DatabaseQualifier qualifier = DatabaseQualifier.ofDocument();
        assertThat(qualifier.provider()).isEqualTo("");
        assertThat(qualifier.value()).isEqualTo(DOCUMENT);
    }

    @DisplayName("Should return document provider")
    @Test
    void shouldReturnDocumentProvider() {
        String provider = "provider";
        DatabaseQualifier qualifier = DatabaseQualifier.ofDocument(provider);
        assertThat(qualifier.provider()).isEqualTo(provider);
        assertThat(qualifier.value()).isEqualTo(DOCUMENT);
    }

    @DisplayName("Should return error when document null")
    @Test
    void shouldReturnErrorWhenDocumentNull() {
        assertThatThrownBy(() -> DatabaseQualifier.ofDocument(null)).isInstanceOf(NullPointerException.class);
    }

    @DisplayName("Should return error when key value null")
    @Test
    void shouldReturnErrorWhenKeyValueNull() {
        assertThatThrownBy(() -> DatabaseQualifier.ofKeyValue(null)).isInstanceOf(NullPointerException.class);
    }

    @DisplayName("Should return key value provider")
    @Test
    void shouldReturnKeyValueProvider() {
        String provider = "provider";
        DatabaseQualifier qualifier = DatabaseQualifier.ofKeyValue(provider);
        assertThat(qualifier.provider()).isEqualTo(provider);
        assertThat(qualifier.value()).isEqualTo(KEY_VALUE);
    }

    @DisplayName("Should return default key value")
    @Test
    void shouldReturnDefaultKeyValue() {
        DatabaseQualifier qualifier = DatabaseQualifier.ofKeyValue();
        assertThat(qualifier.provider()).isEqualTo("");
        assertThat(qualifier.value()).isEqualTo(KEY_VALUE);
    }


    @DisplayName("Should return error when graph null")
    @Test
    void shouldReturnErrorWhenGraphNull() {
        assertThatThrownBy(() -> DatabaseQualifier.ofGraph(null)).isInstanceOf(NullPointerException.class);
    }

    @DisplayName("Should return graph provider")
    @Test
    void shouldReturnGraphProvider() {
        String provider = "provider";
        DatabaseQualifier qualifier = DatabaseQualifier.ofGraph(provider);
        assertThat(qualifier.provider()).isEqualTo(provider);
        assertThat(qualifier.value()).isEqualTo(GRAPH);
    }

    @DisplayName("Should return default graph")
    @Test
    void shouldReturnDefaultGraph() {
        DatabaseQualifier qualifier = DatabaseQualifier.ofGraph();
        assertThat(qualifier.provider()).isEqualTo("");
        assertThat(qualifier.value()).isEqualTo(GRAPH);
    }

    @Nested
    @DisplayName("When the database qualifier is tested")
    class WhenTheDatabaseQualifierIsTested {
    }
}