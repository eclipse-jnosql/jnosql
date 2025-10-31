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
package org.eclipse.jnosql.mapping.semistructured;


import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.nosql.TypedQuery;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.communication.TypeReference;
import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;
import org.eclipse.jnosql.communication.semistructured.DatabaseManager;
import org.eclipse.jnosql.communication.semistructured.DeleteQuery;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.communication.semistructured.UpdateQuery;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.semistructured.entities.Person;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.mockito.Mockito.when;

@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class})
public class TypedQueryTest {

    @Inject
    private EntityConverter converter;

    @Inject
    private EntitiesMetadata entities;

    @Inject
    private Converters converters;

    private DatabaseManager managerMock;

    private DefaultSemiStructuredTemplate template;

    private ArgumentCaptor<SelectQuery> selectCaptor;

    private ArgumentCaptor<DeleteQuery> deleteCaptor;

    private ArgumentCaptor<UpdateQuery> updateCaptor;

    @BeforeEach
    public void setUp() {
        managerMock = Mockito.mock(DatabaseManager.class);
        EventPersistManager persistManager = Mockito.mock(EventPersistManager.class);
        Instance<DatabaseManager> instance = Mockito.mock(Instance.class);
        this.selectCaptor = ArgumentCaptor.forClass(SelectQuery.class);
        this.deleteCaptor = ArgumentCaptor.forClass(DeleteQuery.class);
        this.updateCaptor = ArgumentCaptor.forClass(UpdateQuery.class);
        when(instance.get()).thenReturn(managerMock);
        this.template = new DefaultSemiStructuredTemplate(converter, instance,
                persistManager, entities, converters);
    }

    @ParameterizedTest
    @ValueSource(strings ="FROM Person")
    @DisplayName("Should execute a simple query using From with List")
    void shouldSelectFrom(String textQuery){
        TypedQuery<Person> query = this.template.typedQuery(textQuery, Person.class);
        query.result();

        Mockito.verify(managerMock).select(selectCaptor.capture());
        SelectQuery selectQuery = selectCaptor.getValue();

        SoftAssertions.assertSoftly(soft ->{
            soft.assertThat(selectQuery.name()).isEqualTo("Person");
            soft.assertThat(selectQuery.condition()).isEmpty();
            soft.assertThat(selectQuery.sorts()).isEmpty();
            soft.assertThat(selectQuery.isCount()).isFalse();
        });
    }

    @ParameterizedTest
    @ValueSource(strings ="FROM Person WHERE name = 'Ada'")
    @DisplayName("Should execute a simple query using From with Stream")
    void shouldSelectFromStream(String textQuery){
        TypedQuery<Person> query = this.template.typedQuery(textQuery, Person.class);
        query.stream();

        Mockito.verify(managerMock).select(selectCaptor.capture());
        SelectQuery selectQuery = selectCaptor.getValue();

        SoftAssertions.assertSoftly(soft ->{
            soft.assertThat(selectQuery.name()).isEqualTo("Person");
            soft.assertThat(selectQuery.condition()).isNotEmpty();
            soft.assertThat(selectQuery.sorts()).isEmpty();
            soft.assertThat(selectQuery.isCount()).isFalse();
        });
    }

    @ParameterizedTest
    @ValueSource(strings ="FROM Person WHERE name = 'Ada'")
    @DisplayName("Should execute a simple query using From with Single Result")
    void shouldSelectFromSingleResult(String textQuery){
        TypedQuery<Person> query = this.template.typedQuery(textQuery, Person.class);
        query.singleResult();

        Mockito.verify(managerMock).select(selectCaptor.capture());
        SelectQuery selectQuery = selectCaptor.getValue();

        SoftAssertions.assertSoftly(soft ->{
            soft.assertThat(selectQuery.name()).isEqualTo("Person");
            soft.assertThat(selectQuery.condition()).isNotEmpty();
            soft.assertThat(selectQuery.sorts()).isEmpty();
            soft.assertThat(selectQuery.isCount()).isFalse();
        });
    }

