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
package org.eclipse.jnosql.mapping.semistructured;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.eclipse.jnosql.communication.semistructured.CommunicationEntity;
import org.eclipse.jnosql.communication.semistructured.DatabaseManager;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.semistructured.entities.Address;
import org.eclipse.jnosql.mapping.semistructured.entities.Money;
import org.eclipse.jnosql.mapping.semistructured.entities.Person;
import org.eclipse.jnosql.mapping.semistructured.entities.Worker;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.jnosql.communication.semistructured.SelectQuery.select;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class})
class MapperSelectTest {

    @Inject
    private EntityConverter converter;

    @Inject
    private EntitiesMetadata entities;

    @Inject
    private Converters converters;

    private DatabaseManager managerMock;

    private DefaultSemiStructuredTemplate template;

    private ArgumentCaptor<SelectQuery> captor;

    @BeforeEach
    public void setUp() {
        managerMock = Mockito.mock(DatabaseManager.class);
        EventPersistManager persistManager = Mockito.mock(EventPersistManager.class);
        Instance<DatabaseManager> instance = Mockito.mock(Instance.class);
        this.captor = ArgumentCaptor.forClass(SelectQuery.class);
        when(instance.get()).thenReturn(managerMock);
        this.template = new DefaultSemiStructuredTemplate(converter, instance,
                persistManager, entities, converters);
    }


    @Test
    void shouldExecuteSelectFrom() {
        template.select(Person.class).result();
        SelectQuery queryExpected = select().from("Person").build();
        Mockito.verify(managerMock).select(captor.capture());
        SelectQuery query = captor.getValue();
        assertEquals(queryExpected, query);
    }

    @Test
    void shouldSelectOrderAsc() {
        template.select(Worker.class).orderBy("salary").asc().result();
        Mockito.verify(managerMock).select(captor.capture());
        SelectQuery query = captor.getValue();
        SelectQuery queryExpected = select().from("Worker").orderBy("money").asc().build();
        assertEquals(queryExpected, query);
    }

    @Test
    void shouldSelectOrderDesc() {
        template.select(Worker.class).orderBy("salary").desc().result();
        SelectQuery queryExpected = select().from("Worker").orderBy("money").desc().build();
        Mockito.verify(managerMock).select(captor.capture());
        SelectQuery query = captor.getValue();
        assertEquals(queryExpected, query);
    }

    @Test
    void shouldSelectLimit() {
        template.select(Worker.class).limit(10).result();
        SelectQuery queryExpected = select().from("Worker").limit(10L).build();
        Mockito.verify(managerMock).select(captor.capture());
        SelectQuery query = captor.getValue();
        assertEquals(queryExpected, query);
    }

    @Test
    void shouldSelectStart() {
        template.select(Worker.class).skip(10).result();
        SelectQuery queryExpected = select().from("Worker").skip(10L).build();
        Mockito.verify(managerMock).select(captor.capture());
        SelectQuery query = captor.getValue();
        assertEquals(queryExpected, query);
    }


    @Test
    void shouldSelectWhereEq() {
        template.select(Person.class).where("name").eq("Ada").result();
        SelectQuery queryExpected = select().from("Person").where("name")
                .eq("Ada").build();
        Mockito.verify(managerMock).select(captor.capture());
        SelectQuery query = captor.getValue();
        assertEquals(queryExpected, query);
    }

    @Test
    void shouldSelectWhereLike() {
        template.select(Person.class).where("name").like("Ada").result();
        SelectQuery queryExpected = select().from("Person").where("name")
                .like("Ada").build();
        Mockito.verify(managerMock).select(captor.capture());
        SelectQuery query = captor.getValue();
        assertEquals(queryExpected, query);
    }

    @Test
    void shouldSelectWhereGt() {
        template.select(Person.class).where("id").gt(10).result();
        SelectQuery queryExpected = select().from("Person").where("_id")
                .gt(10L).build();
        Mockito.verify(managerMock).select(captor.capture());
        SelectQuery query = captor.getValue();
        assertEquals(queryExpected, query);
    }

    @Test
    void shouldSelectWhereGte() {
        template.select(Person.class).where("id").gte(10).result();
        SelectQuery queryExpected = select().from("Person").where("_id")
                .gte(10L).build();
        Mockito.verify(managerMock).select(captor.capture());
        SelectQuery query = captor.getValue();
        assertEquals(queryExpected, query);
    }


    @Test
    void shouldSelectWhereLt() {
        template.select(Person.class).where("id").lt(10).result();
        SelectQuery queryExpected = select().from("Person").where("_id")
                .lt(10L).build();
        Mockito.verify(managerMock).select(captor.capture());
        SelectQuery query = captor.getValue();
        assertEquals(queryExpected, query);
    }

    @Test
    void shouldSelectWhereLte() {
        template.select(Person.class).where("id").lte(10).result();
        SelectQuery queryExpected = select().from("Person").where("_id")
                .lte(10L).build();
        Mockito.verify(managerMock).select(captor.capture());
        SelectQuery query = captor.getValue();
        assertEquals(queryExpected, query);
    }

