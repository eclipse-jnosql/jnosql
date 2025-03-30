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

import org.eclipse.jnosql.communication.Value;
import org.eclipse.jnosql.mapping.metadata.ClassConverter;
import org.eclipse.jnosql.mapping.metadata.ConstructorMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.MapParameterMetaData;
import org.eclipse.jnosql.mapping.reflection.entities.constructor.Form;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultMapParameterMetaDataTest {

    private MapParameterMetaData fieldMetadata;

    @BeforeEach
    void setUp(){
        ClassConverter converter = new ReflectionClassConverter();
        EntityMetadata entityMetadata = converter.apply(Form.class);
        ConstructorMetadata constructor = entityMetadata.constructor();
        this.fieldMetadata = (MapParameterMetaData)
                constructor.parameters().stream().filter(p -> p.name().equals("questions"))
                        .findFirst().orElseThrow();
    }
    @Test
    void shouldToString() {
        assertThat(fieldMetadata.toString()).isNotEmpty().isNotNull();
    }

    @Test
    void shouldKeyType(){
        assertThat(fieldMetadata.keyType()).isEqualTo(String.class);
    }

    @Test
    void shouldValueType(){
        Class<?> value = this.fieldMetadata.valueType();
        assertThat(value).isInstanceOf(Object.class);
    }

    @Test
    void shouldValueClass(){
        Map<String, Object> value = Map.of("name", "name");
        assertThat(fieldMetadata.value(Value.of(value))).isInstanceOf(Map.class);
    }

    @Test
    void shouldIsEmbeddable() {
        assertThat(fieldMetadata.isEmbeddable()).isFalse();
    }
}