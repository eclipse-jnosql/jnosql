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
package org.eclipse.jnosql.communication.semistructured;

import jakarta.data.Sort;
import jakarta.data.page.CursoredPage;
import jakarta.data.page.PageRequest;
import jakarta.data.page.impl.CursoredPageRecord;
import org.eclipse.jnosql.communication.CommunicationException;
import org.eclipse.jnosql.communication.TypeReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

enum CursorExecutor {

    OFF_SET {
        @Override
        public CursoredPage<CommunicationEntity> cursor(SelectQuery query, PageRequest pageRequest, DatabaseManager template) {

            var select = new DefaultSelectQuery(pageRequest.size(), 0, query.name(), query.columns(), query.sorts(),
                    query.condition().orElse(null), false);

            var entities = template.select(select).toList();
            var last = entities.isEmpty() ? null : entities.getLast();
            if (last == null) {
                return new CursoredPageRecord<>(entities, Collections.emptyList(), -1, pageRequest,
                        null, null);
            } else {
                PageRequest.Cursor cursor = getCursor(query.sorts(), last);
                PageRequest afterCursor = PageRequest.<CommunicationEntity>ofSize(pageRequest.size()).afterCursor(cursor);

                return new CursoredPageRecord<>(entities, List.of(cursor), -1, pageRequest, afterCursor, null);
            }
        }


    }, CURSOR_NEXT {
        @Override
        public CursoredPage<CommunicationEntity> cursor(SelectQuery query, PageRequest pageRequest, DatabaseManager template) {

            var cursor = pageRequest.cursor().orElseThrow();
            var condition = condition(query, cursor);

            var select = updateQuery(pageRequest.size(), query, condition);

            var entities = template.select(select).toList();
            var last = entities.isEmpty() ? null : entities.getLast();
            if (last == null) {
                return new CursoredPageRecord<>(entities, Collections.emptyList(), -1, pageRequest,
                        null, null);
            } else {
                var nextCursor = getCursor(query.sorts(), last);
                var afterCursor = PageRequest.ofSize(pageRequest.size()).afterCursor(nextCursor);
                return new CursoredPageRecord<>(entities, List.of(cursor, nextCursor), -1,
                        pageRequest, afterCursor, null);
            }
        }

        private static CriteriaCondition condition(SelectQuery query, PageRequest.Cursor cursor) {
            CriteriaCondition condition = null;
            CriteriaCondition previousCondition = null;
            List<Sort<?>> sorts = query.sorts();
            checkCursorKeySizes(cursor, sorts);
            for (int index = 0; index < sorts.size(); index++) {
                Sort<?> sort = sorts.get(index);
                Object key = cursor.get(index);
                if(condition == null) {
                    condition = CriteriaCondition.gt(sort.property(), key);
                    previousCondition = CriteriaCondition.eq(sort.property(), key);
                } else {
                    condition = condition.or(previousCondition.and(CriteriaCondition.gt(sort.property(), key)));
                    previousCondition = previousCondition.and(CriteriaCondition.eq(sort.property(), key));
                }

            }
            return condition;
        }


    }, CURSOR_PREVIOUS {
        @Override
        public CursoredPage<CommunicationEntity> cursor(SelectQuery query, PageRequest pageRequest, DatabaseManager template) {
            var cursor = pageRequest.cursor().orElseThrow();
            var condition = condition(query, cursor);

            var select = updateQuery(pageRequest.size(), query, condition);

            var entities = template.select(select).toList();
            var last = entities.isEmpty() ? null : entities.getLast();
            if (last == null) {
                return new CursoredPageRecord<>(entities, Collections.emptyList(), -1, pageRequest,
                        null, null);
            } else {
                var beforeCursor = getCursor(query.sorts(), last);
                var beforeRequest = PageRequest.<CommunicationEntity>ofSize(pageRequest.size()).beforeCursor(beforeCursor);

                return new CursoredPageRecord<>(entities, List.of(beforeCursor, cursor), -1, pageRequest, null, beforeRequest);
            }
        }

        private static CriteriaCondition condition(SelectQuery query, PageRequest.Cursor cursor) {
            CriteriaCondition condition = null;
            CriteriaCondition previousCondition = null;
            List<Sort<?>> sorts = query.sorts();
            checkCursorKeySizes(cursor, sorts);
            for (int index = 0; index < sorts.size(); index++) {
                Sort<?> sort = sorts.get(index);
                Object key = cursor.get(index);
                if(condition == null) {
                    condition = CriteriaCondition.lt(sort.property(), key);
                    previousCondition = CriteriaCondition.eq(sort.property(), key);
                } else {
                    condition = condition.or(previousCondition.and(CriteriaCondition.lt(sort.property(), key)));
                    previousCondition = previousCondition.and(CriteriaCondition.eq(sort.property(), key));
                }

            }
            return condition;
        }
    };

    abstract CursoredPage<CommunicationEntity> cursor(SelectQuery query, PageRequest pageRequest, DatabaseManager template);

    public static CursorExecutor of(PageRequest.Mode value) {

        return switch (value) {
            case CURSOR_NEXT -> CURSOR_NEXT;
            case CURSOR_PREVIOUS -> CURSOR_PREVIOUS;
            default -> OFF_SET;
        };

    }

    private static PageRequest.Cursor getCursor(List<Sort<?>> sorts, CommunicationEntity entity) {
        List<Object> keys = new ArrayList<>(sorts.size());
        for (Sort<?> sort : sorts) {
            String[] names = sort.property().split("\\.");
            keys.add(value(names, entity));
        }
        return PageRequest.Cursor.forKey(keys.toArray());
    }

    private static Object value(String[] names, CommunicationEntity entity) {
        Element element = entity.find(names[0])
                .orElseThrow(() -> new CommunicationException("The sort name does not exist in the entity: " + names[0]));
        return value(names, element, 0);

    }

    private static Object value(String[] names, Element element, int index) {
        if (names.length == 1) {
            return element.get();
        }
        List<Element> elements = element.get(new TypeReference<>() {});
        Element subElement = elements.stream().filter(e -> e.name().equals(names[index + 1]))
                .findFirst().orElseThrow(() -> new CommunicationException("The sort name does not exist in the entity: " + names[index]));
        if (names.length == index + 2) {
            return subElement.get();
        } else {
            return value(names, subElement, index + 1);
        }
    }

    private static DefaultSelectQuery updateQuery(int limit, SelectQuery query, CriteriaCondition condition) {
        return new DefaultSelectQuery(limit, 0, query.name(), query.columns(), query.sorts(),
                query.condition().map(c -> CriteriaCondition.and(c, condition))
                        .orElse(condition),false);
    }

    private static void checkCursorKeySizes(PageRequest.Cursor cursor, List<Sort<?>> sorts) {
        if(sorts.size() != cursor.size()) {
            throw new IllegalArgumentException("The cursor size is different from the sort size. Cursor: "
                    + cursor.size() + " Sort: " + sorts.size());
        }
    }
}
