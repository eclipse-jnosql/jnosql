/*
 *  Copyright (c) 2024 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *   and Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 */
package org.eclipse.jnosql.mapping.semistructured;

import jakarta.inject.Inject;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.communication.semistructured.CommunicationEntity;
import org.eclipse.jnosql.communication.semistructured.CommunicationPreparedStatement;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.semistructured.entities.Person;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class})
@DisplayName("Prepared statement")
class PreparedStatementTest {

    @Inject
    private EntitiesMetadata entitiesMetadata;

    @Inject
    private EntityConverter converter;

    @Nested
    @DisplayName("When binding query parameters")
    class WhenTheParameterBinding {

        @Test
        @DisplayName("Should bind the named parameter in the communication statement")
        void shouldBindNamedParameter() {

            // Given
            var communicationStatement = mock(CommunicationPreparedStatement.class);
            var preparedStatement = preparedStatement(communicationStatement);

            // When
            var result = preparedStatement.bind("name", "Ada");

            // Then
            assertThat(result).isSameAs(preparedStatement);
            verify(communicationStatement).bind("name", "Ada");
        }

        @Test
        @DisplayName("Should bind the positional parameter in the communication statement")
        void shouldBindPositionalParameter() {

            // Given
            var communicationStatement = mock(CommunicationPreparedStatement.class);
            var preparedStatement = preparedStatement(communicationStatement);

            // When
            var result = preparedStatement.bind(1, "Ada");

            // Then
            assertThat(result).isSameAs(preparedStatement);
            verify(communicationStatement).bind(1, "Ada");
        }
    }

    @Nested
    @DisplayName("When executing a search")
    class WhenTheSearch {

        @Test
        @DisplayName("Should return mapped entities")
        void shouldReturnMappedEntities() {

            // Given
            var communicationStatement = mock(CommunicationPreparedStatement.class);
            var entity = personEntity();
            when(communicationStatement.result()).thenReturn(Stream.of(entity));
            var preparedStatement = preparedStatement(communicationStatement);

            // When
            Stream<Person> people = preparedStatement.result();

            // Then
            assertThat(people).singleElement().satisfies(person -> {
                SoftAssertions.assertSoftly(softly -> {
                    softly.assertThat(person.getName()).isEqualTo("Ada");
                    softly.assertThat(person.getAge()).isEqualTo(20);
                    softly.assertThat(person.getId()).isEqualTo(20L);
                });
            });
        }

        @Test
        @DisplayName("Should return the selected field")
        void shouldReturnSelectedField() {

            // Given
            var communicationStatement = mock(CommunicationPreparedStatement.class);
            when(communicationStatement.result()).thenReturn(Stream.of(personEntity()));
            var mapperObserver = observerSelectingPersonFields("name");
            var preparedStatement = preparedStatement(communicationStatement, mapperObserver);

            // When
            Stream<String> names = preparedStatement.result();

            // Then
            assertThat(names).containsExactly("Ada");
        }

        @Test
        @DisplayName("Should return the selected fields")
        void shouldReturnSelectedFields() {

            // Given
            var communicationStatement = mock(CommunicationPreparedStatement.class);
            when(communicationStatement.result()).thenReturn(Stream.of(personEntity()));
            var mapperObserver = observerSelectingPersonFields("name", "age");
            var preparedStatement = preparedStatement(communicationStatement, mapperObserver);

            // When
            Stream<Object[]> fields = preparedStatement.result();

            // Then
            assertThat(fields).singleElement().satisfies(values -> assertThat(values).containsExactly("Ada", 20));
        }

        @Test
        @DisplayName("Should expose the select query from the communication statement")
        void shouldExposeSelectQuery() {

            // Given
            var communicationStatement = mock(CommunicationPreparedStatement.class);
            var selectQuery = SelectQuery.select().from("Person").build();
            when(communicationStatement.select()).thenReturn(Optional.of(selectQuery));
            var preparedStatement = preparedStatement(communicationStatement);

            // When
            var result = preparedStatement.selectQuery();

            // Then
            assertThat(result).contains(selectQuery);
        }
    }

    @Nested
    @DisplayName("When returning a single result")
    class WhenTheSingleResult {

