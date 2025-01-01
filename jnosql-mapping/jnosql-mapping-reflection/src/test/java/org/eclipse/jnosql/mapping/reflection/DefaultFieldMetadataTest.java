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
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.mapping.metadata.ClassConverter;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.FieldMetadata;
import org.eclipse.jnosql.mapping.reflection.entities.MoneyConverter;
import org.eclipse.jnosql.mapping.reflection.entities.Person;
import org.eclipse.jnosql.mapping.reflection.entities.UDTEntity;
import org.eclipse.jnosql.mapping.reflection.entities.Worker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class DefaultFieldMetadataTest {

    private ClassConverter converter;

    private DefaultFieldMetadata fieldMetadata;

    @BeforeEach
    void setUp(){
        this.converter = new ReflectionClassConverter();
        EntityMetadata entityMetadata = converter.apply(Person.class);
        FieldMetadata name = entityMetadata.fieldMapping("name").orElseThrow();
        this.fieldMetadata = (DefaultFieldMetadata) name;
    }
    @Test
    void shouldToString() {
        assertThat(fieldMetadata.toString()).isNotEmpty().isNotNull();
    }

    @Test
    void shouldEqualsHashCode() {
        assertThat(fieldMetadata).isEqualTo(fieldMetadata);
        assertThat(fieldMetadata).hasSameHashCodeAs(fieldMetadata);
    }

    @Test
    void shouldCreateNewInstanceConverter(){
        EntityMetadata entityMetadata = converter.apply(Worker.class);
        FieldMetadata name = entityMetadata.fieldMapping("salary").orElseThrow();
        fieldMetadata = (DefaultFieldMetadata) name;
        AttributeConverter<Object, Object> result = fieldMetadata.newConverter().orElseThrow();
        assertThat(result).isNotNull().isInstanceOf(MoneyConverter.class);
    }

    @Test
    void shouldReturnEmptyUDTWithEmptyString() {
        EntityMetadata entityMetadata = converter.apply(UDTEntity.class);
        FieldMetadata name = entityMetadata.fieldMapping("empty").orElseThrow();
        fieldMetadata = (DefaultFieldMetadata) name;
        assertThat(fieldMetadata.udt()).isEmpty();
    }

    @Test
    void shouldReturnEmptyUDTWithBlankString() {
        EntityMetadata entityMetadata = converter.apply(UDTEntity.class);
        FieldMetadata name = entityMetadata.fieldMapping("blank").orElseThrow();
        fieldMetadata = (DefaultFieldMetadata) name;
        assertThat(fieldMetadata.udt()).isEmpty();
    }

    @Test
    void shouldReturnUDT() {
        EntityMetadata entityMetadata = converter.apply(UDTEntity.class);
        FieldMetadata name = entityMetadata.fieldMapping("udt").orElseThrow();
        fieldMetadata = (DefaultFieldMetadata) name;

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(fieldMetadata.udt()).isPresent();
            soft.assertThat(fieldMetadata.udt().orElseThrow()).isEqualTo("sample");
        });
    }

}
