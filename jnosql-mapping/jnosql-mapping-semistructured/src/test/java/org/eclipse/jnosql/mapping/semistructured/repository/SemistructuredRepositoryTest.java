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
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.eclipse.jnosql.mapping.semistructured.MockProducer;
import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;
import org.eclipse.jnosql.mapping.semistructured.entities.Person;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;


@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class})
@ExtendWith(MockitoExtension.class)
class SemistructuredRepositoryTest {

    @Mock
    private SemiStructuredTemplate template;
    @Inject
    private EntitiesMetadata entitiesMetadata;

    @Inject
    private Converters converters;

    private  SemistructuredRepository<Object, Object> repository;

    @BeforeEach
    void setUp() {
        EntityMetadata entityMetadata = entitiesMetadata.get(Person.class);
        this.repository = SemistructuredRepository.of(template, entityMetadata);
    }

    @Test
    void shouldCreateInstance() {
        Assertions.assertThat(repository).isNotNull();
    }

    @Test
    void shouldGetTemplate() {
        Assertions.assertThat(repository.template()).isNotNull();
    }

    @Test
    void shouldGetEntityMetadata() {
        Assertions.assertThat(repository.entityMetadata()).isNotNull();
    }

    @Test
    void shouldCountBy() {
        Mockito.when(template.count(Person.class)).thenReturn(1L);
        var countBy = repository.countBy();
        Assertions.assertThat(countBy).isEqualTo(1L);
        Mockito.verify(template).count(Person.class);
    }

    @Test
    void shouldDeleteAll() {
        repository.deleteAll();
        Mockito.verify(template).deleteAll(Person.class);
    }

    @Test
    void shouldFindAll() {
        repository.findAll();
        Mockito.verify(template).findAll(Person.class);
    }

}