    @ParameterizedTest
    @ValueSource(strings = "SELECT count(this) FROM Person WHERE name = 'Ada' ORDER BY name")
    @DisplayName("Should execute a simple query using From with Single Result")
    void shouldSelectFromSingleResultAsCount(String textQuery){
        Mockito.when(managerMock.count(Mockito.any(SelectQuery.class))).thenReturn(1L);
        TypedQuery<Person> query = this.template.typedQuery(textQuery, Person.class);
        var count = query.singleResult();

        Mockito.verify(managerMock).count(selectCaptor.capture());
        SelectQuery selectQuery = selectCaptor.getValue();

        SoftAssertions.assertSoftly(soft ->{
            soft.assertThat(count).isNotEmpty().get().isEqualTo(1L);
            soft.assertThat(selectQuery.name()).isEqualTo("Person");
            soft.assertThat(selectQuery.condition()).isNotEmpty();
            soft.assertThat(selectQuery.sorts()).isNotEmpty();
            soft.assertThat(selectQuery.isCount()).isTrue();
        });
    }

    @ParameterizedTest
    @ValueSource(strings = "SELECT count(this) FROM Person WHERE name = 'Ada' ORDER BY name")
    @DisplayName("Should execute a simple query using From with List")
    void shouldSelectFromSingleResultAsCountList(String textQuery){
        Mockito.when(managerMock.count(Mockito.any(SelectQuery.class))).thenReturn(1L);
        TypedQuery<Person> query = this.template.typedQuery(textQuery, Person.class);
        query.result();

        Mockito.verify(managerMock).count(selectCaptor.capture());
        SelectQuery selectQuery = selectCaptor.getValue();

        SoftAssertions.assertSoftly(soft ->{
            soft.assertThat(selectQuery.name()).isEqualTo("Person");
            soft.assertThat(selectQuery.condition()).isNotEmpty();
            soft.assertThat(selectQuery.sorts()).isNotEmpty();
            soft.assertThat(selectQuery.isCount()).isTrue();
        });
    }

    @ParameterizedTest
    @ValueSource(strings = "SELECT count(this) FROM Person WHERE name = 'Ada' ORDER BY name")
    @DisplayName("Should execute a simple query using From with Stream")
    void shouldSelectFromSingleResultAsCountStream(String textQuery){
        Mockito.when(managerMock.count(Mockito.any(SelectQuery.class))).thenReturn(1L);
        TypedQuery<Person> query = this.template.typedQuery(textQuery, Person.class);
        query.stream();

        Mockito.verify(managerMock).count(selectCaptor.capture());
        SelectQuery selectQuery = selectCaptor.getValue();

        SoftAssertions.assertSoftly(soft ->{
            soft.assertThat(selectQuery.name()).isEqualTo("Person");
            soft.assertThat(selectQuery.condition()).isNotEmpty();
            soft.assertThat(selectQuery.sorts()).isNotEmpty();
            soft.assertThat(selectQuery.isCount()).isTrue();
        });
    }

    @ParameterizedTest
    @ValueSource(strings = "FROM Person WHERE name = :name ORDER BY name")
    @DisplayName("Should execute a simple query using From with List")
    void shouldBindByName(String textQuery){
        TypedQuery<Person> query = this.template.typedQuery(textQuery, Person.class);
        query.bind("name", "Ada");
        query.stream();

        Mockito.verify(managerMock).select(selectCaptor.capture());
        SelectQuery selectQuery = selectCaptor.getValue();

        SoftAssertions.assertSoftly(soft ->{
            var condition = selectQuery.condition().orElseThrow();

            soft.assertThat(selectQuery.name()).isEqualTo("Person");
            soft.assertThat(selectQuery.sorts()).isNotEmpty();
            soft.assertThat(selectQuery.isCount()).isFalse();
            soft.assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(condition.element().get()).isEqualTo("Ada");
        });
    }

    @ParameterizedTest
    @ValueSource(strings = "FROM Person WHERE name = ?1 ORDER BY name")
    @DisplayName("Should execute a simple query using From with List")
    void shouldBindByPosition(String textQuery){
        TypedQuery<Person> query = this.template.typedQuery(textQuery, Person.class);
        query.bind(1, "Ada");
        query.stream();

        Mockito.verify(managerMock).select(selectCaptor.capture());
        SelectQuery selectQuery = selectCaptor.getValue();

        SoftAssertions.assertSoftly(soft ->{
            var condition = selectQuery.condition().orElseThrow();

            soft.assertThat(selectQuery.name()).isEqualTo("Person");
            soft.assertThat(selectQuery.sorts()).isNotEmpty();
            soft.assertThat(selectQuery.isCount()).isFalse();
            soft.assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(condition.element().get()).isEqualTo("Ada");
        });
    }

