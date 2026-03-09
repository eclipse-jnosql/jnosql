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

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@EnableAutoWeld
@AddPackages(value = Convert.class)
@AddPackages(value = VetedConverter.class)
@AddExtensions(ReflectionEntityMetadataExtension.class)
class ConvertersTest {

    @Inject
    private Converters converters;

    @Test
    void shouldReturnNPEWhenClassIsNull() {
        assertThatNullPointerException().isThrownBy(() -> converters.get(null))
                .withMessage("The metadata is required");
    }

    @SuppressWarnings("unchecked")
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

    @Test
    void shouldResolveUsingFactoryWithResolver() {
        var metadata = metadata(MyConverter.class, new VetedConverter());
        var custom = new VetedConverter();
        var withResolver = Converters.withResolver(field -> Optional.of(custom));

        var attributeConverter = withResolver.get(metadata);

        assertThat(attributeConverter).isSameAs(custom);
    }

    @Test
    void shouldResolveUsingFirstMatchWhenWithResolvers() {
        var metadata = metadata(MyConverter.class, new VetedConverter());
        var expected = new VetedConverter();
        AtomicInteger secondResolverCalls = new AtomicInteger();
        var converters = Converters.withResolvers(
                field -> Optional.of(expected),
                field -> {
                    secondResolverCalls.incrementAndGet();
                    return Optional.of(new VetedConverter());
                });

        var attributeConverter = converters.get(metadata);

        assertThat(attributeConverter).isSameAs(expected);
        assertThat(secondResolverCalls).hasValue(0);
    }

    @Test
    void shouldFallbackToNewConverterWhenAutoResolverIsUsed() {
        var metadata = metadata(VetedConverter.class, new VetedConverter());

        var autoResolverConverters = Converters.autoResolver();
        var attributeConverter = autoResolverConverters.get(metadata);

        assertThat(attributeConverter).isInstanceOf(VetedConverter.class);
    }

    @Test
    void shouldThrowWhenWithResolverReceivesNull() {
        assertThatNullPointerException()
                .isThrownBy(() -> Converters.withResolver(null))
                .withMessage("The resolver is required");
    }

    @Test
    void shouldThrowWhenWithResolversReceivesNullArray() {
        assertThatNullPointerException()
                .isThrownBy(() -> Converters.withResolvers((ConverterResolver[]) null))
                .withMessage("The resolvers are required");
    }

    @Test
    void shouldThrowWhenWithResolversReceivesEmptyArray() {
        assertThatThrownBy(Converters::withResolvers)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The resolvers are required");
    }

    @Test
    void shouldThrowWhenWithResolversContainsNullItem() {
        assertThatNullPointerException()
                .isThrownBy(() -> Converters.withResolvers(ConverterResolver.noOp(), null))
                .withMessage("The resolver is required");
    }

    @Test
    void shouldThrowWhenResolverAndFallbackAreMissing() {
        FieldMetadata metadata = Mockito.mock(FieldMetadata.class);
        Mockito.when(metadata.converter()).thenReturn(Optional.empty());
        Mockito.when(metadata.newConverter()).thenReturn(Optional.empty());
        Mockito.when(metadata.name()).thenReturn("salary");
        Mockito.when(metadata.type()).thenReturn((Class) String.class);

        assertThatThrownBy(() -> Converters.autoResolver().get(metadata))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("There is not converter to the field: salary in the Field: class java.lang.String");
    }

    @Test
    void shouldGetToString() {
        assertThat(this.converters.toString()).isNotNull().isNotBlank().isNotEmpty();
    }

    @SuppressWarnings("unchecked")
    private static FieldMetadata metadata(Class<?> converterType, AttributeConverter<?, ?> newConverter) {
        FieldMetadata fieldMetadata = Mockito.mock(FieldMetadata.class);
        Mockito.when(fieldMetadata.converter())
                .thenReturn((Optional<Class<AttributeConverter<Object, Object>>>) Optional.of((Class<AttributeConverter<Object, Object>>) converterType));
        Mockito.when(fieldMetadata.newConverter())
                .thenReturn((Optional<AttributeConverter<Object, Object>>) Optional.of((AttributeConverter<Object, Object>) newConverter));
        return fieldMetadata;
    }
}
