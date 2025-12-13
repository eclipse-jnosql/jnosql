/*
 *  Copyright (c) 2025 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.mapping.semistructured.repository;

import jakarta.inject.Inject;
import org.assertj.core.api.Assertions;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.eclipse.jnosql.mapping.semistructured.MockProducer;
import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;
import org.eclipse.jnosql.mapping.semistructured.query.Tasks;
import org.eclipse.jnosql.mapping.semistructured.repository.entities.ComicBookRepository;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class})
class SemistructuredRepositoryProducerTest {

    @Inject
    private SemistructuredRepositoryProducer producer;

    private SemiStructuredTemplate semiStructuredTemplate;

    @BeforeEach
    void setUP() {
        this.semiStructuredTemplate = Mockito.mock(SemiStructuredTemplate.class);
    }

    @Test
    void shouldReturnInstance() {
        Assertions.assertThat(producer).isNotNull();
    }


    @Test
    void shouldReturnErrorWhenTemplateIsNull() {
        Assertions.assertThatThrownBy(() -> producer.get(Tasks.class, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldReturnErrorWhenRepositoryClassIsNull() {
        Assertions.assertThatThrownBy(() -> producer.get(null, semiStructuredTemplate))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldInstanceDefaultConstructor() {
        SemistructuredRepositoryProducer producer = new SemistructuredRepositoryProducer();
        Assertions.assertThat(producer).isNotNull();
    }


    @Test
    void shouldReturnCustomRepositoryInstance() {
        var repository = producer.get(Tasks.class, semiStructuredTemplate);
        Assertions.assertThat(repository).isNotNull();
    }

    @Test
    void shouldReturnRepositoryInstance() {
        var repository = producer.get(ComicBookRepository.class, semiStructuredTemplate);
        Assertions.assertThat(repository).isNotNull();
    }

}