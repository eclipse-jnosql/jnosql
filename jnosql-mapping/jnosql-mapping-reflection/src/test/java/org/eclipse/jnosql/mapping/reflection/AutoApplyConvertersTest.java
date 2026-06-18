/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
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
import jakarta.nosql.Convert;
import org.eclipse.jnosql.mapping.reflection.entities.converters.UUIDConverter;
import org.eclipse.jnosql.mapping.reflection.entities.converters.UUIDCustomConverter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AutoApplyConvertersTest {

    private final AutoApplyConverters converters = new AutoApplyConverters();

    @Nested
    @DisplayName("When looking up auto-apply converters")
    class WhenLookup {

        @Test
        @DisplayName("Should find UUID converter by attribute type")
        void shouldFindUUIDConverter() {

            Optional<Class<? extends AttributeConverter<?, ?>>> converter =
                    converters.getConverter(UUID.class);

            assertThat(converter).isPresent().contains(UUIDConverter.class);
        }

        @Test
        @DisplayName("Should not find converter for String type")
        void shouldNotFindConverterForString() {

            var converter = converters.getConverter(String.class);
            assertThat(converter).isEmpty();
        }
    }

    @Nested
    @DisplayName("When resolving converters")
    class ConverterResolution {

        @Test
        @DisplayName("Should use converter declared by @Convert annotation")
        void shouldUseConverterFromAnnotation() {

            Convert convert = Mockito.mock(Convert.class);


            Class<? extends AttributeConverter<?, ?>> value = UUIDCustomConverter.class;

            Mockito.doReturn(value)
.when(convert)
                    .value();

            Class<? extends AttributeConverter<?, ?>> converter = converters.converter(convert, String.class);

            assertThat(converter).isEqualTo(UUIDCustomConverter.class);
        }

        @Test
        @DisplayName("Should use auto-apply converter when annotation is absent")
        void shouldUseAutoApplyConverter() {

            Class<? extends AttributeConverter<?, ?>> converter = converters.converter(null, UUID.class);

            assertThat(converter).isEqualTo(UUIDConverter.class);
        }

        @Test
        @DisplayName("Should return null when no annotation and no auto-apply converter exist")
        void shouldReturnNullWhenConverterDoesNotExist() {

            Class<? extends AttributeConverter<?, ?>> converter = converters.converter(null, String.class);

            assertThat(converter).isNull();
        }
    }
}