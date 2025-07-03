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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.eclipse.jnosql.mapping.DatabaseType.COLUMN;
import static org.eclipse.jnosql.mapping.DatabaseType.DOCUMENT;
import static org.eclipse.jnosql.mapping.DatabaseType.GRAPH;
import static org.eclipse.jnosql.mapping.DatabaseType.KEY_VALUE;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DatabaseQualifierTest {

    @Test
    void shouldReturnDefaultColumn() {
        DatabaseQualifier qualifier = DatabaseQualifier.ofColumn();
        assertEquals("", qualifier.provider());
        assertEquals(COLUMN, qualifier.value());
    }

    @Test
    void shouldReturnColumnProvider() {
        String provider = "provider";
        DatabaseQualifier qualifier = DatabaseQualifier.ofColumn(provider);
        assertEquals(provider, qualifier.provider());
        assertEquals(COLUMN, qualifier.value());
    }

    @Test
    void shouldReturnErrorWhenColumnNull() {
        Assertions.assertThrows(NullPointerException.class, () -> DatabaseQualifier.ofColumn(null));
    }

    @Test
    void shouldReturnDefaultDocument() {
        DatabaseQualifier qualifier = DatabaseQualifier.ofDocument();
        assertEquals("", qualifier.provider());
        assertEquals(DOCUMENT, qualifier.value());
    }

    @Test
    void shouldReturnDocumentProvider() {
        String provider = "provider";
        DatabaseQualifier qualifier = DatabaseQualifier.ofDocument(provider);
        assertEquals(provider, qualifier.provider());
        assertEquals(DOCUMENT, qualifier.value());
    }

    @Test
    void shouldReturnErrorWhenDocumentNull() {
        Assertions.assertThrows(NullPointerException.class, () -> DatabaseQualifier.ofDocument(null));
    }

    @Test
    void shouldReturnErrorWhenKeyValueNull() {
        Assertions.assertThrows(NullPointerException.class, () -> DatabaseQualifier.ofKeyValue(null));
    }

    @Test
    void shouldReturnKeyValueProvider() {
        String provider = "provider";
        DatabaseQualifier qualifier = DatabaseQualifier.ofKeyValue(provider);
        assertEquals(provider, qualifier.provider());
        assertEquals(KEY_VALUE, qualifier.value());
    }

    @Test
    void shouldReturnDefaultKeyValue() {
        DatabaseQualifier qualifier = DatabaseQualifier.ofKeyValue();
        assertEquals("", qualifier.provider());
        assertEquals(KEY_VALUE, qualifier.value());
    }


    @Test
    void shouldReturnErrorWhenGraphNull() {
        Assertions.assertThrows(NullPointerException.class, () -> DatabaseQualifier.ofGraph(null));
    }

    @Test
    void shouldReturnGraphProvider() {
        String provider = "provider";
        DatabaseQualifier qualifier = DatabaseQualifier.ofGraph(provider);
        assertEquals(provider, qualifier.provider());
        assertEquals(GRAPH, qualifier.value());
    }

    @Test
    void shouldReturnDefaultGraph() {
        DatabaseQualifier qualifier = DatabaseQualifier.ofGraph();
        assertEquals("", qualifier.provider());
        assertEquals(GRAPH, qualifier.value());
    }
}