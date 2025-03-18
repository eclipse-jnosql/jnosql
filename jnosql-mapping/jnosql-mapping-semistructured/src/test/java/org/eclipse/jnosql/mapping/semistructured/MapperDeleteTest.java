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
import org.eclipse.jnosql.communication.semistructured.DatabaseManager;
import org.eclipse.jnosql.communication.semistructured.DeleteQuery;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.semistructured.entities.Address;
import org.eclipse.jnosql.mapping.semistructured.entities.Money;
import org.eclipse.jnosql.mapping.semistructured.entities.Person;
import org.eclipse.jnosql.mapping.semistructured.entities.Worker;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.math.BigDecimal;

import static org.eclipse.jnosql.communication.semistructured.DeleteQuery.delete;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class})
class MapperDeleteTest {

    @Inject
    private EntityConverter converter;

    @Inject
    private EntitiesMetadata entities;

    @Inject
    private Converters converters;

    private DatabaseManager managerMock;

    private DefaultSemiStructuredTemplate template;


    private ArgumentCaptor<DeleteQuery> captor;

    @BeforeEach
    void setUp() {
        managerMock = Mockito.mock(DatabaseManager.class);
        EventPersistManager persistManager = Mockito.mock(EventPersistManager.class);
        Instance<DatabaseManager> instance = Mockito.mock(Instance.class);
        this.captor = ArgumentCaptor.forClass(DeleteQuery.class);
        when(instance.get()).thenReturn(managerMock);
        this.template = new DefaultSemiStructuredTemplate(converter, instance,
                persistManager, entities, converters);
    }

    @Test
    void shouldReturnDeleteFrom() {
        template.delete(Person.class).execute();
        Mockito.verify(managerMock).delete(captor.capture());
        DeleteQuery query = captor.getValue();
        var queryExpected = delete().from("Person").build();
        assertEquals(queryExpected, query);
    }


    @Test
    void shouldDeleteWhereEq() {
        template.delete(Person.class).where("name").eq("Ada").execute();
        Mockito.verify(managerMock).delete(captor.capture());
        var query = captor.getValue();

        var queryExpected =  delete().from("Person").where("name")
                .eq("Ada").build();
        assertEquals(queryExpected, query);
    }

    @Test
    void shouldDeleteWhereLike() {
        template.delete(Person.class).where("name").like("Ada").execute();
        Mockito.verify(managerMock).delete(captor.capture());
        var query = captor.getValue();
        var queryExpected = delete().from("Person").where("name")
                .like("Ada").build();
        assertEquals(queryExpected, query);
    }

    @Test
    void shouldDeleteWhereGt() {
        template.delete(Person.class).where("id").gt(10).execute();
        Mockito.verify(managerMock).delete(captor.capture());
        var query = captor.getValue();
        var queryExpected = delete().from("Person").where("_id").gt(10L).build();
        assertEquals(queryExpected, query);
    }

    @Test
    void shouldDeleteWhereGte() {
        template.delete(Person.class).where("id").gte(10).execute();
        Mockito.verify(managerMock).delete(captor.capture());
        var query = captor.getValue();
        var queryExpected = delete().from("Person").where("_id")
                .gte(10L).build();
        assertEquals(queryExpected, query);
    }

    @Test
    void shouldDeleteWhereLt() {
        template.delete(Person.class).where("id").lt(10).execute();
        Mockito.verify(managerMock).delete(captor.capture());
        var query = captor.getValue();
        var queryExpected = delete().from("Person").where("_id").lt(10L).build();
        assertEquals(queryExpected, query);
    }

    @Test
    void shouldDeleteWhereLte() {
        template.delete(Person.class).where("id").lte(10).execute();
        Mockito.verify(managerMock).delete(captor.capture());
        var query = captor.getValue();
        var queryExpected = delete().from("Person").where("_id").lte(10L).build();
        assertEquals(queryExpected, query);
    }

    @Test
    void shouldDeleteWhereBetween() {
        template.delete(Person.class).where("id")
                .between(10, 20).execute();
        Mockito.verify(managerMock).delete(captor.capture());
        var query = captor.getValue();
        var queryExpected = delete().from("Person").where("_id")
                .between(10L, 20L).build();
        assertEquals(queryExpected, query);
    }

    @Test
    void shouldDeleteWhereNot() {
        template.delete(Person.class).where("name").not().like("Ada").execute();
        Mockito.verify(managerMock).delete(captor.capture());
        var query = captor.getValue();
        var queryExpected = delete().from("Person").where("name").not().like("Ada").build();
        assertEquals(queryExpected, query);
    }


    @Test
    void shouldDeleteWhereAnd() {
        template.delete(Person.class).where("age").between(10, 20)
                .and("name").eq("Ada").execute();
        Mockito.verify(managerMock).delete(captor.capture());
        var query = captor.getValue();
        var queryExpected = delete().from("Person").where("age")
                .between(10, 20)
                .and("name").eq("Ada").build();

        assertEquals(queryExpected, query);
    }

    @Test
    void shouldDeleteWhereOr() {
        template.delete(Person.class).where("id").between(10, 20)
                .or("name").eq("Ada").execute();
        Mockito.verify(managerMock).delete(captor.capture());
        var query = captor.getValue();
        var queryExpected = delete().from("Person").where("_id")
                .between(10L, 20L)
                .or("name").eq("Ada").build();

        assertEquals(queryExpected, query);
    }

    @Test
    void shouldConvertField() {
        template.delete(Person.class).where("id").eq("20")
                .execute();
        Mockito.verify(managerMock).delete(captor.capture());
        var query = captor.getValue();
        var queryExpected = delete().from("Person").where("_id").eq(20L)
                .build();

        assertEquals(queryExpected, query);
    }

    @Test
    void shouldUseAttributeConverter() {
        template.delete(Worker.class).where("salary")
                .eq(new Money("USD", BigDecimal.TEN)).execute();
        Mockito.verify(managerMock).delete(captor.capture());
        var query = captor.getValue();
        var queryExpected = delete().from("Worker").where("money")
                .eq("USD 10").build();
        assertEquals(queryExpected, query);
    }

    @Test
    void shouldQueryByEmbeddable() {
        template.delete(Worker.class).where("job.city").eq("Salvador")
                .execute();

        Mockito.verify(managerMock).delete(captor.capture());
        var query = captor.getValue();
        var queryExpected = delete().from("Worker").where("city").eq("Salvador")
                .build();

        assertEquals(queryExpected, query);
    }

    @Test
    void shouldQueryBySubEntity() {
        template.delete(Address.class).where("zipCode.zip").eq("01312321")
                .execute();

        Mockito.verify(managerMock).delete(captor.capture());
        var query = captor.getValue();

        var queryExpected = delete().from("Address").where("zipCode.zip").eq("01312321")
                .build();

        assertEquals(queryExpected, query);
    }
}
