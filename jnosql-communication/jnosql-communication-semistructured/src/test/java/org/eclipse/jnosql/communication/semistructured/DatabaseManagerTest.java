/*
 *  Copyright (c) 2024,2026 Contributors to the Eclipse Foundation
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
import jakarta.data.Sort;
import jakarta.data.page.CursoredPage;
import jakarta.data.page.PageRequest;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.communication.TypeReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DatabaseManagerTest {
    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private DatabaseManager databaseManager;

    @Test
    void shouldNotSupportDeleteAndCountByDefault() {
        DeleteQuery query = DeleteQuery.delete().from("person").build();

        assertThrows(UnsupportedOperationException.class, () -> databaseManager.deleteAndCount(query));
        Mockito.verify(databaseManager, Mockito.never()).delete(Mockito.any(DeleteQuery.class));
    }

    @Test
    void shouldValidateDeleteAndCountQueryBeforeSupportCheck() {
        assertThrows(NullPointerException.class, () -> databaseManager.deleteAndCount(null));
        Mockito.verify(databaseManager, Mockito.never()).delete(Mockito.any(DeleteQuery.class));
    }


    @Test
    void shouldReturnErrorWhenThereIsNotSort() {
        SelectQuery query = SelectQuery.builder().from("person").build();
        PageRequest pageRequest = PageRequest.ofSize(10);
        assertThrows(IllegalArgumentException.class, () -> databaseManager.selectCursor(query, pageRequest));
    }

    @Test
    void shouldReturnPaginationOffSet() {
        SelectQuery query = SelectQuery.select().from("person")
                .orderBy("name").asc().build();

        Mockito.when(databaseManager.select(Mockito.any(SelectQuery.class)))
                .thenReturn(stream());

        CursoredPage<CommunicationEntity> entities = databaseManager.selectCursor(query,
                PageRequest.ofSize(10));

        assertSoftly(soft -> {
            PageRequest pageRequest = entities.pageRequest();
            PageRequest nextedPageRequest = entities.nextPageRequest();
            PageRequest.Cursor cursor = nextedPageRequest.cursor().orElseThrow();

            soft.assertThat(entities).hasSize(2);
            soft.assertThat(pageRequest.mode())
                    .isEqualTo(PageRequest.Mode.OFFSET);
            soft.assertThat(nextedPageRequest.mode())
                    .isEqualTo(PageRequest.Mode.CURSOR_NEXT);
            soft.assertThat(cursor.elements())
                    .hasSize(1);
            soft.assertThat(cursor.get(0)).isEqualTo("Poliana");

        });
    }

    @Test
    void shouldReturnPaginationOffSetWhenReturnEmpty() {
        SelectQuery query = SelectQuery.select().from("person")
                .orderBy("name").asc().build();

        Mockito.when(databaseManager.select(Mockito.any(SelectQuery.class)))
                .thenReturn(Stream.empty());

        CursoredPage<CommunicationEntity> entities = databaseManager.selectCursor(query,
                PageRequest.ofSize(10));

        assertSoftly(soft -> {
            PageRequest pageRequest = entities.pageRequest();
            soft.assertThat(entities.hasNext()).isFalse();
            soft.assertThat(entities.hasPrevious()).isFalse();

            soft.assertThat(entities).hasSize(0);
            soft.assertThat(pageRequest.mode())
                    .isEqualTo(PageRequest.Mode.OFFSET);
        });
    }

    @Test
    void shouldReturnPaginationOffSet2() {
        SelectQuery query = SelectQuery.select().from("person")
                .orderBy("name").asc()
                .orderBy("age").desc().build();


        Mockito.when(databaseManager.select(Mockito.any(SelectQuery.class)))
                .thenReturn(stream());
        CursoredPage<CommunicationEntity> entities = databaseManager.selectCursor(query,
                PageRequest.ofSize(10));

        assertSoftly(soft -> {
            PageRequest pageRequest = entities.pageRequest();
            PageRequest nextedPageRequest = entities.nextPageRequest();
            PageRequest.Cursor cursor = nextedPageRequest.cursor().orElseThrow();

            soft.assertThat(entities).hasSize(2);
            soft.assertThat(pageRequest.mode())
                    .isEqualTo(PageRequest.Mode.OFFSET);
            soft.assertThat(nextedPageRequest.mode())
                    .isEqualTo(PageRequest.Mode.CURSOR_NEXT);
            soft.assertThat(cursor.elements())
                    .hasSize(2);
            soft.assertThat(cursor.get(0)).isEqualTo("Poliana");
            soft.assertThat(cursor.get(1)).isEqualTo(35);

        });
    }

    @Test
    void shouldReturnPaginationAfterKeySingleElementWhenConditionIsNull() {
        SelectQuery query = SelectQuery.select().from("person")
                .orderBy("name").asc()
                .orderBy("age").asc()
                .orderBy("id").asc().build();

        Mockito.when(databaseManager.select(Mockito.any(SelectQuery.class)))
                .thenReturn(stream());

        var id = UUID.randomUUID().toString();
        CursoredPage<CommunicationEntity> entities = databaseManager.selectCursor(query,
                PageRequest.afterCursor(PageRequest.Cursor.forKey("Ada", 20, id), 1, 10 ,false));


        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        Mockito.verify(databaseManager).select(captor.capture());
        SelectQuery selectQuery = captor.getValue();

        CriteriaCondition condition = selectQuery.condition().orElseThrow();

        assertSoftly(soft -> {
            soft.assertThat(condition.condition()).isEqualTo(Condition.OR);
            List<CriteriaCondition> criteriaConditions = condition.element().get(new TypeReference<>() {
            });

            soft.assertThat(criteriaConditions).hasSize(3);
            soft.assertThat(criteriaConditions.get(0)).isEqualTo(CriteriaCondition.gt("name", "Ada"));
            soft.assertThat(criteriaConditions.get(1)).isEqualTo(
                    CriteriaCondition.eq("name", "Ada").and(CriteriaCondition.gt("age", 20)));
            soft.assertThat(criteriaConditions.get(2)).isEqualTo(
                    CriteriaCondition.eq("name", "Ada").and(CriteriaCondition.eq("age", 20))
                            .and(CriteriaCondition.gt("id", id)));
        });

        assertSoftly(soft -> {
            PageRequest pageRequest = entities.pageRequest();
            PageRequest nextedPageRequest = entities.nextPageRequest();
            PageRequest.Cursor cursor = nextedPageRequest.cursor().orElseThrow();

            soft.assertThat(entities).hasSize(2);
            soft.assertThat(pageRequest.mode())
                    .isEqualTo(PageRequest.Mode.CURSOR_NEXT);
            soft.assertThat(nextedPageRequest.mode())
                    .isEqualTo(PageRequest.Mode.CURSOR_NEXT);
            soft.assertThat(cursor.elements())
                    .hasSize(3);
            soft.assertThat(cursor.get(0)).isEqualTo("Poliana");
            soft.assertThat(cursor.get(1)).isEqualTo(35);
            soft.assertThat(cursor.get(2)).isNotNull();

        });
    }

    @Test
    void shouldReturnPaginationAfterKeySingleElementWhenThereIsCondition() {
        SelectQuery query = SelectQuery.select().from("person")
                .where("address").eq("street")
                .orderBy("name").asc()
                .orderBy("age").asc()
                .orderBy("id").asc()
                .build();

        Mockito.when(databaseManager.select(Mockito.any(SelectQuery.class)))
                .thenReturn(stream());

        var id = UUID.randomUUID().toString();
        CursoredPage<CommunicationEntity> entities = databaseManager.selectCursor(query,
                PageRequest.afterCursor(PageRequest.Cursor.forKey("Ada", 20, id), 1, 10 ,false));


        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        Mockito.verify(databaseManager).select(captor.capture());
        SelectQuery selectQuery = captor.getValue();

        CriteriaCondition condition = selectQuery.condition().orElseThrow();

        assertSoftly(soft -> {
            soft.assertThat(condition.condition()).isEqualTo(Condition.AND);
            List<CriteriaCondition> criteriaConditions = condition.element().get(new TypeReference<>() {
            });

            soft.assertThat(criteriaConditions).hasSize(2);
            soft.assertThat(criteriaConditions.get(0)).isEqualTo(CriteriaCondition.eq("address", "street"));

            CriteriaCondition secondCondition = criteriaConditions.get(1);
            soft.assertThat(secondCondition.condition()).isEqualTo(Condition.OR);
            List<CriteriaCondition> secondConditions = secondCondition.element().get(new TypeReference<>() {
            });

            soft.assertThat(secondConditions).hasSize(3);
            soft.assertThat(secondConditions.get(0)).isEqualTo(CriteriaCondition.gt("name", "Ada"));
            soft.assertThat(secondConditions.get(1)).isEqualTo(
                    CriteriaCondition.eq("name", "Ada").and(CriteriaCondition.gt("age", 20)));
            soft.assertThat(secondConditions.get(2)).isEqualTo(
                    CriteriaCondition.eq("name", "Ada").and(CriteriaCondition.eq("age", 20))
                            .and(CriteriaCondition.gt("id", id)));
        });

        assertSoftly(soft -> {
            PageRequest pageRequest = entities.pageRequest();
            PageRequest nextedPageRequest = entities.nextPageRequest();
            PageRequest.Cursor cursor = nextedPageRequest.cursor().orElseThrow();

            soft.assertThat(entities).hasSize(2);
            soft.assertThat(pageRequest.mode())
                    .isEqualTo(PageRequest.Mode.CURSOR_NEXT);
            soft.assertThat(nextedPageRequest.mode())
                    .isEqualTo(PageRequest.Mode.CURSOR_NEXT);
            soft.assertThat(cursor.elements())
                    .hasSize(3);
            soft.assertThat(cursor.get(0)).isEqualTo("Poliana");
            soft.assertThat(cursor.get(1)).isEqualTo(35);
            soft.assertThat(cursor.get(2)).isNotNull();

        });
    }

    @Test
    void shouldFindSubElement() {
        SelectQuery query = SelectQuery.select().from("person")
                .orderBy("address.street").asc()
                .build();

        Mockito.when(databaseManager.select(Mockito.any(SelectQuery.class)))
                .thenReturn(streamSubDocument());
        CursoredPage<CommunicationEntity> entities = databaseManager.selectCursor(query,
                PageRequest.afterCursor(PageRequest.Cursor.forKey("Paulista Avenue"), 1, 10 ,false));

        assertSoftly(soft -> {
            PageRequest nextedPageRequest = entities.nextPageRequest();
            PageRequest.Cursor cursor = nextedPageRequest.cursor().orElseThrow();

            soft.assertThat(entities).hasSize(1);
            soft.assertThat(cursor.get(0)).isEqualTo("Paulista Avenue");

        });
    }

    @Test
    void shouldReturnPaginationBeforeKeySingleElementWhenConditionIsNull() {
        SelectQuery query = SelectQuery.select().from("person")
                .orderBy("name").asc()
                .orderBy("age").asc()
                .orderBy("id").asc().build();

        Mockito.when(databaseManager.select(Mockito.any(SelectQuery.class)))
                .thenReturn(stream());

        var id = UUID.randomUUID().toString();
        CursoredPage<CommunicationEntity> entities = databaseManager.selectCursor(query,
                PageRequest.beforeCursor(PageRequest.Cursor.forKey("Ada", 20, id), 1, 10 ,false));

        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        Mockito.verify(databaseManager).select(captor.capture());
        SelectQuery selectQuery = captor.getValue();

        CriteriaCondition condition = selectQuery.condition().orElseThrow();

        assertSoftly(soft -> {
            soft.assertThat(condition.condition()).isEqualTo(Condition.OR);
            List<CriteriaCondition> criteriaConditions = condition.element().get(new TypeReference<>() {
            });

            soft.assertThat(criteriaConditions).hasSize(3);
            soft.assertThat(criteriaConditions.get(0)).isEqualTo(CriteriaCondition.lt("name", "Ada"));
            soft.assertThat(criteriaConditions.get(1)).isEqualTo(
                    CriteriaCondition.eq("name", "Ada").and(CriteriaCondition.lt("age", 20)));
            soft.assertThat(criteriaConditions.get(2)).isEqualTo(
                    CriteriaCondition.eq("name", "Ada").and(CriteriaCondition.eq("age", 20))
                            .and(CriteriaCondition.lt("id", id)));
        });

        assertSoftly(soft -> {
            PageRequest pageRequest = entities.pageRequest();
            PageRequest nextedPageRequest = entities.previousPageRequest();
            PageRequest.Cursor cursor = nextedPageRequest.cursor().orElseThrow();

            soft.assertThat(entities).hasSize(2);
            soft.assertThat(pageRequest.mode())
                    .isEqualTo(PageRequest.Mode.CURSOR_PREVIOUS);
            soft.assertThat(nextedPageRequest.mode())
                    .isEqualTo(PageRequest.Mode.CURSOR_PREVIOUS);
            soft.assertThat(cursor.elements())
                    .hasSize(3);
            soft.assertThat(cursor.get(0)).isEqualTo("Poliana");
            soft.assertThat(cursor.get(1)).isEqualTo(35);
            soft.assertThat(cursor.get(2)).isNotNull();

        });
    }

    @Test
    void shouldReturnPaginationBeforeKeySingleElementWhenThereIsCondition() {
        SelectQuery query = SelectQuery.select().from("person")
                .where("address").eq("street")
                .orderBy("name").asc()
                .orderBy("age").asc()
                .orderBy("id").asc()
                .build();

        Mockito.when(databaseManager.select(Mockito.any(SelectQuery.class)))
                .thenReturn(stream());

        var id = UUID.randomUUID().toString();
        CursoredPage<CommunicationEntity> entities = databaseManager.selectCursor(query,
                PageRequest.beforeCursor(PageRequest.Cursor.forKey("Ada", 20, id), 1, 10 ,false));


        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        Mockito.verify(databaseManager).select(captor.capture());
        SelectQuery selectQuery = captor.getValue();

        CriteriaCondition condition = selectQuery.condition().orElseThrow();

        assertSoftly(soft -> {
            soft.assertThat(condition.condition()).isEqualTo(Condition.AND);
            List<CriteriaCondition> criteriaConditions = condition.element().get(new TypeReference<>() {
            });

            soft.assertThat(criteriaConditions).hasSize(2);
            soft.assertThat(criteriaConditions.get(0)).isEqualTo(CriteriaCondition.eq("address", "street"));

            CriteriaCondition secondCondition = criteriaConditions.get(1);
            soft.assertThat(secondCondition.condition()).isEqualTo(Condition.OR);
            List<CriteriaCondition> secondConditions = secondCondition.element().get(new TypeReference<>() {
            });

            soft.assertThat(secondConditions).hasSize(3);
            soft.assertThat(secondConditions.get(0)).isEqualTo(CriteriaCondition.lt("name", "Ada"));
            soft.assertThat(secondConditions.get(1)).isEqualTo(
                    CriteriaCondition.eq("name", "Ada").and(CriteriaCondition.lt("age", 20)));
            soft.assertThat(secondConditions.get(2)).isEqualTo(
                    CriteriaCondition.eq("name", "Ada").and(CriteriaCondition.eq("age", 20))
                            .and(CriteriaCondition.lt("id", id)));
        });

        assertSoftly(soft -> {
            PageRequest pageRequest = entities.pageRequest();
            PageRequest nextedPageRequest = entities.previousPageRequest();
            PageRequest.Cursor cursor = nextedPageRequest.cursor().orElseThrow();

            soft.assertThat(entities).hasSize(2);
            soft.assertThat(pageRequest.mode())
                    .isEqualTo(PageRequest.Mode.CURSOR_PREVIOUS);
            soft.assertThat(nextedPageRequest.mode())
                    .isEqualTo(PageRequest.Mode.CURSOR_PREVIOUS);
            soft.assertThat(cursor.elements())
                    .hasSize(3);
            soft.assertThat(cursor.get(0)).isEqualTo("Poliana");
            soft.assertThat(cursor.get(1)).isEqualTo(35);
            soft.assertThat(cursor.get(2)).isNotNull();

        });
    }


    @Test
    void shouldReturnPaginationAfterKeyAndReturnEmpty() {
        SelectQuery query = SelectQuery.select().from("person")
                .orderBy("name").asc()
                .orderBy("age").asc()
                .orderBy("id").asc().build();

        Mockito.when(databaseManager.select(Mockito.any(SelectQuery.class)))
                .thenReturn(Stream.empty());

        var id = UUID.randomUUID().toString();
        CursoredPage<CommunicationEntity> entities = databaseManager.selectCursor(query,
                PageRequest.afterCursor(PageRequest.Cursor.forKey("Ada", 20, id), 1, 10 ,false));

        assertSoftly(soft -> {
            PageRequest pageRequest = entities.pageRequest();

            soft.assertThat(entities).isEmpty();
            soft.assertThat(entities.hasNext()).isFalse();
            soft.assertThat(entities.hasPrevious()).isFalse();
            soft.assertThat(pageRequest.mode())
                    .isEqualTo(PageRequest.Mode.CURSOR_NEXT);
        });
    }


    @Test
    void shouldReturnPaginationBeforeKeyAndReturnEmpty() {
        SelectQuery query = SelectQuery.select().from("person")
                .orderBy("name").asc()
                .orderBy("age").asc()
                .orderBy("id").asc().build();

        Mockito.when(databaseManager.select(Mockito.any(SelectQuery.class)))
                .thenReturn(Stream.empty());

        var id = UUID.randomUUID().toString();
        CursoredPage<CommunicationEntity> entities = databaseManager.selectCursor(query,
                PageRequest.beforeCursor(PageRequest.Cursor.forKey("Ada", 20, id), 1, 10 ,false));

        assertSoftly(soft -> {
            PageRequest pageRequest = entities.pageRequest();

            soft.assertThat(entities).isEmpty();
            soft.assertThat(entities.hasNext()).isFalse();
            soft.assertThat(entities.hasPrevious()).isFalse();
            soft.assertThat(pageRequest.mode())
                    .isEqualTo(PageRequest.Mode.CURSOR_PREVIOUS);
        });
    }

    @Test
    void shouldReturnErrorSortSizeDifferentFromOrderSizeBeforeKey() {
        SelectQuery query = SelectQuery.select().from("person")
                .orderBy("name").asc()
                .orderBy("age").asc()
                .orderBy("id").asc().build();

        assertThrows(IllegalArgumentException.class, () -> databaseManager.selectCursor(query,
                PageRequest.beforeCursor(PageRequest.Cursor.forKey("Ada", 20), 1, 10 ,false)));

    }

    @Test
    void shouldReturnErrorSortSizeDifferentFromOrderSizeAfterKey() {
        SelectQuery query = SelectQuery.select().from("person")
                .orderBy("name").asc()
                .orderBy("age").asc()
                .orderBy("id").asc().build();

        assertThrows(IllegalArgumentException.class, () -> databaseManager.selectCursor(query,
                PageRequest.afterCursor(PageRequest.Cursor.forKey("Ada", 20), 1, 10 ,false)));

    }


    @Test
    void shouldNavigateForwardAndBackwardInDeclaredOrder() {
        List<SelectQuery> delegatedQueries = new ArrayList<>();
        useData(List.of(entity(1), entity(2), entity(3)), delegatedQueries);
        SelectQuery query = SelectQuery.select().from("person").orderBy("id").asc().build();

        CursoredPage<CommunicationEntity> firstPage = databaseManager.selectCursor(query, PageRequest.ofSize(2));
        CursoredPage<CommunicationEntity> secondPage = databaseManager.selectCursor(query,
                firstPage.nextPageRequest());
        CursoredPage<CommunicationEntity> returnedFirstPage = databaseManager.selectCursor(query,
                secondPage.previousPageRequest());
        CursoredPage<CommunicationEntity> returnedSecondPage = databaseManager.selectCursor(query,
                returnedFirstPage.nextPageRequest());
        CursoredPage<CommunicationEntity> beforeFirstPage = databaseManager.selectCursor(query,
                returnedFirstPage.previousPageRequest());

        assertSoftly(soft -> {
            soft.assertThat(ids(firstPage)).containsExactly(1, 2);
            soft.assertThat(firstPage.hasPrevious()).isFalse();
            soft.assertThat(firstPage.nextPageRequest().cursor().orElseThrow().get(0)).isEqualTo(2);

            soft.assertThat(ids(secondPage)).containsExactly(3);
            soft.assertThat(secondPage.hasPrevious()).isTrue();
            soft.assertThat(secondPage.previousPageRequest().cursor().orElseThrow().get(0)).isEqualTo(3);

            soft.assertThat(ids(returnedFirstPage)).containsExactly(1, 2);
            soft.assertThat(returnedFirstPage.hasPrevious()).isTrue();
            soft.assertThat(returnedFirstPage.hasNext()).isTrue();
            soft.assertThat(returnedFirstPage.previousPageRequest().cursor().orElseThrow().get(0)).isEqualTo(1);
            soft.assertThat(returnedFirstPage.nextPageRequest().cursor().orElseThrow().get(0)).isEqualTo(2);

            soft.assertThat(ids(returnedSecondPage)).containsExactly(3);
            soft.assertThat(beforeFirstPage).isEmpty();
            soft.assertThat(beforeFirstPage.hasPrevious()).isFalse();
            soft.assertThat(beforeFirstPage.hasNext()).isFalse();
            soft.assertThat(delegatedQueries).allSatisfy(
                    delegated -> soft.assertThat(delegated.limit()).isEqualTo(2));
            soft.assertThat(delegatedQueries.get(2).sorts()).containsExactly(Sort.desc("id"));
        });
    }

    @Test
    void shouldReturnTheImmediatePreviousPageDuringDeepTraversal() {
        List<SelectQuery> delegatedQueries = new ArrayList<>();
        useData(List.of(entity(1), entity(2), entity(3), entity(4), entity(5), entity(6)), delegatedQueries);
        SelectQuery query = SelectQuery.select().from("person").orderBy("id").asc().build();

        CursoredPage<CommunicationEntity> firstPage = databaseManager.selectCursor(query, PageRequest.ofSize(2));
        CursoredPage<CommunicationEntity> secondPage = databaseManager.selectCursor(query,
                firstPage.nextPageRequest());
        CursoredPage<CommunicationEntity> thirdPage = databaseManager.selectCursor(query,
                secondPage.nextPageRequest());
        CursoredPage<CommunicationEntity> returnedSecondPage = databaseManager.selectCursor(query,
                thirdPage.previousPageRequest());
        CursoredPage<CommunicationEntity> returnedFirstPage = databaseManager.selectCursor(query,
                returnedSecondPage.previousPageRequest());
        CursoredPage<CommunicationEntity> reciprocalThirdPage = databaseManager.selectCursor(query,
                returnedSecondPage.nextPageRequest());

        assertSoftly(soft -> {
            soft.assertThat(ids(firstPage)).containsExactly(1, 2);
            soft.assertThat(ids(secondPage)).containsExactly(3, 4);
            soft.assertThat(ids(thirdPage)).containsExactly(5, 6);
            soft.assertThat(thirdPage.previousPageRequest().cursor().orElseThrow().get(0)).isEqualTo(5);

            soft.assertThat(ids(returnedSecondPage)).containsExactly(3, 4);
            soft.assertThat(returnedSecondPage.previousPageRequest().cursor().orElseThrow().get(0)).isEqualTo(3);
            soft.assertThat(returnedSecondPage.nextPageRequest().cursor().orElseThrow().get(0)).isEqualTo(4);

            soft.assertThat(ids(returnedFirstPage)).containsExactly(1, 2);
            soft.assertThat(ids(reciprocalThirdPage)).containsExactly(5, 6);
            soft.assertThat(delegatedQueries.get(3).sorts()).containsExactly(Sort.desc("id"));
            soft.assertThat(delegatedQueries.get(4).sorts()).containsExactly(Sort.desc("id"));
            soft.assertThat(delegatedQueries).allSatisfy(
                    delegated -> soft.assertThat(delegated.limit()).isEqualTo(2));
        });
    }

    @Test
    void shouldNavigateDescendingCursorPages() {
        List<SelectQuery> delegatedQueries = new ArrayList<>();
        useData(List.of(entity(1), entity(2), entity(3), entity(4), entity(5)), delegatedQueries);
        SelectQuery query = SelectQuery.select().from("person").orderBy("id").desc().build();

        CursoredPage<CommunicationEntity> firstPage = databaseManager.selectCursor(query, PageRequest.ofSize(2));
        CursoredPage<CommunicationEntity> secondPage = databaseManager.selectCursor(query,
                firstPage.nextPageRequest());
        CursoredPage<CommunicationEntity> returnedFirstPage = databaseManager.selectCursor(query,
                secondPage.previousPageRequest());

        assertSoftly(soft -> {
            soft.assertThat(ids(firstPage)).containsExactly(5, 4);
            soft.assertThat(ids(secondPage)).containsExactly(3, 2);
            soft.assertThat(ids(returnedFirstPage)).containsExactly(5, 4);
            soft.assertThat(delegatedQueries.get(1).condition()).contains(CriteriaCondition.lt("id", 4));
            soft.assertThat(delegatedQueries.get(1).sorts()).containsExactly(Sort.desc("id"));
            soft.assertThat(delegatedQueries.get(2).condition()).contains(CriteriaCondition.gt("id", 3));
            soft.assertThat(delegatedQueries.get(2).sorts()).containsExactly(Sort.asc("id"));
            soft.assertThat(delegatedQueries).allSatisfy(
                    delegated -> soft.assertThat(delegated.limit()).isEqualTo(2));
        });
    }

    @Test
    void shouldNavigateMixedDirectionsAndPreserveOriginalFilter() {
        List<CommunicationEntity> data = List.of(
                mixedEntity(1, "Ada", 40, "active"),
                mixedEntity(2, "Ada", 30, "active"),
                mixedEntity(3, "Ada", 30, "active"),
                mixedEntity(99, "Bob", 55, "inactive"),
                mixedEntity(4, "Bob", 50, "active"),
                mixedEntity(5, "Bob", 20, "active"),
                mixedEntity(6, "Cara", 60, "active"));
        CriteriaCondition originalCondition = CriteriaCondition.eq("status", "active");
        List<Sort<?>> sorts = List.of(Sort.ascIgnoreCase("group"), Sort.desc("rank"), Sort.asc("id"));
        SelectQuery query = new DefaultSelectQuery(99, 0, "person", List.of("group", "rank", "id"),
                sorts, originalCondition, false);
        List<SelectQuery> delegatedQueries = new ArrayList<>();
        useData(data, delegatedQueries);

        CursoredPage<CommunicationEntity> firstPage = databaseManager.selectCursor(query, PageRequest.ofSize(2));
        CursoredPage<CommunicationEntity> secondPage = databaseManager.selectCursor(query,
                firstPage.nextPageRequest());
        CursoredPage<CommunicationEntity> thirdPage = databaseManager.selectCursor(query,
                secondPage.nextPageRequest());
        CursoredPage<CommunicationEntity> returnedSecondPage = databaseManager.selectCursor(query,
                thirdPage.previousPageRequest());

        CriteriaCondition forwardCursor = CriteriaCondition.gt("group", "Ada")
                .or(CriteriaCondition.eq("group", "Ada").and(CriteriaCondition.lt("rank", 30)))
                .or(CriteriaCondition.eq("group", "Ada").and(CriteriaCondition.eq("rank", 30))
                        .and(CriteriaCondition.gt("id", 2)));
        CriteriaCondition backwardCursor = CriteriaCondition.lt("group", "Bob")
                .or(CriteriaCondition.eq("group", "Bob").and(CriteriaCondition.gt("rank", 20)))
                .or(CriteriaCondition.eq("group", "Bob").and(CriteriaCondition.eq("rank", 20))
                        .and(CriteriaCondition.lt("id", 5)));

        assertSoftly(soft -> {
            soft.assertThat(ids(firstPage)).containsExactly(1, 2);
            soft.assertThat(ids(secondPage)).containsExactly(3, 4);
            soft.assertThat(ids(thirdPage)).containsExactly(5, 6);
            soft.assertThat(ids(returnedSecondPage)).containsExactly(3, 4);

            SelectQuery forwardQuery = delegatedQueries.get(1);
            soft.assertThat(forwardQuery.condition()).contains(
                    CriteriaCondition.and(originalCondition, forwardCursor));
            soft.assertThat(forwardQuery.sorts()).containsExactlyElementsOf(sorts);

            SelectQuery previousQuery = delegatedQueries.get(3);
            soft.assertThat(previousQuery.condition()).contains(
                    CriteriaCondition.and(originalCondition, backwardCursor));
            soft.assertThat(previousQuery.sorts()).containsExactly(
                    Sort.descIgnoreCase("group"), Sort.asc("rank"), Sort.desc("id"));
            soft.assertThat(previousQuery.sorts().get(0).ignoreCase()).isTrue();

            soft.assertThat(delegatedQueries).allSatisfy(delegated -> {
                soft.assertThat(delegated.limit()).isEqualTo(2);
                soft.assertThat(delegated.skip()).isZero();
                soft.assertThat(delegated.name()).isEqualTo("person");
                soft.assertThat(delegated.columns()).containsExactly("group", "rank", "id");
                soft.assertThat(delegated.isCount()).isFalse();
            });
        });
    }

    @Test
    void shouldCount(){
        SelectQuery query = SelectQuery.select().from("person").build();
        Mockito.when(databaseManager.select(query)).thenReturn(stream());

        long count = databaseManager.count(query);
        Assertions.assertThat(count).isNotZero().isEqualTo(2L);
    }

    @Test
    void shouldReturnZeroWhenCountIsEmpty(){
        SelectQuery query = SelectQuery.select().from("person").build();
        Mockito.when(databaseManager.select(query)).thenReturn(Stream.empty());
        long count = databaseManager.count(query);
        Assertions.assertThat(count).isZero();
    }

    @Test
    void shouldExists(){
        SelectQuery query = SelectQuery.select().from("person").build();
        Mockito.when(databaseManager.select(Mockito.any())).thenReturn(stream());

        boolean exists = databaseManager.exists(query);
        Assertions.assertThat(exists).isTrue();
    }

    @Test
    void shouldNotExists(){
        var query = SelectQuery.select().from("person").build();
        Mockito.when(databaseManager.select(Mockito.any())).thenReturn(Stream.empty());

        boolean exists = databaseManager.exists(query);
        Assertions.assertThat(exists).isFalse();
    }

    @Test
    void shouldQuery(){
        SelectQuery query = SelectQuery.select().from("person").build();
        Mockito.when(databaseManager.select(query)).thenReturn(stream());

        Stream<CommunicationEntity> entities = databaseManager.query("FROM person");
        Assertions.assertThat(entities).hasSize(2);
    }

    @Test
    void shouldPrepare(){
        var prepare = databaseManager.prepare("FROM person WHERE name = :name");
        Assertions.assertThat(prepare).isNotNull();
    }

    @Test
    void shouldReturnErrorSingleResult(){
        SelectQuery query = SelectQuery.select().from("person").build();
        Mockito.when(databaseManager.select(query)).thenReturn(stream());

        Assertions.assertThatThrownBy(() -> databaseManager.singleResult(query))
                .isInstanceOf(NonUniqueResultException.class);

    }

    @Test
    void shouldSingleResult(){
        SelectQuery query = SelectQuery.select().from("person").build();
        Mockito.when(databaseManager.select(query)).thenReturn(Stream.of(CommunicationEntity.of("name")));

        var entity = databaseManager.singleResult(query);
        Assertions.assertThat(entity).isPresent();
    }

    @Test
    void shouldReturnEmptyAtSingleResult(){
        SelectQuery query = SelectQuery.select().from("person").build();
        Mockito.when(databaseManager.select(query)).thenReturn(Stream.empty());

        var entity = databaseManager.singleResult(query);
        Assertions.assertThat(entity).isEmpty();
    }

    @Test
    void shouldExecuteUpdate(){
        List<Element> elements = List.of(Element.of("name", "Ada"), Element.of("age", 10));
        var updateQuery = new DefaultUpdateQuery("person", elements, CriteriaCondition.eq("id", "id"));
        var select = SelectQuery.select().from("person").where("id").eq("id").build();
        var entity = CommunicationEntity.of("person");
        entity.add("name", "Poliana");
        Mockito.when(databaseManager.select(select)).thenReturn(Stream.of(entity));

        databaseManager.update(updateQuery);

        ArgumentCaptor<CommunicationEntity> captor = ArgumentCaptor.forClass(CommunicationEntity.class);
        Mockito.verify(databaseManager).update(captor.capture());

        CommunicationEntity communication = captor.getValue();

        SoftAssertions.assertSoftly(soft ->{
            soft.assertThat(communication.find("name").orElseThrow().get()).isEqualTo("Ada");
            soft.assertThat(communication.find("age").orElseThrow().get()).isEqualTo(10);
            soft.assertThat(communication.name()).isEqualTo("person");
        });


    }



    private void useData(List<CommunicationEntity> data, List<SelectQuery> delegatedQueries) {
        Mockito.when(databaseManager.select(Mockito.any(SelectQuery.class))).thenAnswer(invocation -> {
            SelectQuery query = invocation.getArgument(0);
            delegatedQueries.add(query);
            Stream<CommunicationEntity> stream = data.stream();
            if (query.condition().isPresent()) {
                CriteriaCondition condition = query.condition().orElseThrow();
                stream = stream.filter(entity -> matches(condition, entity));
            }
            if (!query.sorts().isEmpty()) {
                stream = stream.sorted(comparator(query.sorts()));
            }
            return stream.limit(query.limit());
        });
    }

    private boolean matches(CriteriaCondition condition, CommunicationEntity entity) {
        if (condition.condition() == Condition.AND || condition.condition() == Condition.OR) {
            List<CriteriaCondition> conditions = condition.element().get(new TypeReference<>() {
            });
            if (condition.condition() == Condition.AND) {
                return conditions.stream().allMatch(current -> matches(current, entity));
            }
            return conditions.stream().anyMatch(current -> matches(current, entity));
        }

        Object entityValue = entity.find(condition.element().name()).orElseThrow().get();
        Object conditionValue = condition.element().get();
        return switch (condition.condition()) {
            case EQUALS -> Objects.equals(entityValue, conditionValue);
            case GREATER_THAN -> compare(entityValue, conditionValue, false) > 0;
            case LESSER_THAN -> compare(entityValue, conditionValue, false) < 0;
            default -> throw new IllegalArgumentException("Unsupported test condition: " + condition.condition());
        };
    }

    private Comparator<CommunicationEntity> comparator(List<Sort<?>> sorts) {
        Comparator<CommunicationEntity> comparator = (left, right) -> 0;
        for (Sort<?> sort : sorts) {
            Comparator<CommunicationEntity> current = (left, right) -> compare(
                    left.find(sort.property()).orElseThrow().get(),
                    right.find(sort.property()).orElseThrow().get(), sort.ignoreCase());
            if (sort.isDescending()) {
                current = current.reversed();
            }
            comparator = comparator.thenComparing(current);
        }
        return comparator;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private int compare(Object left, Object right, boolean ignoreCase) {
        if (ignoreCase && left instanceof String leftString && right instanceof String rightString) {
            return leftString.compareToIgnoreCase(rightString);
        }
        return ((Comparable) left).compareTo(right);
    }

    private CommunicationEntity entity(int id) {
        CommunicationEntity entity = CommunicationEntity.of("person");
        entity.add("id", id);
        entity.add("name", "name-" + id);
        entity.add("age", id);
        entity.add("status", "active");
        return entity;
    }

    private CommunicationEntity mixedEntity(int id, String group, int rank, String status) {
        CommunicationEntity entity = CommunicationEntity.of("person");
        entity.add("id", id);
        entity.add("group", group);
        entity.add("rank", rank);
        entity.add("status", status);
        return entity;
    }

    private List<Integer> ids(CursoredPage<CommunicationEntity> page) {
        return page.content().stream()
                .map(entity -> entity.find("id", Integer.class).orElseThrow())
                .toList();
    }

    private Stream<CommunicationEntity> stream() {
        var entity = CommunicationEntity.of("name");
        entity.add("name", "Ada");
        entity.add("age", 10);
        entity.add("id", UUID.randomUUID().toString());

        var entity2 = CommunicationEntity.of("name");
        entity2.add("name", "Poliana");
        entity2.add("age", 35);
        entity2.add("id", UUID.randomUUID().toString());
        return Stream.of(entity, entity2);
    }

    private Stream<CommunicationEntity> streamSubDocument() {
        var entity = CommunicationEntity.of("name");
        entity.add("name", "Ada");
        entity.add("age", 10);
        entity.add("id", UUID.randomUUID().toString());
        entity.add("address", List.of(
                Element.of("street", "Paulista Avenue"),
                Element.of("number", 100)));
        return Stream.of(entity);
    }


}