    @ParameterizedTest
    @ValueSource(strings = "FROM Person WHERE name = ?1 AND age = :age ORDER BY name")
    @DisplayName("Should execute a simple query using From with List")
    void shouldBindByBoth(String textQuery){
        TypedQuery<Person> query = this.template.typedQuery(textQuery, Person.class);
        query.bind(1, "Ada");
        query.bind("age", 20);
        query.stream();

        Mockito.verify(managerMock).select(selectCaptor.capture());
        SelectQuery selectQuery = selectCaptor.getValue();

        SoftAssertions.assertSoftly(soft ->{
            var condition = selectQuery.condition().orElseThrow();
            var conditions = condition.element().get(new TypeReference<List<CriteriaCondition>>() {
            });
            var values = conditions.stream().map(c -> c.element().get()).toList();
            soft.assertThat(selectQuery.name()).isEqualTo("Person");
            soft.assertThat(selectQuery.sorts()).isNotEmpty();
            soft.assertThat(selectQuery.isCount()).isFalse();
            soft.assertThat(condition.condition()).isEqualTo(Condition.AND);
            soft.assertThat(values).containsExactly("Ada", 20);
        });
    }

    @ParameterizedTest
    @ValueSource(strings = "SELECT count(this) FROM Person WHERE name = 'Ada' ORDER BY name")
    @DisplayName("Should execute a simple query using From with List")
    void shouldReturnErrorWhenSelectExecuteUpdate(String textQuery){
        Mockito.when(managerMock.count(Mockito.any(SelectQuery.class))).thenReturn(1L);
        TypedQuery<Person> query = this.template.typedQuery(textQuery, Person.class);
        Assertions.assertThrows(UnsupportedOperationException.class, query::executeUpdate);
    }


    @ParameterizedTest
    @ValueSource(strings = "DELETE FROM Person WHERE name = 'Ada'")
    @DisplayName("Should execute delete query")
    void shouldDelete(String textQuery){
        TypedQuery<Person> query = this.template.typedQuery(textQuery, Person.class);
        query.executeUpdate();
        Mockito.verify(managerMock).delete(deleteCaptor.capture());
        DeleteQuery deleteQuery = deleteCaptor.getValue();

        SoftAssertions.assertSoftly(soft ->{
            soft.assertThat(deleteQuery.name()).isEqualTo("Person");
            soft.assertThat(deleteQuery.condition()).isNotEmpty();
        });

    }


    @ParameterizedTest
    @ValueSource(strings = "UPDATE Person SET age = 19 WHERE name = 'Ada'")
    @DisplayName("Should execute update query")
    void shouldUpdate(String textQuery){
        TypedQuery<Person> query = this.template.typedQuery(textQuery, Person.class);
        query.executeUpdate();
        Mockito.verify(managerMock).update(updateCaptor.capture());
        UpdateQuery updateQuery = updateCaptor.getValue();

        SoftAssertions.assertSoftly(soft ->{
            soft.assertThat(updateQuery.name()).isEqualTo("Person");
            soft.assertThat(updateQuery.condition()).isNotEmpty();
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"DELETE FROM Person WHERE name = 'Ada'", "UPDATE Person SET age = 19 WHERE name = 'Ada'"})
    @DisplayName("Should return error when modification execute single result")
    void shouldReturnErrorWhenModificationExecuteSingleResult(String textQuery){
        TypedQuery<Person> query = this.template.typedQuery(textQuery, Person.class);
        Assertions.assertThrows(UnsupportedOperationException.class, query::singleResult);
    }

    @ParameterizedTest
    @ValueSource(strings = {"DELETE FROM Person WHERE name = 'Ada'", "UPDATE Person SET age = 19 WHERE name = 'Ada'"})
    @DisplayName("Should return error when modification execute List")
    void shouldReturnErrorWhenModificationExecuteList(String textQuery){
        TypedQuery<Person> query = this.template.typedQuery(textQuery, Person.class);
        Assertions.assertThrows(UnsupportedOperationException.class, query::result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"DELETE FROM Person WHERE name = 'Ada'", "UPDATE Person SET age = 19 WHERE name = 'Ada'"})
    @DisplayName("Should return error when modification execute Stream")
    void shouldReturnErrorWhenModificationExecuteStream(String textQuery){
        TypedQuery<Person> query = this.template.typedQuery(textQuery, Person.class);
        Assertions.assertThrows(UnsupportedOperationException.class, query::stream);
    }

}
