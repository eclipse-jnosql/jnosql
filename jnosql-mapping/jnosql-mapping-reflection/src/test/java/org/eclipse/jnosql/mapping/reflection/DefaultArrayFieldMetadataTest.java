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
package org.eclipse.jnosql.mapping.reflection;

import org.assertj.core.api.Assertions;
import org.eclipse.jnosql.communication.Value;
import org.eclipse.jnosql.mapping.metadata.ArrayFieldMetadata;
import org.eclipse.jnosql.mapping.metadata.ClassConverter;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.reflection.entities.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


class DefaultArrayFieldMetadataTest {

    private ArrayFieldMetadata fieldMetadata;

    @BeforeEach
    void setUp() {
        ClassConverter converter = new ReflectionClassConverter();
        EntityMetadata entityMetadata = converter.apply(Person.class);
        var mobile = entityMetadata.fieldMapping("mobile").orElseThrow();
        this.fieldMetadata = (ArrayFieldMetadata) mobile;
    }

    @Test
    void shouldToString() {
        assertThat(fieldMetadata.toString()).isNotEmpty().isNotNull();
    }

    @Test
    void shouldGetElementType() {
        assertThat(fieldMetadata.elementType()).isEqualTo(String.class);
    }


    @Test
    void shouldEqualsHashCode() {
        Assertions.assertThat(fieldMetadata).isEqualTo(fieldMetadata);
        Assertions.assertThat(fieldMetadata).hasSameHashCodeAs(fieldMetadata);
    }

    @Test
    void shouldValue() {
        List<String> phones = List.of("Ada", "Lovelace");
        Object value = fieldMetadata.value(Value.of(phones));
        assertThat(value).isNotNull().isInstanceOf(Object[].class);
    }

    @Test
    void shouldConverter() {
        assertThat(fieldMetadata.converter()).isNotNull().isEmpty();
    }

    @Test
    void shouldNewConverter() {
        assertThat(fieldMetadata.newConverter()).isNotNull().isEmpty();
    }

    @Test
    void shouldArrayInstance() {
        List<String> phones = List.of("Ada", "Lovelace");
        String[] value = (String[]) fieldMetadata.arrayInstance(phones);
        assertThat(value).containsExactly("Ada", "Lovelace");
    }

    @Test
    void shouldReturnElementType() {
        assertThat(fieldMetadata.elementType()).isEqualTo(String.class);
    }

    @Test
    void shouldIsEmbeddable() {
        assertThat(fieldMetadata.isEmbeddable()).isFalse();
    }

    @Test
    void shouldIsId() {
        assertThat(fieldMetadata.isId()).isFalse();
    }
}
