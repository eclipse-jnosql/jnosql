/*
 *  Copyright (c) 2024 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.mapping.semistructured;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.communication.semistructured.CommunicationEntity;
import org.eclipse.jnosql.communication.semistructured.DatabaseManager;
import org.eclipse.jnosql.communication.semistructured.Element;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.semistructured.entities.Person;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class})
class SelectFieldMapperTest {

    private final List<Element> columns = List.of(
            Element.of("age", 10L),
            Element.of("phones", Arrays.asList("234", "432")),
            Element.of("name", "Name"),
            Element.of("id", 19L));

    @Inject
    private EntityConverter converter;

    @Inject
    private EntitiesMetadata entities;

    @Inject
    private Converters converters;

    private DatabaseManager managerMock;

    private DefaultSemiStructuredTemplate template;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        managerMock = Mockito.mock(DatabaseManager.class);
        EventPersistManager eventPersistManager = Mockito.mock(EventPersistManager.class);
        Instance<DatabaseManager> instance = Mockito.mock(Instance.class);
        Mockito.when(instance.get()).thenReturn(managerMock);
        this.template = new DefaultSemiStructuredTemplate(converter, instance,
                eventPersistManager, entities, converters);
    }

    @Test
    void shouldSelectField() {
        CommunicationEntity entity = CommunicationEntity.of("Person", columns);
        Mockito.when(managerMock.select(Mockito.any(SelectQuery.class))).thenAnswer(a -> Stream.of(entity));
        Stream<Person> people = template.prepare("from Person").result();
        SoftAssertions.assertSoftly(s -> {
            s.assertThat(people).isNotNull();
            s.assertThat(people).hasSize(1).allMatch(Person.class::isInstance);
        });

        List<Person> result = template.prepare("from Person").<Person>result().toList();

        SoftAssertions.assertSoftly(s -> {
            s.assertThat(result).isNotNull();
            s.assertThat(result).hasSize(1).allMatch(Person.class::isInstance);
        });
    }

    @Test
    void shouldSingleSelectField() {
        CommunicationEntity entity = CommunicationEntity.of("Person", columns);
        Mockito.when(managerMock.select(Mockito.any(SelectQuery.class))).thenAnswer(a -> Stream.of(entity));
        Stream<Integer> ages = template.prepare("select age from Person").result();
        SoftAssertions.assertSoftly(s -> {
            s.assertThat(ages).isNotNull();
            s.assertThat(ages).hasSize(1).contains(10);
        });

        List<Integer> result = template.prepare("select age from Person").<Integer>result().toList();
        assertEquals(1, result.size());
    }

    @Test
    void shouldMultipleSelectFields() {
        CommunicationEntity entity = CommunicationEntity.of("Person", columns);
        Mockito.when(managerMock.select(Mockito.any(SelectQuery.class))).thenAnswer(a -> Stream.of(entity));
        Stream<Object[]> ages = template.prepare("select age, name from Person").result();
        SoftAssertions.assertSoftly(s -> {
            s.assertThat(ages).isNotNull();
            s.assertThat(ages).hasSize(1).contains(new Object[]{10, "Name"});
        });

        List<Object[]> result = template.prepare("select age, name from Person").<Object[]>result().toList();
       SoftAssertions.assertSoftly(s -> {
            s.assertThat(result).isNotNull();
            s.assertThat(result).hasSize(1).contains(new Object[]{10, "Name"});
        });
    }


}