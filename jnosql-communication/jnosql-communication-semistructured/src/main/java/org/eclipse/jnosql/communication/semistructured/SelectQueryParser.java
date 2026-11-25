/*
 *  Copyright (c) 2024 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 * and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 * You may elect to redistribute this code under either of these licenses.
 *
 */
package org.eclipse.jnosql.communication.semistructured;


import jakarta.data.Direction;
import jakarta.data.Sort;
import org.eclipse.jnosql.communication.Params;
import org.eclipse.jnosql.communication.QueryException;
import org.eclipse.jnosql.communication.query.data.SelectProvider;

import java.time.Clock;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Converts communication select queries into semi-structured query parameters.
 */
public final class SelectQueryParser implements BiFunction<org.eclipse.jnosql.communication.query.SelectQuery, CommunicationObserverParser, QueryParams> {



    Stream<CommunicationEntity> query(String query, String entity, DatabaseManager manager, CommunicationObserverParser observer) {

        var selectQuery = query(query, entity, observer);
        return manager.select(selectQuery);
    }


    CommunicationPreparedStatement prepare(String query, String entity, DatabaseManager manager, CommunicationObserverParser observer) {
        return prepare(query, entity, manager, observer, Clock.systemDefaultZone());
    }

    CommunicationPreparedStatement prepare(String query, String entity, DatabaseManager manager,
                                           CommunicationObserverParser observer, Clock clock) {

        Params params = Params.newParams();
        var reference = TemporalReference.live(clock);
        var selectQuery = SelectProvider.INSTANCE.apply(query, entity);

        var prepareQuery = query(params, selectQuery, observer, reference);
        return CommunicationPreparedStatement.select(prepareQuery, params, query, manager, reference);
    }


    @Override
    public QueryParams apply(org.eclipse.jnosql.communication.query.SelectQuery selectQuery, CommunicationObserverParser observer) {
        Objects.requireNonNull(selectQuery, "selectQuery is required");
        Objects.requireNonNull(observer, "observer is required");

        Params params = Params.newParams();
        SelectQuery columnQuery = query(params, selectQuery, observer, TemporalReference.live());
        return new QueryParams(columnQuery, params);
    }


    private SelectQuery query(String query, String entity, CommunicationObserverParser observer) {

        var selectQuery = SelectProvider.INSTANCE.apply(query, entity);
        var entityName = observer.fireEntity(selectQuery.entity());
        var limit = selectQuery.limit();
        var skip = selectQuery.skip();
        var columns = selectQuery.fields().stream()
                .map(f -> observer.fireSelectField(entityName, f)).toList();
        List<Sort<?>> sorts = selectQuery.orderBy().stream().map(s -> toSort(s, observer, entityName))
                .collect(toList());

        var params = Params.newParams();
        var reference = TemporalReference.pinned(Clock.systemDefaultZone());
        var condition = selectQuery.where()
                .map(c -> Conditions.getCondition(c, params, observer, entityName, reference)).orElse(null);

        if (params.isNotEmpty()) {
            throw new QueryException("To run a query with a parameter use a PrepareStatement instead.");
        }
        boolean count = selectQuery.isCount();
        return new DefaultSelectQuery(limit, skip, entityName, columns, sorts, condition, count);
    }

    private SelectQuery query(Params params, org.eclipse.jnosql.communication.query.SelectQuery selectQuery,
                              CommunicationObserverParser observer, TemporalReference reference) {

        var entity = observer.fireEntity(selectQuery.entity());
        long limit = selectQuery.limit();
        long skip = selectQuery.skip();
        List<String> columns = selectQuery.fields().stream()
                .map(f -> observer.fireSelectField(entity, f))
                .toList();

        List<Sort<?>> sorts = selectQuery.orderBy().stream().map(s -> toSort(s, observer, entity)).collect(toList());
        CriteriaCondition condition = selectQuery.where()
                .map(c -> Conditions.getCondition(c, params, observer, entity, reference))
                .orElse(null);

        boolean count = selectQuery.isCount();

        return new DefaultSelectQuery(limit, skip, entity, columns, sorts, condition, count);
    }

    private Sort<?> toSort(Sort<?> sort, CommunicationObserverParser observer, String entity) {
        return Sort.of(observer.fireSortProperty(entity, sort.property()),
                sort.isAscending()? Direction.ASC: Direction.DESC, false);
    }


}
