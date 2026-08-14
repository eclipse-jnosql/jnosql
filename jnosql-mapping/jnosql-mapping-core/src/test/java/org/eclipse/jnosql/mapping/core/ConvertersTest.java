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
package org.eclipse.jnosql.mapping.core;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.DisplayName;

import jakarta.inject.Inject;
import jakarta.nosql.AttributeConverter;
import jakarta.nosql.Convert;
import org.eclipse.jnosql.mapping.metadata.FieldMetadata;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@EnableAutoWeld
@AddPackages(value = Convert.class)
@AddPackages(value = VetedConverter.class)
@AddExtensions(ReflectionEntityMetadataExtension.class)
class ConvertersTest {

    @Inject
    private Converters converters;
    @DisplayName("Should return npewhen class is null")
    @Test
    void shouldReturnNPEWhenClassIsNull() {
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> converters.get(null));
    }

    @SuppressWarnings("unchecked")
    @DisplayName("Should create attribute converter with injections")
    @Test
    void shouldCreateAttributeConverterWithInjections() {
        FieldMetadata fieldMetadata = Mockito.mock(FieldMetadata.class);
        Optional<?> converter = Optional.of(MyConverter.class);
        Optional<?> newInstance = Optional.of(new MyConverter());

        Mockito.when(fieldMetadata.converter())
                .thenReturn((Optional<Class<AttributeConverter<Object, Object>>>) converter);
        Mockito.when(fieldMetadata.newConverter())
                .thenReturn((Optional<AttributeConverter<Object, Object>>) newInstance);
        AttributeConverter<String, String> attributeConverter = converters.get(fieldMetadata);
        Object text = attributeConverter.convertToDatabaseColumn("Text");
        assertThat(text).isNotNull();
    }

    @DisplayName("Should create not using injections")
    @Test
    @SuppressWarnings("unchecked")
    void shouldCreateNotUsingInjections() {

        FieldMetadata fieldMetadata = Mockito.mock(FieldMetadata.class);
        Optional<?> converter = Optional.of(VetedConverter.class);
        Optional<?> newInstance = Optional.of(new VetedConverter());

        Mockito.when(fieldMetadata.converter())
                .thenReturn((Optional<Class<AttributeConverter<Object, Object>>>) converter);
        Mockito.when(fieldMetadata.newConverter())
                .thenReturn((Optional<AttributeConverter<Object, Object>>) newInstance);

        AttributeConverter<String, String> attributeConverter = converters.get(fieldMetadata);
        Object text = attributeConverter.convertToDatabaseColumn("Text");
        assertThat(text).isNotNull();
        assertThat(text).isEqualTo("Text");
    }

    @DisplayName("Should get to string")
    @Test
    void shouldGetToString(){
        assertThat(this.converters.toString()).isNotNull().isNotBlank().isNotEmpty();
    }

}
