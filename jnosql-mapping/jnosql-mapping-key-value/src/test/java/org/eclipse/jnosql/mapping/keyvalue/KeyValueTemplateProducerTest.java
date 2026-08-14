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
package org.eclipse.jnosql.mapping.keyvalue;

import jakarta.inject.Inject;
import org.eclipse.jnosql.communication.keyvalue.BucketManager;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.keyvalue.spi.KeyValueExtension;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@EnableAutoWeld
@AddPackages(value = {Converters.class, KeyValueEntityConverter.class})
@AddPackages(MockProducer.class)
@AddExtensions({ReflectionEntityMetadataExtension.class, KeyValueExtension.class})
@AddPackages(Reflections.class)
class KeyValueTemplateProducerTest {


    @Inject
    private KeyValueTemplateProducer producer;

    @Nested
    @DisplayName("When the producer creates a template")
    class WhenTheProducerCreatesTemplate {

        @DisplayName("Should Return Error When Manager Null")
        @Test
        void shouldReturnErrorWhenManagerNull() {
            assertThatThrownBy(() -> producer.apply(null)).isInstanceOf(NullPointerException.class);
        }

        @DisplayName("Should Return")
        @Test
        void shouldReturn() {
            BucketManager manager = Mockito.mock(BucketManager.class);
            KeyValueTemplate repository = producer.apply(manager);
            assertThat(repository).isNotNull();
        }
    }

}
