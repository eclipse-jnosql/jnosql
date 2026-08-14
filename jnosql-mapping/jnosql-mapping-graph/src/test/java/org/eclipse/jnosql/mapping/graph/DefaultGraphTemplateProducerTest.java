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
package org.eclipse.jnosql.mapping.graph;

import jakarta.inject.Inject;
import org.eclipse.jnosql.communication.graph.GraphDatabaseManager;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.graph.spi.GraphExtension;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;


@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class, GraphTemplate.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class, GraphExtension.class})
class DefaultGraphTemplateProducerTest {

    @Inject
    private GraphTemplateProducer producer;


    @Nested
    @DisplayName("When the producer applies a manager")
    class WhenTheProducerAppliesManager {

        @Test
        @DisplayName("Should throw an exception when manager is null")
        void shouldReturnErrorWhenManagerNull() {
            assertThatNullPointerException().isThrownBy(() -> producer.apply(null));
        }

        @Test
        @DisplayName("Should return a graph template")
        void shouldReturnGraphTemplate() {
            var manager = Mockito.mock(GraphDatabaseManager.class);
            GraphTemplate graphTemplate = producer.apply(manager);

            assertThat(graphTemplate).isNotNull();
        }
    }

    @Nested
    @DisplayName("When the template is constructed")
    class WhenTheTemplateIsConstructed {

        @Test
        @DisplayName("Should have a default constructor for CDI")
        void shouldHaveDefaultConstructor() {
            GraphTemplateProducer.ProducerGraphTemplate template = new GraphTemplateProducer.ProducerGraphTemplate();

            assertThat(template).isNotNull();
        }
    }

}
