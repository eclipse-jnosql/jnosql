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
package org.eclipse.jnosql.mapping.keyvalue.query;

import jakarta.inject.Inject;
import org.eclipse.jnosql.communication.keyvalue.BucketManager;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.keyvalue.KeyValueEntityConverter;
import org.eclipse.jnosql.mapping.keyvalue.KeyValueTemplate;
import org.eclipse.jnosql.mapping.keyvalue.MockProducer;
import org.eclipse.jnosql.mapping.keyvalue.entities.PersonRepository;
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

import static org.assertj.core.api.Assertions.assertThat;

@EnableAutoWeld
@AddPackages(value = {Converters.class, KeyValueEntityConverter.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class, KeyValueExtension.class})
class KeyValueRepositoryProducerTest {

    @Inject
    private KeyValueRepositoryProducer producer;

    @Nested
    @DisplayName("When the producer creates repositories")
    class WhenTheProducerCreatesRepositories {

        @DisplayName("Should Create From Manager")
        @Test
        void shouldCreateFromManager() {
            BucketManager manager = Mockito.mock(BucketManager.class);
            PersonRepository personRepository = producer.get(PersonRepository.class, manager);
            assertThat(personRepository).isNotNull();
        }

        @DisplayName("Should Create From Template")
        @Test
        void shouldCreateFromTemplate() {
            KeyValueTemplate template = Mockito.mock(KeyValueTemplate.class);
            PersonRepository personRepository = producer.get(PersonRepository.class, template);
            assertThat(personRepository).isNotNull();
        }

    }

}