        @Test
        @DisplayName("Should return the mapped entity")
        void shouldReturnMappedEntity() {

            // Given
            var communicationStatement = mock(CommunicationPreparedStatement.class);
            when(communicationStatement.singleResult()).thenReturn(Optional.of(personEntity()));
            var preparedStatement = preparedStatement(communicationStatement);

            // When
            Optional<Person> person = preparedStatement.singleResult();

            // Then
            assertThat(person).hasValueSatisfying(value -> {
                SoftAssertions.assertSoftly(softly -> {
                    softly.assertThat(value.getName()).isEqualTo("Ada");
                    softly.assertThat(value.getAge()).isEqualTo(20);
                    softly.assertThat(value.getId()).isEqualTo(20L);
                });
            });
        }

        @Test
        @DisplayName("Should return empty when the communication statement has no entity")
        void shouldReturnEmptyWhenNoEntityExists() {

            // Given
            var communicationStatement = mock(CommunicationPreparedStatement.class);
            when(communicationStatement.singleResult()).thenReturn(Optional.empty());
            var preparedStatement = preparedStatement(communicationStatement);

            // When
            Optional<Person> person = preparedStatement.singleResult();

            // Then
            assertThat(person).isEmpty();
        }

        @Test
        @DisplayName("Should return the selected field")
        void shouldReturnSelectedField() {

            // Given
            var communicationStatement = mock(CommunicationPreparedStatement.class);
            when(communicationStatement.singleResult()).thenReturn(Optional.of(personEntity()));
            var mapperObserver = observerSelectingPersonFields("name");
            var preparedStatement = preparedStatement(communicationStatement, mapperObserver);

            // When
            Optional<String> name = preparedStatement.singleResult();

            // Then
            assertThat(name).contains("Ada");
        }

        @Test
        @DisplayName("Should return the selected fields")
        void shouldReturnSelectedFields() {

            // Given
            var communicationStatement = mock(CommunicationPreparedStatement.class);
            when(communicationStatement.singleResult()).thenReturn(Optional.of(personEntity()));
            var mapperObserver = observerSelectingPersonFields("name", "age");
            var preparedStatement = preparedStatement(communicationStatement, mapperObserver);

            // When
            Optional<Object[]> fields = preparedStatement.singleResult();

            // Then
            assertThat(fields).hasValueSatisfying(values -> assertThat(values).containsExactly("Ada", 20));
        }
    }

    @Nested
    @DisplayName("When counting query results")
    class WhenTheCount {

        @Test
        @DisplayName("Should return the count from the communication statement")
        void shouldReturnCount() {

            // Given
            var communicationStatement = mock(CommunicationPreparedStatement.class);
            when(communicationStatement.count()).thenReturn(10L);
            var preparedStatement = preparedStatement(communicationStatement);

            // When
            var result = preparedStatement.count();

            // Then
            assertThat(result).isEqualTo(10L);
            verify(communicationStatement).count();
        }

