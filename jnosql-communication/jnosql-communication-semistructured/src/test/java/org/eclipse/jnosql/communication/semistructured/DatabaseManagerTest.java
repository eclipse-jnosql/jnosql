/*
 *  Copyright (c) 2024 Contributors to the Eclipse Foundation
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

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class DatabaseManagerTest {
    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private DatabaseManager databaseManager;


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