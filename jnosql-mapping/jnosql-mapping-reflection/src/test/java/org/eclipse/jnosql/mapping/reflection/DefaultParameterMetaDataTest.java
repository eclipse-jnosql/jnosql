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

import jakarta.nosql.AttributeConverter;
import org.assertj.core.api.Assertions;
import org.eclipse.jnosql.mapping.metadata.ClassConverter;
import org.eclipse.jnosql.mapping.metadata.ConstructorMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.MappingType;
import org.eclipse.jnosql.mapping.metadata.ParameterMetaData;
import org.eclipse.jnosql.mapping.reflection.entities.constructor.BookUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;


class DefaultParameterMetaDataTest {

    private ParameterMetaData parameterMetaData;


    @BeforeEach
    void setUp(){
        ClassConverter converter = new ReflectionClassConverter();
        EntityMetadata entityMetadata = converter.apply(BookUser.class);
        ConstructorMetadata constructor = entityMetadata.constructor();
        this.parameterMetaData =
                constructor.parameters().stream().filter(p -> p.name().equals("native_name"))
                        .findFirst().orElseThrow();
    }


    @Test
    void shouldParamType() {
        assertEquals(MappingType.DEFAULT, parameterMetaData.mappingType());
    }

    @Test
    void shouldName() {
        assertEquals("native_name", parameterMetaData.name());
    }

    @Test
    void shouldType() {
        assertEquals(String.class, parameterMetaData.type());
    }

    @Test
    void shouldIsId() {
        assertFalse(parameterMetaData.isId());
    }

    @Test
    void shouldConverter() {
        Optional<Class<AttributeConverter<Object, Object>>> converter = parameterMetaData.converter();
        assertFalse(converter.isPresent());
    }

    @Test
    void testNewConverter() {
        Assertions.assertThat(parameterMetaData.newConverter()).isEmpty();
    }
}