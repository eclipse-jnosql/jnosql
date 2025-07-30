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


import jakarta.data.Direction;
import jakarta.data.Sort;
import org.eclipse.jnosql.communication.Params;
import org.eclipse.jnosql.communication.QueryException;
import org.eclipse.jnosql.communication.query.data.SelectParser;

import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public final class SelectQueryParser implements BiFunction<org.eclipse.jnosql.communication.query.SelectQuery, CommunicationObserverParser, QueryParams> {



    Stream<CommunicationEntity> query(String query, String entity, DatabaseManager manager, CommunicationObserverParser observer) {

        var selectQuery = query(query, entity, observer);
        return manager.select(selectQuery);
    }


    CommunicationPreparedStatement prepare(String query, String entity, DatabaseManager manager, CommunicationObserverParser observer) {

        Params params = Params.newParams();
        var converter = new SelectParser();
        var selectQuery = converter.apply(query, entity);

        var prepareQuery = query(params, selectQuery, observer);
        return CommunicationPreparedStatement.select(prepareQuery, params, query, manager);
    }


    @Override
    public QueryParams apply(org.eclipse.jnosql.communication.query.SelectQuery selectQuery, CommunicationObserverParser observer) {
        Objects.requireNonNull(selectQuery, "selectQuery is required");
        Objects.requireNonNull(observer, "observer is required");

        Params params = Params.newParams();
        SelectQuery columnQuery = query(params, selectQuery, observer);
        return new QueryParams(columnQuery, params);
    }


    private SelectQuery query(String query, String entity, CommunicationObserverParser observer) {

        var converter = new SelectParser();
        var selectQuery = converter.apply(query, entity);
        var entityName = observer.fireEntity(selectQuery.entity());
        var limit = selectQuery.limit();
        var skip = selectQuery.skip();
        var columns = selectQuery.fields().stream()
                .map(f -> observer.fireSelectField(entityName, f)).toList();
        List<Sort<?>> sorts = selectQuery.orderBy().stream().map(s -> toSort(s, observer, entityName))
                .collect(toList());

        var params = Params.newParams();
        var condition = selectQuery.where()
                .map(c -> Conditions.getCondition(c, params, observer, entityName)).orElse(null);

        if (params.isNotEmpty()) {
            throw new QueryException("To run a query with a parameter use a PrepareStatement instead.");
        }
        boolean count = selectQuery.isCount();
        return new DefaultSelectQuery(limit, skip, entityName, columns, sorts, condition, count);
    }

    private SelectQuery query(Params params, org.eclipse.jnosql.communication.query.SelectQuery selectQuery, CommunicationObserverParser observer) {

        var entity = observer.fireEntity(selectQuery.entity());
        long limit = selectQuery.limit();
        long skip = selectQuery.skip();
        List<String> columns = selectQuery.fields().stream()
                .map(f -> observer.fireSelectField(entity, f))
                .toList();

        List<Sort<?>> sorts = selectQuery.orderBy().stream().map(s -> toSort(s, observer, entity)).collect(toList());
        CriteriaCondition condition = selectQuery.where()
                .map(c -> Conditions.getCondition(c, params, observer, entity))
                .orElse(null);

        boolean count = selectQuery.isCount();

        return new DefaultSelectQuery(limit, skip, entity, columns, sorts, condition, count);
    }

    private Sort<?> toSort(Sort<?> sort, CommunicationObserverParser observer, String entity) {
        return Sort.of(observer.fireSortProperty(entity, sort.property()),
                sort.isAscending()? Direction.ASC: Direction.DESC, false);
    }


}
