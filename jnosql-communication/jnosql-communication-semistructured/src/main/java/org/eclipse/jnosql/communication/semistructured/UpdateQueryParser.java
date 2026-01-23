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


import org.eclipse.jnosql.communication.Params;
import org.eclipse.jnosql.communication.QueryException;
import org.eclipse.jnosql.communication.query.UpdateItem;
import org.eclipse.jnosql.communication.query.data.UpdateProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * The {@link UpdateQueryParser} has the logic to convert from {@link org.eclipse.jnosql.communication.query.UpdateQuery}
 * to {@link UpdateQueryParams}.
 */
public final class UpdateQueryParser implements BiFunction<org.eclipse.jnosql.communication.query.UpdateQuery, CommunicationObserverParser, UpdateQueryParams> {

    Stream<CommunicationEntity> query(String query, DatabaseManager manager, CommunicationObserverParser observer) {
        var updateQuery = getQuery(query, observer);
        manager.update(updateQuery);
        return Stream.empty();
    }

    CommunicationPreparedStatement prepare(String query, DatabaseManager manager,
                                           CommunicationObserverParser observer) {

        Params params = Params.newParams();
        var updateQuery = getQuery(query, params, observer);
        return CommunicationPreparedStatement.update(updateQuery, params, query, manager);
    }



    @Override
    public UpdateQueryParams apply(org.eclipse.jnosql.communication.query.UpdateQuery updateQuery,
                                   CommunicationObserverParser communicationObserverParser) {

        requireNonNull(updateQuery, "updateQuery is required");
        requireNonNull(communicationObserverParser, "columnObserverParser is required");
        Params params = Params.newParams();
        var query = getQuery(params, communicationObserverParser, updateQuery);
        return new UpdateQueryParams(query, params);
    }

    private UpdateQuery getQuery(String query, Params params, CommunicationObserverParser observer) {
        var updateQuery = UpdateProvider.INSTANCE.apply(query);
        return getQuery(params, observer, updateQuery);
    }

    private UpdateQuery getQuery(Params params, CommunicationObserverParser observer, org.eclipse.jnosql.communication.query.UpdateQuery updateQuery) {
        var entity = observer.fireEntity(updateQuery.entity());

        List<Element> set = new ArrayList<>();
        for (UpdateItem updateItem : updateQuery.set()) {
            var field = observer.fireSelectField(entity, updateItem.name());
            var value = Values.get(updateItem.value(), params);
            set.add(Element.of(field, value));
        }
        CriteriaCondition condition = updateQuery.where().map(c -> Conditions.getCondition(c, params, observer, entity))
                .orElse(null);

        return new DefaultUpdateQuery(entity, set, condition);
    }

    private UpdateQuery getQuery(String query, CommunicationObserverParser observer) {

        var updateQuery = UpdateProvider.INSTANCE.apply(query);

        var entity = observer.fireEntity(updateQuery.entity());
        Params params = Params.newParams();

        List<Element> set = new ArrayList<>();

        for (UpdateItem updateItem : updateQuery.set()) {
            var field = observer.fireSelectField(entity, updateItem.name());
            var value = Values.get(updateItem.value(), params);
            set.add(Element.of(field, value));
        }

        CriteriaCondition condition = updateQuery.where()
                .map(c -> Conditions.getCondition(c, params, observer, entity)).orElse(null);

        if (params.isNotEmpty()) {
            throw new QueryException("To run a query with a parameter use a PrepareStatement instead.");
        }
        return new DefaultUpdateQuery(entity, set, condition);
    }
}