        @Test
        @DisplayName("Should identify a count select query")
        void shouldIdentifyCountQuery() {

            // Given
            var communicationStatement = mock(CommunicationPreparedStatement.class);
            var query = mock(SelectQuery.class);
            when(query.isCount()).thenReturn(true);
            when(communicationStatement.select()).thenReturn(Optional.of(query));
            var preparedStatement = preparedStatement(communicationStatement);

            // When
            var result = preparedStatement.isCount();

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should not identify a plain select query as a count")
        void shouldNotIdentifyPlainSelectQueryAsCount() {

            // Given
            var communicationStatement = mock(CommunicationPreparedStatement.class);
            var query = mock(SelectQuery.class);
            when(query.isCount()).thenReturn(false);
            when(communicationStatement.select()).thenReturn(Optional.of(query));
            var preparedStatement = preparedStatement(communicationStatement);

            // When
            var result = preparedStatement.isCount();

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should not identify a statement without a select query as a count")
        void shouldNotIdentifyStatementWithoutSelectQueryAsCount() {

            // Given
            var communicationStatement = mock(CommunicationPreparedStatement.class);
            when(communicationStatement.select()).thenReturn(Optional.empty());
            var preparedStatement = preparedStatement(communicationStatement);

            // When
            var result = preparedStatement.isCount();

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("When classifying a prepared statement")
    class WhenTheClassification {

        @Test
        @DisplayName("Should return the communication statement type")
        void shouldReturnType() {

            // Given
            var communicationStatement = mock(CommunicationPreparedStatement.class);
            when(communicationStatement.getType()).thenReturn(CommunicationPreparedStatement.PreparedStatementType.SELECT);
            var preparedStatement = preparedStatement(communicationStatement);

            // When
            var result = preparedStatement.type();

            // Then
            assertThat(result).isEqualTo(CommunicationPreparedStatement.PreparedStatementType.SELECT);
        }
    }

    @Nested
    @DisplayName("When customizing a search")
    class WhenTheSearchCustomization {

        @Test
        @DisplayName("Should apply the select mapper in the communication statement")
        void shouldApplySelectMapper() {

            // Given
            var communicationStatement = mock(CommunicationPreparedStatement.class);
            UnaryOperator<SelectQuery> mapper = query -> query;
            var preparedStatement = preparedStatement(communicationStatement);

            // When
            preparedStatement.setSelectMapper(mapper);

            // Then
            verify(communicationStatement).setSelectMapper(mapper);
        }

        @Test
        @DisplayName("Should reject a null select mapper")
        void shouldRejectNullSelectMapper() {

            // Given
            var communicationStatement = mock(CommunicationPreparedStatement.class);
            var preparedStatement = preparedStatement(communicationStatement);

            // When / Then
            assertThatThrownBy(() -> preparedStatement.setSelectMapper(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("selectMapper is required");
            verify(communicationStatement, never()).setSelectMapper(null);
        }

        @Test
        @DisplayName("Should restrict inherited searches to the selected entity type")
        void shouldRestrictInheritedSearchesToSelectedEntityType() {

            // Given
            var communicationStatement = mock(CommunicationPreparedStatement.class);
            when(communicationStatement.result()).thenReturn(Stream.empty());
            var mapperObserver = new MapperObserver(entitiesMetadata);
            mapperObserver.fireEntity("SmallProject");
            var preparedStatement = preparedStatement(communicationStatement, mapperObserver);

            // When
            preparedStatement.result().toList();

            // Then
            var captor = ArgumentCaptor.forClass(UnaryOperator.class);
            verify(communicationStatement).setSelectMapper(captor.capture());

            @SuppressWarnings("unchecked")
            var mapper = (UnaryOperator<SelectQuery>) captor.getValue();
            var mappedQuery = mapper.apply(SelectQuery.select().from("Project").build());
            var condition = mappedQuery.condition().orElseThrow();
            var element = condition.element();
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
                softly.assertThat(element.name()).isEqualTo("size");
                softly.assertThat(element.get()).isEqualTo("Small");
            });
        }

        @Test
        @DisplayName("Should preserve an explicit select mapper for inherited searches")
        void shouldPreserveExplicitSelectMapperForInheritedSearches() {

            // Given
            var communicationStatement = mock(CommunicationPreparedStatement.class);
            when(communicationStatement.result()).thenReturn(Stream.empty());
            UnaryOperator<SelectQuery> mapper = query -> query;
            var mapperObserver = new MapperObserver(entitiesMetadata);
            mapperObserver.fireEntity("SmallProject");
            var preparedStatement = preparedStatement(communicationStatement, mapperObserver);

            // When
            preparedStatement.setSelectMapper(mapper);
            preparedStatement.result().toList();

            // Then
            verify(communicationStatement).setSelectMapper(mapper);
        }
    }

    private PreparedStatement preparedStatement(CommunicationPreparedStatement communicationStatement) {
        return preparedStatement(communicationStatement, new MapperObserver(entitiesMetadata));
    }

    private PreparedStatement preparedStatement(CommunicationPreparedStatement communicationStatement,
                                                MapperObserver mapperObserver) {
        return new PreparedStatement(communicationStatement, converter, mapperObserver, entitiesMetadata);
    }

    private MapperObserver observerSelectingPersonFields(String... fields) {
        var mapperObserver = new MapperObserver(entitiesMetadata);
        mapperObserver.fireEntity("Person");
        for (String field : fields) {
            mapperObserver.fireSelectField("Person", field);
        }
        return mapperObserver;
    }

    private CommunicationEntity personEntity() {
        var entity = CommunicationEntity.of("Person");
        entity.add("name", "Ada");
        entity.add("age", 20);
        entity.add("_id", 20L);
        return entity;
    }
}