    @Test
    void shouldSelectWhereBetween() {
        template.select(Person.class).where("id")
                .between(10, 20).result();
        SelectQuery queryExpected = select().from("Person").where("_id")
                .between(10L, 20L).build();
        Mockito.verify(managerMock).select(captor.capture());
        SelectQuery query = captor.getValue();
        assertEquals(queryExpected, query);
    }

    @Test
    void shouldSelectWhereNot() {
        template.select(Person.class).where("name").not().like("Ada").result();
        SelectQuery queryExpected = select().from("Person").where("name")
                .not().like("Ada").build();
        Mockito.verify(managerMock).select(captor.capture());
        SelectQuery query = captor.getValue();
        assertEquals(queryExpected, query);
    }


    @Test
    void shouldSelectWhereAnd() {
        template.select(Person.class).where("age").between(10, 20)
                .and("name").eq("Ada").result();
        SelectQuery queryExpected = select().from("Person").where("age")
                .between(10, 20)
                .and("name").eq("Ada").build();
        Mockito.verify(managerMock).select(captor.capture());
        SelectQuery query = captor.getValue();

        assertEquals(queryExpected, query);
    }

    @Test
    void shouldSelectWhereOr() {
        template.select(Person.class).where("id").between(10, 20)
                .or("name").eq("Ada").result();
        SelectQuery queryExpected = select().from("Person").where("_id")
                .between(10L, 20L)
                .or("name").eq("Ada").build();

        Mockito.verify(managerMock).select(captor.capture());
        SelectQuery query = captor.getValue();
        assertEquals(queryExpected, query);
    }

    @Test
    void shouldConvertField() {
        template.select(Person.class).where("id").eq("20")
                .result();
        SelectQuery queryExpected = select().from("Person").where("_id").eq(20L)
                .build();

        Mockito.verify(managerMock).select(captor.capture());
        SelectQuery query = captor.getValue();

        assertEquals(queryExpected, query);
    }

    @Test
    void shouldUseAttributeConverter() {
        template.select(Worker.class).where("salary")
                .eq(new Money("USD", BigDecimal.TEN)).result();
        SelectQuery queryExpected = select().from("Worker").where("money")
                .eq("USD 10").build();

        Mockito.verify(managerMock).select(captor.capture());
        SelectQuery query = captor.getValue();
        assertEquals(queryExpected, query);
    }

    @Test
    void shouldQueryByEmbeddable() {
        template.select(Worker.class).where("job.city").eq("Salvador")
                .result();
        SelectQuery queryExpected = select().from("Worker").where("city")
                .eq("Salvador")
                .build();

        Mockito.verify(managerMock).select(captor.capture());
        SelectQuery query = captor.getValue();
        assertEquals(queryExpected, query);
    }

    @Test
    void shouldQueryBySubEntity() {
        template.select(Address.class).where("zipCode.zip").eq("01312321")
                .result();
        SelectQuery queryExpected = select().from("Address").where("zipCode.zip")
                .eq("01312321")
                .build();

        Mockito.verify(managerMock).select(captor.capture());
        SelectQuery query = captor.getValue();
        assertEquals(queryExpected, query);
    }


    @Test
    void shouldResult() {
        SelectQuery query = select().from("Person").build();
        CommunicationEntity entity = CommunicationEntity.of("Person");
        entity.add("_id", 1L);
        entity.add("name", "Ada");
        entity.add("age", 20);
        Mockito.when(managerMock.select(query)).thenReturn(Stream.of(entity));
        List<Person> result = template.select(Person.class).result();
        Assertions.assertNotNull(result);
        assertThat(result).hasSize(1)
                .map(Person::getName).contains("Ada");
    }


    @Test
    void shouldStream() {

        SelectQuery query = select().from("Person").build();
        CommunicationEntity entity = CommunicationEntity.of("Person");
        entity.add("_id", 1L);
        entity.add("name", "Ada");
        entity.add("age", 20);
        Mockito.when(managerMock.select(query)).thenReturn(Stream.of(entity));
        Stream<Person> result = template.select(Person.class).stream();
        Assertions.assertNotNull(result);
    }

    @Test
    void shouldSingleResult() {

        SelectQuery query = select().from("Person").build();
        CommunicationEntity entity = CommunicationEntity.of("Person");
        entity.add("_id", 1L);
        entity.add("name", "Ada");
        entity.add("age", 20);
        Mockito.when(managerMock.select(query)).thenReturn(Stream.of(entity));
        Optional<Person> result = template.select(Person.class).singleResult();
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isPresent());
    }

    @Test
    void shouldReturnErrorSelectWhenOrderIsNull() {
        Assertions.assertThrows(NullPointerException.class, () -> template.select(Worker.class).orderBy(null));
    }

}
