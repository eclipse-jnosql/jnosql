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
package org.eclipse.jnosql.mapping.semistructured.query;

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
import org.eclipse.jnosql.mapping.semistructured.entities.PersonRepository;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;


@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class})
@ExtendWith(MockitoExtension.class)
class SemiStructuredRepositoryProxyTest {

    @Mock
    private SemiStructuredTemplate template;
    @Inject
    private EntitiesMetadata entitiesMetadata;

    @Inject
    private Converters converters;

    @Test
    void shouldCreateSemiStructuredRepositoryProxy() {
        EntityMetadata entityMetadata = entitiesMetadata.get(Person.class);

        SemiStructuredRepositoryProxy.SemiStructuredRepository<Object, Object> repository = SemiStructuredRepositoryProxy.SemiStructuredRepository.of(template, entityMetadata);
        Assertions.assertThat(repository).isNotNull();
    }

    @Test
    void shouldExecuteFindAll() {
        SemiStructuredRepositoryProxy proxy = new SemiStructuredRepositoryProxy(template, entitiesMetadata,
                PersonRepository.class,
                converters);

        Method findAll = Arrays.stream(SemiStructuredRepositoryProxyTest.class.getDeclaredMethods())
                .filter(method -> method.getName().equals("findAll")).findFirst().orElseThrow();
        proxy.executeFindAll(proxy, findAll, new Object[0]);
    }

    @Test
    void shouldGetErrorOnFindRestriction() {
        var proxy = new SemiStructuredRepositoryProxy(template, entitiesMetadata,
                PersonRepository.class,
                converters);

        Assertions.assertThatThrownBy(() -> proxy.restriction(new Object[0])
                ).isInstanceOf(IllegalArgumentException.class);

        Assertions.assertThatThrownBy(() -> proxy.restriction(new Object[] {new Object(), new Object()})
        ).isInstanceOf(IllegalArgumentException.class);
    }

    List<Person> findAll() {
        return List.of();
    }
}