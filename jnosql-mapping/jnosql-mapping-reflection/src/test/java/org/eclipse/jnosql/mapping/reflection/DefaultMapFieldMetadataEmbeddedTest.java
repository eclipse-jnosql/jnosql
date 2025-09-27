/*
 *  Copyright (c) 2024 Contributors to the Eclipse Foundation
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
import org.eclipse.jnosql.mapping.metadata.ClassConverter;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.FieldMetadata;
import org.eclipse.jnosql.mapping.metadata.MapFieldMetadata;
import org.eclipse.jnosql.mapping.reflection.entities.Computer;
import org.eclipse.jnosql.mapping.reflection.entities.Program;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultMapFieldMetadataEmbeddedTest {

    private MapFieldMetadata fieldMetadata;

    @BeforeEach
    void setUp() {
        ClassConverter converter = new ReflectionClassConverter();
        EntityMetadata entityMetadata = converter.apply(Computer.class);
        FieldMetadata programs = entityMetadata.fieldMapping("programs").orElseThrow();
        this.fieldMetadata = (MapFieldMetadata) programs;
    }

    @Test
    void shouldToString() {
        assertThat(fieldMetadata.toString()).isNotEmpty().isNotNull();
    }

    @Test
    void shouldGetValueType() {
        assertThat(fieldMetadata.valueType()).isEqualTo(Program.class);
    }

    @Test
    void shouldGetKeyType() {
        assertThat(fieldMetadata.keyType()).isEqualTo(String.class);
    }

    @Test
    void shouldEqualsHashCode() {
        Assertions.assertThat(fieldMetadata).isEqualTo(fieldMetadata);
        Assertions.assertThat(fieldMetadata).hasSameHashCodeAs(fieldMetadata);
    }

    @Test
    void shouldIsEmbedded() {
        assertThat(fieldMetadata.isEmbeddable()).isTrue();
    }

    @Test
    void shouldConverter() {
        assertThat(fieldMetadata.converter()).isNotNull().isEmpty();
    }

    @Test
    void shouldNewConverter() {
        assertThat(fieldMetadata.newConverter()).isNotNull().isEmpty();
    }
}