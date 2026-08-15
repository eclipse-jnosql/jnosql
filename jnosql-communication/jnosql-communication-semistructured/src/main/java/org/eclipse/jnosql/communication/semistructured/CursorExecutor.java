/*
 *  Copyright (c) 2024,2026 Contributors to the Eclipse Foundation
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
                PageRequest afterCursor = PageRequest.ofSize(pageRequest.size()).afterCursor(cursor);

                return new CursoredPageRecord<>(entities, List.of(cursor), -1, pageRequest, afterCursor, null);
            }
        }


    }, CURSOR_NEXT {
        @Override
        public CursoredPage<CommunicationEntity> cursor(SelectQuery query, PageRequest pageRequest, DatabaseManager template) {

            var cursor = pageRequest.cursor().orElseThrow();
            var condition = condition(query, cursor, true);

            var select = updateQuery(pageRequest.size(), query, condition, query.sorts());

            var entities = template.select(select).toList();
            if (entities.isEmpty()) {
                return new CursoredPageRecord<>(entities, Collections.emptyList(), -1, pageRequest,
                        null, null);
            } else {
                var firstCursor = getCursor(query.sorts(), entities.getFirst());
                var nextCursor = getCursor(query.sorts(), entities.getLast());
                var afterCursor = PageRequest.ofSize(pageRequest.size()).afterCursor(nextCursor);
                var beforeCursor = PageRequest.ofSize(pageRequest.size()).beforeCursor(firstCursor);
                return new CursoredPageRecord<>(entities, List.of(cursor, nextCursor), -1,
                        pageRequest, afterCursor, beforeCursor);
            }
        }

    }, CURSOR_PREVIOUS {
        @Override
        public CursoredPage<CommunicationEntity> cursor(SelectQuery query, PageRequest pageRequest, DatabaseManager template) {
            var cursor = pageRequest.cursor().orElseThrow();
            var condition = condition(query, cursor, false);

            var select = updateQuery(pageRequest.size(), query, condition, invert(query.sorts()));

            var entities = new ArrayList<>(template.select(select).toList());
            Collections.reverse(entities);
            if (entities.isEmpty()) {
                return new CursoredPageRecord<>(entities, Collections.emptyList(), -1, pageRequest,
                        null, null);
            } else {
                var beforeCursor = getCursor(query.sorts(), entities.getFirst());
                var nextCursor = getCursor(query.sorts(), entities.getLast());
                var beforeRequest = PageRequest.ofSize(pageRequest.size()).beforeCursor(beforeCursor);
                var nextRequest = PageRequest.ofSize(pageRequest.size()).afterCursor(nextCursor);

                return new CursoredPageRecord<>(entities, List.of(beforeCursor, cursor), -1, pageRequest,
                        nextRequest, beforeRequest);
            }
        }
    };

    abstract CursoredPage<CommunicationEntity> cursor(SelectQuery query, PageRequest pageRequest, DatabaseManager template);

    /**
     * Returns the cursor executor for a page request mode.
     *
     * @param value the page request mode
     * @return the cursor executor
     */
    static CursorExecutor of(PageRequest.Mode value) {

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

    private static CriteriaCondition condition(SelectQuery query, PageRequest.Cursor cursor, boolean after) {
        CriteriaCondition condition = null;
        CriteriaCondition equalities = null;
        List<Sort<?>> sorts = query.sorts();
        checkCursorKeySizes(cursor, sorts);
        for (int index = 0; index < sorts.size(); index++) {
            Sort<?> sort = sorts.get(index);
            Object key = cursor.get(index);
            CriteriaCondition comparison = comparison(sort, key, after);
            CriteriaCondition current = equalities == null ? comparison : equalities.and(comparison);
            condition = condition == null ? current : condition.or(current);
            CriteriaCondition equality = CriteriaCondition.eq(sort.property(), key);
            equalities = equalities == null ? equality : equalities.and(equality);
        }
        return condition;
    }

    private static CriteriaCondition comparison(Sort<?> sort, Object key, boolean after) {
        boolean greaterThan = sort.isAscending() == after;
        return greaterThan ? CriteriaCondition.gt(sort.property(), key) : CriteriaCondition.lt(sort.property(), key);
    }

    private static List<Sort<?>> invert(List<Sort<?>> sorts) {
        return sorts.stream().map(CursorExecutor::invert).toList();
    }

    private static Sort<?> invert(Sort<?> sort) {
        if (sort.isAscending()) {
            return sort.ignoreCase() ? Sort.descIgnoreCase(sort.property()) : Sort.desc(sort.property());
        }
        return sort.ignoreCase() ? Sort.ascIgnoreCase(sort.property()) : Sort.asc(sort.property());
    }

    private static DefaultSelectQuery updateQuery(int limit, SelectQuery query, CriteriaCondition condition,
                                                   List<Sort<?>> sorts) {
        return new DefaultSelectQuery(limit, 0, query.name(), query.columns(), sorts,
                query.condition().map(c -> CriteriaCondition.and(c, condition))
                        .orElse(condition), false);
    }

    private static void checkCursorKeySizes(PageRequest.Cursor cursor, List<Sort<?>> sorts) {
        if (sorts.size() != cursor.size()) {
            throw new IllegalArgumentException("The cursor size is different from the sort size. Cursor: "
                    + cursor.size() + " Sort: " + sorts.size());
        }
    }
}
