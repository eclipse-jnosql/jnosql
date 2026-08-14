/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses.
 *
 */
package org.eclipse.jnosql.communication.semistructured;

import jakarta.data.exceptions.NonUniqueResultException;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.communication.Params;
import org.eclipse.jnosql.communication.QueryException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@DisplayName("Communication prepared statement")
class CommunicationPreparedStatementTest {

    private static final String QUERY = "FROM God WHERE age = :age";

    private final DatabaseManager manager = mock(DatabaseManager.class);

    @Nested
    @DisplayName("When binding query parameters")
    class WhenTheParameterBinding {

        @Test
        @DisplayName("Should bind the named parameter before executing the search")
        void shouldBindNamedParameter() {

            // Given
            var params = Params.newParams();
            var statement = selectStatement(selectWithNamedParameter(params), params);

            // When
            statement.bind("age", 12);
            statement.result().toList();

            // Then
            var captor = ArgumentCaptor.forClass(SelectQuery.class);
            verify(manager).select(captor.capture());

            var condition = captor.getValue().condition().orElseThrow();
            var element = condition.element();
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
                softly.assertThat(element.name()).isEqualTo("age");
                softly.assertThat(element.get()).isEqualTo(12);
            });
        }

        @Test
        @DisplayName("Should bind the positional parameter before executing the search")
        void shouldBindPositionalParameter() {

            // Given
            var params = Params.newParams();
            var statement = selectStatement(selectWithPositionalParameter(params), params);

            // When
            statement.bind(1, 12);
            statement.result().toList();

            // Then
            var captor = ArgumentCaptor.forClass(SelectQuery.class);
            verify(manager).select(captor.capture());

            assertThat(captor.getValue().condition().orElseThrow().element().get()).isEqualTo(12);
        }

        @Test
        @DisplayName("Should return the same prepared statement")
        void shouldReturnSameStatement() {

            // Given
            var params = Params.newParams();
            var statement = selectStatement(selectWithNamedParameter(params), params);

            // When
            var result = statement.bind("age", 12);

            // Then
            assertThat(result).isSameAs(statement);
        }

        @Test
        @DisplayName("Should reject a null parameter name")
        void shouldRejectNullName() {

            // Given
            var statement = selectStatement();

            // When / Then
            assertThatThrownBy(() -> statement.bind(null, 12))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("name is required");
        }

        @Test
        @DisplayName("Should reject a null parameter value")
        void shouldRejectNullValue() {

            // Given
            var statement = selectStatement();

            // When / Then
            assertThatThrownBy(() -> statement.bind("age", null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("value is required");
        }

        @Test
        @DisplayName("Should reject a null positional parameter value")
        void shouldRejectNullPositionalValue() {

            // Given
            var statement = selectStatement();

            // When / Then
            assertThatThrownBy(() -> statement.bind(1, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("value is required");
        }

        @Test
        @DisplayName("Should reject a positional parameter below one")
        void shouldRejectInvalidIndex() {

            // Given
            var statement = selectStatement();

            // When / Then
            assertThatThrownBy(() -> statement.bind(0, 12))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("The index should be greater than zero");
        }
    }

    @Nested
    @DisplayName("When executing a search")
    class WhenTheSearch {

        @Test
        @DisplayName("Should return the selected entities")
        void shouldReturnSelectedEntities() {

            // Given
            var entity = mock(CommunicationEntity.class);
            var statement = selectStatement();
            when(manager.select(any(SelectQuery.class))).thenReturn(Stream.of(entity));

            // When
            var result = statement.result().toList();

            // Then
            assertThat(result).containsExactly(entity);
        }

        @Test
        @DisplayName("Should expose the select query")
        void shouldExposeSelectQuery() {

            // Given
            var selectQuery = selectQuery();
            var statement = selectStatement(selectQuery, Params.newParams());

            // When
            var result = statement.select();

            // Then
            assertThat(result).contains(selectQuery);
        }

        @Test
        @DisplayName("Should reject execution when parameters remain unbound")
        void shouldRejectUnboundParameters() {

            // Given
            var params = Params.newParams();
            var statement = selectStatement(selectWithNamedParameter(params), params);

            // When / Then
            assertThatThrownBy(statement::result)
                    .isInstanceOf(QueryException.class)
                    .hasMessageContaining("Check all the parameters before execute the query")
                    .hasMessageContaining("age");
            verifyNoInteractions(manager);
        }
    }

    @Nested
    @DisplayName("When customizing a search")
    class WhenTheSearchCustomization {

        @Test
        @DisplayName("Should use the original select query by default")
        void shouldUseOriginalSelectQuery() {

            // Given
            var selectQuery = selectQuery();
            var statement = selectStatement(selectQuery, Params.newParams());

            // When
            var result = statement.operator().apply(selectQuery);

            // Then
            assertThat(result).isSameAs(selectQuery);
        }

        @Test
        @DisplayName("Should apply the replacement select query")
        void shouldApplyReplacementSelectQuery() {

            // Given
            var originalQuery = selectQuery();
            var mappedQuery = SelectQuery.select().from("Hero").build();
            var statement = selectStatement(originalQuery, Params.newParams());
            UnaryOperator<SelectQuery> mapper = query -> mappedQuery;

            // When
            statement.setSelectMapper(mapper);
            statement.result().toList();

            // Then
            verify(manager).select(mappedQuery);
            verify(manager, never()).select(originalQuery);
            assertThat(statement.operator()).isSameAs(mapper);
        }

        @Test
        @DisplayName("Should reject a null select mapper")
        void shouldRejectNullSelectMapper() {

            // Given
            var statement = selectStatement();

            // When / Then
            assertThatThrownBy(() -> statement.setSelectMapper(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("selectMapper is required");
        }
    }

    @Nested
    @DisplayName("When returning a single result")
    class WhenTheSingleResult {

        @Test
        @DisplayName("Should return the selected entity")
        void shouldReturnSelectedEntity() {

            // Given
            var entity = mock(CommunicationEntity.class);
            var statement = selectStatement();
            when(manager.select(any(SelectQuery.class))).thenReturn(Stream.of(entity));

            // When
            var result = statement.singleResult();

            // Then
            assertThat(result).contains(entity);
        }

        @Test
        @DisplayName("Should return empty when no entity is selected")
        void shouldReturnEmptyWhenNoEntityIsSelected() {

            // Given
            var statement = selectStatement();
            when(manager.select(any(SelectQuery.class))).thenReturn(Stream.empty());

            // When
            var result = statement.singleResult();

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should reject non unique search results")
        void shouldRejectNonUniqueResults() {

            // Given
            var first = mock(CommunicationEntity.class);
            var second = mock(CommunicationEntity.class);
            var statement = selectStatement();
            when(manager.select(any(SelectQuery.class))).thenReturn(Stream.of(first, second));

            // When / Then
            assertThatThrownBy(statement::singleResult)
                    .isInstanceOf(NonUniqueResultException.class)
                    .hasMessageContaining("The select returns more than one entity")
                    .hasMessageContaining(QUERY);
        }
    }

    @Nested
    @DisplayName("When executing a removal")
    class WhenTheRemoval {

        @Test
        @DisplayName("Should delete matching entities")
        void shouldDeleteMatchingEntities() {

            // Given
            var deleteQuery = deleteQuery();
            var statement = CommunicationPreparedStatement.delete(deleteQuery, Params.newParams(), QUERY, manager);

            // When
            var result = statement.result().toList();

            // Then
            assertThat(result).isEmpty();
            verify(manager).delete(deleteQuery);
        }

        @Test
        @DisplayName("Should return the number of deleted entities")
        void shouldReturnNumberOfDeletedEntities() {

            // Given
            var deleteQuery = deleteQuery();
            var statement = CommunicationPreparedStatement.delete(deleteQuery, Params.newParams(), QUERY, manager);
            when(manager.deleteAndCount(deleteQuery)).thenReturn(3L);

            // When
            var result = statement.count();

            // Then
            assertThat(result).isEqualTo(3L);
            verify(manager).deleteAndCount(deleteQuery);
            verify(manager, never()).delete(deleteQuery);
        }

        @Test
        @DisplayName("Should reject removal when parameters remain unbound")
        void shouldRejectUnboundParameters() {

            // Given
            var params = Params.newParams();
            var statement = CommunicationPreparedStatement.delete(deleteWithNamedParameter(params), params, QUERY, manager);

            // When / Then
            assertThatThrownBy(statement::result)
                    .isInstanceOf(QueryException.class)
                    .hasMessageContaining("Check all the parameters before execute the query")
                    .hasMessageContaining("age");
            verifyNoInteractions(manager);
        }
    }

    @Nested
    @DisplayName("When executing an update")
    class WhenTheUpdate {

        @Test
        @DisplayName("Should update matching entities")
        void shouldUpdateMatchingEntities() {

            // Given
            var updateQuery = mock(UpdateQuery.class);
            var statement = CommunicationPreparedStatement.update(updateQuery, Params.newParams(), QUERY, manager);

            // When
            var result = statement.result().toList();

            // Then
            assertThat(result).isEmpty();
            verify(manager).update(updateQuery);
        }

        @Test
        @DisplayName("Should reject update when parameters remain unbound")
        void shouldRejectUnboundParameters() {

            // Given
            var params = Params.newParams();
            var updateQuery = updateWithNamedParameter(params);
            var statement = CommunicationPreparedStatement.update(updateQuery, params, QUERY, manager);

            // When / Then
            assertThatThrownBy(statement::result)
                    .isInstanceOf(QueryException.class)
                    .hasMessageContaining("Check all the parameters before execute the query")
                    .hasMessageContaining("age");
            verifyNoInteractions(manager);
        }
    }

    @Nested
    @DisplayName("When counting query results")
    class WhenTheCount {

        @Test
        @DisplayName("Should return the number of selected entities")
        void shouldReturnNumberOfSelectedEntities() {

            // Given
            var selectQuery = DefaultSelectQuery.countBy(selectQuery());
            var statement = selectStatement(selectQuery, Params.newParams());
            when(manager.count(selectQuery)).thenReturn(5L);

            // When
            var result = statement.count();

            // Then
            assertThat(result).isEqualTo(5L);
            verify(manager).count(selectQuery);
            verify(manager, never()).select(any(SelectQuery.class));
        }

        @Test
        @DisplayName("Should reject count for a plain search")
        void shouldRejectPlainSearch() {

            // Given
            var statement = selectStatement();

            // When / Then
            assertThatThrownBy(statement::count)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("The count operation is only allowed for COUNT and DELETE queries");
            verifyNoInteractions(manager);
        }

        @Test
        @DisplayName("Should reject count for an update")
        void shouldRejectUpdate() {

            // Given
            var statement = CommunicationPreparedStatement.update(mock(UpdateQuery.class), Params.newParams(), QUERY, manager);

            // When / Then
            assertThatThrownBy(statement::count)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("The count operation is only allowed for COUNT and DELETE queries");
            verifyNoInteractions(manager);
        }

        @Test
        @DisplayName("Should reject count when parameters remain unbound")
        void shouldRejectUnboundParameters() {

            // Given
            var params = Params.newParams();
            var selectQuery = DefaultSelectQuery.countBy(selectWithNamedParameter(params));
            var statement = selectStatement(selectQuery, params);

            // When / Then
            assertThatThrownBy(statement::count)
                    .isInstanceOf(QueryException.class)
                    .hasMessageContaining("Check all the parameters before execute the query")
                    .hasMessageContaining("age");
            verifyNoInteractions(manager);
        }

        @Test
        @DisplayName("Should reject removal count when parameters remain unbound")
        void shouldRejectRemovalCountWithUnboundParameters() {

            // Given
            var params = Params.newParams();
            var statement = CommunicationPreparedStatement.delete(deleteWithNamedParameter(params), params, QUERY, manager);

            // When / Then
            assertThatThrownBy(statement::count)
                    .isInstanceOf(QueryException.class)
                    .hasMessageContaining("Check all the parameters before execute the query")
                    .hasMessageContaining("age");
            verifyNoInteractions(manager);
        }
    }

    @Nested
    @DisplayName("When creating a prepared statement")
    class WhenTheCreation {

        @Test
        @DisplayName("Should classify a plain select query as a search")
        void shouldClassifyPlainSelectAsSearch() {

            // Given
            var statement = selectStatement();

            // When
            var type = statement.getType();

            // Then
            assertThat(type).isEqualTo(CommunicationPreparedStatement.PreparedStatementType.SELECT);
        }

        @Test
        @DisplayName("Should classify a count select query as a count")
        void shouldClassifyCountSelectAsCount() {

            // Given
            var countQuery = DefaultSelectQuery.countBy(selectQuery());
            var statement = selectStatement(countQuery, Params.newParams());

            // When
            var type = statement.getType();

            // Then
            assertThat(type).isEqualTo(CommunicationPreparedStatement.PreparedStatementType.COUNT);
        }

        @Test
        @DisplayName("Should classify a delete query as a removal")
        void shouldClassifyDeleteAsRemoval() {

            // Given
            var statement = CommunicationPreparedStatement.delete(deleteQuery(), Params.newParams(), QUERY, manager);

            // When
            var type = statement.getType();

            // Then
            assertThat(type).isEqualTo(CommunicationPreparedStatement.PreparedStatementType.DELETE);
        }

        @Test
        @DisplayName("Should classify an update query as an update")
        void shouldClassifyUpdateAsUpdate() {

            // Given
            var statement = CommunicationPreparedStatement.update(mock(UpdateQuery.class), Params.newParams(), QUERY, manager);

            // When
            var type = statement.getType();

            // Then
            assertThat(type).isEqualTo(CommunicationPreparedStatement.PreparedStatementType.UPDATE);
        }

        @Test
        @DisplayName("Should keep the original query text")
        void shouldKeepOriginalQueryText() {

            // Given
            var statement = selectStatement();

            // When
            var result = statement.toString();

            // Then
            assertThat(result).isEqualTo(QUERY);
        }

        @Test
        @DisplayName("Should not expose a select query for a removal")
        void shouldNotExposeSelectQueryForRemoval() {

            // Given
            var statement = CommunicationPreparedStatement.delete(deleteQuery(), Params.newParams(), QUERY, manager);

            // When
            var result = statement.select();

            // Then
            assertThat(result).isEmpty();
        }
    }

    private CommunicationPreparedStatement selectStatement() {
        return selectStatement(selectQuery(), Params.newParams());
    }

    private CommunicationPreparedStatement selectStatement(SelectQuery selectQuery, Params params) {
        return CommunicationPreparedStatement.select(selectQuery, params, QUERY, manager);
    }

    private SelectQuery selectQuery() {
        return SelectQuery.select().from("God").build();
    }

    private SelectQuery selectWithNamedParameter(Params params) {
        return SelectQuery.select().from("God").where("age").eq(params.add("age")).build();
    }

    private SelectQuery selectWithPositionalParameter(Params params) {
        return SelectQuery.select().from("God").where("age").eq(params.add("?1")).build();
    }

    private DeleteQuery deleteQuery() {
        return DeleteQuery.delete().from("God").where("age").eq(12).build();
    }

    private DeleteQuery deleteWithNamedParameter(Params params) {
        return DeleteQuery.delete().from("God").where("age").eq(params.add("age")).build();
    }

    private UpdateQuery updateWithNamedParameter(Params params) {
        var condition = CriteriaCondition.eq(Element.of("age", params.add("age")));
        return new DefaultUpdateQuery("God", List.of(Element.of("name", "Zeus")), condition);
    }
}
