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
import org.eclipse.jnosql.mapping.reflection.entities.converters.UUIDConverter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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
}