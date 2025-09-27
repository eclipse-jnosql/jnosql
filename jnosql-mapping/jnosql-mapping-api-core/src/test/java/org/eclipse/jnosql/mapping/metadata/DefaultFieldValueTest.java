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
package org.eclipse.jnosql.mapping.metadata;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DefaultFieldValueTest {

    @Test
    void shouldConstructWithValidArguments() {
        FieldMetadata fieldMetadata = mock(FieldMetadata.class);
        when(fieldMetadata.name()).thenReturn("fieldName");

        Object value = "testValue";

        DefaultFieldValue fieldValue = new DefaultFieldValue(value, fieldMetadata);

        assertEquals(value, fieldValue.value());
        assertEquals(fieldMetadata, fieldValue.field());
    }

    @Test
    void shouldThrowExceptionForNullFieldMetadataInConstructor() {
        Object value = "testValue";
        assertThrows(NullPointerException.class, () -> new DefaultFieldValue(value, null));
    }

    @Test
    void shouldReturnTrueForIsNotEmptyWithValue() {
        FieldMetadata fieldMetadata = mock(FieldMetadata.class);
        when(fieldMetadata.name()).thenReturn("fieldName");
        DefaultFieldValue fieldValue = new DefaultFieldValue("testValue", fieldMetadata);
        assertTrue(fieldValue.isNotEmpty());
    }

    @Test
    void shouldReturnFalseForIsNotEmptyWithNullValue() {
        FieldMetadata fieldMetadata = mock(FieldMetadata.class);
        when(fieldMetadata.name()).thenReturn("fieldName");
        DefaultFieldValue fieldValue = new DefaultFieldValue(null, fieldMetadata);
        assertFalse(fieldValue.isNotEmpty());
    }

    @Test
    void shouldReturnCorrectStringRepresentation() {
        FieldMetadata fieldMetadata = mock(FieldMetadata.class);
        when(fieldMetadata.name()).thenReturn("fieldName");
        DefaultFieldValue fieldValue = new DefaultFieldValue("testValue", fieldMetadata);
        String expected = "FieldValue{value=testValue, field=FieldMetadata{name='fieldName', type=class java.lang.String}}";
        Assertions.assertThat(fieldValue.toString()).isNotEmpty().isNotBlank().isNotNull();
    }
}